package io.github.prospector.modmenu.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;

public class ModMenuTexturedButtonWidget extends ButtonWidget {
	private final String texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture) {
		this(buttonId, x, y, width, height, u, v, texture, 256, 256);
	}

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture, int uWidth, int vHeight) {
		this(buttonId, x, y, width, height, u, v, texture, uWidth, vHeight, "");
	}

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture, int uWidth, int vHeight, String message) {
		super(buttonId, x, y, width, height, message);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	protected void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	protected boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
	}

	@Override
	public void render(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			TextRenderer font = mc.textRenderer;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.textureManager.getTextureId(texture));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			boolean hovered = isHovered(mouseX, mouseY);

			int adjustedV = this.v;
			if (!active) {
				adjustedV += this.height * 2;
			} else if (hovered) {
				adjustedV += this.height;
			}
			float uScale = 1f / uWidth;
			float vScale = 1f / vHeight;
			Tessellator tess = Tessellator.INSTANCE;
			tess.startQuads();
			tess.vertex(x, y + height, this.zOffset, (float) u * uScale, (float)(adjustedV + height) * vScale);
			tess.vertex(x + width, y + height, this.zOffset, ((float)(u + width) * uScale), (float)(adjustedV + height) * vScale);
			tess.vertex(x + width, y, this.zOffset, (float)(u + width) * uScale, (float)adjustedV * vScale);
			tess.vertex(x, y, this.zOffset, (float) u * uScale, (float) adjustedV * vScale);
			tess.draw();

			this.renderBackground(mc, mouseX, mouseY);
			if (!this.active) {
				this.drawCenteredTextWithShadow(font, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffa0a0a0);
			} else if (hovered) {
				this.drawCenteredTextWithShadow(font, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffa0);
			} else {
				this.drawCenteredTextWithShadow(font, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xe0e0e0);
			}
		}
	}
}
