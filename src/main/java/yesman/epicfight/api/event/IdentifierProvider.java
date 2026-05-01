package yesman.epicfight.api.event;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;

import java.util.function.Function;

/// A utility interface to specify an owner of a module
///
/// Its main usage is [EntityEventListener] and [EntityDecorations] to attach or
/// detach each module by equipping state of a [Skill]
///
/// @see EntityEventListener usage of this inherits
/// @see EntityDecorations usage of this inherits
/// @see Skill implementation of this interface
public interface IdentifierProvider {
    Function<ResourceLocation, IdentifierProvider> CONSTANT_IDENTIFIER_PROVIDERS = Util.memoize(identifier -> {
        return new IdentifierProvider() {
            final ResourceLocation constIdentifier = identifier;

            @Override
            public ResourceLocation getId() {
                return this.constIdentifier;
            }
        };
    });

    static IdentifierProvider constant(ResourceLocation constantIdentifier) {
        return CONSTANT_IDENTIFIER_PROVIDERS.apply(constantIdentifier);
    }

    static IdentifierProvider constant(String constantIdentifier) {
        return constant(ResourceLocation.parse(constantIdentifier));
    }

    IdentifierProvider PERMANENT_LISTENER = constant("permanent_subscriber");;

    static IdentifierProvider permanent() {
        return PERMANENT_LISTENER;
    }

    ResourceLocation getId();

    default String getStringId() {
        return this.getId().toString();
    }
}
