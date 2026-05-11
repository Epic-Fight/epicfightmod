package yesman.epicfight.api.animation.types;

import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Self-accessing variant of {@link MirroredAnimation}. Used for inline construction
 * (e.g. inside {@link MirrorAnimation}) where there is no registry slot to hand out
 * an {@link AnimationAccessor}, but a caller still needs an {@link AssetAccessor}
 * pointing at a mirrored clip. Modeled on {@link DirectStaticAnimation}.
 */
public class DirectMirroredAnimation extends MirroredAnimation implements AnimationAccessor<DirectMirroredAnimation> {
    private final ResourceLocation registryName;

    @ApiStatus.Internal
    public DirectMirroredAnimation(float transitionTime, boolean repeatPlay,
                                   ResourceLocation registryName,
                                   AssetAccessor<? extends StaticAnimation> source,
                                   AssetAccessor<? extends Armature> armature) {
        super(transitionTime, repeatPlay, registryName.toString(), source, armature);
        this.registryName = registryName;
        this.accessor = this;
    }

    @Override
    public DirectMirroredAnimation get() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends DynamicAnimation> AnimationAccessor<A> getAccessor() {
        return (AnimationAccessor<A>) this;
    }

    @Override
    public ResourceLocation registryName() {
        return this.registryName;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public int id() {
        return -1;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public boolean inRegistry() {
        return false;
    }

    /** {@link MirrorAnimation} owns this instance as a sub-animation, and {@link
     *  yesman.epicfight.api.animation.types.StaticAnimation#addProperty} propagates the
     *  meta-animation's properties to every sub. The meta-animation's data file (e.g.
     *  {@code data/spyglass.json}) carries the mainhand-style mask -- {@code right_arms_body},
     *  {@code right_arms}, etc. -- which is exactly the wrong mask for a mirrored pose that
     *  lives on the {@code _L} joints. Bypassing the propagated value here means
     *  {@link MirroredAnimation#getJointMaskEntry}'s fallback runs every time and returns the
     *  source's mask reflected through {@link JointMaskEntry#mirror}, so {@code right_arms}
     *  becomes {@code left_arms} and the offhand pose's left-arm joints survive masking. The
     *  separately-registered {@link MirroredAnimation} accessors (offhand equip/unequip
     *  flourishes) author their own data file and aren't affected by this override. */
    @Override
    public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
        return this.getSource().get().getJointMaskEntry(entitypatch, useCurrentMotion).map(JointMaskEntry::mirror);
    }
}
