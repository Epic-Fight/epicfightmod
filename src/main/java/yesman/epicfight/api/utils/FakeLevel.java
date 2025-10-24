package yesman.epicfight.api.utils;

import java.util.HashMap;
import java.util.Map;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FakeLevel extends ClientLevel {
	private static FakeLevel instance;
	private static final Map<GameProfile, FakeClientPlayer> FAKE_PLAYERS = new HashMap<> ();
	
	public static FakeLevel getFakeLevel(RegistryAccess registryAccess) {
		if (instance == null || instance.registryAccess() != registryAccess) {
			instance = new FakeLevel(registryAccess, Minecraft.getInstance());
		}
		
		return instance;
	}
	
	public static void unloadFakeLevel() {
		instance = null;
	}
	
	public static FakeClientPlayer getFakePlayer(GameProfile playerprofile) {
		return FAKE_PLAYERS.computeIfAbsent(playerprofile, key -> new FakeClientPlayer(instance, key));
	}
	
	public FakeLevel(RegistryAccess registryAccess, Minecraft minecraft) {
		super(
			new FakeClientPacketListener(minecraft, registryAccess),
			new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false),
			Level.OVERWORLD,
			registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
			0,
			0,
			minecraft::getProfiler,
			minecraft.levelRenderer,
			true,
			0
		);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class FakeClientPlayer extends AbstractClientPlayer {
		public FakeClientPlayer(FakeLevel fakeLevel, GameProfile gameProfile) {
			super(fakeLevel, gameProfile);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class FakeClientPacketListener extends ClientPacketListener {
		private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);
		private RegistryAccess registryAccess;
		
		public FakeClientPacketListener(Minecraft minecraft, RegistryAccess registryAccess) {
			super(minecraft, null, DUMMY_CONNECTION, null, null, null);
			this.registryAccess = registryAccess;
		}
		
		@Override
		public void close() {
		}
		
		@Override
		public RegistryAccess registryAccess() {
			return this.registryAccess;
		}
	}
}
