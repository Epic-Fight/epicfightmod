package yesman.epicfight.world.level.block.entity;

import com.google.common.collect.ImmutableSet;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.level.block.EpicFightBlocks;

public class EpicFightBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EpicFightMod.MODID);
	
	public static final RegistryObject<BlockEntityType<FractureBlockEntity>> FRACTURE = BLOCK_ENTITIES.register("fracture_block", () -> 
		new UniversalBlockEntityType<FractureBlockEntity>(FractureBlockEntity::new, ImmutableSet.of(EpicFightBlocks.FRACTURE.get()), Util.fetchChoiceType(References.BLOCK_ENTITY, "fracture_block")));
}