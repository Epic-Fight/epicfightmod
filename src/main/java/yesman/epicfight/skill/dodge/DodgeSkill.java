package yesman.epicfight.skill.dodge;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DodgeSkill extends Skill {
	public static class Builder extends SkillBuilder<DodgeSkill> {
		protected AnimationAccessor<? extends StaticAnimation>[] animations;
		
		@SafeVarargs
		public final Builder setAnimations(AnimationAccessor<? extends StaticAnimation>... animations) {
			this.animations = animations;
			return this;
		}
	}
	
	public static Builder createDodgeBuilder() {
		return new Builder().setCategory(SkillCategories.DODGE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.STAMINA);
	}
	
	protected final AnimationAccessor<? extends StaticAnimation>[] animations;
	
	public DodgeSkill(Builder builder) {
		super(builder);
		
		this.animations = builder.animations;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Object getExecutionPacket(SkillContainer skillContainer, FriendlyByteBuf args) {
		LocalPlayerPatch executor = skillContainer.getClientExecutor();
		LocalPlayer localPlayer = executor.getOriginal();
		float pulse = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(executor.getOriginal()), 0.0F, 1.0F);
        InputUtils.sneakingTick(localPlayer, false, pulse);
		
        final MovementDirection movementDirection = MovementDirection.fromInputState(InputManager.getInputState(localPlayer.input));
		final int vertic = movementDirection.vertical();
		final int horizon = movementDirection.horizontal();
		float yRot = EpicFightCameraAPI.getInstance().getForwardYRot();
		float degree = Mth.wrapDegrees(-(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon) + yRot);
		
		CPSkillRequest packet = new CPSkillRequest(skillContainer.getSlot());
		packet.getBuffer().writeInt(vertic >= 0 ? 0 : 1);
		packet.getBuffer().writeFloat(degree);
		
		return packet;
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.consumption));
		return list;
	}
	
	@Override
	public void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args) {
		super.executeOnServer(skillContainer, args);
		
		ServerPlayerPatch executor = skillContainer.getServerExecutor();
		int i = args.readInt();
		float yRot = args.readFloat();
		
		executor.playAnimationSynchronized(this.animations[i], 0);
		executor.setModelYRot(yRot, true);
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		return !(executor.isInAir() || !playerState.canUseSkill()) && !executor.getOriginal().isInWater() && !executor.getOriginal().onClimbable() && executor.getOriginal().getVehicle() == null;
	}
}