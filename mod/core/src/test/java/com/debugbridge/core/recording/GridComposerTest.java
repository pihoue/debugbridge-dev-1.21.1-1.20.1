package com.debugbridge.core.recording;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure-pixel grid composer. No MC, no I/O — just verifies
 * that frames land at the right offsets and that empty trailing slots stay
 * black.
 */
class GridComposerTest {

    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int BLUE = 0xFF0000FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    /** A frameW × frameH array filled with one color. */
    private static int[] solidFrame(int frameW, int frameH, int argb) {
        int[] px = new int[frameW * frameH];
        Arrays.fill(px, argb);
        return px;
    }

    /** Read a single pixel from the composed image as 0xFFRRGGBB (alpha = FF). */
    private static int pixel(BufferedImage img, int x, int y) {
        // TYPE_INT_RGB.getRGB returns the pixel with alpha forced to 0xFF.
        return img.getRGB(x, y);
    }

    @Test
    void fullGrid_2x2_placesFramesRowMajor() {
        int fw = 4, fh = 3;
        List<int[]> frames = Arrays.asList(
                solidFrame(fw, fh, RED), // slot 0 → top-left
                solidFrame(fw, fh, GREEN), // slot 1 → top-right
                solidFrame(fw, fh, BLUE), // slot 2 → bottom-left
                solidFrame(fw, fh, WHITE) // slot 3 → bottom-right
                );
        BufferedImage out = GridComposer.compose(frames, fw, fh, 2);

        assertEquals(8, out.getWidth());
        assertEquals(6, out.getHeight());
        // Top-left = RED
        assertEquals(RED, pixel(out, 0, 0));
        assertEquals(RED, pixel(out, fw - 1, fh - 1));
        // Top-right = GREEN
        assertEquals(GREEN, pixel(out, fw, 0));
        assertEquals(GREEN, pixel(out, fw * 2 - 1, fh - 1));
        // Bottom-left = BLUE
        assertEquals(BLUE, pixel(out, 0, fh));
        // Bottom-right = WHITE
        assertEquals(WHITE, pixel(out, fw, fh));
    }

    @Test
    void partialLastRow_trailingSlotsBlack() {
        // 3 frames into a 2-col grid → 2 rows, last row has only 1 frame; the
        // unused bottom-right slot must stay black so the JPEG renders cleanly.
        int fw = 2, fh = 2;
        List<int[]> frames =
                Arrays.asList(solidFrame(fw, fh, RED), solidFrame(fw, fh, GREEN), solidFrame(fw, fh, BLUE));
        BufferedImage out = GridComposer.compose(frames, fw, fh, 2);

        assertEquals(4, out.getWidth());
        assertEquals(4, out.getHeight());
        // Filled slots
        assertEquals(RED, pixel(out, 0, 0));
        assertEquals(GREEN, pixel(out, fw, 0));
        assertEquals(BLUE, pixel(out, 0, fh));
        // Empty trailing slot (bottom-right) — should be black on TYPE_INT_RGB.
        assertEquals(BLACK, pixel(out, fw, fh));
        assertEquals(BLACK, pixel(out, fw * 2 - 1, fh * 2 - 1));
    }

    @Test
    void singleFrame_singleColumn() {
        int fw = 5, fh = 7;
        List<int[]> frames = Collections.singletonList(solidFrame(fw, fh, RED));
        BufferedImage out = GridComposer.compose(frames, fw, fh, 1);
        assertEquals(fw, out.getWidth());
        assertEquals(fh, out.getHeight());
        assertEquals(RED, pixel(out, 0, 0));
        assertEquals(RED, pixel(out, fw - 1, fh - 1));
    }

    @Test
    void rejectsMismatchedFrameLength() {
        int[] tooShort = new int[3]; // expected 4 (2*2)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> GridComposer.compose(Collections.singletonList(tooShort), 2, 2, 1));
        assertTrue(ex.getMessage().contains("frame 0"));
    }

    @Test
    void rejectsEmptyFrameList() {
        assertThrows(IllegalArgumentException.class, () -> GridComposer.compose(Collections.emptyList(), 2, 2, 1));
    }

    @Test
    void rejectsBadDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> GridComposer.compose(Collections.singletonList(new int[1]), 0, 1, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> GridComposer.compose(Collections.singletonList(new int[1]), 1, 1, 0));
    }
}
