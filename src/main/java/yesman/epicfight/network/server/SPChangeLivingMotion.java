package yesman.epicfight.network.server;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPChangeLivingMotion {
	private final int entityId;
	private int count;
	private final boolean setChangesAsDefault;
	private List<LivingMotion> motionList = Lists.newArrayList();
	private List<AssetAccessor<? extends StaticAnimation>> animationList = Lists.newArrayList();
	
	public SPChangeLivingMotion() {
		this(-1);
	}
	
	public SPChangeLivingMotion(int entityId) {
		this(entityId, 0, false);
	}
	
	public SPChangeLivingMotion(int entityId, boolean setChangesAsDefault) {
		this(entityId, 0, setChangesAsDefault);
	}
	
	private SPChangeLivingMotion(int entityId, int count, boolean setChangesAsDefault) {
		this.entityId = entityId;
		this.count = count;
		this.setChangesAsDefault = setChangesAsDefault;
	}
	
	public SPChangeLivingMotion putPair(LivingMotion motion, AssetAccessor<? extends StaticAnimation> animation) {
		if (animation != null) {
			this.motionList.add(motion);
			this.animationList.add(animation);
			this.count++;
		}
		
		return this;
	}
	
	public void putEntries(Set<Map.Entry<LivingMotion, AssetAccessor<? extends StaticAnimation>>> motionSet) {
		motionSet.forEach((entry) -> {
			if (entry.getValue() != null) {
				this.motionList.add(entry.getKey());
				this.animationList.add(entry.getValue());
				this.count++;
			}
		});
	}
	
	public static SPChangeLivingMotion fromBytes(FriendlyByteBuf buf) {
		SPChangeLivingMotion msg = new SPChangeLivingMotion(buf.readInt(), buf.readInt(), buf.readBoolean());
		List<LivingMotion> motionList = Lists.newArrayList();
		List<AssetAccessor<? extends StaticAnimation>> animationList = Lists.newArrayList();
		
		for (int i = 0; i < msg.count; i++) {
			motionList.add(LivingMotion.ENUM_MANAGER.getOrThrow(buf.readInt()));
		}
		
		for (int i = 0; i < msg.count; i++) {
			try {
				animationList.add(AnimationManager.byId(buf.readInt()));
			} catch (NoSuchElementException e) {
				e.printStackTrace();
				animationList.add(Animations.EMPTY_ANIMATION);
			}
		}
		
		msg.motionList = motionList;
		msg.animationList = animationList;
		
		return msg;
	}
	
	public static void toBytes(SPChangeLivingMotion msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.count);
		buf.writeBoolean(msg.setChangesAsDefault);
		
		for (LivingMotion motion : msg.motionList) {
			buf.writeInt(motion.universalOrdinal());
		}
		
		for (AssetAccessor<? extends StaticAnimation> anim : msg.animationList) {
			buf.writeInt(anim.get().getId());
		}
	}
	
	public static void handle(SPChangeLivingMotion msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level().getEntity(msg.entityId);
			
			if (entity != null) {
				if (entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof LivingEntityPatch<?> entitypatch) {
					ClientAnimator animator = entitypatch.getClientAnimator();
					animator.resetLivingAnimations();
					animator.offAllLayers();
					animator.resetMotion(false);
					animator.resetCompositeMotion();
					
					for (int i = 0; i < msg.count; i++) {
						entitypatch.getClientAnimator().addLivingAnimation(msg.motionList.get(i), msg.animationList.get(i));
					}
					
					if (msg.setChangesAsDefault) {
						animator.setCurrentMotionsAsDefault();
					}
				}
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}