package dev.enjarai.projectv.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.JsonOps;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.pack.RuntimePack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class BlockVariantTagGenerator {

    public static final RuntimePack PACK = RuntimePack.create("Project V: Block Tags", ResourceType.SERVER_DATA);

    public static void reload() {
        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        BlockVariantGenerator.iterateOverBlockTags((blocks, tag) -> {
            var result = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(
                    blocks.stream()
                            .map(Registries.BLOCK::getId)
                            .map(TagEntry::create)
                            .toList(),
                    false
            ));
            var json = result.resultOrPartial(ProjectV.LOGGER::error).orElseThrow();
            System.out.println(json);
            mapBuilder.put(tag.id().withPrefixedPath("tags/blocks/").withSuffixedPath(".json"), json.toString().getBytes(StandardCharsets.UTF_8));
        });
        var map = mapBuilder.buildKeepingLast();
        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.SERVER_DATA, key, value));
    }

}
