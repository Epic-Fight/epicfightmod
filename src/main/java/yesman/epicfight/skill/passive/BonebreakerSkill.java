package yesman.epicfight.skill.passive;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class BonebreakerSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("06212e76-6dbe-4d4b-a875-562829bb6db4");
	
	private float damageBonus;
	private int maxDamageBonusStacks;
	
	public BonebreakerSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		
		this.damageBonus = parameters.getFloat("damage_bonus");
		this.maxDamageBonusStacks = parameters.getInt("max_damage_bonus_stacks");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId == -1) {
				container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), event.getTarget().getId());
				container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 1);
				EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(event.getTarget().getId(), EntityPairingPacketTypes.BONEBREAKER_BEGIN), event.getPlayerPatch().getOriginal());
			} else if (currentTargetId == event.getTarget().getId()) {
				int stacks = container.getDataManager().getDataValue(SkillDataKeys.STACKS.get());
				event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + this.damageBonus * stacks));
				
				if (stacks + 1 == this.maxDamageBonusStacks) {
					event.getTarget().playSound(EpicFightSounds.OLD_FALL.get(), 50.0F, 1.0F);
					EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(event.getTarget().getId(), EntityPairingPacketTypes.BONEBREAKER_MAX_STACK), event.getPlayerPatch().getOriginal());
				}
				
				container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), Math.min(stacks + 1, this.maxDamageBonusStacks));
			}
		});
		
		listener.addEventListener(EventType.ATTACK_PHASE_END_EVENT, EVENT_UUID, (event) -> {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId != -1) {
				Entity entity = container.getExecutor().getOriginal().level().getEntity(currentTargetId);
				
				if (!event.getPlayerPatch().getCurrentlyActuallyHitEntities().contains(entity) && event.getPlayerPatch().getCurrentlyActuallyHitEntities().size() > 0) {
					Entity newTarget = event.getPlayerPatch().getCurrentlyActuallyHitEntities().get(0);
					
					if (entity != null) {
						EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(entity.getId(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), event.getPlayerPatch().getOriginal());
					}
					
					container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), newTarget.getId());
					container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 1);
					EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(newTarget.getId(), EntityPairingPacketTypes.BONEBREAKER_BEGIN), event.getPlayerPatch().getOriginal());
				}
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		listener.removeListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
		listener.removeListener(EventType.ATTACK_PHASE_END_EVENT, EVENT_UUID);
		
		if (!container.getExecutor().isLogicalClient()) {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId != -1) {
				Entity entity = container.getExecutor().getOriginal().level().getEntity(currentTargetId);
				
				if (entity != null) {
					EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(entity.getId(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), container.getServerExecutor().getOriginal());
				}
			}
		}
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		
		if (!container.getExecutor().isLogicalClient()) {
			int currentTargetId = container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get());
			
			if (currentTargetId > -1) {
				Entity entity = container.getExecutor().getOriginal().level().getEntity(currentTargetId);
				
				if (entity == null || !entity.isAlive()) {
					container.getDataManager().setDataSync(SkillDataKeys.ENTITY_ID.get(), -1);
					container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 0);
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		Entity target = container.getExecutor().getOriginal().level().getEntity(container.getDataManager().getDataValue(SkillDataKeys.ENTITY_ID.get()));
		return target != null && target.isAlive();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		guiGraphics.drawString(gui.getFont(), String.valueOf(container.getDataManager().getDataValue(SkillDataKeys.STACKS.get())), x + 10, y + 10, 16777215, true);
		poseStack.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damageBonus * 100.0F));
		list.add(this.maxDamageBonusStacks);
		
		return list;
	}
}
