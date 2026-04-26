package yesman.epicfight.api.collider;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.EpicFight;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public final class WeaponReachHitboxUtil {
	public static float getInteractionRangeBonusOverEntityBase(LivingEntityPatch<?> entitypatch) {
		final boolean dbg = EpicFight.LOGGER.isDebugEnabled();
		Holder<Attribute> reachHolder = Attributes.ENTITY_INTERACTION_RANGE;
		var entity = entitypatch.getOriginal();
		if (!entity.getAttributes().hasAttribute(reachHolder)) {
			return 0.0F;
		}
		AttributeInstance inst = entity.getAttribute(reachHolder);
		if (inst == null) {
			if (dbg) {
				EpicFight.LOGGER.debug("[ReachHitbox] ENTITY_INTERACTION_RANGE instance null -> bonus 0");
			}
			return 0.0F;
		}
		double base = inst.getBaseValue();
		double current = inst.getValue();
		float bonus = (float) (current - base);
		float clamped = Mth.clamp(bonus, 0.0F, (float) CommonConfig.reachHitboxMaxBonus);
		if (dbg) {
			EpicFight.LOGGER.debug(
				"[ReachHitbox] entity={} reach base={} current={} bonusRaw={} bonusClamped={}",
				entity,
				base,
				current,
				bonus,
				clamped
			);
		}
		return clamped;
	}

	public static Collider copyColliderWithWeaponReach(LivingEntityPatch<?> entitypatch, InteractionHand hand, Collider source) {
		Collider copy = source.deepCopy();
		if (copy == null) {
			return source;
		}
		float bonus = getInteractionRangeBonusOverEntityBase(entitypatch);
		if (bonus <= 0.0F) {
			return copy;
		}
		double deltaLength = bonus * CommonConfig.reachHitboxBonusToColliderScale;
		if (copy instanceof OBBCollider obb) {
			return obb.withSymmetricLocalZExtension(deltaLength);
		}
		if (copy instanceof MultiOBBCollider multi) {
			return multi.withSymmetricLocalZExtension(deltaLength);
		}
		return copy;
	}
}
