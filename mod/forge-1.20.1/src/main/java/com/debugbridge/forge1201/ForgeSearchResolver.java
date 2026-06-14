package com.debugbridge.forge1201;

import com.debugbridge.core.mapping.MappingResolver;
import com.debugbridge.core.mapping.ParsedMappings;
import java.util.Collection;
import java.util.Collections;

/**
 * Resolver for Forge 1.20.1 which uses official (Mojang) mappings at runtime.
 * Class/field/method names at runtime ARE Mojang names, so resolve/unresolve
 * are passthrough. The ParsedMappings are used only for search/exploration
 * via getAllClassNames, getFieldNames, and getMethodSignatures.
 */
public class ForgeSearchResolver implements MappingResolver {
    private final String version;
    private final ParsedMappings mappings;

    public ForgeSearchResolver(String version, ParsedMappings mappings) {
        this.version = version;
        this.mappings = mappings;
    }

    @Override
    public String resolveClass(String mojangClassName) {
        return mojangClassName;
    }

    @Override
    public String resolveField(String mojangClassName, String mojangFieldName) {
        return mojangFieldName;
    }

    @Override
    public String resolveMethod(String mojangClassName, String mojangMethodName, String[] mojangParamTypes) {
        return mojangMethodName;
    }

    @Override
    public String unresolveClass(String runtimeClassName) {
        return runtimeClassName;
    }

    @Override
    public Collection<String> getAllClassNames() {
        return mappings.classes.keySet();
    }

    @Override
    public Collection<String> getFieldNames(String mojangClassName) {
        var fields = mappings.fields.get(mojangClassName);
        if (fields == null) return Collections.emptyList();
        return fields.keySet();
    }

    @Override
    public Collection<String> getMethodSignatures(String mojangClassName) {
        var methods = mappings.methods.get(mojangClassName);
        if (methods == null) return Collections.emptyList();
        return methods.keySet();
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
