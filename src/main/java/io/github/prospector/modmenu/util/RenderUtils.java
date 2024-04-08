package io.github.prospector.modmenu.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RenderUtils extends Screen {

	public static final RenderUtils INSTANCE = new RenderUtils();
	private RenderUtils() {}

	public List<String> wrapStringToWidthAsList(TextRenderer font, String text, int width) {
		List<String> words = new ArrayList<>();
		if (text != null)
			Collections.addAll(words, text.split(" "));

		List<String> strings = new ArrayList<>();
		String current = "";
		while (!words.isEmpty()) {
			String nextWord = words.remove(0);
			String next = current.isEmpty() ? nextWord : current + " " + nextWord;
			if (font.getWidth(next) > width) {
				strings.add(current);
				current = nextWord;
			} else {
				current = next;
			}
		}
		if (!current.isEmpty()) {
			strings.add(current);
		}

		return strings;
	}

	public void drawWrappedString(TextRenderer font, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = wrapStringToWidthAsList(font, string, wrapWidth);

		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String line = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				line += "...";
			}
			int x1 = x;
			font.draw(line, x1, y + i * 9, color);
		}
	}

	public void drawBadge(TextRenderer font, int x, int y, int tagWidth, String text, int outlineColor, int fillColor, int textColor) {
		fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		fill(x, y, x + 1, y + 9, outlineColor);
		fill(x + 1, y + 1 + 9 - 1, x + tagWidth, y + 9 + 1, outlineColor);
		fill(x + tagWidth, y, x + tagWidth + 1, y + 9, outlineColor);
		fill(x + 1, y, x + tagWidth, y + 9, fillColor);
		font.draw(text, (x + 1 + (tagWidth - font.getWidth(text)) / 2), y + 1, textColor);
	}
}
