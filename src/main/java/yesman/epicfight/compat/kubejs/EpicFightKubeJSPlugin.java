package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.kubejs.skill.CustomSkill;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class EpicFightKubeJSPlugin implements KubeJSPlugin {
    @Override
	public void registerBuilderTypes(BuilderTypeRegistry registry) {
    	registry.addDefault(EpicFightRegistries.Keys.SKILL, CustomSkill.CustomSkillBuilder.class, CustomSkill.CustomSkillBuilder::new);
    	
    	registry.of(EpicFightRegistries.Keys.SKILL, reg -> {
    		reg.add("basic", CustomSkill.CustomSkillBuilder.class, CustomSkill.CustomSkillBuilder::new);
    		reg.add("passive", CustomSkill.CustomSkillBuilder.class, CustomSkill.CustomSkillBuilder::new);
    		reg.add("chargeable", CustomSkill.CustomSkillBuilder.class, CustomSkill.CustomSkillBuilder::new);
    	});
    }
    
    @Override
    public void registerBindings(BindingRegistry event) {
        event.add("EpicFightCapabilities", EpicFightCapabilities.class);
        event.add("ServerPlayerPatch", ServerPlayerPatch.class);
        event.add("PlayerPatch", PlayerPatch.class);
        //event.add("EventType", PlayerEventListener.EventType.class);
        event.add("SkillSlots", SkillSlots.class);
        event.add("EpicFightSkillDataKeys", EpicFightSkillDataKeys.class);

        event.add("EFUtils", EFUtilsJS.class);
        
        if (event.type() == ScriptType.CLIENT && FMLEnvironment.dist.isClient()) {
            event.add("ClientEngine", ClientEngine.getInstance());
            event.add("ControlEngine", ControlEngine.getInstance());
            event.add("LocalPlayerPatch", LocalPlayerPatch.class);
        }
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry typeWrappers) {
        typeWrappers.register(Skill.class, o -> {
            if (o instanceof Skill skill) return skill;
            if (o instanceof String) {
                return EpicFightRegistries.SKILL.get(ResourceLocation.parse((String)o));
            }
            if (o instanceof ResourceLocation) {
                return EpicFightRegistries.SKILL.get((ResourceLocation) o);
            }
            if (o instanceof DeferredHolder reg) {
                return (Skill) reg.get();
            }
            if (o instanceof BuilderBase builder) {
                return EpicFightRegistries.SKILL.get(builder.id);
            }
            throw new IllegalArgumentException("Object " + o + " of class " + o.getClass().getName() + " cannot be converted to type yesman.epicfight.skill.Skill");
        });
    }
}