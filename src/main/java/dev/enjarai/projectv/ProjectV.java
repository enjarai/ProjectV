package dev.enjarai.projectv;

import dev.enjarai.projectv.block.BlockVariantGenerator;
import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ProjectV implements ModInitializer {

	public static final String MOD_ID = "projectv";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BlockVariantGenerator.addDefaultVariants();

		// TODO entry points?

		BlockVariantGenerator.registerVariants();
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