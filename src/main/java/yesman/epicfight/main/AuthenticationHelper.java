package yesman.epicfight.main;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

@OnlyIn(Dist.CLIENT)
public interface AuthenticationHelper {
	boolean valid();
	
	void initialize(
		ForgeConfigSpec.ConfigValue<String> accessToken,
		ForgeConfigSpec.ConfigValue<String> refreshToken,
		ForgeConfigSpec.EnumValue<AuthenticationProvider> provider
	);
	
	default Screen getAvatarEditorScreen(Screen parentScreen) {
		return null;
	}
	
	Status status();
	
	@OnlyIn(Dist.CLIENT)
	public enum Status {
		UNAUTHENTICATED, AUTHENTICATED, OFFLINE_MODE;
	}
	
	@OnlyIn(Dist.CLIENT)
	public enum AuthenticationProvider {
		NULL("null"), DISCORD("discord"), PATREON("patreon");
		
		String signature;
		
		AuthenticationProvider(String signature) {
			this.signature = signature;
		}
		
		@Override
		public String toString() {
			return this.signature;
		}
	}
}
