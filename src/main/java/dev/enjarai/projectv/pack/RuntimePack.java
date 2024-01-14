package dev.enjarai.projectv.pack;

import dev.enjarai.projectv.ProjectV;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RuntimePack implements ResourcePack {
    private final String name;
    private final Map<Identifier, byte[]> contents = new HashMap<>();
    private final ResourceType type;

    public static RuntimePack create(String name, ResourceType type) {
        return new RuntimePack(name, type);
    }

    protected RuntimePack(String name, ResourceType type) {
        this.name = name;
        this.type = type;
    }

    public void addFileContents(ResourceType type, Identifier id, byte[] data) {
        getMap(type).put(id, data);
    }

    public void clear() {
        contents.clear();
    }


    /**
     * Used for things like pack.png apparently. TODO Maybe return the mod icon here?
     */
    @Nullable
    @Override
    public InputSupplier<InputStream> openRoot(String... segments) {
        return () -> new ByteArrayInputStream(new byte[0]);
    }

    @Nullable
    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (type == this.type) {
            var content = getMap(type).get(id);
            if (content != null) {
                return () -> new ByteArrayInputStream(content);
            }
        }
        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        var map = getMap(type);
        for (Identifier identifier : map.keySet()) {
            if (identifier.getNamespace().equals(namespace) && identifier.getPath().startsWith(prefix)) {
                var content = map.get(identifier);
                consumer.accept(identifier, () -> new ByteArrayInputStream(content));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Set.of(ProjectV.MOD_ID, "c", "minecraft");
    }

    /**
     * TODO Should do something with pack.mcmeta, we'll want to return nice values here at some point.
     */
    @Nullable
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
        return null;
    }

    @Override
    public String getName() {
        return "ProjectV Runtime Resource Pack";
    }

    @Override
    public void close() {

    }


    protected Map<Identifier, byte[]> getMap(ResourceType type) {
        if (type != this.type) {
            return Map.of();
        }

        return contents;
    }
}
