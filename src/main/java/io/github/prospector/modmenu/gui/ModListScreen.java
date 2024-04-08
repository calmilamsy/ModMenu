package io.github.prospector.modmenu.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.io.File;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.*;

public class ModListScreen extends Screen {
	private static final String FILTERS_BUTTON_LOCATION = "/assets/" + ModMenu.MOD_ID + "/textures/gui/filters_button.png";
	private static final String CONFIGURE_BUTTON_LOCATION = "/assets/" + ModMenu.MOD_ID + "/textures/gui/configure_button.png";
	private static final Logger LOGGER = LogManager.getLogger();
	private final String textTitle;
	private TextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private Screen parent;
	private ModListWidget modList;
	private String tooltip;
	private ModListEntry selected;
	private BadgeRenderer badgeRenderer;
	private double scrollPercent = 0;
	private boolean showModCount = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	public Set<String> showModChildren = new HashSet<>();
	private String lastSearchString = null;

	private static final int CONFIGURE_BUTTON_ID = 0;
	private static final int WEBSITE_BUTTON_ID = 1;
	private static final int ISSUES_BUTTON_ID = 2;
	private static final int TOGGLE_FILTER_OPTIONS_BUTTON_ID = 3;
	private static final int TOGGLE_SORT_MODE_BUTTON_ID = 4;
	private static final int TOGGLE_SHOW_LIBRARIES_BUTTON_ID = 5;
	private static final int MODS_FOLDER_BUTTON_ID = 6;
	private static final int DONE_BUTTON_ID = 7;

	public ModListScreen(Screen previousGui) {
		this.parent = previousGui;
		this.textTitle = "Mods";
	}

	@Override
	public void onMouseEvent() {
		super.onMouseEvent();
		int dWheel = Mouse.getEventDWheel()/50;
		if (dWheel != 0) {
			int mouseX = Mouse.getEventX() * this.width / this.minecraft.displayWidth; // field_6326_c
			int mouseY = this.height - Mouse.getEventY() * this.height / this.minecraft.displayHeight - 1; // field_6325_d
			mouseScrolled(mouseX, mouseY, dWheel);
		}
	}

	public void mouseScrolled(double double_1, double double_2, double double_3) {
		if (modList.isMouseOver(double_1, double_2))
			this.modList.mouseScrolled(double_1, double_2, double_3);
		if (descriptionListWidget.isMouseOver(double_1, double_2))
			this.descriptionListWidget.mouseScrolled(double_1, double_2, double_3);
	}

