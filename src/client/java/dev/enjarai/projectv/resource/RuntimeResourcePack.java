package dev.enjarai.projectv.resource;

import dev.enjarai.projectv.ProjectV;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RuntimeResourcePack implements ResourcePack {
    private final String name;
    private final Map<Identifier, byte[]> assets = new HashMap<>();


    public static RuntimeResourcePack create(String name) {
        return new RuntimeResourcePack(name);
    }

    protected RuntimeResourcePack(String name) {
        this.name = name;
    }


    public void addTextFile(ResourceType type, Identifier id, String text) {
        addFileContents(type, id, text.getBytes(StandardCharsets.UTF_8));
    }

    public void addImage(Identifier id, NativeImage image) {
        try {
            addFileContents(ResourceType.CLIENT_RESOURCES, id, image.getBytes());
        } catch (IOException e) {
            ProjectV.LOGGER.error("Could not add image to runtime pack: ", e);
        }
    }

    public void addFileContents(ResourceType type, Identifier id, byte[] data) {
        getMap(type).put(id, data);
    }

    public void clear() {
        assets.clear();
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
        if (type == ResourceType.CLIENT_RESOURCES) {
            var asset = getMap(type).get(id);
            if (asset != null) {
                return () -> new ByteArrayInputStream(asset);
            }
        }
        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        var map = getMap(type);
        for (Identifier identifier : map.keySet()) {
            if (identifier.getNamespace().equals(namespace) && identifier.getPath().startsWith(prefix)) {
                var asset = map.get(identifier);
                consumer.accept(identifier, () -> new ByteArrayInputStream(asset));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Set.of(ProjectV.MOD_ID);
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


    /**
     * Added this to potentially support data as well, not really required rn tho.
     */
    protected Map<Identifier, byte[]> getMap(ResourceType type) {
        if (type != ResourceType.CLIENT_RESOURCES) {
            return Map.of();
        }

        return assets;
    }
}
