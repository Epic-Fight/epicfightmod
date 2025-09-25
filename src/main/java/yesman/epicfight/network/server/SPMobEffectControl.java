package yesman.epicfight.network.server;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPMobEffectControl(SPMobEffectControl.Action action, Holder<MobEffect> mobEffect, int entityId) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPMobEffectControl> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.enumCodec(SPMobEffectControl.Action.class),
			SPMobEffectControl::action,
			MobEffect.STREAM_CODEC,
			SPMobEffectControl::mobEffect,
			ByteBufCodecs.INT,
			SPMobEffectControl::entityId,
			SPMobEffectControl::new
	    );
	
	public enum Action {
		ACTIVATE, REMOVE;
	}
}