package yesman.epicfight.skill.passive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.api.neoevent.playerpatch.DodgeSuccessEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.skill.SkillSlots;

public class CatharsisSkill extends PassiveSkill {
	private float regenBonus;
	
	public CatharsisSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.regenBonus = parameters.getFloat("regen_bonus");
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void dodgeSuccessEvent(DodgeSuccessEvent event, SkillContainer container) {
		SkillContainer innateSkillContainer = container.getExecutor().getSkill(SkillSlots.WEAPON_INNATE);
		
		if (innateSkillContainer.getSkill() != null && innateSkillContainer.getStack() < innateSkillContainer.getSkill().getMaxStack()) {
			ServerPlayer serverplayer = event.getPlayerPatch().getOriginal();
			event.getPlayerPatch().playSound(EpicFightSounds.CATHARSIS.get(), 1.0F, 1.0F, 1.0F);
			serverplayer.serverLevel().sendParticles(EpicFightParticles.CATHARSIS.get(), serverplayer.getX(), serverplayer.getEyeY(), serverplayer.getZ(), 0, 0.0D, 0.0D, 0.0D, 0.0D);
			
			float damage = (float)serverplayer.getAttributeValue(Attributes.ATTACK_DAMAGE);
			innateSkillContainer.getSkill().setConsumptionSynchronize(innateSkillContainer, innateSkillContainer.getResource() + damage + this.consumption * this.regenBonus);
		}
	}
}
