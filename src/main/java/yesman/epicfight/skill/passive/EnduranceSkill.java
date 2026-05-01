package yesman.epicfight.skill.passive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

import java.util.List;

public class EnduranceSkill extends PassiveSkill {
    private float staminaRatio;

    public EnduranceSkill(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);

        this.staminaRatio = parameters.getFloat("stamina_ratio");
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE,
            event -> {
                if (skillContainer.getExecutor().getEntityState().getLevel() == 1 && event.getDamageSource().getEntity() != null && skillContainer.getExecutor().consumeForSkill(this, this.resource)) {
                    float staminaConsumption = Math.max(skillContainer.getExecutor().getStamina() * this.staminaRatio, 1.5F);

                    if (skillContainer.getExecutor().consumeForSkill(this, Skill.Resource.STAMINA, staminaConsumption)) {
                        CompoundTag argument = new CompoundTag();
                        argument.putFloat("staminaConsumption", staminaConsumption);
                        this.executeOnServer(skillContainer, argument);
                    }
                }
            },
            this
        );
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container, arguments);

        float staminaConsume = arguments.getFloat("staminaConsumption");
        container.getExecutor().setMaxStunShield(staminaConsume);
        container.getExecutor().setStunShield(staminaConsume);

        Player player = container.getExecutor().getOriginal();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), EpicFightSounds.ENDURACNE.get(), player.getSoundSource(), 1.0F, 1.0F);

        SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.FLASH_WHITE);

        // durationTick, maxOverlay, maxBrightness
        pairingPacket.buffer().writeInt(9);
        pairingPacket.buffer().writeInt(15);
        pairingPacket.buffer().writeInt(1);
        pairingPacket.buffer().writeBoolean(true);

        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, container.getServerExecutor().getOriginal());
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag arguments) {
        container.getExecutor().setStunShield(0.0F);
        container.getExecutor().setMaxStunShield(0.0F);

        super.cancelOnServer(container, arguments);
    }

    @Override @ClientOnly
    public boolean shouldDraw(SkillContainer container) {
        return container.getStack() == 0;
    }

    @Override @ClientOnly
    public List<Object> getTooltipArgsOfScreen(List<Object> list) {
        list.add(String.format("%d", this.maxDuration / 20));
        return list;
    }

    @Override @ClientOnly
    public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
        consumptionList.add(Component.translatable("attribute.name.epicfight.cooldown.consume.tooltip"), Component.translatable("attribute.name.epicfight.cooldown.consume", ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.getConsumption())), SkillBookScreen.COOLDOWN_TEXTURE_INFO);
        consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("attribute.name.epicfight.stamina_current_ratio.consume", ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.staminaRatio * 100.0F)), SkillBookScreen.STAMINA_TEXTURE_INFO);
        return true;
    }
}