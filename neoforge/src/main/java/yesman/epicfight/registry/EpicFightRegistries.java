package yesman.epicfight.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.callbacks.SkillCallbacks;
import yesman.epicfight.registry.callbacks.SkillDataKeyCallbacks;
import yesman.epicfight.registry.callbacks.SynchedAnimationVariableKeyCallbacks;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.world.entity.data.ExpandedEntityDataAccessor;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public abstract class EpicFightRegistries {
    public static final Registry<Supplier<Condition<?>>> CONDITION = new RegistryBuilder<> (Keys.CONDITION).create();
    public static final Registry<ExpandedEntityDataAccessor<?>> EXPANDED_ENTITY_DATA_ACCESSOR = new RegistryBuilder<> (Keys.EXPANDED_ENTITY_DATA_ACCESSOR).sync(true).create();
    public static final Registry<Skill> SKILL = new RegistryBuilder<> (Keys.SKILL).callback(SkillCallbacks.getSkillCallback()).sync(true).create();
    public static final Registry<SkillDataKey<?>> SKILL_DATA_KEY = new RegistryBuilder<> (Keys.SKILL_DATA_KEY).callback(SkillDataKeyCallbacks.getRegistryCallback()).sync(true).create();
    public static final Registry<SynchedAnimationVariableKey<?>> SYNCHED_ANIMATION_VARIABLE = new RegistryBuilder<> (Keys.SYNCHED_ANIMATION_VARIABLE_KEY).callback(SynchedAnimationVariableKeyCallbacks.getRegistryCallback()).sync(true).create();

    // Deferred Registries
    public static final List<DeferredRegister<?>> DEFERRED_REGISTRIES = List.of(
        EpicFightArmorMaterials.REGISTRY,
        EpicFightAttachmentTypes.REGISTRY,
        EpicFightAttributes.REGISTRY,
        EpicFightBlockEntities.REGISTRY,
        EpicFightBlocks.REGISTRY,
        EpicFightCommandArgumentTypes.REGISTRY,
        EpicFightConditions.REGISTRY,
        EpicFightCreativeTabs.REGISTRY,
        EpicFightDataComponentTypes.REGISTRY,
        EpicFightEntityTypes.REGISTRY,
        EpicFightExpandedEntityDataAccessors.REGISTRY,
        EpicFightGlobalLootModifiers.REGISTRY,
        EpicFightItems.REGISTRY,
        EpicFightLootItemFunctions.REGISTRY,
        EpicFightMobEffects.REGISTRY,
        EpicFightParticles.REGISTRY,
        EpicFightPotions.REGISTRY,
        EpicFightSkillDataKeys.REGISTRY,
        EpicFightSkills.REGISTRY,
        EpicFightSounds.REGISTRY,
        EpicFightSynchedAnimationVariableKeys.REGISTRY
    );

    public interface Keys {
        ResourceKey<Registry<Supplier<Condition<?>>>> CONDITION = key("condition");
        ResourceKey<Registry<ExpandedEntityDataAccessor<?>>> EXPANDED_ENTITY_DATA_ACCESSOR = key("expanded_entity_data_accessor");
        ResourceKey<Registry<SynchedAnimationVariableKey<?>>> SYNCHED_ANIMATION_VARIABLE_KEY = key("synched_animation_variable_key");
        ResourceKey<Registry<Skill>> SKILL = key("skill");
        ResourceKey<Registry<SkillDataKey<?>>> SKILL_DATA_KEY = key("skill_data_key");

        // Data Pack Registries
        ResourceKey<Registry<Emote>> EMOTE = key("emote");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(EpicFightMod.identifier(name));
        }
    }

    @SubscribeEvent
    public static void addNewRegistries(NewRegistryEvent event) {
        event.register(CONDITION);
        event.register(EXPANDED_ENTITY_DATA_ACCESSOR);
        event.register(SKILL);
        event.register(SKILL_DATA_KEY);
        event.register(SYNCHED_ANIMATION_VARIABLE);
    }
}
