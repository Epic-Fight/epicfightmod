package yesman.epicfight.skill.identity;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MeteorSlamSkill extends Skill {
	public static class Builder extends SkillBuilder<MeteorSlamSkill.Builder> {
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> slamMotions = Maps.newHashMap();

		public Builder(Function<MeteorSlamSkill.Builder, MeteorSlamSkill> constructor) {
			super(constructor);
		}

		public MeteorSlamSkill.Builder addSlamMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
			this.slamMotions.put(weaponCategory, function);
			return this;
		}
	}

	public static float getFallDistance(SkillContainer skillContainer) {
		return skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.FALL_DISTANCE);
	}

	public static MeteorSlamSkill.Builder createMeteorSlamBuilder() {
		return new MeteorSlamSkill.Builder(MeteorSlamSkill::new)
				    .addSlamMotion(WeaponCategories.SPEAR, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.TACHI, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.METEOR_SLAM)
				    .setCategory(SkillCategories.IDENTITY)
				    .setResource(Resource.NONE);
	}

	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> slamMotions;
	private final double minDistance = 6.0D;

	public MeteorSlamSkill(MeteorSlamSkill.Builder builder) {
		super(builder);
		this.slamMotions = builder.slamMotions;
	}

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Player.CAST_SKILL,
            event -> {
                if (!skillContainer.getExecutor().isLogicalClient()) {
                    Skill skill = event.getSkillContainer().getSkill();

                    if (
                        skill.getCategory() != SkillCategories.BASIC_ATTACK ||          // do not cast the skill if it's not a basic attack
                        skillContainer.getExecutor().getOriginal().onGround() ||        // do not cast the skill in ground
                        skillContainer.getExecutor().getOriginal().getXRot() < 40.0F    // do not cast the skill if the player isn't looking at downside
                    ) {
                        return;
                    }

                    CapabilityItem holdingItem = skillContainer.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);

                    if (!this.slamMotions.containsKey(holdingItem.getWeaponCategory())) {
                        return;
                    }

                    AnimationAccessor<? extends StaticAnimation> slamAnimation = this.slamMotions.get(holdingItem.getWeaponCategory()).apply(holdingItem, skillContainer.getExecutor());

                    if (slamAnimation == null) {
                        return;
                    }

                    Vec3 vec3 = skillContainer.getExecutor().getOriginal().getEyePosition(1.0F);
                    Vec3 vec31 = skillContainer.getExecutor().getOriginal().getViewVector(1.0F);
                    Vec3 vec32 = vec3.add(vec31.x * 50.0D, vec31.y * 50.0D, vec31.z * 50.0D);
                    HitResult hitResult = skillContainer.getExecutor().getOriginal().level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, skillContainer.getExecutor().getOriginal()));

                    if (hitResult.getType() != HitResult.Type.MISS) {
                        Vec3 to = hitResult.getLocation();
                        Vec3 from = skillContainer.getExecutor().getOriginal().position();
                        double distance = to.distanceTo(from);

                        if (distance > this.minDistance) {
                            skillContainer.getExecutor().playAnimationSynchronized(slamAnimation, 0.0F);
                            skillContainer.getDataManager().setDataSync(EpicFightSkillDataKeys.FALL_DISTANCE, (float)distance);
                            skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, true);
                            event.cancel();
                        }
                    }
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE,
            event -> {
                if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.PROTECT_NEXT_FALL)) {
                    float stamina = skillContainer.getExecutor().getStamina();
                    float damage = event.getDamage();
                    event.attachValueModifier(ValueModifier.adder(-stamina));
                    skillContainer.getExecutor().setStamina(stamina - damage);
                    skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.ON_FALL,
            event -> {
                if (LevelUtil.calculateLivingEntityFallDamage(event.getEntityPatch().getOriginal(), event.getDamageMultiplier(), event.getDistance()) == 0) {
                    skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
                }
            },
            this
        );
    }

	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.slamMotions.keySet();
	}

	@ClientOnly @Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("skill.epicfight.meteor_slam.consume.tooltip"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}