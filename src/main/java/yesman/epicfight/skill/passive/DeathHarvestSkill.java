package yesman.epicfight.skill.passive;

import java.util.UUID;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.DeathHarvestOrb;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class DeathHarvestSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("816118e6-b902-11ed-afa1-0242ac120002");
	
	public DeathHarvestSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
			PlayerPatch<?> playerpatch = container.getExecutor();
			Player original = playerpatch.getOriginal();
			LivingEntity target = event.getKilledEntity();
			
			if (event.getDamageSource().is(EpicFightDamageTypeTags.WEAPON_INNATE)) {
				original.level().playSound(null, original.getX(), original.getY(), original.getZ(), SoundEvents.WITHER_AMBIENT, original.getSoundSource(), 0.3F, 1.25F);
				
				int damage = (int)original.getAttributeValue(Attributes.ATTACK_DAMAGE);
				DeathHarvestOrb harvestOrb = new DeathHarvestOrb(original, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), damage);
				original.level().addFreshEntity(harvestOrb);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
	}
}