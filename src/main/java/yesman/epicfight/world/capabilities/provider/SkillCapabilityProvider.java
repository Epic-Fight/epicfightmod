package yesman.epicfight.world.capabilities.provider;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

public class SkillCapabilityProvider implements ICapabilityProvider, NonNullSupplier<CapabilitySkill>, ICapabilitySerializable<CompoundTag> {
	private final CapabilitySkill capability;
	private final LazyOptional<CapabilitySkill> optional = LazyOptional.of(this);
	
	public SkillCapabilityProvider(PlayerPatch<?> player) {
		this.capability = new CapabilitySkill(player);
	}
	
	@Override
	public CapabilitySkill get() {
		return this.capability;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == EpicFightCapabilities.CAPABILITY_SKILL ? this.optional.cast() :  LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return this.capability.serialize();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.capability.deserialize(nbt);
	}
}