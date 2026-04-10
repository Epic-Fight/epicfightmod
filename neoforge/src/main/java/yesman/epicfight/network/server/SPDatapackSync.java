package yesman.epicfight.network.server;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPDatapackSync(PacketType packetType, List<CompoundTag> tags) implements ManagedCustomPacketPayload {
    public static final StreamCodec<ByteBuf, SPDatapackSync> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecsExtends.enumCodec(PacketType.class),
                    SPDatapackSync::packetType,
                    ByteBufCodecsExtends.listOf(ByteBufCodecs.COMPOUND_TAG),
                    SPDatapackSync::tags,
                    SPDatapackSync::new
            );

    public SPDatapackSync(PacketType packetType) {
        this(packetType, new ArrayList<>());
    }


    public void addTag(CompoundTag compound) {
        this.tags.add(compound);
    }

    public enum PacketType {
        ARMOR, WEAPON, MOB, SKILL_PARAMS, WEAPON_TYPE, ITEM_KEYWORD, MANDATORY_RESOURCE_PACK_ANIMATION, RESOURCE_PACK_ANIMATION,
        EX_CAP_BUILDER, EX_CAP_MOVESET, EX_CAP_DATA, EX_CAP_CONDITIONAL, EX_CAP_INJECTION,
    }
}