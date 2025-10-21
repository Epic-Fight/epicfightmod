package yesman.epicfight.skill.passive;

import java.util.List;

import org.joml.Vector4f;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.neoevent.playerpatch.ModifyAttackSpeedEvent;
import yesman.epicfight.api.neoevent.playerpatch.ModifyBaseDamageEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations.DecorationOverlay;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations.ParticleGenerator;

public class BerserkerSkill extends PassiveSkill {
	private float speedBonus;
	private float damageBonus;
	
	public BerserkerSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.speedBonus = parameters.getFloat("speed_bonus");
		this.damageBonus = parameters.getFloat("damage_bonus");
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onInitiateClient(SkillContainer container) {
		Player player = container.getExecutor().getOriginal();
		
		container.getExecutor().getEntityDecorations().addDecorationOverlay(EntityDecorations.BERSERKER_OVERLAY, new DecorationOverlay() {
			@Override
			public Vector4f color(float partialTick) {
				float alpha = Mth.clampedLerp(0.0F, 0.42F, 1.0F - (player.getHealth() / player.getMaxHealth()));
				return new Vector4f(0.66F, 0.06F, 0.07F, alpha);
			}
			
			@Override
			public boolean shouldRemove() {
				return container.getExecutor().getSkill(BerserkerSkill.this) == null;
			}
		});
		
		container.getExecutor().getEntityDecorations().addParticleGenerator(EntityDecorations.BERSERKER_PARTICLE, new ParticleGenerator() {
			@Override
			public void generateParticles() {
				float healthRatio = player.getHealth() / player.getMaxHealth();
				RandomSource random = player.getRandom();
				float chance = Mth.clampedLerp(0.0F, 0.04F, (1.0F - healthRatio) - 0.2F);
				
				for (int i = 0; i < 4; i++) {
					if (random.nextFloat() < chance) {
						player.level().addParticle(
							ParticleTypes.POOF,
							player.getX() + random.nextGaussian() * 0.4F,
							player.getY() + player.getBbHeight() * 0.5D + random.nextGaussian() * 0.6F,
							player.getZ() + random.nextGaussian() * 0.4F,
							0.0F,
							0.2F,
							0.0F
						);
					}
				}
			}
			
			@Override
			public boolean shouldRemove() {
				return container.getExecutor().getSkill(BerserkerSkill.this) == null;
			}
		});
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.BOTH)
	public void modifyAttackSpeedEvent(ModifyAttackSpeedEvent event, SkillContainer skillContainer) {
		Player player = event.getPlayerPatch().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		float lostHealthPercentage = (maxHealth - health) / maxHealth;
		lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.01F * this.speedBonus;
		float attackSpeed = event.getAttackSpeed();
		event.setAttackSpeed(Math.min(5.0F, attackSpeed * (1.0F + lostHealthPercentage)));
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.BOTH)
	public void modifyBaseDamageEvent(ModifyBaseDamageEvent event, SkillContainer skillContainer) {
		Player player = event.getPlayerPatch().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		float lostHealthPercentage = (maxHealth - health) / maxHealth;
		lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.01F * this.damageBonus;
		event.attachValueModifier(ValueModifier.multiplier(1.0F + lostHealthPercentage));
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		Player player = container.getExecutor().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		return (maxHealth - health) > 0.0F;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		Player player = container.getExecutor().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		float lostHealthPercentage = (maxHealth - health) / maxHealth;
		lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F);
		guiGraphics.drawString(gui.getFont(), String.format("%.0f%%", lostHealthPercentage), x + 4, y + 6, 16777215, true);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%.1f", this.speedBonus));
		list.add(String.format("%.1f", this.damageBonus));
		
		return list;
	}
}