package yesman.epicfight.skill.mover;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

public class PhantomAscentSkill extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac120002");
	private final List<AnimationAccessor<? extends StaticAnimation>> animations = new ArrayList<> (2);
	private int extraJumps;
	private double jumpPower;
	
	public PhantomAscentSkill(SkillBuilder<? extends Skill> builder) {
		super(builder);
		
		this.animations.add(Animations.BIPED_PHANTOM_ASCENT_FORWARD);
		this.animations.add(Animations.BIPED_PHANTOM_ASCENT_BACKWARD);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.extraJumps = parameters.getInt("extra_jumps");
		this.consumption = 0.2F;
		this.jumpPower = parameters.getDouble("jump_power");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
			if (event.getPlayerPatch().getOriginal().getVehicle() != null || !event.getPlayerPatch().isEpicFightMode() || event.getPlayerPatch().getOriginal().getAbilities().flying 
					|| event.getPlayerPatch().isHoldingAny() || event.getPlayerPatch().getEntityState().inaction()) {
				return;
			}
			
			// Check directly from the keybind because event.getMovementInput().isJumping doesn't allow to be set as true while player's jumping
			boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();
			boolean jumpPressedPrev = container.getDataManager().getDataValue(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get());
			
			if (jumpPressed && !jumpPressedPrev) {
				if (container.getStack() < 1) {
					return;
				}
				
				int jumpCounter = container.getDataManager().getDataValue(SkillDataKeys.JUMP_COUNT.get());
				
				if (jumpCounter > 0 || event.getPlayerPatch().currentLivingMotion == LivingMotions.FALL) {
					if (jumpCounter < (this.extraJumps + 1)) {
						SkillCastEvent skillexecuteevent = new SkillCastEvent(container.getExecutor(), container, null);
						container.getExecutor().getEventListener().triggerEvents(EventType.SKILL_CAST_EVENT, skillexecuteevent);
						
						if (skillexecuteevent.isCanceled()) {
							return;
						}
						
						container.setResource(0.0F);
						
						if (jumpCounter == 0 && event.getPlayerPatch().currentLivingMotion == LivingMotions.FALL) {
							container.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 2);
						} else {
							container.getDataManager().setDataF(SkillDataKeys.JUMP_COUNT.get(), (v) -> v + 1);
						}
						
						container.getDataManager().setDataSync(SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
						
						Input input = event.getMovementInput();
						float f = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(container.getExecutor().getOriginal()), 0.0F, 1.0F);
						input.tick(false, f);
						
				        int forward = event.getMovementInput().up ? 1 : 0;
				        int backward = event.getMovementInput().down ? -1 : 0;
				        int left = event.getMovementInput().left ? 1 : 0;
				        int right = event.getMovementInput().right ? -1 : 0;
						int vertic = forward + backward;
						int horizon = left + right;
						int degree = -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon);
						int scale = forward == 0 && backward == 0 && left == 0 && right == 0 ? 0 : (vertic < 0 ? -1 : 1);
						Vec3 forwardHorizontal = Vec3.directionFromRotation(new Vec2(0, container.getExecutor().getOriginal().getViewYRot(1.0F)));
						Vec3 jumpDir = OpenMatrix4f.transform(OpenMatrix4f.createRotatorDeg(-degree, Vec3f.Y_AXIS), forwardHorizontal.scale(0.15D * scale));
						Vec3 deltaMove = container.getExecutor().getOriginal().getDeltaMovement();
						container.getExecutor().getOriginal().setDeltaMovement(deltaMove.x + jumpDir.x, this.jumpPower + container.getExecutor().getOriginal().getJumpBoostPower(), deltaMove.z + jumpDir.z);
						event.getPlayerPatch().setModelYRot(container.getExecutor().getOriginal().getYRot() + degree, true);
						event.getPlayerPatch().playAnimationInClientSide(this.animations.get(vertic < 0 ? 1 : 0), 0.0F);
						ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
					};
				} else {
					container.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 1);
				}
			}
			
			container.getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), jumpPressed);
		});
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && container.getDataManager().getDataValue(SkillDataKeys.PROTECT_NEXT_FALL.get())) { // This is not synced
				float damage = event.getDamage();
				
				if (damage < 2.5F) {
					event.attachValueModifier(ValueModifier.setter(0.0F));
				}
				
				container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
			}
		});
		
		listener.addEventListener(EventType.FALL_EVENT, EVENT_UUID, (event) -> {
			container.getDataManager().setData(SkillDataKeys.JUMP_COUNT.get(), 0);
			
			if (event.getPlayerPatch().isLogicalClient()) {
				container.getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
		listener.removeListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
		listener.removeListener(EventType.FALL_EVENT, EVENT_UUID);
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(this.extraJumps);
		
		return list;
	}
}