package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.ModMenu;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Properties;

public class ModMenuConfigManager {
	private static File file;
	@Getter
    private static ModMenuConfig config;

	private static void prepareConfigFile() {
		if (file != null) {
			return;
		}
		file = new File(FabricLoader.getInstance().getConfigDir().toFile(), ModMenu.MOD_ID + ".properties");
	}

	public static void initializeConfig() {
		if (config != null) {
			return;
		}

		config = new ModMenuConfig();
		load();
	}

	private static void load() {
		prepareConfigFile();

		try {
			if (!file.exists()) {
				save();
			}
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));

				Properties modProps = new Properties();
				modProps.load(br);
				config = new ModMenuConfig();
				config.deserialize(modProps);
			}
		} catch (IOException e) {
			System.err.println("Couldn't load Mod Menu configuration file; reverting to defaults");
			e.printStackTrace();
        }
    }

	public static void save() {
		prepareConfigFile();

		try (FileWriter fileWriter = new FileWriter(file)) {
			config.serialize().store(fileWriter, "ModMenu Config");
		} catch (IOException e) {
			System.err.println("Couldn't save Mod Menu configuration file");
			e.printStackTrace();
		}
	}

}
