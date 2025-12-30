package yesman.epicfight.registry.entries;

import com.google.common.collect.ImmutableSet;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.level.block.entity.FractureBlockEntity;
import yesman.epicfight.world.level.block.entity.UniversalBlockEntityType;

public final class EpicFightBlockEntities {
	private EpicFightBlockEntities() {}
	
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FractureBlockEntity>> FRACTURE =
		REGISTRY.register(
			  "fracture_block"
			, () ->
				new UniversalBlockEntityType<>(
					  FractureBlockEntity::new
					, ImmutableSet.of(EpicFightBlocks.FRACTURE.get())
					, Util.fetchChoiceType(References.BLOCK_ENTITY, "fracture_block")
				)
		);
}