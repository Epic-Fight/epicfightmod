package yesman.epicfight.skill.passive;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector4f;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations.DecorationOverlay;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class AdaptiveSkinSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("e9cd15f0-72cc-474b-bfee-66276d06157d");
	
	public static class Builder extends SkillBuilder<AdaptiveSkinSkill> {
		protected final Map<TagKey<DamageType>, Vec3f> protectableDamageTypeTags = new LinkedHashMap<> ();
		
		public Builder addProtectableDamageTypeTags(Map<TagKey<DamageType>, Vec3f> tags) {
			this.protectableDamageTypeTags.putAll(tags);
			return this;
		}
	}
	
	public static AdaptiveSkinSkill.Builder createAdaptiveSkinBuilder() {
		return new AdaptiveSkinSkill.Builder()
				.addProtectableDamageTypeTags(
					ImmutableMap.of(
						EpicFightDamageTypeTags.IS_MELEE, new Vec3f(227 / 255.0F, 127 / 255.0F, 127 / 255.0F),
						DamageTypeTags.IS_PROJECTILE, new Vec3f(102 / 255.0F, 197 / 255.0F, 255 / 255.0F),
						DamageTypeTags.IS_FIRE, new Vec3f(229 / 255.0F, 143 / 255.0F, 66 / 255.0F),
						EpicFightDamageTypeTags.IS_MAGIC, new Vec3f(226 / 255.0F, 154 / 255.0F, 234 / 255.0F),
						DamageTypeTags.IS_EXPLOSION, new Vec3f(207 / 255.0F, 205 / 255.0F, 120 / 255.0F)
					)
				)
				.setCategory(SkillCategories.PASSIVE)
				.setResource(Resource.NONE);
	}
	
	private final Map<TagKey<DamageType>, Vec3f> protectableDamageTypeTags;
	
	private float damageResistance;
	private int maxResistanceStack;
	
	public AdaptiveSkinSkill(Builder builder) {
		super(builder);
		
		this.protectableDamageTypeTags = Collections.unmodifiableMap(builder.protectableDamageTypeTags);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		
		this.damageResistance = parameters.getFloat("damage_resistance");
		this.maxResistanceStack = parameters.getInt("max_resistance_stack");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			TagKey<DamageType> currentResisting = container.getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
			int stacks = container.getDataManager().getDataValueOptional(SkillDataKeys.STACKS.get()).orElse(0);
			
			if (event.getDamageSource().is(currentResisting)) {
				event.attachValueModifier(ValueModifier.multiplier(1.0F - this.damageResistance * stacks));
				
				if (stacks < this.maxResistanceStack) {
					container.getExecutor().playSound(EpicFightSounds.ADAPTIVE_SKIN_INCREASE.get(), 1.0F, 0.0F, 0.0F);
					container.getDataManager().setDataSyncF(SkillDataKeys.STACKS.get(), v -> v + 1);
				}
				
				container.getDataManager().setDataSync(SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().tickCount);
			} else {
				if (stacks <= 1) {
					for (TagKey<DamageType> protectableTag : this.protectableDamageTypeTags.keySet()) {
						if (event.getDamageSource().is(protectableTag)) {
							container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 1);
							container.getDataManager().setDataSync(SkillDataKeys.RESISTING_DAMAGE_TYPE.get(), protectableTag);
							container.getDataManager().setDataSync(SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().tickCount);
							
							if (stacks == 0) {
								container.getExecutor().playSound(EpicFightSounds.ADAPTIVE_SKIN_INCREASE.get(), 1.0F, 0.0F, 0.0F);
							} else {
								container.getExecutor().playSound(EpicFightSounds.ADAPTIVE_SKIN_DECREASE.get(), 1.0F, 0.0F, 0.0F);
							}
							
							break;
						}
					}
				} else {
					boolean adaptableType = false;
					
					for (TagKey<DamageType> protectableTag : this.protectableDamageTypeTags.keySet()) {
						if (event.getDamageSource().is(protectableTag)) {
							adaptableType = true;
							break;
						}
					}
					
					if (adaptableType) {
						container.getExecutor().playSound(EpicFightSounds.ADAPTIVE_SKIN_DECREASE.get(), 1.0F, 0.0F, 0.0F);
						container.getDataManager().setDataSyncF(SkillDataKeys.STACKS.get(), v -> v - 1);
					}
				}
			}
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onInitiateClient(SkillContainer container) {
		container.getExecutor().getEntityDecorations().addDecorationOverlay(EntityDecorations.ADAPTIVE_SKIN_OVERLAY, new DecorationOverlay() {
			@Override
			public RenderType getRenderType() {
				TagKey<DamageType> resistingDamageTypeTagKey = container.getExecutor().getSkill(AdaptiveSkinSkill.this).getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
				Vec3f color = AdaptiveSkinSkill.this.getGlintColor(resistingDamageTypeTagKey);
				return EpicFightRenderTypes.coloredGlintWorldRendertype(container.getExecutor().getOriginal(), color.x, color.y, color.z);
			}
			
			@Override
			public boolean shouldRender() {
				return container.getExecutor().getSkill(AdaptiveSkinSkill.this).getDataManager().getDataValueOptional(SkillDataKeys.RESISTING_DAMAGE_TYPE.get()).orElse(EpicFightDamageTypeTags.NONE) != EpicFightDamageTypeTags.NONE;
			}
			
			@Override
			public boolean shouldRemove() {
				return container.getExecutor().getSkill(AdaptiveSkinSkill.this) == null;
			}
		});
		
		container.getExecutor().getEntityDecorations().addColorModifier(EntityDecorations.ADAPTIVE_SKIN_COLOR, new yesman.epicfight.world.capabilities.entitypatch.EntityDecorations.RenderAttributeModifier<> () {
			@Override
			public void modifyValue(Vector4f val, float partialTick) {
				TagKey<DamageType> resistingDamageTypeTagKey = container.getExecutor().getSkill(AdaptiveSkinSkill.this).getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
				
				if (!EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTagKey)) {
					Vec3f color = AdaptiveSkinSkill.this.getGlintColor(resistingDamageTypeTagKey);
					val.x = color.x;
					val.y = color.y;
					val.z = color.z;
				}
			}
			
			@Override
			public boolean shouldRemove() {
				return container.getExecutor().getSkill(AdaptiveSkinSkill.this) == null;
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		listener.removeListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		
		TagKey<DamageType> resistingDamageTypeTag = container.getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
		
		if (!EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTag)) {
			if (container.getExecutor().getOriginal().tickCount - container.getDataManager().getDataValueOptional(SkillDataKeys.TICK_RECORD.get()).orElse(0) > 300) {
				container.getDataManager().setDataSync(SkillDataKeys.RESISTING_DAMAGE_TYPE.get(), EpicFightDamageTypeTags.NONE);
				container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 0);
			}
		}
	}
	
	private Vec3f getGlintColor(TagKey<DamageType> tagKey) {
		return this.protectableDamageTypeTags.get(tagKey);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		TagKey<DamageType> resistingDamageTypeTag = container.getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
		return !EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTag) && this.protectableDamageTypeTags.containsKey(resistingDamageTypeTag);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		
		Vec3f color = this.protectableDamageTypeTags.get(container.getDataManager().getDataValue(SkillDataKeys.RESISTING_DAMAGE_TYPE.get()));
		guiGraphics.innerBlit(this.getSkillTexture(), (int)x, (int)x + 24, (int)y, (int)y + 24, 0, 0.0F, 1.0F, 0.0F, 1.0F, color.x, color.y, color.z, 1.0F);
		int stacks = container.getDataManager().getDataValue(SkillDataKeys.STACKS.get());
		
		if (stacks > 1) {
			guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18, y + 16, 16777215, true);
		}
		
		int lastHitTick = container.getDataManager().getDataValueOptional(SkillDataKeys.TICK_RECORD.get()).orElse(0);
		
		if (container.getExecutor().getOriginal().tickCount - lastHitTick > 200) {
			int remainseconds = 1 + (100 - (container.getExecutor().getOriginal().tickCount - lastHitTick - 200)) / 20;
			guiGraphics.drawString(gui.getFont(), String.valueOf(remainseconds), x + 8, y + 8, 16777215, true);
		}
		
		poseStack.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.damageResistance * 100.0F));
		list.add(this.maxResistanceStack);
		
		StringBuilder sb = new StringBuilder();
		
		for (TagKey<DamageType> tag : this.protectableDamageTypeTags.keySet()) {
			String tagKey = String.format("tag.%s.%s.%s", tag.registry().location().getPath(), tag.location().getNamespace(), tag.location().getPath());
			sb.append("- " + Component.translatable(tagKey).getString() + "\n");
		}
		
		list.add(sb.toString());
		
		return list;
	}
}
