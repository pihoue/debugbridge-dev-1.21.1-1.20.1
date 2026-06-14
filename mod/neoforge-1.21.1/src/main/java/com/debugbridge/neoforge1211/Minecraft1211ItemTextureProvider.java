package com.debugbridge.neoforge1211;

import com.debugbridge.core.texture.ItemTextureProvider;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class Minecraft1211ItemTextureProvider implements ItemTextureProvider {
    private static final Logger LOG = Logger.getLogger("DebugBridge");
    private static final int MAP_SIZE = 128;
    private static final int[] BRIGHTNESS_MOD = {180, 220, 255, 135};
    private static volatile Field spritePixelsField;

    private static int mapPixelArgb(byte packedColor) {
        int colorId = (packedColor & 0xFF) >> 2;
        int shade = packedColor & 3;
        if (colorId == 0) return 0;
        MapColor color = MapColor.byId(colorId);
        if (color == null) return 0;
        int col = color.col;
        int modifier = BRIGHTNESS_MOD[shade];
        int r = ((col >> 16) & 255) * modifier / 255;
        int g = ((col >> 8) & 255) * modifier / 255;
        int b = (col & 255) * modifier / 255;
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private static int nativeToArgb(int abgr) {
        int a = (abgr >>> 24) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int r = abgr & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public TextureResult getItemTexture(int slot) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        return renderStack(() -> {
            if (mc.player == null) throw new Exception("Player not available");
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty()) throw new Exception("Slot " + slot + " is empty");
            return stack;
        });
    }

    @Override
    public TextureResult getItemTextureById(String itemId) throws Exception {
        return renderStack(() -> {
            ResourceLocation key = ResourceLocation.tryParse(itemId);
            if (key == null) throw new Exception("Invalid item id: " + itemId);
            if (!BuiltInRegistries.ITEM.containsKey(key)) throw new Exception("Unknown item: " + itemId);
            Item item = BuiltInRegistries.ITEM.get(key);
            return new ItemStack(item);
        });
    }

    @Override
    public TextureResult getEntityItemTexture(int entityId, String slotName) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        return renderStack(() -> {
            if (mc.level == null) throw new Exception("Level not loaded");

            Entity target = null;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e.getId() == entityId) {
                    target = e;
                    break;
                }
            }
            if (target == null) throw new Exception("Entity " + entityId + " not found");

            ItemStack stack;
            if ("FRAME".equals(slotName) && target instanceof ItemFrame frame) {
                stack = frame.getItem();
            } else if (target instanceof LivingEntity living) {
                EquipmentSlot slot;
                try {
                    slot = EquipmentSlot.valueOf(slotName);
                } catch (IllegalArgumentException e) {
                    throw new Exception("Unknown slot " + slotName);
                }
                stack = living.getItemBySlot(slot);
            } else {
                throw new Exception("Entity " + entityId + " has no equipment");
            }

            if (stack.isEmpty()) throw new Exception("Slot " + slotName + " is empty on entity " + entityId);
            return stack;
        });
    }

    private TextureResult renderStack(StackSupplier supplier) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture<TextureResult> future = new CompletableFuture<>();

        mc.execute(() -> {
            try {
                ItemStack stack = supplier.get();

                TextureResult mapResult = tryRenderFilledMap(mc, stack);
                if (mapResult != null) {
                    future.complete(mapResult);
                    return;
                }

                BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
                TextureResult result = renderFromBakedModel(stack, model, mc);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future.get(10, TimeUnit.SECONDS);
    }

    private TextureResult tryRenderFilledMap(Minecraft mc, ItemStack stack) {
        if (!stack.is(Items.FILLED_MAP)) return null;
        if (mc.level == null) return null;

        MapItemSavedData mapData = MapItem.getSavedData(stack, mc.level);
        if (mapData == null || mapData.colors == null || mapData.colors.length < MAP_SIZE * MAP_SIZE) {
            return null;
        }

        try {
            BufferedImage img = new BufferedImage(MAP_SIZE, MAP_SIZE, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < MAP_SIZE; y++) {
                for (int x = 0; x < MAP_SIZE; x++) {
                    img.setRGB(x, y, mapPixelArgb(mapData.colors[x + y * MAP_SIZE]));
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new TextureResult(base64, MAP_SIZE, MAP_SIZE, "filled_map");
        } catch (Exception e) {
            return null;
        }
    }

    private TextureResult renderFromBakedModel(ItemStack stack, BakedModel model, Minecraft mc) throws Exception {
        List<BakedQuad> quads = model.getQuads(null, Direction.SOUTH, null);
        if (quads.isEmpty()) {
            quads = model.getQuads(null, null, null);
        }

        TextureAtlasSprite[] sprites;
        if (quads.isEmpty()) {
            TextureAtlasSprite particle = model.getParticleIcon();
            if (particle == null) throw new Exception("No sprite found for item");
            sprites = new TextureAtlasSprite[] {particle};
        } else {
            sprites = new TextureAtlasSprite[quads.size()];
            for (int i = 0; i < quads.size(); i++) {
                sprites[i] = quads.get(i).getSprite();
            }
        }

        NativeImage[] imgs = new NativeImage[sprites.length];
        int maxW = 0, maxH = 0;
        for (int i = 0; i < sprites.length; i++) {
            NativeImage img = getSpriteMainImage(sprites[i]);
            if (img == null) continue;
            imgs[i] = img;
            if (img.getWidth() > maxW) maxW = img.getWidth();
            if (img.getHeight() > maxH) maxH = img.getHeight();
        }
        if (maxW == 0 || maxH == 0) {
            throw new Exception("Sprite has no pixel data");
        }

        BufferedImage canvas = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < sprites.length; i++) {
            NativeImage img = imgs[i];
            if (img == null) continue;

            int w = img.getWidth();
            int h = img.getHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = nativeToArgb(img.getPixelRGBA(x, y));
                    canvas.setRGB(x, y, argb);
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        String spriteName = getSpriteName(sprites[0]);
        return new TextureResult(base64, maxW, maxH, spriteName);
    }

    private NativeImage getSpriteMainImage(TextureAtlasSprite sprite) throws Exception {
        if (spritePixelsField == null) {
            synchronized (Minecraft1211ItemTextureProvider.class) {
                if (spritePixelsField == null) {
                    Object contents = null;
                    try {
                        contents = sprite.getClass().getMethod("contents").invoke(sprite);
                    } catch (NoSuchMethodException ignored) {
                    }

                    Class<?> searchClass = (contents != null) ? contents.getClass() : sprite.getClass();
                    Field found = findNativeImageArrayField(searchClass);
                    if (found == null && contents != null) {
                        found = findNativeImageArrayField(sprite.getClass());
                    }
                    if (found == null) {
                        throw new Exception("Cannot locate NativeImage[] field on sprite");
                    }
                    found.setAccessible(true);
                    spritePixelsField = found;
                }
            }
        }

        Object target;
        if (spritePixelsField.getDeclaringClass().isInstance(sprite)) {
            target = sprite;
        } else {
            target = sprite.getClass().getMethod("contents").invoke(sprite);
        }

        NativeImage[] mipmaps = (NativeImage[]) spritePixelsField.get(target);
        if (mipmaps == null || mipmaps.length == 0) return null;
        return mipmaps[0];
    }

    private static Field findNativeImageArrayField(Class<?> cls) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == NativeImage[].class) {
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private String getSpriteName(TextureAtlasSprite sprite) {
        try {
            Object contents = sprite.getClass().getMethod("contents").invoke(sprite);
            if (contents != null) {
                Object name = contents.getClass().getMethod("name").invoke(contents);
                if (name != null) return name.toString();
            }
        } catch (Exception ignored) {
        }
        try {
            Object name = sprite.getClass().getMethod("getName").invoke(sprite);
            if (name != null) return name.toString();
        } catch (Exception ignored) {
        }
        return "";
    }

    @FunctionalInterface
    private interface StackSupplier {
        ItemStack get() throws Exception;
    }
}
