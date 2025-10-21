package yesman.epicfight.main;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;

@OnlyIn(Dist.CLIENT)
public interface AuthenticationHelper {
	public boolean valid();
	
	default void initialize(
		ConfigValue<String> accessToken,
		ConfigValue<String> refreshToken,
		EnumValue<AuthenticationProvider> provider
	) {}
	
	default Screen getAvatarEditorScreen(Screen parentScreen) {
		return null;
	}
	
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
