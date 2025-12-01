package yesman.epicfight.skill.identity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import com.google.common.collect.Maps;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class MeteorSlamSkill extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("03181ad0-e750-11ed-a05b-0242ac120003");
	
	public static class Builder extends SkillBuilder<MeteorSlamSkill> {
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> slamMotions = Maps.newHashMap();
		
		public Builder addSlamMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
			this.slamMotions.put(weaponCategory, function);
			return this;
		}
	}
	
	public static float getFallDistance(SkillContainer skillContainer) {
		return skillContainer.getDataManager().getDataValue(SkillDataKeys.FALL_DISTANCE.get());
	}
	
	public static MeteorSlamSkill.Builder createMeteorSlamBuilder() {
		return (new MeteorSlamSkill.Builder())
				    .addSlamMotion(WeaponCategories.SPEAR, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.TACHI, (item, player) -> Animations.METEOR_SLAM)
				    .addSlamMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.METEOR_SLAM)
				    .setCategory(SkillCategories.IDENTITY)
				    .setResource(Resource.NONE);
	}
	
	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> slamMotions;
	private final double minDistance = 6.0D;
	
	public MeteorSlamSkill(Builder builder) {
		super(builder);
		
		this.slamMotions = builder.slamMotions;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.SKILL_CAST_EVENT, EVENT_UUID, (event) -> {
			if (!container.getExecutor().isLogicalClient()) {
				Skill skill = event.getSkillContainer().getSkill();
				
				if (skill.getCategory() != SkillCategories.BASIC_ATTACK) {
					return;
				}
				
				if (container.getExecutor().getOriginal().onGround() || container.getExecutor().getOriginal().getXRot() < 40.0F) {
					return;
				}
				
				CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
				
				if (!this.slamMotions.containsKey(holdingItem.getWeaponCategory())) {
					return;
				}
				
				AnimationAccessor<? extends StaticAnimation> slamAnimation = this.slamMotions.get(holdingItem.getWeaponCategory()).apply(holdingItem, container.getExecutor());
				
				if (slamAnimation == null) {
					return;
				}
				
				Vec3 vec3 = container.getExecutor().getOriginal().getEyePosition(1.0F);
				Vec3 vec31 = container.getExecutor().getOriginal().getViewVector(1.0F);
				Vec3 vec32 = vec3.add(vec31.x * 50.0D, vec31.y * 50.0D, vec31.z * 50.0D);
				HitResult hitResult = container.getExecutor().getOriginal().level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, container.getExecutor().getOriginal()));
				
				if (hitResult.getType() != HitResult.Type.MISS) {
					Vec3 to = hitResult.getLocation();
					Vec3 from = container.getExecutor().getOriginal().position();
					double distance = to.distanceTo(from);
					
					if (distance > this.minDistance) {
						container.getExecutor().playAnimationSynchronized(slamAnimation, 0.0F);
						container.getDataManager().setDataSync(SkillDataKeys.FALL_DISTANCE.get(), (float)distance);
						container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
						event.setCanceled(true);
					}
				}
			}
		});
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && container.getDataManager().getDataValue(SkillDataKeys.PROTECT_NEXT_FALL.get())) {
				float stamina = container.getExecutor().getStamina();
				float damage = event.getDamage();
				event.attachValueModifier(ValueModifier.adder(-stamina));
				container.getExecutor().setStamina(stamina - damage);
				container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
			}
		});
		
		listener.addEventListener(EventType.FALL_EVENT, EVENT_UUID, (event) -> {
			if (LevelUtil.calculateLivingEntityFallDamage(event.getForgeEvent().getEntity(), event.getForgeEvent().getDamageMultiplier(), event.getForgeEvent().getDistance()) == 0) {
				container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		container.getExecutor().getEventListener().removeListener(EventType.FALL_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SKILL_CAST_EVENT, EVENT_UUID);
	}
	
	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.slamMotions.keySet();
	}
	
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("skill.epicfight.meteor_slam.consume.tooltip"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}