	@Override
	public void tick() {
		this.searchBox.updateCursorCounter();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		Keyboard.enableRepeatEvents(true);
		TextRenderer font = textRenderer;
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int searchBoxWidth = paneWidth - 32 - 22;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
		String oldText = this.searchBox == null ? "" : this.searchBox.getText();
		this.searchBox = new TextFieldWidget(this.textRenderer, searchBoxX, 22, searchBoxWidth, 20); // field_6451_g
		this.searchBox.setText(oldText);
		this.modList = new ModListWidget(this.minecraft, paneWidth, this.height, paneY + 19, this.height - 36, 36, this.searchBox.getText(), this.modList, this);
		this.modList.setLeftPos(0);
		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneWidth, this.height, paneY + 60, this.height - 36, 9 + 1, this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		ButtonWidget configureButton = new ModMenuTexturedButtonWidget(CONFIGURE_BUTTON_ID, width - 24, paneY, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64) {
			@Override
			public void render(Minecraft mc, int mouseX, int mouseY) {
				if (selected != null) {
					String modid = selected.getMetadata().getId();
					active = ModMenu.hasConfigScreenFactory(modid) || ModMenu.hasLegacyConfigScreenTask(modid);
				} else {
					active = false;
				}
				visible = active; // visible = enabled
				GL11.glColor4f(1f, 1f, 1f, 1f);
				super.render(mc, mouseX, mouseY);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		ButtonWidget websiteButton = new ButtonWidget(WEBSITE_BUTTON_ID, rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20, "Website") {
			@Override
			public void render(Minecraft mc, int var1, int var2) {
				visible = selected != null; // visible = selected != null
				active = visible && selected.getMetadata().getContact().get("homepage").isPresent();
				super.render(mc, var1, var2);
			}
		};
		ButtonWidget issuesButton = new ButtonWidget(ISSUES_BUTTON_ID, rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, Math.min(urlButtonWidths, 200), 20, "Issues") {
			@Override
			public void render(Minecraft mc, int var1, int var2) {
				visible = selected != null; // visible = selected != null
				active = visible  && selected.getMetadata().getContact().get("issues").isPresent();
				super.render(mc, var1, var2);
			}
		};
		this.buttons.add(new ModMenuTexturedButtonWidget(TOGGLE_FILTER_OPTIONS_BUTTON_ID, paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, FILTERS_BUTTON_LOCATION, 32, 64) {
			@Override
			public void render(Minecraft mc, int int_1, int int_2) {
				super.render(mc, int_1, int_2);
				if (isHovered(int_1, int_2)) {
					setTooltip("Toggle Filter Options");
				}
			}
		});
		String showLibrariesText = ModMenuConfigManager.getConfig().showLibraries() ? "Libraries: Shown" : "Libraries: Hidden";
		String sortingText = "Sort: " + ModMenuConfigManager.getConfig().getSorting().getName();
		int showLibrariesWidth = font.getWidth(showLibrariesText) + 20;
		int sortingWidth = font.getWidth(sortingText) + 20;
		int filtersX;
		int filtersWidth = showLibrariesWidth + sortingWidth + 2;
		if ((filtersWidth + font.getWidth("Showing " + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size()) + "/" + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size()) + " Mods") + 20) >= searchBoxX + searchBoxWidth + 22) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			showModCount = false;
		} else {
			filtersX = searchBoxX + searchBoxWidth + 22 - filtersWidth + 1;
			showModCount = true;
		}
		this.buttons.add(new ButtonWidget(TOGGLE_SORT_MODE_BUTTON_ID, filtersX, 45, sortingWidth, 20, sortingText) {
			@Override
			public void render(Minecraft mc, int mouseX, int mouseY) {
				visible = active = filterOptionsShown;
				this.text = "Sort: " + ModMenuConfigManager.getConfig().getSorting().getName();
				super.render(mc, mouseX, mouseY);
			}
		});
		this.buttons.add(new ButtonWidget(TOGGLE_SHOW_LIBRARIES_BUTTON_ID, filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText) {
			@Override
			public void render(Minecraft mc, int mouseX, int mouseY) {
				visible = active = filterOptionsShown;
				this.text = ModMenuConfigManager.getConfig().showLibraries() ? "Libraries: Shown" : "Libraries: Hidden";
				super.render(mc, mouseX, mouseY);
			}
		});
		this.buttons.add(configureButton);
		this.buttons.add(websiteButton);
		this.buttons.add(issuesButton);
		this.buttons.add(GuiButtonAccessor.createButton(MODS_FOLDER_BUTTON_ID, this.width / 2 - 154, this.height - 28, 150, 20, "Open Mods Folder"));
		this.buttons.add(GuiButtonAccessor.createButton(DONE_BUTTON_ID, this.width / 2 + 4, this.height - 28, 150, 20, "Done"));
		this.searchBox.setFocused(true);

