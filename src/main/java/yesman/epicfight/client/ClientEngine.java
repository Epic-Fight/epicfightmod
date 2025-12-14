package yesman.epicfight.client;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfigSpec;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.network.server.SPPlayUISound;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class ClientEngine {
	private static ClientEngine instance = new ClientEngine();
	
	public static ClientEngine getInstance() {
		return instance;
	}
	
	public Minecraft minecraft;
	public RenderEngine renderEngine;
	public ControlEngine controlEngine;
	private boolean vanillaModelDebuggingMode = false;
	private AuthenticationHelper authenticationHelper = new AuthenticationHelper() {
		@Override
		public void initialize(
			ForgeConfigSpec.ConfigValue<String> accessToken,
			ForgeConfigSpec.ConfigValue<String> refreshToken,
			ForgeConfigSpec.EnumValue<AuthenticationProvider> provider
		) {
		}
		
		@Override
		public boolean valid() {
			return false;
		}

		@Override
		public Status status() {
			return Status.OFFLINE_MODE;
		}
	};
	
	public ClientEngine() {
		instance = this;
		this.minecraft = Minecraft.getInstance();
		this.renderEngine = new RenderEngine();
		this.controlEngine = new ControlEngine();
	}
	
	public boolean switchVanillaModelDebuggingMode() {
		this.vanillaModelDebuggingMode = !this.vanillaModelDebuggingMode;
		return this.vanillaModelDebuggingMode;
	}
	
	public boolean isVanillaModelDebuggingMode() {
		return this.vanillaModelDebuggingMode;
	}
	
	/**
	 * DEPRECATED: use {@link EpicFightCapabilities#getUnparameterizedEntityPatch} for better null check
	 */
	@Deprecated(forRemoval = true, since = "1.21.1")
	@Nullable
	public LocalPlayerPatch getPlayerPatch() {
		return EpicFightCapabilities.getEntityPatch(this.minecraft.player, LocalPlayerPatch.class);
	}
	
	public void initAuthHelper(AuthenticationHelper authHelper) {
		this.authenticationHelper = authHelper;
	}
	
	public AuthenticationHelper getAuthHelper() {
		return this.authenticationHelper;
	}
	
	public void playUISound(SPPlayUISound msg) {
		SoundInstance soundinstance = SimpleSoundInstance.forUI(msg.sound(), msg.pitch(), msg.volume());
		
		// Playing a sound twice corrects volume issue...
		Minecraft.getInstance().getSoundManager().play(soundinstance);
		Minecraft.getInstance().getSoundManager().play(soundinstance);
	}

    /// @deprecated Use [#isEpicFightMode] instead for a smoother migration when porting to MC 1.21.1
    @Deprecated(forRemoval = true, since = "1.21.1")
	public boolean isBattleMode() {
		return isEpicFightMode();
	}

    public boolean isEpicFightMode() {
        LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(this.minecraft.player, LocalPlayerPatch.class);

        if (localPlayerPatch == null) {
            return false;
        }

        return localPlayerPatch.isEpicFightMode();
    }
	
	/**
	 * Copy from {@link ForgeHooksClient#makeParticleRenderTypeComparator} but prioritize {@link ParticleRenderType#CUSTOM} lowest since it resets GL parameters setup
	 */
	public static Comparator<ParticleRenderType> makeCustomLowestParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
		Comparator<ParticleRenderType> vanillaComparator = Comparator.comparingInt(renderOrder::indexOf);
		
		return (typeOne, typeTwo) -> {
			boolean vanillaOne = renderOrder.contains(typeOne);
			boolean vanillaTwo = renderOrder.contains(typeTwo);
			
			if (vanillaOne && vanillaTwo) {
				return vanillaComparator.compare(typeOne, typeTwo);
			} else if (!vanillaOne && !vanillaTwo) {
				return Integer.compare(System.identityHashCode(typeOne), System.identityHashCode(typeTwo));
			}
			
			if (typeOne == ParticleRenderType.CUSTOM) {
				return 1;
			} else if (typeTwo == ParticleRenderType.CUSTOM) {
				return -1;
			}
			
			return vanillaOne ? -1 : 1;
		};
	}
}