package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class WeaponInnateSkill extends Skill {
	public static SkillBuilder<WeaponInnateSkill> createWeaponInnateBuilder() {
		return new SkillBuilder<WeaponInnateSkill>().setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
	}
	
	protected List<Map<AttackPhaseProperty<?>, Object>> properties;
	
	public WeaponInnateSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
		super(builder);
		
		this.properties = Lists.newArrayList();
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		ItemStack itemstack = container.getExecutor().getOriginal().getMainHandItem();
		
		return super.canExecute(container)
				&& EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this
				&& container.getExecutor().getOriginal().getVehicle() == null
				&& (!this.isActivated(container) || this.activateType == ActivateType.TOGGLE);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = Lists.newArrayList();
		String traslatableText = this.getTranslationKey();
		
		list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
		list.add(Component.translatable(traslatableText + ".tooltip").withStyle(ChatFormatting.DARK_GRAY));
		
		return list;
	}
	
	protected void generateTooltipforPhase(List<Component> list, ItemStack itemstack, CapabilityItem itemcap, PlayerPatch<?> playerpatch, Map<AttackPhaseProperty<?>, Object> propertyMap, String title) {
		double weaponBaseDamage = playerpatch.getWeaponAttribute(Attributes.ATTACK_DAMAGE, itemstack);
		double armorNegation = playerpatch.getWeaponAttribute(EpicFightAttributes.ARMOR_NEGATION.get(), itemstack);
		double impact = playerpatch.getWeaponAttribute(EpicFightAttributes.IMPACT.get(), itemstack);
		double maxStrikes = playerpatch.getWeaponAttribute(EpicFightAttributes.MAX_STRIKES.get(), itemstack);
		ValueModifier.ResultCalculator damageModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator armorNegationModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator impactModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator maxStrikesModifier = ValueModifier.calculator();
		
		this.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, propertyMap).ifPresent(damageModifier::attach);
		this.getProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, propertyMap).ifPresent(armorNegationModifier::attach);
		this.getProperty(AttackPhaseProperty.IMPACT_MODIFIER, propertyMap).ifPresent(impactModifier::attach);
		this.getProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, propertyMap).ifPresent(maxStrikesModifier::attach);
		impactModifier.multiply(1.0F + itemstack.getEnchantmentLevel(Enchantments.KNOCKBACK) * 0.12F);
		
		final double fBaseDamage = weaponBaseDamage;
		weaponBaseDamage = damageModifier.getResult(playerpatch.getModifiedBaseDamage((float)weaponBaseDamage));
		armorNegation = armorNegationModifier.getResult((float)armorNegation);
		impact = impactModifier.getResult((float)impact);
		maxStrikes = maxStrikesModifier.getResult((float)maxStrikes);
		
		list.add(Component.literal(title).withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.GRAY));
		
		MutableComponent damageComponent =
			Component
				.translatable(
					"damage_source.epicfight.damage",
					Component
						.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(weaponBaseDamage))
						.withStyle(ChatFormatting.RED)
				)
				.withStyle(ChatFormatting.DARK_GRAY);
		
		this.getProperty(AttackPhaseProperty.EXTRA_DAMAGE, propertyMap).ifPresent((extraDamageSet) -> {
			extraDamageSet.forEach((extraDamage) -> {
				extraDamage.setTooltips(itemstack, damageComponent, fBaseDamage);
			});
		});
		
		list.add(damageComponent);
		
		if (armorNegation != 0.0D) {
			list.add(
				Component
					.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(armorNegation) + "% ")
					.withStyle(ChatFormatting.GOLD)
					.append(
						Component
							.translatable(EpicFightAttributes.ARMOR_NEGATION.get().getDescriptionId())
							.withStyle(ChatFormatting.DARK_GRAY)
					)
			);
		}
		
		if (impact != 0.0D) {
			list.add(
				Component
					.translatable(
						EpicFightAttributes.IMPACT.get().getDescriptionId() + ".value",
						Component
							.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(impact))
							.withStyle(ChatFormatting.AQUA)
					)
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		}
		
		list.add(
			Component
				.translatable(
					EpicFightAttributes.MAX_STRIKES.get().getDescriptionId() + ".value",
					Component
						.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxStrikes))
						.withStyle(ChatFormatting.WHITE))
					.withStyle(ChatFormatting.DARK_GRAY)
		);
		
		Optional<StunType> stunOption = this.getProperty(AttackPhaseProperty.STUN_TYPE, propertyMap);
		
		stunOption.ifPresent((stunType) -> {
			list.add(Component.translatable(stunType.toString()).withStyle(ChatFormatting.DARK_GRAY));
		});
		
		if (!stunOption.isPresent()) {
			list.add(Component.translatable(StunType.SHORT.toString()).withStyle(ChatFormatting.DARK_GRAY));
		}	
	}
	
	@SuppressWarnings("unchecked")
	protected <V> Optional<V> getProperty(AttackPhaseProperty<V> propertyKey, Map<AttackPhaseProperty<?>, Object> map) {
		return (Optional<V>) Optional.ofNullable(map.get(propertyKey));
	}
	
	public WeaponInnateSkill newProperty() {
		this.properties.add(Maps.newHashMap());
		
		return this;
	}
	
	public <T> WeaponInnateSkill addProperty(AttackPhaseProperty<T> propertyKey, T object) {
		this.properties.get(properties.size() - 1).put(propertyKey, object);
		
		return this;
	}
	
	private static final Vec2f[] CLOCK_POS = {
		new Vec2f(0.5F, 0.5F),
		new Vec2f(0.5F, 0.0F),
		new Vec2f(0.0F, 0.0F),
		new Vec2f(0.0F, 1.0F),
		new Vec2f(1.0F, 1.0F),
		new Vec2f(1.0F, 0.0F)
	};
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldDraw(SkillContainer container) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, (float)gui.getSlidingProgression(), 0);
		
		boolean creative = container.getExecutor().getOriginal().isCreative();
		boolean fullstack = creative || container.isFull();
		boolean canUse = !container.isDisabled() && container.getSkill().checkExecuteCondition(container);
		
		float cooldownRatio = (fullstack || container.isActivated()) ? 1.0F : container.getResource(partialTick);
		int vertexNum = 0;
		float iconSize = 32.0F;
		float bottom = y + iconSize;
		float right = x + iconSize;
		float middle = x + iconSize * 0.5F;
		float lastVertexX = 0;
		float lastVertexY = 0;
		float lastTexX = 0;
		float lastTexY = 0;
		
		if (cooldownRatio < 0.125F) {
			vertexNum = 6;
			lastTexX = cooldownRatio / 0.25F;
			lastTexY = 0.0F;
			lastVertexX = middle + iconSize * lastTexX;
			lastVertexY = y;
			lastTexX += 0.5F;
		} else if (cooldownRatio < 0.375F) {
			vertexNum = 5;
			lastTexX = 1.0F;
			lastTexY = (cooldownRatio - 0.125F) / 0.25F;
			lastVertexX = right;
			lastVertexY = y + iconSize * lastTexY;
		} else if (cooldownRatio < 0.625F) {
			vertexNum = 4;
			lastTexX = (cooldownRatio - 0.375F) / 0.25F;
			lastTexY = 1.0F;
			lastVertexX = right - iconSize * lastTexX;
			lastVertexY = bottom;
			lastTexX = 1.0F - lastTexX;
		} else if (cooldownRatio < 0.875F) {
			vertexNum = 3;
			lastTexX = 0.0F;
			lastTexY = (cooldownRatio - 0.625F) / 0.25F;
			lastVertexX = x;
			lastVertexY = bottom - iconSize * lastTexY;
			lastTexY = 1.0F - lastTexY;
		} else {
			vertexNum = 2;
			lastTexX = (cooldownRatio - 0.875F) / 0.25F;
			lastTexY = 0.0F;
			lastVertexX = x + iconSize * lastTexX;
			lastVertexY = y;
		}
		
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, container.getSkill().getSkillTexture());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		if (canUse) {
			if (container.getStack() > 0) {
				RenderSystem.setShaderColor(0.0F, 0.64F, 0.72F, 0.8F);
			} else {
				RenderSystem.setShaderColor(0.0F, 0.5F, 0.5F, 0.6F);
			}
		} else {
			RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
		}
		
		Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        
        for (int j = 0; j < vertexNum; j++) {
        	bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
		}
        
        bufferbuilder.vertex(guiGraphics.pose().last().pose(), lastVertexX, lastVertexY, 0.0F).uv(lastTexX, lastTexY).endVertex();
        tessellator.end();
        
        if (canUse) {
			RenderSystem.setShaderColor(0.08F, 0.79F, 0.95F, 1.0F);
		} else {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
        
        GL11.glCullFace(GL11.GL_FRONT);
        
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        
        for (int j = 0; j < 2; j++) {
        	bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
		}
		
		for (int j = CLOCK_POS.length - 1; j >= vertexNum; j--) {
        	bufferbuilder.vertex(guiGraphics.pose().last().pose(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F).uv(CLOCK_POS[j].x, CLOCK_POS[j].y).endVertex();
		}
        
        bufferbuilder.vertex(guiGraphics.pose().last().pose(), lastVertexX, lastVertexY, 0.0F).uv(lastTexX, lastTexY).endVertex();
        tessellator.end();
        
        GL11.glCullFace(GL11.GL_BACK);
        
     	RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (container.isActivated() && (container.getSkill().getActivateType() == ActivateType.DURATION || container.getSkill().getActivateType() == ActivateType.DURATION_INFINITE)) {
			String s = String.format("%.0f", container.getRemainDuration() / 20.0F);
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
		} else if (!fullstack) {
			String s = String.valueOf((int)(cooldownRatio * 100.0F));
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.drawString(gui.getFont(), s, x + 13 - stringWidth, y + 13, 16777215, true);
		}
		
		if (container.getSkill().getMaxStack() > 1) {
			String s = String.valueOf(container.getStack());
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.drawString(gui.getFont(), s, x + 25 - stringWidth, y + 22, 16777215, true);
		}
		
		guiGraphics.pose().popPose();
	}
}
