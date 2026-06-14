package com.debugbridge.neoforge1211;

import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.mapping.ParsedMappings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mapping resolver for NeoForge 1.21.1.
 *
 * NeoForge uses Mojang mappings at runtime — all names pass through
 * unchanged for resolution. The downloaded ProGuard mapping data is used
 * only to support java.find() search queries.
 */
public class NeoForgeSearchResolver implements MappingResolver {
    private final String version;
    private final ParsedMappings mappings;

    public NeoForgeSearchResolver(String version, ParsedMappings mappings) {
        this.version = version;
        this.mappings = mappings;
    }

    @Override
    public String resolveClass(String name) {
        return name;
    }

    @Override
    public String resolveField(String cls, String name) {
        return name;
    }

    @Override
    public String resolveMethod(String cls, String name, String[] params) {
        return name;
    }

    @Override
    public String unresolveClass(String name) {
        return name;
    }

    @Override
    public Collection<String> getAllClassNames() {
        return mappings.classes.keySet();
    }

    @Override
    public Collection<String> getFieldNames(String mojangClassName) {
        Map<String, String> classFields = mappings.fields.get(mojangClassName);
        if (classFields == null) return Collections.emptyList();
        return new ArrayList<>(classFields.keySet());
    }

    @Override
    public Collection<String> getMethodSignatures(String mojangClassName) {
        Map<String, String> classMethods = mappings.methods.get(mojangClassName);
        if (classMethods == null) return Collections.emptyList();
        List<String> sigs = new ArrayList<>();
        for (String key : classMethods.keySet()) {
            sigs.add(ParsedMappings.simpleMethodName(key));
        }
        return sigs;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }
}
