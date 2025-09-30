package yesman.epicfight.skill.passive;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class EnduranceSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("12ce9f7a-0457-11ee-be56-0242ac120002");
	
	private float staminaRatio;
	
	public EnduranceSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		
		this.staminaRatio = parameters.getFloat("stamina_ratio");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
			if (container.getExecutor().getEntityState().getLevel() == 1 && event.getDamageSource().getEntity() != null && event.getPlayerPatch().consumeForSkill(this, this.resource)) {
				float staminaConsumption = Math.max(container.getExecutor().getStamina() * this.staminaRatio, 1.5F);
				
				if (container.getExecutor().consumeForSkill(this, Skill.Resource.STAMINA, staminaConsumption)) {
					FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
					buf.writeFloat(staminaConsumption);
					this.executeOnServer(container, buf);
				}
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		super.executeOnServer(container, args);
		
		float staminaConsume = args.readFloat();
		container.getExecutor().setMaxStunShield(staminaConsume);
		container.getExecutor().setStunShield(staminaConsume);
		
		Player player = container.getExecutor().getOriginal();
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), EpicFightSounds.ENDURACNE.get(), player.getSoundSource(), 1.0F, 1.0F);
		
		SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.FLASH_WHITE);
		
		// durationTick, maxOverlay, maxBrightness
		pairingPacket.getBuffer().writeInt(9);
		pairingPacket.getBuffer().writeInt(15);
		pairingPacket.getBuffer().writeInt(1);
		pairingPacket.getBuffer().writeBoolean(true);
		
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, container.getServerExecutor().getOriginal());
	}
	
	@Override
	public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.getExecutor().setStunShield(0.0F);
		container.getExecutor().setMaxStunShield(0.0F);
		
		super.cancelOnServer(container, args);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		return container.getStack() == 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%d", this.maxDuration / 20));
		return list;
	}
	
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.cooldown.consume.tooltip"), Component.translatable("attribute.name.epicfight.cooldown.consume", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.getConsumption())), SkillBookScreen.COOLDOWN_TEXTURE_INFO);
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("attribute.name.epicfight.stamina_current_ratio.consume", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.staminaRatio * 100.0F)), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}