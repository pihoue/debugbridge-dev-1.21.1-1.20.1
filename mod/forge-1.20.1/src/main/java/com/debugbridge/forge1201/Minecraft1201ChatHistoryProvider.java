package com.debugbridge.forge1201;

import com.debugbridge.core.chat.ChatHistoryProvider;
import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.protocol.dto.ChatMessageDto;
import com.google.gson.JsonElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

public class Minecraft1201ChatHistoryProvider implements ChatHistoryProvider {

    private static volatile Field allMessagesField;

    private static Field allMessagesField(MappingResolver resolver) throws NoSuchFieldException {
        Field f = allMessagesField;
        if (f != null) return f;
        String runtime = resolver.resolveField("net.minecraft.client.gui.components.ChatComponent", "allMessages");
        f = ChatComponent.class.getDeclaredField(runtime);
        f.setAccessible(true);
        allMessagesField = f;
        return f;
    }

    @Override
    public List<ChatMessageDto> getRecentMessages(int limit, MappingResolver resolver, boolean includeJson)
            throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) return Collections.emptyList();
        ChatComponent chat = mc.gui.getChat();
        if (chat == null) return Collections.emptyList();

        @SuppressWarnings("unchecked")
        List<GuiMessage> messages =
                (List<GuiMessage>) allMessagesField(resolver).get(chat);
        if (messages == null) return Collections.emptyList();

        int n = Math.min(limit, messages.size());
        List<ChatMessageDto> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            GuiMessage msg = messages.get(i);
            ChatMessageDto dto = new ChatMessageDto();
            dto.plain = msg.content().getString();
            dto.addedTime = msg.addedTime();
            if (includeJson) {
                try {
                    Object serializer = net.minecraft.network.chat.Component.class
                            .getField("Serializer")
                            .get(null);
                    JsonElement json = (JsonElement) serializer
                            .getClass()
                            .getMethod("toJsonTree", net.minecraft.network.chat.Component.class)
                            .invoke(serializer, msg.content());
                    dto.json = json;
                } catch (Exception ignore) {
                }
            }
            out.add(dto);
        }
        return out;
    }
}
