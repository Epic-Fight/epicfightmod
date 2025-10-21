package yesman.epicfight.skill.passive;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.neoevent.playerpatch.AttackPhaseEndEvent;
import yesman.epicfight.api.neoevent.playerpatch.DealDamageEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;

public class BonebreakerSkill extends PassiveSkill {
	private float damageBonus;
	private int maxDamageBonusStacks;
	
	public BonebreakerSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.damageBonus = parameters.getFloat("damage_bonus");
		this.maxDamageBonusStacks = parameters.getInt("max_damage_bonus_stacks");
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void dealDamagePre(DealDamageEvent.Pre event, SkillContainer container) {
		int currentTargetId = container.getDataManager().getDataValue(EpicFightSkillDataKeys.ENTITY_ID);
		
		if (currentTargetId == -1) {
			container.getDataManager().setDataSync(EpicFightSkillDataKeys.ENTITY_ID, event.getTarget().getId());
			container.getDataManager().setDataSync(EpicFightSkillDataKeys.STACKS, 1);
			EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(event.getTarget().getId(), EntityPairingPacketTypes.BONEBREAKER_BEGIN), container.getServerExecutor().getOriginal());
		} else if (currentTargetId == event.getTarget().getId()) {
			int stacks = container.getDataManager().getDataValue(EpicFightSkillDataKeys.STACKS);
			event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + this.damageBonus * stacks));
			
			if (stacks + 1 == this.maxDamageBonusStacks) {
				event.getTarget().playSound(EpicFightSounds.OLD_FALL.get(), 50.0F, 1.0F);
				EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(event.getTarget().getId(), EntityPairingPacketTypes.BONEBREAKER_MAX_STACK), container.getServerExecutor().getOriginal());
			}
			
			container.getDataManager().setDataSync(EpicFightSkillDataKeys.STACKS, Math.min(stacks + 1, this.maxDamageBonusStacks));
		}
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void modifyBaseDamageEvent(AttackPhaseEndEvent event, SkillContainer container) {
		container.runOnServer(serverExecutor -> {
			int currentTargetId = container.getDataManager().getDataValue(EpicFightSkillDataKeys.ENTITY_ID);
			
			if (currentTargetId != -1) {
				Entity entity = serverExecutor.getLevel().getEntity(currentTargetId);
				
				if (!serverExecutor.getCurrentlyActuallyHitEntities().contains(entity) && serverExecutor.getCurrentlyActuallyHitEntities().size() > 0) {
					Entity newTarget = serverExecutor.getCurrentlyActuallyHitEntities().get(0);
					
					if (entity != null) {
						EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(entity.getId(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), serverExecutor.getOriginal());
					}
					
					container.getDataManager().setDataSync(EpicFightSkillDataKeys.ENTITY_ID, newTarget.getId());
					container.getDataManager().setDataSync(EpicFightSkillDataKeys.STACKS, 1);
					EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(newTarget.getId(), EntityPairingPacketTypes.BONEBREAKER_BEGIN), serverExecutor.getOriginal());
				}
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.runOnServer(serverExecutor -> {
			int currentTargetId = container.getDataManager().getDataValue(EpicFightSkillDataKeys.ENTITY_ID);
			
			if (currentTargetId != -1) {
				Entity entity = container.getExecutor().getLevel().getEntity(currentTargetId);
				
				if (entity != null) {
					EpicFightNetworkManager.sendToPlayer(new SPEntityPairingPacket(entity.getId(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), serverExecutor.getOriginal());
				}
			}
		});
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		
		container.runOnServer(serverExecutor -> {
			int currentTargetId = container.getDataManager().getDataValue(EpicFightSkillDataKeys.ENTITY_ID);
			
			if (currentTargetId > -1) {
				Entity entity = container.getExecutor().getLevel().getEntity(currentTargetId);
				
				if (entity == null || !entity.isAlive()) {
					container.getDataManager().setDataSync(EpicFightSkillDataKeys.ENTITY_ID, -1);
					container.getDataManager().setDataSync(EpicFightSkillDataKeys.STACKS, 0);
				}
			}
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		Entity target = container.getExecutor().getLevel().getEntity(container.getDataManager().getDataValue(EpicFightSkillDataKeys.ENTITY_ID));
		return target != null && target.isAlive();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		guiGraphics.drawString(gui.getFont(), String.valueOf(container.getDataManager().getDataValue(EpicFightSkillDataKeys.STACKS)), x + 10, y + 10, 16777215, true);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.damageBonus * 100.0F));
		list.add(this.maxDamageBonusStacks);
		
		return list;
	}
}
