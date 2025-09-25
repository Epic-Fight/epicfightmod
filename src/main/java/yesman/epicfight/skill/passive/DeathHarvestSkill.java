package yesman.epicfight.skill.passive;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.neoevent.playerpatch.PlayerKilledEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.DeathHarvestOrb;

public class DeathHarvestSkill extends PassiveSkill {
	public DeathHarvestSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void dealDamageEvent(PlayerKilledEvent event, SkillContainer skillContainer) {
		PlayerPatch<?> playerpatch = skillContainer.getExecutor();
		Player original = playerpatch.getOriginal();
		LivingEntity target = event.getKilledEntity();
		
		if (event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
			original.level().playSound(null, original.getX(), original.getY(), original.getZ(), SoundEvents.WITHER_AMBIENT, original.getSoundSource(), 0.3F, 1.25F);
			
			int damage = (int)original.getAttributeValue(Attributes.ATTACK_DAMAGE);
			DeathHarvestOrb harvestOrb = new DeathHarvestOrb(original, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), damage);
			original.level().addFreshEntity(harvestOrb);
		}
	}
}