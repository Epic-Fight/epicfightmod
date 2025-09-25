package yesman.epicfight.api.neoevent.playerpatch;

import java.util.function.Consumer;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public abstract class PlayerPatchEvent<T extends PlayerPatch<?>> extends Event {
	protected final T playerpatch;
	
	public PlayerPatchEvent(T playerpatch) {
		this.playerpatch = playerpatch;
	}
	
	public T getPlayerPatch() {
		return this.playerpatch;
	}
	
	public void runOnClient(Consumer<AbstractClientPlayerPatch<?>> task) {
		if (this.playerpatch.isLogicalClient()) {
			task.accept((AbstractClientPlayerPatch<?>)playerpatch);
		}
	}
	
	public void runOnLocalClient(Consumer<LocalPlayerPatch> task) {
		if (this.playerpatch.getOriginal().isLocalPlayer()) {
			task.accept((LocalPlayerPatch)playerpatch);
		}
	}
	
	public void runOnServer(Consumer<ServerPlayerPatch> task) {
		if (!this.playerpatch.isLogicalClient()) {
			task.accept((ServerPlayerPatch)playerpatch);
		}
	}
	
	public static <T extends PlayerPatchEvent<?>> T postAndFireSkillListeners(T event) {
		T postedEvent = NeoForge.EVENT_BUS.post(event);
		
		if (postedEvent instanceof ICancellableEvent canceclable && canceclable.isCanceled()) {
			return postedEvent;
		}
		
		event.playerpatch.getPlayerSkills().fireSkillEvents(EpicFightMod.MODID, postedEvent);
		
		return postedEvent;
	}
}