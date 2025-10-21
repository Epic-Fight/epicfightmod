package yesman.epicfight.main;

import java.util.function.Function;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EpicFightSharedConstants {
	// Model variables
	public static final int MAX_WEIGHTS = 3;
	public static final int MAX_JOINTS = 1000;
	
	// Ingame variables
	public static final float A_TICK = 0.05F;
	public static final float GENERAL_ANIMATION_TRANSITION_TIME = 0.15F;
	public static final float EXECUTION_DAMAGE = 2147483647F;
	
	// Environment varables
	public static final boolean IS_DEV_ENV = !FMLEnvironment.production;
	public static final String SERVER_URL = "https://epic-fight.com";
	
	// Sided variables
	private static final Function<LivingEntityPatch<?>, Animator> ANIMATOR_PROVIDER;
	
	static {
		ANIMATOR_PROVIDER = isPhysicalClient() ? ClientAnimator::getAnimator : ServerAnimator::getAnimator;
	}
	
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return ANIMATOR_PROVIDER.apply(entitypatch);
	}
	
	public static boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}
}