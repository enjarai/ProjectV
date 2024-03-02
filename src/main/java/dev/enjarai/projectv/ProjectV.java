package dev.enjarai.projectv;

import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.data.BlockVariantLootTableGenerator;
import dev.enjarai.projectv.data.BlockVariantTagGenerator;
import dev.enjarai.projectv.pack.PackAdderEvent;
import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import nl.enjarai.cicada.api.conversation.ConversationManager;
import nl.enjarai.cicada.api.util.CicadaEntrypoint;
import nl.enjarai.cicada.api.util.JsonSource;
import nl.enjarai.cicada.api.util.ProperLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ProjectV implements ModInitializer, CicadaEntrypoint {

	public static final String MOD_ID = "projectv";
    public static final Logger LOGGER = ProperLogger.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BlockVariantGenerator.addDefaultVariants();

		// TODO entry points?

		BlockVariantGenerator.registerVariants();

		PackAdderEvent.EVENT.register((managerType, packs) -> {
			if (managerType == ResourceType.SERVER_DATA) {
				packs.add(BlockVariantTagGenerator.PACK);
				packs.add(BlockVariantLootTableGenerator.PACK);
			}
		});
	}

	@Override
	public void registerConversations(ConversationManager conversationManager) {
		conversationManager.registerSource(
				JsonSource.fromUrl("https://raw.githubusercontent.com/enjarai/ProjectV/master/src/main/resources/cicada/projectv/conversations.json")
						.or(JsonSource.fromResource("cicada/projectv/conversations.json")),
				LOGGER::info
		);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static <T> Identifier constructVariantIdentifier(Registry<T> registry, T original, T material) {
		var originalIdentifier = Objects.requireNonNull(registry.getId(original));
		var materialIdentifier = Objects.requireNonNull(registry.getId(material));

		return new Identifier(
				MOD_ID,
				originalIdentifier.getNamespace() + '_' + originalIdentifier.getPath() + '_' + materialIdentifier.getNamespace() + '_' + materialIdentifier.getPath()
		);
	}
}