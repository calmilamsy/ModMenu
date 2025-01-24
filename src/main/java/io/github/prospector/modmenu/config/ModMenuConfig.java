package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.util.HardcodedUtil;
import lombok.Getter;
import net.fabricmc.loader.api.ModContainer;

import java.util.Comparator;
import java.util.Properties;

public class ModMenuConfig {
	private boolean showLibraries = false;
	@Getter
    private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Properties serialize() {
		Properties properties = new Properties();
		properties.put("showLibraries", showLibraries ? "true" : "false");
		properties.put("sorting", sorting.name());
		return properties;
	}

	public void deserialize(Properties properties) {
		showLibraries = properties.getOrDefault("showLibraries", "false").equals("true");
		sorting = Sorting.valueOf((String) properties.getOrDefault("sorting", "ASCENDING"));
    }

    @Getter
    public enum Sorting {
		ASCENDING(Comparator.comparing(modContainer -> HardcodedUtil.formatFabricModuleName(modContainer.getMetadata().getName())), "A-Z"),
		DECENDING(ASCENDING.getComparator().reversed(), "Z-A");

		final Comparator<ModContainer> comparator;
		final String name;

		Sorting(Comparator<ModContainer> comparator, String name) {
			this.comparator = comparator;
			this.name = name;
		}

    }
}
