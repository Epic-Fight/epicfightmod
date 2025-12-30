package yesman.epicfight.skill.passive;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class ForbiddenStrengthSkill extends PassiveSkill {
	public ForbiddenStrengthSkill(SkillBuilder<?> builder) {
		super(builder);
	}

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Player.CONSUME_SKILL,
            event -> {
                if (event.getResourceType() == Skill.Resource.STAMINA && event.getSkill() != this) {
                    if (!container.getExecutor().hasStamina(event.getAmount()) && !container.getExecutor().getOriginal().isCreative()) {
                        event.setResourceType(Skill.Resource.HEALTH);

                        float healthConsumeAmount = event.getAmount() - container.getExecutor().getStamina();
                        event.setAmount(healthConsumeAmount);

                        if (!container.getExecutor().isLogicalClient() && event.getResourceType().predicate.canExecute(container, container.getExecutor(), healthConsumeAmount)) {
                            container.getExecutor().setStamina(0.0F);

                            Player player = container.getExecutor().getOriginal();
                            ServerLevel serverLevel = (ServerLevel)player.level();

                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), EpicFightSounds.FORBIDDEN_STRENGTH.get(), player.getSoundSource(), 1.0F, 1.0F);
                            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY(0.5D), player.getZ(), (int)healthConsumeAmount, 0.1D, 0.0D, 0.1D, 0.2D);
                            serverLevel.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY(0.5D), player.getZ(), (int)healthConsumeAmount * 3, 0.0D, 0.0D, 0.75D, 0.1D);
                        }
                    }
                }
            },
            this
        );
    }

	@Override @ClientOnly
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.health.consume.tooltip"), Component.translatable("skill.epicfight.forbidden_strength.consume.tooltip"), SkillBookScreen.HEALTH_TEXTURE_INFO);
		return true;
	}
}