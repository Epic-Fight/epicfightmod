package yesman.epicfight.network.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record BiDirectionalSyncEmoteSlots(int playerId, CompoundTag compoundTag) implements ManagedCustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, BiDirectionalSyncEmoteSlots> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT,
            BiDirectionalSyncEmoteSlots::playerId,
            ByteBufCodecs.COMPOUND_TAG,
            BiDirectionalSyncEmoteSlots::compoundTag,
            BiDirectionalSyncEmoteSlots::new
        );

    public BiDirectionalSyncEmoteSlots(PlayerPatch<?> playerpatch) {
        this(playerpatch.getId(), playerpatch.getEmoteSlots().getSerialized());
    }
}
