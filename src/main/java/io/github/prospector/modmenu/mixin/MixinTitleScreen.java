package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.gui.screen.menu.MainMenu;
import net.minecraft.client.gui.widgets.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainMenu.class)
public class MixinTitleScreen extends ScreenBase {

	@SuppressWarnings("unchecked")
	@Inject(at = @At("RETURN"), method = "init")
	public void drawMenuButton(CallbackInfo info) {
		Button texturePackButton = (Button) this.buttons.stream().filter(o -> ((Button) o).id == 3).findFirst().orElse(null);
		if (texturePackButton == null) return;
		texturePackButton.text = "Texture Packs";
		int newWidth = ((MixinGuiButton) texturePackButton).getWidth() / 2 - 1;
		((MixinGuiButton) texturePackButton).setWidth(newWidth);
		this.buttons.add(new ModMenuButtonWidget(100, this.width / 2 + 2, texturePackButton.y, newWidth, 20,  "Mods (" + ModMenu.getFormattedModCount() + " loaded)"));
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void onActionPerformed(Button button, CallbackInfo ci) {
		if (button.id == 100) {
			minecraft.openScreen(new ModListScreen(this));
		}
	}

}
