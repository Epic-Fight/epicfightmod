package yesman.epicfight.api.animation.types;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

/**
 * Meta-animation that routes between an explicitly authored mainhand clip and an explicitly
 * authored offhand clip at {@link #begin(LivingEntityPatch)} based on which hand the entity is
 * using. Both clips are real JSON files on disk -- no pose-time mirroring is performed.
 */
public class MirrorAnimation extends StaticAnimation {
	public DirectStaticAnimation original;
	public DirectStaticAnimation mirror;

	public MirrorAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends MirrorAnimation> accessor, String mainhandPath, String offhandPath, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, false, accessor, armature);

		ResourceLocation mainhand = ResourceLocation.fromNamespaceAndPath(accessor.registryName().getNamespace(), mainhandPath);
		ResourceLocation offhand = ResourceLocation.fromNamespaceAndPath(accessor.registryName().getNamespace(), offhandPath);
		this.original = new DirectStaticAnimation(transitionTime, repeatPlay, mainhand, armature);
		this.mirror = new DirectStaticAnimation(transitionTime, repeatPlay, offhand, armature);
	}

	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);

		if (entitypatch.isLogicalClient()) {
			DirectStaticAnimation animation = this.checkHandAndReturnAnimation(this.resolveActiveHand(entitypatch));
			entitypatch.getClientAnimator().playAnimation(animation, 0.0F);
		}
	}

	/** Picks which hand currently holds the active item.
	 *
	 *  Three things to handle:
	 *
	 *  - Shield-bearing animations ({@code BIPED_BLOCK}, {@code BIPED_HIT_SHIELD}):
	 *    {@link yesman.epicfight.skill.guard.GuardSkill} / {@link yesman.epicfight.skill.guard.ParryingSkill}
	 *    call {@code startUsingItem(getPrimaryHand())}, and {@code getPrimaryHand()} returns
	 *    {@code MAIN_HAND} whenever the offhand is just a shield (shields don't qualify as a
	 *    weapon under {@code isMirrorMode}). So {@code isUsingItem()} ends up true with
	 *    {@code getUsedItemHand() == MAIN_HAND} even when the actual shield is in the offhand --
	 *    routing on {@code usedItemHand} alone would play the mainhand variant on an empty hand.
	 *    Route directly to whichever hand actually holds a {@link UseAnim#BLOCK} item first.
	 *
	 *  - F-swap mid-use ({@code BIPED_DRINK} / {@code _EAT} / {@code _SPYGLASS_USE}): the player
	 *    presses F while right-clicking, items trade slots, but {@code usedItemHand} stays on
	 *    the original side. Compare the using {@link ItemStack} to each hand's contents and
	 *    follow the item itself.
	 *
	 *  - Animation triggered without {@code isUsingItem}: fall back to the hand that actually
	 *    holds an item. */
	private InteractionHand resolveActiveHand(LivingEntityPatch<?> entitypatch) {
		LivingEntity entity = entitypatch.getOriginal();
		ItemStack main = entity.getMainHandItem();
		ItemStack off = entity.getOffhandItem();

		boolean mainBlock = main.getUseAnimation() == UseAnim.BLOCK;
		boolean offBlock = off.getUseAnimation() == UseAnim.BLOCK;
		if (mainBlock != offBlock) {
			return mainBlock ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		}

		InteractionHand declared = entity.getUsedItemHand();

		if (entity.isUsingItem()) {
			ItemStack using = entity.getUseItem();
			if (!using.isEmpty()) {
				if (entity.getItemInHand(declared) != using) {
					InteractionHand other = declared == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
					if (entity.getItemInHand(other) == using) {
						return other;
					}
				}
				return declared;
			}
		}

		if (main.isEmpty() && !off.isEmpty()) {
			return InteractionHand.OFF_HAND;
		}

		return declared;
	}

	@Override
	public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
		return List.of(this.original, this.mirror);
	}

	@Override
	public boolean isMetaAnimation() {
		return true;
	}

	@Override
	public boolean isClientAnimation() {
		return true;
	}

	@Override @ClientOnly
	public Layer.Priority getPriority() {
		return this.original.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.LOWEST);
	}

	@Override @ClientOnly
	public Layer.LayerType getLayerType() {
		return this.original.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER);
	}

	private DirectStaticAnimation checkHandAndReturnAnimation(InteractionHand hand) {
		if (hand == InteractionHand.OFF_HAND) {
			return this.mirror;
		}

		return this.original;
	}
}
