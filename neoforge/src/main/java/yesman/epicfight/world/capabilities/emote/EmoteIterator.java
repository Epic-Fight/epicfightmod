package yesman.epicfight.world.capabilities.emote;

import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.online.cosmetics.Emote;

/// Iterates emote slots by pages
/// emote pages: 1~9
/// emote wheels: 1~6
@FunctionalInterface
public interface EmoteIterator {
    void doWork(int pageIndex, int emoteWheelIndex, @Nullable Holder.Reference<Emote> emote);
}
