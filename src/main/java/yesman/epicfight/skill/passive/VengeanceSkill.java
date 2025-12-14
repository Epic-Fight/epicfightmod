package yesman.epicfight.skill.passive;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.network.server.SPPlayUISound;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class VengeanceSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("bcb1f110-c987-412e-9e40-5831c62eba2e");
	
	private float damageBonus;
	
	public VengeanceSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.damageBonus = parameters.getFloat("damage_bonus");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
			if (event.getDamageSource().getEntity() == null) {
				return;
			}
			
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId == -1 && event.getDamageSource().getEntity() instanceof LivingEntity livingentity) {
				setNewTarget(container, livingentity);
			} else if (currentTargetId == event.getDamageSource().getEntity().getId()) {
				container.getDataManager().setDataSync(SkillDataKeys.TICK_RECORD.get(), event.getPlayerPatch().getOriginal().tickCount);
			} else if (currentTargetId != event.getDamageSource().getEntity().getId() && event.getDamageSource().getEntity() instanceof LivingEntity livingentity) {
				if (canResetTarget(container)) {
					setNewTarget(container, livingentity);
				}
			}
		});
		
		listener.addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId == event.getTarget().getId()) {
				event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + this.damageBonus));
				container.getDataManager().setDataSync(SkillDataKeys.TICK_RECORD.get(), event.getPlayerPatch().getOriginal().tickCount);
			} else if (container.isActivated()) {
				float f = this.damageBonus * container.getDurationRatio(1.0F);
				event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + f));
			}
		});
		
		listener.addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
			if (container.isActivated()) {
				return;
			}
			
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId == event.getKilledEntity().getId()) {
				this.executeOnServer(container, null);
				container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), -1);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		cancelTarget(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
		listener.removeListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
		listener.removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		
		if (!container.getExecutor().isLogicalClient()) {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId > -1) {
				Entity entity = container.getExecutor().getOriginal().level().getEntity(currentTargetId);
				
				if (container.getExecutor().getOriginal().tickCount - container.getDataManager().getDataValue(SkillDataKeys.TICK_RECORD.get()) >= 160) {
					cancelTarget(container);
				} else if (entity == null || !entity.isAlive()) {
					cancelTarget(container);
				}
			}
		}
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		super.executeOnServer(container, args);
		
		container.getExecutor().getOriginal().playSound(EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F);
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.VENGEANCE_OVERLAY), container.getServerExecutor().getOriginal());
	}
	
	@Override
	public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
		super.cancelOnServer(container, args);
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.VENGEANCE_TARGET_CANCEL), container.getServerExecutor().getOriginal());
	}
	
	@Override
	public void onTracked(SkillContainer container, EpicFightNetworkManager.PayloadBundleBuilder payloadBuilder) {
		if (container.isActivated()) {
			payloadBuilder.and(new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.VENGEANCE_OVERLAY));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
		container.activate();
		// Playing sound twice fixes volume issue...
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
	}
	
	public static boolean tickExceeded(SkillContainer container) {
		return container.getExecutor().getOriginal().tickCount - container.getDataManager().getDataValue(SkillDataKeys.TICK_RECORD.get()) >= 160;
	}
	
	public static boolean canResetTarget(SkillContainer container) {
		return container.getExecutor().getOriginal().tickCount - container.getDataManager().getDataValue(SkillDataKeys.TICK_RECORD.get()) >= 80;
	}
	
	public static void setNewTarget(SkillContainer container, LivingEntity target) {
		cancelTarget(container);
		
		container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), target.getId());
		container.getDataManager().setDataSync(SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().tickCount);
		
		EpicFightNetworkManager.sendToPlayer(new SPPlayUISound(EpicFightSounds.VENGEANCE.get()), container.getServerExecutor().getOriginal());
		EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(target.getId(), EntityPairingPacketTypes.VENGEANCE_OVERLAY), container.getServerExecutor().getOriginal());
	}
	
	public static void cancelTarget(SkillContainer container) {
		int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
		Entity entity = container.getExecutor().getOriginal().level().getEntity(currentTargetId);
		container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), -1);
		
		if (entity != null && !container.getExecutor().isLogicalClient()) {
			EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(currentTargetId, EntityPairingPacketTypes.VENGEANCE_TARGET_CANCEL), container.getServerExecutor().getOriginal());
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		return container.isActivated() || (container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get()) > -1 && !tickExceeded(container));
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		
		if (container.isActivated()) {
			float f = Math.round(this.damageBonus * 100.0F * container.getDurationRatio(1.0F));
			guiGraphics.drawString(gui.getFont(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(f) + "%", x + 6, y + 8, 16777215, true);
		} else if (canResetTarget(container)) {
			int seconds = 4 - ((container.getExecutor().getOriginal().tickCount - container.getDataManager().getDataValue(SkillDataKeys.TICK_RECORD.get())) - 80) / 20;
			guiGraphics.drawString(gui.getFont(), String.valueOf(seconds), x + 6, y + 8, 16777215, true);
		}
		
		poseStack.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damageBonus * 100.0F));
		list.add(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damageBonus * 100.0F));
		list.add(String.valueOf(this.maxDuration / 20));
		
		return list;
	}
}
