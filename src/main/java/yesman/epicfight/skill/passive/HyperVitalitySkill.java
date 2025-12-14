package yesman.epicfight.skill.passive;

import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class HyperVitalitySkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("06fb3f66-b900-11ed-afa1-0242ac120002");
	
	public HyperVitalitySkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.SKILL_CONSUME_EVENT, EVENT_UUID, (event) -> {
			if (!this.isDisabled(container) && event.getSkill().getCategory() == SkillCategories.WEAPON_INNATE) {
				PlayerPatch<?> playerpatch = event.getPlayerPatch();
				Player player = playerpatch.getOriginal();
				
				if (playerpatch.getSkill(SkillSlots.WEAPON_INNATE).getStack() < 1) {
					if (container.getStack() > 0 && !player.isCreative()) {
						float consumption = event.getSkill().getConsumption();
						
						if (playerpatch.consumeForSkill(this, Skill.Resource.STAMINA, consumption * 0.1F)) {
							event.setResourceType(Skill.Resource.NONE);
							container.setMaxResource(consumption * 0.2F);
							
							if (!container.getExecutor().isLogicalClient()) {
								player.level().playSound(null, player.getX(), player.getY(), player.getZ(), EpicFightSounds.HYPERVITALITY.get(), player.getSoundSource(), 1.0F, 1.0F);
								container.setMaxDuration(event.getSkill().getMaxDuration());
								container.activate();
								EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), (ServerPlayer)player);
								
								SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(player.getId(), EntityPairingPacketTypes.FLASH_WHITE);
								
								// durationTick, maxOverlay, maxBrightness
								pairingPacket.getBuffer().writeInt(4);
								pairingPacket.getBuffer().writeInt(15);
								pairingPacket.getBuffer().writeInt(8);
								pairingPacket.getBuffer().writeBoolean(false);
								
								EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, (ServerPlayer)player);
							}
						}
					}
				}
			}
		}, 1);
		
		container.getExecutor().getEventListener().addEventListener(EventType.SKILL_CANCEL_EVENT, EVENT_UUID, (event) -> {
			if (!container.getExecutor().isLogicalClient() && !container.getExecutor().getOriginal().isCreative() && event.getSkillContainer().getSkill().getCategory() == SkillCategories.WEAPON_INNATE && this.isActivated(container)) {
				container.setResource(0.0F);
				container.deactivate();
				ServerPlayerPatch serverPlayerPatch = (ServerPlayerPatch)container.getExecutor();
				this.setStackSynchronize(container, container.getStack() - 1);
				EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), serverPlayerPatch.getOriginal());
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.SKILL_CONSUME_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SKILL_CANCEL_EVENT, EVENT_UUID);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
		super.executeOnClient(container, args);
		container.activate();
	}
	
	@Override
	public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
		super.cancelOnClient(container, args);
		container.deactivate();
	}
	
	@Override
	public boolean shouldDraw(SkillContainer container) {
		return this.isActivated(container) || container.getStack() == 0;
	}
	
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.translate(0, (float) gui.getSlidingProgression(), 0);
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		
		if (!this.isActivated(container)) {
			String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
			guiGraphics.drawString(gui.getFont(), remainTime, (x + 12 - 4 * (remainTime.length())), y + 6, 16777215, true);
		}
		
		poseStack.popPose();
	}
	
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("skill.epicfight.hypervitality.consume.tooltip"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}