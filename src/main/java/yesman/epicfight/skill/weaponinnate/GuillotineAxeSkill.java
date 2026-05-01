package yesman.epicfight.skill.weaponinnate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuillotineAxeSkill extends SimpleWeaponInnateSkill {
    public GuillotineAxeSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.DELIVER_DAMAGE_PRE,
            event -> {
                if (event.getDamageSource().getAnimation() == Animations.THE_GUILLOTINE) {
                    ValueModifier.ResultCalculator executionMinHealth = ValueModifier.calculator();
                    getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(executionMinHealth::attach);
                    executionMinHealth.multiply(0.8F);

                    float health = event.getTarget().getHealth();
                    float baseDamage = (float)container.getExecutor().getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float modifiedBaseDamage = container.getExecutor().getModifiedBaseDamage(baseDamage);
                    float executionHealth = executionMinHealth.getResult(modifiedBaseDamage);

                    if (health < executionHealth) {
                        event.getDamageSource().setExecute();
                    }
                }
            },
            this
        );
    }

    @Override @ClientOnly
    public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
        List<Component> list = new ArrayList<> ();
        List<Object> tooltipArgs = new ArrayList<> ();
        String traslatableText = this.getTranslationKey();
        double itemBaseDamage = playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();

        Set<AttributeModifier> attributeModifiers = new HashSet<> ();
        attributeModifiers.addAll(playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getModifiers());
        attributeModifiers.addAll(CapabilityItem.getAttributeModifiersAsWeapon(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, itemstack, playerpatch));

        for (AttributeModifier modifier : attributeModifiers) {
            itemBaseDamage += modifier.amount();
        }

        ValueModifier.ResultCalculator executionMinHealth = ValueModifier.calculator();
        getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(executionMinHealth::attach);
        executionMinHealth.multiply(0.8F);

        tooltipArgs.add(ChatFormatting.RED + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(executionMinHealth.getResult((float)itemBaseDamage)));
        list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
        list.add(Component.translatable(traslatableText + ".tooltip", tooltipArgs.toArray(new Object[0])).withStyle(ChatFormatting.DARK_GRAY));

        this.generateTooltipforPhase(list, itemstack, cap, playerpatch, this.properties.get(0), "Each Strike:");

        return list;
    }
}