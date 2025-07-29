package yesman.epicfight.client;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@OnlyIn(Dist.CLIENT)
public class ClientEngine {
	private static ClientEngine instance = new ClientEngine();
	
	public static ClientEngine getInstance() {
		return instance;
	}
	
	public Minecraft minecraft;
	public RenderEngine renderEngine;
	public ControllEngine controllEngine;
	private boolean vanillaModelDebuggingMode = false;
	
	public ClientEngine() {
		instance = this;
		this.minecraft = Minecraft.getInstance();
		this.renderEngine = new RenderEngine();
		this.controllEngine = new ControllEngine();
	}
	
	public boolean switchVanillaModelDebuggingMode() {
		this.vanillaModelDebuggingMode = !this.vanillaModelDebuggingMode;
		return this.vanillaModelDebuggingMode;
	}
	
	public boolean isVanillaModelDebuggingMode() {
		return this.vanillaModelDebuggingMode;
	}
	
	@Nullable
	public LocalPlayerPatch getPlayerPatch() {
		return EpicFightCapabilities.getEntityPatch(this.minecraft.player, LocalPlayerPatch.class);
	}
	
	public boolean isBattleMode() {
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