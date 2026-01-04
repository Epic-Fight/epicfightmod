package yesman.epicfight.world.capabilities.emote;

import net.minecraft.resources.ResourceKey;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.main.EpicFightNeoForge;
import yesman.epicfight.registry.EpicFightRegistries;

public interface BuiltInEmotes {
    ResourceKey<Emote> FRUSTRATED = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("frustrated"));
    ResourceKey<Emote> HOPAK = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("hopak"));
    ResourceKey<Emote> LAUGH = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("laugh"));
    ResourceKey<Emote> SALUTE = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("salute"));
    ResourceKey<Emote> SLIT_THROAT = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("slit_throat"));
    ResourceKey<Emote> WAVE_HAND = ResourceKey.create(EpicFightRegistries.Keys.EMOTE, EpicFightNeoForge.identifier("wave_hand"));
}
