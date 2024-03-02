package dev.enjarai.projectv.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.pack.RuntimePack;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BlockVariantLootTableGenerator {

    public static final RuntimePack PACK = RuntimePack.create("Project V: Block Loot Tables", ResourceType.SERVER_DATA);

    public static void reload(ResourceManager manager) {
        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        BlockVariantGenerator.iterateOverGroups(materialGroup ->
                BlockVariantGenerator.iterateOverVariants(materialGroup, ((baseBlock, materialBlock) ->
                        manager.getResource(baseBlock.getLootTableId().withPrefixedPath("loot_tables/").withSuffixedPath(".json")).ifPresent(resource -> {
                var variantIdentifier = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);
                try (var reader = resource.getReader()) {
                    var lootTable = JsonParser.parseReader(reader).getAsJsonObject();
                    for (var pool : lootTable.get("pools").getAsJsonArray()) {
                        for (var entry : pool.getAsJsonObject().get("entries").getAsJsonArray()) {
                            var entryObject = entry.getAsJsonObject();
                            // for stuff like silk touch
                            if (entryObject.get("type").getAsString().equals("minecraft:alternatives")) {
                                for (var child : entryObject.get("children").getAsJsonArray()) {
                                    replaceEntry(child.getAsJsonObject(), baseBlock, variantIdentifier);
                                }
                            }
                            // for normal drops
                            replaceEntry(entryObject, baseBlock, variantIdentifier);
                        }
                    }
                    mapBuilder.put(variantIdentifier.withPrefixedPath("loot_tables/blocks/").withSuffixedPath(".json"), lootTable.toString().getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    // could be IO based or malformed JSON
                    ProjectV.LOGGER.error("While generating loot tables", e);
                }
        }))));
        var map = mapBuilder.buildKeepingLast();
        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.SERVER_DATA, key, value));
    }

    private static void replaceEntry(JsonObject entry, Block baseBlock, Identifier variantIdentifier) {
        if (entry.get("type").getAsString().equals("minecraft:item")) {
            var name = entry.getAsJsonObject().get("name").getAsString();
            if (name.equals(Registries.BLOCK.getId(baseBlock).toString())) {
                entry.getAsJsonObject().addProperty("name", variantIdentifier.toString());
            }
            // TODO replace the material once we store the original material
        }
    }

}