		init = true;
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case CONFIGURE_BUTTON_ID: {
				final String modid = Objects.requireNonNull(selected).getMetadata().getId();
				final Screen screen = ModMenu.getConfigScreen(modid, this);
				if (screen != null) {
					minecraft.setScreen(screen);
				} else {
					ModMenu.openConfigScreen(modid);
				}
				break;
			}
			case WEBSITE_BUTTON_ID: {
				final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
				metadata.getContact().get("homepage").ifPresent(Sys::openURL);
				break;
			}
			case ISSUES_BUTTON_ID: {
				final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
				metadata.getContact().get("issues").ifPresent(Sys::openURL);
				break;
			}
			case TOGGLE_FILTER_OPTIONS_BUTTON_ID: {
				filterOptionsShown = !filterOptionsShown;
				break;
			}
			case TOGGLE_SORT_MODE_BUTTON_ID: {
				ModMenuConfigManager.getConfig().toggleSortMode();
				modList.reloadFilters();
				break;
			}
			case TOGGLE_SHOW_LIBRARIES_BUTTON_ID: {
				ModMenuConfigManager.getConfig().toggleShowLibraries();
				modList.reloadFilters();
				break;
			}
			case MODS_FOLDER_BUTTON_ID: {
				File modsFolder = new File(FabricLoader.getInstance().getGameDirectory(), "mods");
				try {
					Sys.openURL(modsFolder.toURI().toURL().toString());
				} catch (MalformedURLException e) {
					LOGGER.error("Malformed mods folder URL", e);
				}
				break;
			}
			case DONE_BUTTON_ID: {
				minecraft.setScreen(parent);
				break;
			}
		}
	}

	public ModListWidget getModList() {
		return modList;
	}

	@Override
	public void keyPressed(char char_1, int int_1) {
		this.searchBox.textboxKeyTyped(char_1, int_1);
		super.keyPressed(char_1, int_1);
		modList.keyPressed(int_1, 0, 0);
		descriptionListWidget.keyPressed(int_1, 0, 0);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		modList.mouseClicked(mouseX, mouseY, mouseButton);
		descriptionListWidget.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		super.mouseReleased(mouseX, mouseY, mouseButton);
		if (mouseButton != -1) {
			modList.mouseReleased(mouseX, mouseY, mouseButton);
			descriptionListWidget.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		int mouseDX = Mouse.getEventDX() * this.width / this.minecraft.displayWidth; // field_6326_c
		int mouseDY = this.height - Mouse.getEventDY() * this.height / this.minecraft.displayHeight - 1; // field_6325_d
		for (int button = 0; button < Mouse.getButtonCount(); button++) {
			if (Mouse.isButtonDown(button)) {
				modList.mouseDragged(mouseX, mouseY, button, mouseDX, mouseDY);
				descriptionListWidget.mouseDragged(mouseX, mouseY, button, mouseDX, mouseDY);
			}
		}
		TextRenderer font = this.textRenderer;
		if (!searchBox.getText().equals(lastSearchString)) {
			lastSearchString = searchBox.getText();
			modList.filter(lastSearchString, false);
		}
		overlayBackground(paneWidth, 0, rightPaneX, height, 64, 64, 64, 255, 255);
		this.tooltip = null;
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(mouseX, mouseY, delta);
		}
		this.modList.render(mouseX, mouseY, delta);
		this.searchBox.drawTextBox();
		GL11.glDisable(GL11.GL_BLEND);
		this.drawCenteredTextWithShadow(font, this.textTitle, this.modList.getWidth() / 2, 8, 0xffffff);
		super.render(mouseX, mouseY, delta);
		if (showModCount || !filterOptionsShown) {
			font.draw("Showing " + NumberFormat.getInstance().format(modList.getDisplayedCount()) + "/" + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size()) + " Mods", searchBoxX, 52, 0xFFFFFF);
		}
		if (selectedEntry != null) {
			ModMetadata metadata = selectedEntry.getMetadata();
			int x = rightPaneX;
			GL11.glColor4f(1f, 1f, 1f, 1f);
			this.selected.bindIconTexture();
			GL11.glEnable(GL11.GL_BLEND);
			Tessellator tess = Tessellator.INSTANCE;
			tess.startQuads();
			tess.vertex(x, paneY, 0, 0, 0);
			tess.vertex(x, paneY + 32, 0, 0, 1);
			tess.vertex(x + 32, paneY + 32, 0, 1, 1);
			tess.vertex(x + 32, paneY, 0, 1, 0);
			tess.draw();
			GL11.glDisable(GL11.GL_BLEND);
			int lineSpacing = 9 + 1;
			int imageOffset = 36;
			String name = metadata.getName();
			name = HardcodedUtil.formatFabricModuleName(name);
			String trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.getWidth(name) > maxNameWidth) {
				int maxWidth = maxNameWidth - font.getWidth("...");
				trimmedName = "";
				while (font.getWidth(trimmedName) < maxWidth && trimmedName.length() < name.length()) {
					trimmedName += name.charAt(trimmedName.length());
				}
				trimmedName = trimmedName.isEmpty() ? "..." : trimmedName.substring(0, trimmedName.length() - 1) + "...";
			}
			font.draw(trimmedName, x + imageOffset, paneY + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + 9 && mouseX < x + imageOffset + font.getWidth(trimmedName)) {
				setTooltip("Mod ID: " + metadata.getId());
			}
			if (init || badgeRenderer == null || badgeRenderer.getMetadata() != metadata) {
				badgeRenderer = new BadgeRenderer(minecraft, x + imageOffset + font.getWidth(trimmedName) + 2, paneY, width - 28, selectedEntry.container, this);
				init = false;
			}
			badgeRenderer.draw(mouseX, mouseY);
			font.draw("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080);
			String authors;
			List<String> names = new ArrayList<>();

			metadata.getAuthors().stream()
				.filter(Objects::nonNull)
				.map(Person::getName)
				.filter(Objects::nonNull)
				.forEach(names::add);

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				RenderUtils.INSTANCE.drawWrappedString(font, "By " + authors, x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
			if (this.tooltip != null) {
				this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), mouseX, mouseY);
			}
		}

	}

	public void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.INSTANCE;
		minecraft.textureManager.bindTexture(minecraft.textureManager.getTextureId("/gui/background.png"));
		GL11.glColor4f(1f, 1f, 1f, 1f);
		tessellator.startQuads();
		tessellator.color(red, green, blue, endAlpha);
		tessellator.vertex(x1, y2, 0.0D, x1 / 32.0F, y2 / 32.0F);
		tessellator.vertex(x2, y2, 0.0D, x2 / 32.0F, y2 / 32.0F);
		tessellator.color(red, green, blue, startAlpha);
		tessellator.vertex(x2, y1, 0.0D, x2 / 32.0F, y1 / 32.0F);
		tessellator.vertex(x1, y1, 0.0D, x1 / 32.0F, y1 / 32.0F);
		tessellator.draw();
	}

	@Override
	public void removed() {
		super.removed();
		this.modList.close();
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getText();
	}

	public boolean showingFilterOptions() {
		return filterOptionsShown;
	}

	public void renderTooltip(List<String> list, int i, int j) {
		if (!list.isEmpty()) {
			TextRenderer font = textRenderer;

			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;

			for (String string : list) {
				int l = font.getWidth(string);
				if (l > k) {
					k = l;
				}
			}

			int m = i + 12;
			int n = j - 12;
			int p = 8;
			if (list.size() > 1) {
				p += 2 + (list.size() - 1) * 10;
			}

			if (m + k > this.width) {
				m -= 28 + k;
			}

			if (n + p + 6 > this.height) {
				n = this.height - p - 6;
			}

			int transparentGrey = -1073741824;
			int margin = 3;
			this.fillGradient(m - margin, n - margin, m + k + margin,
					n + p + margin, transparentGrey, transparentGrey);
			GL11.glPushMatrix();
			GL11.glTranslatef(0, 0, 300);

			for(int t = 0; t < list.size(); ++t) {
				String string2 = list.get(t);
				if (string2 != null) {
					font.draw(string2, m, n, 0xffffff);
				}

				if (t == 0) {
					n += 2;
				}

				n += 10;
			}

			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	protected void fillGradient(int i, int j, int k, int l, int m, int n) {
		float f = (float)(m >> 24 & 255) / 255.0F;
		float g = (float)(m >> 16 & 255) / 255.0F;
		float h = (float)(m >> 8 & 255) / 255.0F;
		float o = (float)(m & 255) / 255.0F;
		float p = (float)(n >> 24 & 255) / 255.0F;
		float q = (float)(n >> 16 & 255) / 255.0F;
		float r = (float)(n >> 8 & 255) / 255.0F;
		float s = (float)(n & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.startQuads();
		tessellator.color(g, h, o, f);
		tessellator.vertex(k, j, 300);
		tessellator.vertex(i, j, 300);
		tessellator.color(q, r, s, p);
		tessellator.vertex(i, l, 300);
		tessellator.vertex(k, l, 300);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
