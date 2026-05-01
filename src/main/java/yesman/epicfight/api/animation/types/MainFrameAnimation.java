package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.EntityState.StateFactor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.animation.StartActionEvent;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.datastructure.ParameterizedHashMap;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class MainFrameAnimation extends StaticAnimation {
    public MainFrameAnimation(float convertTime, AnimationAccessor<? extends MainFrameAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(convertTime, false, accessor, armature);
    }

    public MainFrameAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature) {
        super(convertTime, false, path, armature);
    }

    @Override
    public void begin(LivingEntityPatch<?> entitypatch) {
        if (entitypatch.getAnimator().getPlayerFor(null).getAnimation().get() == this) {
            ParameterizedHashMap<StateFactor<?>> stateMap = this.stateSpectrum.getStateMap(entitypatch, 0.0F);
            ParameterizedHashMap<StateFactor<?>> modifiedStateMap = new ParameterizedHashMap<> ();
            stateMap.forEach((k, v) -> modifiedStateMap.put(k, this.getModifiedLinkState(k, v, entitypatch, 0.0F)));
            entitypatch.updateEntityState(new EntityState(modifiedStateMap));
        }

        if (entitypatch.isLogicalClient()) {
            entitypatch.updateMotion(false);

            this.getProperty(StaticAnimationProperty.RESET_LIVING_MOTION).ifPresentOrElse(livingMotion -> {
                entitypatch.getClientAnimator().forceResetBeforeAction(livingMotion, livingMotion);
            }, () -> {
                entitypatch.getClientAnimator().resetMotion(true);
                entitypatch.getClientAnimator().resetCompositeMotion();
            });

            entitypatch.getClientAnimator().getPlayerFor(this.getAccessor()).setReversed(false);
        }

        super.begin(entitypatch);

        StartActionEvent startActionEvent = new StartActionEvent(entitypatch, this.getAccessor());
        EpicFightEventHooks.Animation.START_ACTION.postWithListener(startActionEvent, entitypatch.getEventListener());

        if (startActionEvent.shouldResetActionTick() && entitypatch instanceof PlayerPatch<?> playerPatch) {
            playerPatch.resetActionTick();
        }
    }

    @Override
    public void tick(LivingEntityPatch<?> entitypatch) {
        super.tick(entitypatch);

        if (entitypatch.getEntityState().movementLocked()) {
            entitypatch.getOriginal().walkAnimation.setSpeed(0);
        }
    }

    @Override
    public boolean isMainFrameAnimation() {
        return true;
    }

    @Override @ClientOnly
    public Layer.Priority getPriority() {
        return this.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.HIGHEST);
    }
}