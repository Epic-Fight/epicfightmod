package yesman.epicfight.api.forgeevent;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

/**
 * An event hook when weapon innate skill is changed to new item
 * You can use {@link LivingEquipmentChangeEvent} if the skill change is not your interest
 */
public class InnateSkillChangeEvent extends Event {
	private final ServerPlayerPatch playerpatch;
	private final ItemStack from;
	private final ItemStack to;
	private final CapabilityItem fromItemCapability;
	private final CapabilityItem toItemCapability;
	private final InteractionHand hand;
	
	public InnateSkillChangeEvent(ServerPlayerPatch playerpatch, ItemStack from, CapabilityItem fromItemCapability, ItemStack to, CapabilityItem toItemCapability, InteractionHand hand) {
		this.playerpatch = playerpatch;
		this.from = from;
		this.to = to;
		this.fromItemCapability = fromItemCapability;
		this.toItemCapability = toItemCapability;
		this.hand = hand;
	}
	
	public ServerPlayerPatch getPlayerPatch() {
		return this.playerpatch;
	}
	
	public ItemStack getFrom() {
		return this.from;
	}
	
	public ItemStack getTo() {
		return this.to;
	}
	
	public CapabilityItem getFromItemCapability() {
		return this.fromItemCapability;
	}
	
	public CapabilityItem getToItemCapability() {
		return this.toItemCapability;
	}
	
	public InteractionHand getHand() {
		return this.hand;
	}
}
