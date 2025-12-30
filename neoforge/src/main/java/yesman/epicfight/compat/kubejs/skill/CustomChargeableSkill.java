package yesman.epicfight.compat.kubejs.skill;

import java.util.Arrays;
import java.util.function.Consumer;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class CustomChargeableSkill extends CustomSkill implements ChargeableSkill {
    public record StopHoldingContext(SkillContainer getSkillContainer, SPSkillFeedback getFeedbackPacket) {}
    public record GatherChargingArgumentsContext(Skill getSkill, LocalPlayerPatch getCaster, ControlEngine getControlEngine, CompoundTag arguments) {}

    private final Consumer<PlayerPatch<?>> startCharging;
    private final Consumer<SkillContainer> resetCharging;
    private final int allowedMaxChargingTicks;
    private final int maxChargingTicks;
    private final int minChargingTicks;
    private final Consumer<StopHoldingContext> castSkill;
    private final Consumer<GatherChargingArgumentsContext> gatherChargingArguments;
    private final Consumer<PlayerPatch<?>> chargingTick;
    private final String keyMapping;

    public CustomChargeableSkill(CustomChargeableSkillBuilder builder) {
        super(builder);
        this.startCharging = builder.startCharging;
        this.resetCharging = builder.resetCharging;
        this.allowedMaxChargingTicks = builder.allowedMaxChargingTicks;
        this.maxChargingTicks = builder.maxChargingTicks;
        this.minChargingTicks = builder.minChargingTicks;
        this.castSkill = builder.stopHolding;
        this.gatherChargingArguments = builder.gatherChargingArguments;
        this.chargingTick = builder.chargingTick;
        this.keyMapping = builder.keyMapping;
    }

    @Override
    public void startHolding(SkillContainer container)
    {
        ChargeableSkill.super.startHolding(container);
        if (startCharging != null)
        {
            startCharging.accept(container.getExecutor());
        }
    }

    @Override
    public void resetHolding(SkillContainer container)
    {
        if (resetCharging != null) {
            resetCharging.accept(container);
        }
    }

    @Override
    public int getAllowedMaxChargingTicks() {
        return allowedMaxChargingTicks;
    }

    @Override
    public int getMaxChargingTicks() {
        return maxChargingTicks;
    }

    @Override
    public int getMinChargingTicks() {
        return minChargingTicks;
    }

    @Override
    public void onStopHolding(SkillContainer skillContainer, SPSkillFeedback spSkillExecutionFeedback) {
        if (castSkill != null) {
            castSkill.accept(new StopHoldingContext(skillContainer, spSkillExecutionFeedback));
        }
    }
    
    @Override
	public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		if (gatherChargingArguments != null) {
			gatherChargingArguments.accept(new GatherChargingArgumentsContext(this, container.getClientExecutor(), controlEngine, arguments));
		}
	}

    @Override
    public void holdTick(SkillContainer container)
    {
        ChargeableSkill.super.holdTick(container);
        if (chargingTick != null) {
            chargingTick.accept(container.getExecutor());
        }
    }

    @Override
    public KeyMapping getKeyMapping() {
        return Arrays.stream(Minecraft.getInstance().options.keyMappings).filter(keyMapping -> keyMapping.getName().equals(this.keyMapping)).findFirst().orElse(null);
    }

    @Info("""
        Creates a new chargeable skill. Must provide at least one of the following:
        - allowedMaxChargingTicks
        - maxChargingTicks
        - minChargingTicks
        - setKeyMapping
        """)
    public static class CustomChargeableSkillBuilder extends CustomSkillBuilder {
        private Consumer<PlayerPatch<?>> startCharging;
        private Consumer<SkillContainer> resetCharging;
        private int allowedMaxChargingTicks;
        private int maxChargingTicks;
        private int minChargingTicks;
        private Consumer<StopHoldingContext> stopHolding;
        private Consumer<GatherChargingArgumentsContext> gatherChargingArguments;
        private Consumer<PlayerPatch<?>> chargingTick;
        private String keyMapping;

        public CustomChargeableSkillBuilder(ResourceLocation id) {
            super(id);
        }

        @Info("""
                Called when the skill starts charging.
                """)
        public CustomChargeableSkillBuilder startCharging(Consumer<PlayerPatch<?>> startCharging) {
            this.startCharging = startCharging;
            return this;
        }

        @Info("""
                Called when the skill charge is reset.
                """)
        public CustomChargeableSkillBuilder resetCharging(Consumer<SkillContainer> resetCharging) {
            this.resetCharging = resetCharging;
            return this;
        }

        @Info("""
                The maximum amount of ticks the skill can be charged.
                """)
        public CustomChargeableSkillBuilder allowedMaxChargingTicks(int allowedMaxChargingTicks) {
            this.allowedMaxChargingTicks = allowedMaxChargingTicks;
            return this;
        }

        @Info("""
                The cap for the amount of ticks the skill can be charged.
                """)
        public CustomChargeableSkillBuilder maxChargingTicks(int maxChargingTicks) {
            this.maxChargingTicks = maxChargingTicks;
            return this;
        }

        @Info("""
                The minimum amount of ticks the skill can be charged before it is cast.
                """)
        public CustomChargeableSkillBuilder minChargingTicks(int minChargingTicks) {
            this.minChargingTicks = minChargingTicks;
            return this;
        }

        @Info("""
                Called when the skill is done charging and the key is released.
                """)
        public CustomChargeableSkillBuilder onCastSkill(Consumer<StopHoldingContext> stopHolding) {
            this.stopHolding = stopHolding;
            return this;
        }

        @Info("""
                Called when the skill has started charging.
                """)
        public CustomChargeableSkillBuilder gatherChargingArguments(Consumer<GatherChargingArgumentsContext> gatherChargingArguments) {
            this.gatherChargingArguments = gatherChargingArguments;
            return this;
        }

        @Info("""
                The key mapping that is used to charge the skill.
                This uses a string that matches the ID of the key mapping. IDs of key mappings can be found in the `options.txt` file in the Minecraft directory. (e.g. `key.jump` or `key.attack`)
                """)
        public CustomChargeableSkillBuilder setKeyMapping(String keyMapping) {
            this.keyMapping = keyMapping;
            return this;
        }

        public CustomChargeableSkillBuilder onChargingTick(Consumer<PlayerPatch<?>> chargingTick) {
            this.chargingTick = chargingTick;
            return this;
        }

        @Override
        public CustomChargeableSkill createObject() {
            return new CustomChargeableSkill(this);
        }
    }
}
