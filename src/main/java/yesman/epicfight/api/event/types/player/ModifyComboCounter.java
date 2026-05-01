package yesman.epicfight.api.event.types.player;

import com.google.common.collect.Sets;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ModifyComboCounter extends LivingEntityPatchEvent implements CancelableEvent {
	private final Causal causal;
	private final AnimationAccessor<? extends StaticAnimation> animation;
	private final int prevValue;
	private int nextValue;
	
	public ModifyComboCounter(Causal causal, ServerPlayerPatch playerPatch, AnimationAccessor<? extends StaticAnimation> animation, int prevValue, int nextValue) {
        super(playerPatch);

		this.causal = causal;
		this.animation = animation;
		this.prevValue = prevValue;
		this.nextValue = nextValue;
	}
	
	public Causal getCausal() {
		return this.causal;
	}

    public ServerPlayerPatch getPlayerPatch() {
        return (ServerPlayerPatch)this.getEntityPatch();
    }

	public AnimationAccessor<? extends StaticAnimation> getAnimation() {
		return this.animation;
	}

	public int getPrevValue() {
		return this.prevValue;
	}

	public int getNextValue() {
		return this.nextValue;
	}

	public void setNextValue(int nextValue) {
		this.nextValue = nextValue;
	}

	public enum Causal {
		ANOTHER_ACTION_ANIMATION, TIME_EXPIRED
	}

    @FunctionalInterface
    public interface ComboCounterHandler {
        ComboCounterHandler DEFAULT_COMBO_HANDLER = (CapabilityItem itemCapability, ModifyComboCounter.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int comboCounter) -> {
            // When causal is time expiration, reset the counter
            if (causal == ModifyComboCounter.Causal.TIME_EXPIRED) {
                return 0;
            }

            List<AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(entitypatch);

            if (comboAnimations == null) {
                return 0;
            }

            Set<AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Sets.newHashSet(comboAnimations);

            // when the next animation is not included in weapon attack motion, reset the counter
            if (!attackMotionSet.contains(nextAnimation) && causal != ModifyComboCounter.Causal.TIME_EXPIRED && itemCapability.shouldCancelCombo(entitypatch)) {
                return 0;
            }

            int comboSize = comboAnimations.size();

            // When the next animation is dash or air attacks, reset combo counter
            if (nextAnimation.equals(comboAnimations.get(comboSize - 1)) || nextAnimation.equals(comboAnimations.get(comboSize - 2))) {
                return 0;
            }

            return comboCounter;
        };

        Function<Class<? extends StaticAnimation>, ComboCounterHandler> NO_RESET_WITH_ANIM_TYPE = Util.memoize(animType -> {
            return (CapabilityItem itemCapability, ModifyComboCounter.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int comboCounter) -> {
                // When causal is time expiration, reset the counter
                if (causal == ModifyComboCounter.Causal.TIME_EXPIRED) {
                    return 0;
                }

                List<AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(entitypatch);

                if (comboAnimations == null) {
                    return 0;
                }

                Set<AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Sets.newHashSet(comboAnimations);

                // when the next animation is not included in weapon attack motion, reset the counter
                if (!nextAnimation.checkType(animType) && !attackMotionSet.contains(nextAnimation)) {
                    return 0;
                }

                int comboSize = comboAnimations.size();

                // When the next animation is dash or air attacks, reset combo counter
                if (nextAnimation.equals(comboAnimations.get(comboSize - 1)) || nextAnimation.equals(comboAnimations.get(comboSize - 2))) {
                    return 0;
                }

                return comboCounter;
            };
        });

        /// Control the combo counter with given parameters
        /// @param nextAnimation null when ModifyComboCounter.Causal is {@link ModifyComboCounter.Causal#TIME_EXPIRED}
        int handleComboCounter(CapabilityItem itemCapability, ModifyComboCounter.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int comboCounter);
    }
}