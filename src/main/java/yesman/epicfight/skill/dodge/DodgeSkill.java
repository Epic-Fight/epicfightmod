package yesman.epicfight.skill.dodge;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DodgeSkill extends Skill {
	public static class Builder<B extends DodgeSkill.Builder<B>> extends SkillBuilder<B> {
		protected AnimationAccessor<? extends StaticAnimation>[] animations;
		
		public Builder(Function<B, ? extends DodgeSkill> constructor) {
			super(constructor);
		}
		
		@SuppressWarnings("unchecked")
		@SafeVarargs
		public final B setAnimations(AnimationAccessor<? extends StaticAnimation>... animations) {
			this.animations = animations;
			return (B)this;
		}
	}
	
	public static <B extends DodgeSkill.Builder<B>> B createDodgeBuilder(Function<B, ? extends DodgeSkill> constructor) {
		return (B)new DodgeSkill.Builder<> (constructor).setCategory(SkillCategories.DODGE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.STAMINA);
	}
	
	protected final AnimationAccessor<? extends StaticAnimation>[] animations;
	
	public DodgeSkill(DodgeSkill.Builder<?> builder) {
		super(builder);
		
		this.animations = builder.animations;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		LocalPlayerPatch executor = container.getClientExecutor();
		Input input = executor.getOriginal().input;
		float sneakingSpeed = (float)executor.getOriginal().getAttributeValue(Attributes.SNEAKING_SPEED);
		input.tick(false, sneakingSpeed);
		
        int forward = input.up ? 1 : 0;
        int backward = input.down ? -1 : 0;
        int left = input.left ? 1 : 0;
        int right = input.right ? -1 : 0;
		int vertic = forward + backward;
		int horizon = left + right;
		float yRot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
		float degree = Mth.wrapDegrees(-(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon) + yRot);
		
		arguments.putInt("direction", vertic >= 0 ? 0 : 1);
		arguments.putFloat("yRot", degree);
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.consumption));
		return list;
	}
	
	@Override
	public void executeOnServer(SkillContainer skillContainer, CompoundTag args) {
		super.executeOnServer(skillContainer, args);
		
		ServerPlayerPatch executor = skillContainer.getServerExecutor();
		int i = args.getInt("direction");
		float yRot = args.getFloat("yRot");
		
		executor.playAnimationSynchronized(this.animations[i], 0);
		executor.setModelYRot(yRot, true);
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		return !(executor.isInAir() || !playerState.canUseSkill()) && !executor.getOriginal().isInWater() && !executor.getOriginal().onClimbable() && executor.getOriginal().getVehicle() == null;
	}
}