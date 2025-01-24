package io.github.prospector.modmenu;

import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_285;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final List<String> LIBRARY_MODS = new ArrayList<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
	public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
	public static final Map<ModContainer, List<ModContainer>> PARENT_MAP = new HashMap<>();
	private static Map<String, Function<Screen, ? extends Screen>> configScreenFactories = new HashMap<>();
	private static int libraryCount = 0;
	public static class_285 currentTexturePack;

	public static boolean hasConfigScreenFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		Function<Screen, ? extends Screen> factory = configScreenFactories.get(modid);
		return factory != null ? factory.apply(menuScreen) : null;
	}

	public static void openConfigScreen(String modid) {
		Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
		if (opener != null) opener.run();
	}

	public static void addLegacyConfigScreenTask(String modid, Runnable task) {
		LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
	}

	public static boolean hasLegacyConfigScreenTask(String modid) {
		return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
	}

	public static void addLibraryMod(String modid) {
		if (LIBRARY_MODS.contains(modid)) return;

		LIBRARY_MODS.add(modid);
	}

	@Override
	public void onInitializeClient() {
		ModMenuConfigManager.initializeConfig();
		FabricLoader.getInstance().getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> configScreenFactories.put(api.getModId(), api.getConfigScreenFactory()));
		configScreenFactories.put("minecraft", (screenBase -> new OptionsScreen(screenBase, ((Minecraft) FabricLoader.getInstance().getGameInstance()).options)));
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		HardcodedUtil.initializeHardcodings();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();

			if (metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean()) {
				addLibraryMod(id);
			}
			if (!id.equals("minecraft") && metadata.getType().equals("builtin")) {
				addLibraryMod(id);
			}
			for (ModContainer containedMod : mod.getContainedMods()) {
				if(!metadata.containsCustomValue("modmenu:showIfContained") || !metadata.getCustomValue("modmenu:showIfContained").getAsBoolean()) {
					addLibraryMod(containedMod.getMetadata().getId());
				}
			}
			if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
				CLIENTSIDE_MODS.add(id);
			}
			if (metadata.containsCustomValue("patchwork:source") && metadata.getCustomValue("patchwork:source").getAsObject() != null) {
				CustomValue.CvObject object = metadata.getCustomValue("patchwork:source").getAsObject();
				if ("forge".equals(object.get("loader").getAsString())) {
					PATCHWORK_FORGE_MODS.add(id);
				}
			}
			if (metadata.containsCustomValue("modmenu:parent")) {
				String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
				if (parentId != null) {
					Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
					parent.ifPresent(modContainer -> {
						if (PARENT_MAP.containsKey(modContainer)) {
							PARENT_MAP.put(modContainer, new ArrayList<>());
						}
						PARENT_MAP.get(modContainer).add(mod);
					});
				}
			} else {
				HardcodedUtil.hardcodeModuleMetadata(mod, metadata, id);
			}
		}
		libraryCount = LIBRARY_MODS.size();
	}

	public static String getFormattedModCount() {
		return NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size());
	}
}
