package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

	@SuppressWarnings("unchecked")
	@Inject(at = @At("RETURN"), method = "init")
	public void drawMenuButton(CallbackInfo info) {
		ButtonWidget texturePackButton = minecraft.isApplet ? (ButtonWidget) this.buttons.get(this.buttons.size() - 2) : (ButtonWidget) this.buttons.get(2);

		if (FabricLoader.getInstance().isModLoaded("legacytranslations")) {
			TranslationStorage var2 = TranslationStorage.getInstance();
			texturePackButton.text = var2.get("menu.texturepacks");
			int newWidth = ((MixinGuiButton) texturePackButton).getWidth() / 2 - 1;
			((MixinGuiButton) texturePackButton).setWidth(newWidth);
			String strLoaded = (var2.get("menu.mods.loaded").equals("menu.mods.loaded")) ? "loaded" : var2.get("menu.mods.loaded");
			this.buttons.add(new ModMenuButtonWidget(100, this.width / 2 + 2, texturePackButton.y, newWidth, 20,  var2.get("menu.mods") + " (" + ModMenu.getFormattedModCount() + " " + strLoaded + ")"));
		} else {
			texturePackButton.text = "Texture Packs";
			int newWidth = ((MixinGuiButton) texturePackButton).getWidth() / 2 - 1;
			((MixinGuiButton) texturePackButton).setWidth(newWidth);
			this.buttons.add(new ModMenuButtonWidget(100, this.width / 2 + 2, texturePackButton.y, newWidth, 20,  "Mods (" + ModMenu.getFormattedModCount() + " loaded)"));
		}
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void onActionPerformed(ButtonWidget button, CallbackInfo ci) {
		if (button.id == 1 || button.id == 2) {
			ModMenu.currentTexturePack = this.minecraft.texturePacks.selected;
		}
		if (button.id == 100) {
			minecraft.setScreen(new ModListScreen(this));
		}
	}

}
