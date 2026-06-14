package com.debugbridge.core.session;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClientSettleGate}. A single-thread executor stands in
 * for the game thread (it drains tasks promptly, like the real per-frame
 * drain); the settled check is a plain flag the test flips. Tight intervals
 * via the package-private constructor keep each test well under a second.
 */
class ClientSettleGateTest {

    private static final long POLL_MS = 10;
    private static final int SETTLED_POLLS = 2;
    private static final long GRACE_MS = 300;

    private final ExecutorService fakeGameThread = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        fakeGameThread.shutdownNow();
    }

    private ClientSettleGate gate(java.util.function.BooleanSupplier settled) {
        return new ClientSettleGate(fakeGameThread, settled, POLL_MS, SETTLED_POLLS, GRACE_MS);
    }

    @Test
    void runsPromptlyWhenAlreadySettled() throws Exception {
        AtomicInteger ran = new AtomicInteger();
        gate(() -> true).runWhenSettled(ran::incrementAndGet, 1_000);
        assertEquals(1, ran.get());
    }

    @Test
    void defersUntilSettledAndThenRuns() throws Exception {
        AtomicBoolean settled = new AtomicBoolean(false);
        AtomicBoolean ranWhileUnsettled = new AtomicBoolean(false);
        AtomicInteger ran = new AtomicInteger();

        Thread flipper = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            settled.set(true);
        });
        flipper.start();

        gate(settled::get)
                .runWhenSettled(
                        () -> {
                            if (!settled.get()) ranWhileUnsettled.set(true);
                            ran.incrementAndGet();
                        },
                        2_000);
        flipper.join();

        assertEquals(1, ran.get());
        assertFalse(ranWhileUnsettled.get(), "action must not run before the client settles");
    }

    @Test
    void settledStreakRestartsAfterFlapping() throws Exception {
        // settled, unsettled, then settled for good — the one-poll blip must
        // reset the consecutive-polls counter, not count toward it.
        AtomicInteger polls = new AtomicInteger();
        AtomicInteger ran = new AtomicInteger();
        gate(() -> polls.incrementAndGet() != 2).runWhenSettled(ran::incrementAndGet, 2_000);
        assertEquals(1, ran.get());
        // 1 settled poll, the blip, then a full fresh streak of SETTLED_POLLS.
        assertEquals(2 + SETTLED_POLLS, polls.get());
    }

    @Test
    void timesOutWhenNeverSettled() {
        AtomicInteger ran = new AtomicInteger();
        TimeoutException e =
                assertThrows(TimeoutException.class, () -> gate(() -> false).runWhenSettled(ran::incrementAndGet, 150));
        assertTrue(e.getMessage().contains("did not settle"), e.getMessage());
        assertEquals(0, ran.get(), "action must not run after a settle timeout");
    }

    @Test
    void timesOutWhenGameThreadNeverDrains() {
        ClientSettleGate gate = new ClientSettleGate(task -> {}, () -> true, POLL_MS, SETTLED_POLLS, GRACE_MS);
        TimeoutException e =
                assertThrows(TimeoutException.class, () -> gate.runWhenSettled(() -> fail("must not run"), 100));
        assertTrue(e.getMessage().contains("game thread unresponsive"), e.getMessage());
    }

    @Test
    void newerCallSupersedesPendingOne() throws Exception {
        AtomicBoolean settled = new AtomicBoolean(false);
        ClientSettleGate gate = gate(settled::get);
        AtomicInteger firstRan = new AtomicInteger();
        AtomicInteger secondRan = new AtomicInteger();

        CountDownLatch firstFailed = new CountDownLatch(1);
        AtomicBoolean firstGotSuperseded = new AtomicBoolean(false);
        Thread first = new Thread(() -> {
            try {
                gate.runWhenSettled(firstRan::incrementAndGet, 5_000);
            } catch (CancellationException e) {
                firstGotSuperseded.set(true);
            } catch (Exception ignored) {
            } finally {
                firstFailed.countDown();
            }
        });
        first.start();
        Thread.sleep(50); // let the first call enter its poll loop

        Thread second = new Thread(() -> {
            try {
                gate.runWhenSettled(secondRan::incrementAndGet, 5_000);
            } catch (Exception ignored) {
            }
        });
        second.start();

        assertTrue(firstFailed.await(2, TimeUnit.SECONDS), "superseded caller must fail promptly");
        assertTrue(firstGotSuperseded.get(), "superseded caller must see the cancellation");

        settled.set(true);
        second.join(2_000);
        first.join(2_000);

        assertEquals(0, firstRan.get(), "superseded action must never run");
        assertEquals(1, secondRan.get(), "newest action must run once settled");
    }

    @Test
    void actionExceptionPropagatesToCaller() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> gate(() -> true)
                .runWhenSettled(
                        () -> {
                            throw new IllegalStateException("connect blew up");
                        },
                        1_000));
        assertEquals("connect blew up", e.getMessage());
    }

    @Test
    void settledCheckExceptionPropagatesToCaller() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> gate(() -> {
                    throw new IllegalStateException("overlay probe failed");
                })
                .runWhenSettled(() -> fail("must not run"), 1_000));
        assertEquals("overlay probe failed", e.getMessage());
    }
}
