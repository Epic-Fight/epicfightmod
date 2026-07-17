package yesman.epicfight.data.conditions.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.List;

public class PlayerName extends EntityPatchCondition {
	private String name;
	
	@Override
	public PlayerName read(CompoundTag tag) {
		this.name = this.assertTag("identifier", "string", tag, StringTag.class, CompoundTag::getString);
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("identifier", this.name);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		if (target instanceof PlayerPatch<?> playerpatch) {
			return playerpatch.getOriginal().getName().getString().equals(this.name);
		}
		
		return false;
	}
}