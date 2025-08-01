package yesman.epicfight.main;

import java.util.function.Function;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EpicFightSharedConstants {
	// Model variables
	public static final int MAX_WEIGHTS = 3;
	public static final int MAX_JOINTS = 1000;
	
	// Animation variables
	public static final float A_TICK = 0.05F;
	public static final float GENERAL_ANIMATION_TRANSITION_TIME = 0.15F;
	
	// Environment varables
	public static final boolean IS_DEV_ENV = !FMLEnvironment.production;
	public static final String SERVER_URL = "https://epic-fight.com";
	
	// Sided variables
	private static final Function<LivingEntityPatch<?>, Animator> ANIMATOR_PROVIDER;
	
	static {
		ANIMATOR_PROVIDER = isPhysicalClient() ? ClientAnimator::getAnimator : ServerAnimator::getAnimator;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static AuthenticationHelper AUTH_HELPER = new AuthenticationHelper() {
		@Override
		public boolean valid() {
			return false;
		}
	};
	
	@OnlyIn(Dist.CLIENT)
	public static void initAuthHelper(AuthenticationHelper authHelper) {
		AUTH_HELPER = authHelper;
	}
	
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return ANIMATOR_PROVIDER.apply(entitypatch);
	}
	
	public static boolean isPhysicalClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}
}