package yesman.epicfight.data.tags;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import yesman.epicfight.EpicFight;
import yesman.epicfight.main.EpicFightNeoForge;

public class EpicFightBlockTagsProvider extends BlockTagsProvider {
	public EpicFightBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EpicFight.MODID, existingFileHelper);
    }
	
	@Override
	protected void addTags(Provider provider) {
	}
}
