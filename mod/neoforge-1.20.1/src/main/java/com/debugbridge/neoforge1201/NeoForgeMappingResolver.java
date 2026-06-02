package com.debugbridge.neoforge1201;

import com.debugbridge.core.mapping.MappingCache;
import com.debugbridge.core.mapping.MappingDownloader;
import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.mapping.ParsedMappings;
import com.debugbridge.core.mapping.ProGuardParser;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class NeoForgeMappingResolver implements MappingResolver {
    private static final Logger LOG = Logger.getLogger("DebugBridge");

    private final String version;
    private final ParsedMappings mappings;
    private final Map<String, String> srgToMojang = new HashMap<>();

    public NeoForgeMappingResolver(String version) {
        this.version = version;
        ParsedMappings parsed;
        try {
            MappingCache cache = new MappingCache();
            String content;
            if (cache.has(version)) {
                LOG.info("[DebugBridge] Loading cached " + version + " mappings...");
                content = cache.load(version);
            } else {
                LOG.info("[DebugBridge] Downloading " + version + " mappings from Mojang...");
                content = new MappingDownloader().download(version);
                cache.save(version, content);
            }
            parsed = ProGuardParser.parse(content);
            LOG.info("[DebugBridge] Parsed " + parsed.classes.size() + " classes from mappings.");
        } catch (Exception e) {
            LOG.warning("[DebugBridge] Failed to load mappings, using passthrough: " + e.getMessage());
            parsed = new ParsedMappings(
                    java.util.Collections.emptyMap(),
                    java.util.Collections.emptyMap(),
                    java.util.Collections.emptyMap(),
                    java.util.Collections.emptyMap(),
                    java.util.Collections.emptyMap(),
                    java.util.Collections.emptyMap());
        }
        this.mappings = parsed;

        for (var entry : parsed.classes.entrySet()) {
            srgToMojang.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String resolveClass(String mojangClassName) {
        return mappings.classes.getOrDefault(mojangClassName, mojangClassName);
    }

    @Override
    public String unresolveClass(String srgClassName) {
        return srgToMojang.getOrDefault(srgClassName, srgClassName);
    }

    @Override
    public String resolveField(String mojangClassName, String mojangFieldName) {
        Map<String, String> classFields = mappings.fields.get(mojangClassName);
        if (classFields == null) return mojangFieldName;
        return classFields.getOrDefault(mojangFieldName, mojangFieldName);
    }

    @Override
    public String resolveMethod(String mojangClassName, String mojangMethodName, String[] mojangParamTypes) {
        Map<String, String> classMethods = mappings.methods.get(mojangClassName);
        if (classMethods == null) return mojangMethodName;
        if (mojangParamTypes != null) {
            String key = mojangMethodName + "(" + String.join(",", mojangParamTypes) + ")";
            String result = classMethods.get(key);
            if (result != null) return result;
        }
        String prefix = mojangMethodName + "(";
        for (var entry : classMethods.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                return entry.getValue();
            }
        }
        return mojangMethodName;
    }

    @Override
    public Collection<String> getAllClassNames() {
        return mappings.classes.keySet();
    }

    @Override
    public Collection<String> getFieldNames(String mojangClassName) {
        Map<String, String> classFields = mappings.fields.get(mojangClassName);
        return classFields != null ? classFields.keySet() : Collections.emptyList();
    }

    @Override
    public Collection<String> getMethodSignatures(String mojangClassName) {
        Map<String, String> classMethods = mappings.methods.get(mojangClassName);
        return classMethods != null ? classMethods.keySet() : Collections.emptyList();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isObfuscated() {
        return true;
    }
}
