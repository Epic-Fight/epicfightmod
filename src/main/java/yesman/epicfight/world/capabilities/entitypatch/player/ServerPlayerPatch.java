package yesman.epicfight.world.capabilities.entitypatch.player;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.neoevent.InnateSkillChangeEvent;
import yesman.epicfight.api.neoevent.playerpatch.DodgeSuccessEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.SetTargetEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.network.server.SPInitSkills;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ServerPlayerPatch extends PlayerPatch<ServerPlayer> {
	private LivingEntity attackTarget;
	private boolean updatedMotionCurrentTick;
	
	public ServerPlayerPatch(ServerPlayer entity) {
		super(entity);
	}
	
	@Override
	public void onJoinWorld(ServerPlayer player, EntityJoinLevelEvent event) {
		super.onJoinWorld(player, event);
		EpicFightNetworkManager.sendToPlayer(new SPInitSkills(this.getPlayerSkills()), player);
	}
	
	@Override
	public void onStartTracking(ServerPlayer trackingPlayer) {
		PayloadBundleBuilder payloadBundleBuilder = PayloadBundleBuilder.create();
		SPChangeLivingMotion msg = new SPChangeLivingMotion(this.getOriginal().getId());
		msg.putEntries(this.getAnimator().getLivingAnimations().entrySet());
		
		payloadBundleBuilder.and(msg);
		
		this.getPlayerSkills().listSkillContainers().filter(skillContainer -> !skillContainer.isEmpty() && skillContainer.getSkill().getCategory().shouldSynchronize()).forEach(skillContainer -> {
			payloadBundleBuilder.and(skillContainer.createSyncPacketToRemotePlayer());
			skillContainer.getDataManager().onTracked(payloadBundleBuilder);
			skillContainer.getSkill().onTracked(skillContainer, payloadBundleBuilder);
		});
		
		payloadBundleBuilder.and(SPModifyPlayerData.setPlayerMode(this.getOriginal().getId(), this.playerMode));
		payloadBundleBuilder.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, trackingPlayer, others));
	}
	
	@Override
	public void preTick(EntityTickEvent.Pre event) {
		super.preTick(event);
		this.updatedMotionCurrentTick = false;
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
	}
	
	@Override
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
		if (this.isHoldingAny()) {
			this.getSkillContainerFor(this.holdingSkill.asSkill()).ifPresent((container) -> {
				container.getSkill().cancelOnServer(container, null);
				EpicFightNetworkManager.sendToPlayer(SPSkillFeedback.expired(container.getSlot()), this.original);
			});
			
			this.resetHolding();
		}
		
		CapabilityItem mainHandCap = (hand == InteractionHand.MAIN_HAND) ? toCap : this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		mainHandCap.changeWeaponInnateSkill(this, (hand == InteractionHand.MAIN_HAND) ? to : this.original.getMainHandItem());
		NeoForge.EVENT_BUS.post(new InnateSkillChangeEvent(this, from, fromCap, to, toCap, hand));
		
		if (hand == InteractionHand.OFF_HAND) {
			if (!from.isEmpty()) {
				from.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
					if (Attributes.ATTACK_SPEED.equals(attribute)) {
						this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED).removeModifier(modifier);
					}
				});
			}
			
			if (!fromCap.isEmpty()) {
				Multimap<Holder<Attribute>, AttributeModifier> modifiers = fromCap.getAllAttributeModifiers();
				modifiers.get(EpicFightAttributes.ARMOR_NEGATION).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION)::removeModifier);
				modifiers.get(EpicFightAttributes.IMPACT).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT)::removeModifier);
				modifiers.get(EpicFightAttributes.MAX_STRIKES).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES)::removeModifier);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED)::removeModifier);
			}
			
			if (!to.isEmpty()) {
				to.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
					if (Attributes.ATTACK_SPEED.equals(attribute)) {
						this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED).addTransientModifier(modifier);
					}
				});
			}
			
			if (!toCap.isEmpty()) {
				Multimap<Holder<Attribute>, AttributeModifier> modifiers = toCap.getAttributeModifiers(this);
				modifiers.get(EpicFightAttributes.ARMOR_NEGATION).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION)::addTransientModifier);
				modifiers.get(EpicFightAttributes.IMPACT).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT)::addTransientModifier);
				modifiers.get(EpicFightAttributes.MAX_STRIKES).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES)::addTransientModifier);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED)::addTransientModifier);
			}
		}
		
		this.modifyLivingMotionByCurrentItem(true);
		
		super.updateHeldItem(fromCap, toCap, from, to, hand);
	}
	
	public void modifyLivingMotionByCurrentItem() {
		this.modifyLivingMotionByCurrentItem(false);
	}
	
	/**
	 * @param checkOldAnimations: when true, it compares the animations and send the packet if it has any changes
	 */
	public void modifyLivingMotionByCurrentItem(boolean checkOldAnimations) {
		if (this.updatedMotionCurrentTick && checkOldAnimations) {
			return;
		}
		
		Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> oldLivingAnimations = this.getAnimator().getLivingAnimations();
		Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> newLivingAnimations = Maps.newHashMap();
		
		CapabilityItem mainhandCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
		
		Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> livingMotionModifiers = new HashMap<>(mainhandCap.getLivingMotionModifier(this, InteractionHand.MAIN_HAND));
		livingMotionModifiers.putAll(offhandCap.getLivingMotionModifier(this, InteractionHand.OFF_HAND));
		
		for (Map.Entry<LivingMotion, AssetAccessor<? extends StaticAnimation>> entry : livingMotionModifiers.entrySet()) {
			AssetAccessor<? extends StaticAnimation> aniamtion = entry.getValue();
			
			if (!oldLivingAnimations.containsKey(entry.getKey())) {
				this.updatedMotionCurrentTick = true;
			} else if (oldLivingAnimations.get(entry.getKey()) != aniamtion) {
				this.updatedMotionCurrentTick = true;
			}
			
			newLivingAnimations.put(entry.getKey(), aniamtion);
		}
		
		for (LivingMotion oldLivingMotion : oldLivingAnimations.keySet()) {
			if (!newLivingAnimations.containsKey(oldLivingMotion)) {
				this.updatedMotionCurrentTick = true;
				break;
			}
		}
		
		if (this.updatedMotionCurrentTick || !checkOldAnimations) {
			this.getAnimator().resetLivingAnimations();
			newLivingAnimations.forEach(this.getAnimator()::addLivingAnimation);
			
			SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
			msg.putEntries(newLivingAnimations.entrySet());
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(msg, this.original);
		}
	}
	
	@Override
	public void sendToAllPlayersTrackingMe(CustomPacketPayload packet, CustomPacketPayload... otherPackets) {
		if (!this.isLogicalClient()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(packet, this.original, otherPackets);
		}
	}
	
	@Override
	public void setModelYRot(float amount, boolean sendPacket) {
		super.setModelYRot(amount, sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerYRot(this.original.getId(), this.modelYRot), this.original);
		}
	}
	
	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.disablePlayerYRot(this.original.getId()), this.original);
		}
	}
	
	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		if (this.getOriginal().getAbilities().invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return AttackResult.missed(amount); 
		}
		
		TakeDamageEvent.Income takeDamageEvent$pre = new TakeDamageEvent.Income(this, damageSource, amount);
		
		if (PlayerPatchEvent.postAndFireSkillListeners(takeDamageEvent$pre).isCanceled()) {
			return new AttackResult(takeDamageEvent$pre.getResult(), takeDamageEvent$pre.getDamage());
		} else {
			return super.tryHurt(damageSource, amount);
		}
	}
	
	@Override
	public void onDodgeSuccess(DamageSource damageSource, Vec3 location) {
		super.onDodgeSuccess(damageSource, location);

		DodgeSuccessEvent dodgeSuccessEvent = new DodgeSuccessEvent(this, damageSource, location);
		PlayerPatchEvent.postAndFireSkillListeners(dodgeSuccessEvent);
	}

	@Override
	public void toVanillaMode(boolean synchronize) {
		super.toVanillaMode(synchronize);
		
		if (synchronize) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerMode(this.original.getId(), PlayerMode.VANILLA), this.original);
		}
	}
	
	@Override
	public void toEpicFightMode(boolean synchronize) {
		super.toEpicFightMode(synchronize);
		
		if (synchronize) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerMode(this.original.getId(), PlayerMode.EPICFIGHT), this.original);
		}
	}
	
	@Override
	public boolean isTargetInvulnerable(Entity target) {
		if (target instanceof Player && !this.getOriginal().server.isPvpAllowed()) {
			return true;
		}
		
		return super.isTargetInvulnerable(target);
	}
	
	@Override
	public void setLastAttackSuccess(boolean setter) {
		if (setter) {
			EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setLastAttackResult(this.original.getId(), true), this.original);
		}
		
		this.isLastAttackSuccess = setter;
	}
	
	public void setAttackTarget(LivingEntity entity) {
		SetTargetEvent event = new SetTargetEvent(this, entity);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
		
		this.attackTarget = event.getTarget();
	}
	
	@Override
	public boolean startSkillHolding(HoldableSkill chargingSkill) {
		if (super.startSkillHolding(chargingSkill)) {
			EpicFightNetworkManager.sendToPlayer(SPSkillFeedback.held(this.getSkillContainerFor(chargingSkill.asSkill()).get().getSlot()), this.getOriginal());
			return true;
		}
		
		return false;
	}
	
	@Override
	public LivingEntity getTarget() {
		return this.attackTarget;
	}
	
	@Override
	public void setGrapplingTarget(LivingEntity grapplingTarget) {
		super.setGrapplingTarget(grapplingTarget);
		EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setGrapplingTarget(this.original.getId(), grapplingTarget), this.original);
	}
}