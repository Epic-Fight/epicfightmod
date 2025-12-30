package yesman.epicfight.skill.passive;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class EmergencyEscapeSkill extends PassiveSkill {
    public static Builder createEmergencyEscapeBuilder() {
        return new EmergencyEscapeSkill.Builder(EmergencyEscapeSkill::new).setCategory(SkillCategories.PASSIVE).setResource(Resource.COOLDOWN);
    }

    public static class Builder extends SkillBuilder<EmergencyEscapeSkill.Builder> {
        protected final Set<WeaponCategory> availableWeapons = Sets.newHashSet();

        public Builder(Function<EmergencyEscapeSkill.Builder, ? extends Skill> constructor) {
            super(constructor);
        }

        public Builder addAvailableWeaponCategory(WeaponCategory... wc) {
            this.availableWeapons.addAll(Arrays.asList(wc));
            return this;
        }
    }

    private final Set<WeaponCategory> availableWeapons;

    public EmergencyEscapeSkill(EmergencyEscapeSkill.Builder builder) {
        super(builder);

        this.availableWeapons = builder.availableWeapons;
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Player.CAST_SKILL,
            event -> {
                if (event.getSkillContainer().getSkill().getCategory() == SkillCategories.DODGE) {
                    EntityState state = container.getExecutor().getEntityState();
                    DynamicAnimation animation = container.getExecutor().getAnimator().getPlayerFor(null).getRealAnimation().get();

                    if (
                        (
                            !event.isStateExecutable() && animation instanceof AttackAnimation &&
                            this.availableWeapons.contains(container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory())
                        ) ||
                        (
                            state.hurt() &&
                            container.getStack() > 0
                        )
                    ) {
                        event.setStateExecutable(true);
                    }
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Player.CONSUME_SKILL,
            event -> {
                if (event.getSkill().getCategory() == SkillCategories.DODGE) {
                    if (!container.getExecutor().getOriginal().isCreative()) {
                        if (event.getSkill().getConsumption() > container.getExecutor().getStamina()) {
                            if (container.getExecutor().consumeForSkill(this, this.resource)) {
                                if (!container.getExecutor().isLogicalClient()) {
                                    this.executeOnServer(container, event.getArguments());
                                }

                                event.setResourceType(Skill.Resource.NONE);
                            }
                        } else if (container.getExecutor().getEntityState().hurt() && container.getExecutor().consumeForSkill(this, this.resource)) {
                            if (!container.getExecutor().isLogicalClient()) {
                                this.executeOnServer(container, event.getArguments());
                            }
                        }
                    }
                }
            },
            this
        );
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        this.setStackSynchronize(container, container.getStack() - 1);
        float yRot = container.getExecutor().getYRot();

        if (arguments.contains("yRot")) {
            yRot = arguments.getFloat("yRot");
        }

        container.getExecutor().playSound(EpicFightSounds.EMERGENCY_ESCAPE.get(), 1.0F, 1.0F);

        SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.EMERGENCY_ESCAPE_ACTIVATED);
        pairingPacket.buffer().writeFloat(yRot);

        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, container.getServerExecutor().getOriginal());
    }

    @ClientOnly
    @Override
    public boolean shouldDraw(SkillContainer container) {
        return container.getStack() == 0;
    }

    @ClientOnly
    @Override
    public List<Object> getTooltipArgsOfScreen(List<Object> list) {
        list.add(String.format("%.1f", this.consumption));
        return list;
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return this.availableWeapons;
    }
}