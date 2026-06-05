package com.debugbridge.neoforge1211;

import com.debugbridge.core.lua.ThreadDispatcher;
import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.screenshot.ScreenshotProvider;
import com.debugbridge.core.server.BridgeServer;
import com.debugbridge.core.snapshot.GameStateProvider;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Helper to create BridgeServer instances in dev mode where ModDevGradle's
 * module layer may not include Java-WebSocket. Uses a child-first classloader
 * that loads from the application classpath before the module layer.
 */
public class BridgeServerFactory {

    private static volatile ClassLoader extLoader;

    private static synchronized ClassLoader extLoader() {
        ClassLoader l = extLoader;
        if (l != null) return l;
        // Scan the classpath for the Java-WebSocket jar
        String cp = System.getProperty("java.class.path", "");
        URL[] urls = java.util.Arrays.stream(
                        cp.split(java.util.regex.Pattern.quote(System.getProperty("path.separator"))))
                .filter(s -> s.contains("java-websocket") || s.contains("Java-WebSocket"))
                .map(s -> {
                    try {
                        return new java.io.File(s).toURI().toURL();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(u -> u != null)
                .toArray(URL[]::new);
        if (urls.length == 0) {
            extLoader = Thread.currentThread().getContextClassLoader();
        } else {
            extLoader = new URLClassLoader(urls, BridgeServerFactory.class.getClassLoader());
        }
        extLoader = l;
        return extLoader;
    }

    public static BridgeServer create(
            int port,
            MappingResolver resolver,
            ThreadDispatcher dispatcher,
            GameStateProvider stateProvider,
            ScreenshotProvider screenshotProvider)
            throws Exception {
        ClassLoader cl = extLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            return new BridgeServer(port, resolver, dispatcher, stateProvider, screenshotProvider);
        } finally {
            Thread.currentThread().setContextClassLoader(null);
        }
    }
}
