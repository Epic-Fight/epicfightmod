package yesman.epicfight.world.level.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EpicFightMod.MODID);
	
	public static final RegistryObject<FractureBlock> FRACTURE = BLOCKS.register("fracture_block", () -> new FractureBlock(BlockBehaviour.Properties.of()));
}