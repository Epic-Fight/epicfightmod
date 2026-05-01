package yesman.epicfight.api.client.event.types.camera;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ItemUsedInDecoupledCamera extends CameraAPIEvent {
    private Player player;
    private PlayerPatch<?> playerpatch;
    private ItemStack itemstack;
    private InteractionHand hand;

    public ItemUsedInDecoupledCamera(EpicFightCameraAPI cameraApi, Player player, PlayerPatch<?> playerpatch, ItemStack itemstack, InteractionHand hand) {
        super(cameraApi);
        this.player = player;
        this.playerpatch = playerpatch;
        this.itemstack = itemstack;
        this.hand = hand;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerPatch<?> getPlayerPatch() {
        return this.playerpatch;
    }

    public ItemStack getItemStack() {
        return this.itemstack;
    }

    public InteractionHand getInteractionHand() {
        return this.hand;
    }
}
