package yesman.epicfight.api.client.event.types.camera;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

/// Fired when TPS camera validates the player state whether couple to camera or not
/// @see EpicFightCameraAPI#predicateCouplingPlayer
public class CoupleTPSCamera extends CameraAPIEvent {
    private final boolean isMoving;
    private final boolean isPressingVanillaAttackKeybind;
    private final boolean isUsingCouplingItem;
    private final boolean isZooming;
    private final boolean isHoldingSkill;
    private final boolean manualCoupling;

    private boolean shouldCouple;
    private boolean modified = false;

    public CoupleTPSCamera(EpicFightCameraAPI cameraApi, boolean isMoving, boolean isPressingVanillaAttackKeybind, boolean isUsingCouplingItem, boolean isZooming, boolean isHoldingSkill, boolean manualCoupling) {
        super(cameraApi);
        this.isMoving = isMoving;
        this.isPressingVanillaAttackKeybind = isPressingVanillaAttackKeybind;
        this.isUsingCouplingItem = isUsingCouplingItem;
        this.isZooming = isZooming;
        this.isHoldingSkill = isHoldingSkill;
        this.manualCoupling = manualCoupling;
    }

    public boolean isMoving() {
        return this.isMoving;
    }

    public boolean isPressingVanillaAttackKeybind() {
        return this.isPressingVanillaAttackKeybind;
    }

    public boolean isUsingCouplingItem() {
        return this.isUsingCouplingItem;
    }

    public boolean isZooming() {
        return this.isZooming;
    }

    public boolean isHoldingSkill() {
        return this.isHoldingSkill;
    }

    public boolean manualCoupling() {
        return this.manualCoupling;
    }

    public boolean shouldCoupleCamera() {
        return this.modified ? this.shouldCouple : this.isMoving || this.isPressingVanillaAttackKeybind || this.isUsingCouplingItem || this.isZooming || this.isHoldingSkill || this.manualCoupling;
    }

    public boolean isOnlyMoving() {
        return this.isMoving &&
            !this.isPressingVanillaAttackKeybind &&
            !this.isUsingCouplingItem &&
            !this.isZooming &&
            !this.isHoldingSkill &&
            !this.manualCoupling;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setShouldCouple(boolean flag) {
        this.shouldCouple = flag;
        this.modified = true;
    }
}