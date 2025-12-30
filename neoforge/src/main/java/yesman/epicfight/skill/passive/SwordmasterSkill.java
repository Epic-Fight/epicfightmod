package yesman.epicfight.skill.passive;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class SwordmasterSkill extends PassiveSkill {
    public static class Builder extends SkillBuilder<SwordmasterSkill.Builder> {
        protected final Set<WeaponCategory> availableWeaponCategories = Sets.newHashSet();

        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
        }

        public Builder addAvailableWeaponCategory(WeaponCategory... wc) {
            this.availableWeaponCategories.addAll(Arrays.asList(wc));
            return this;
        }
    }

    public static SwordmasterSkill.Builder createSwordMasterBuilder() {
        return new SwordmasterSkill.Builder(SwordmasterSkill::new)
            .addAvailableWeaponCategory(WeaponCategories.UCHIGATANA, WeaponCategories.LONGSWORD, WeaponCategories.SWORD, WeaponCategories.TACHI)
            .setCategory(SkillCategories.PASSIVE)
            .setResource(Resource.NONE);
    }

    private float speedBonus;
    private final Set<WeaponCategory> availableWeaponCategories;

    public SwordmasterSkill(SwordmasterSkill.Builder builder) {
        super(builder);

        this.availableWeaponCategories = ImmutableSet.copyOf(builder.availableWeaponCategories);
    }

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        this.speedBonus = parameters.getFloat("speed_bonus");
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        container.runOnServer(playerpatch -> {
            playerpatch.getEntityDecorations().addSwingSoundModifier(this, (soundEvent, capabilityItem) -> (SwordmasterSkill.this.availableWeaponCategories.contains(capabilityItem.getWeaponCategory()) && soundEvent == EpicFightSounds.WHOOSH.get()) ? EpicFightSounds.SWORDMASTER_SWING.get() : soundEvent);
        });

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.MODIFY_ATTACK_SPEED,
            event -> {
                WeaponCategory heldWeaponCategory = event.getItemCapability().getWeaponCategory();

                if (this.availableWeaponCategories.contains(heldWeaponCategory)) {
                    float attackSpeed = event.getAttackSpeed();
                    event.setAttackSpeed(attackSpeed * (1.0F + this.speedBonus * 0.01F));
                }
            },
            this
        );
    }

    @Override @ClientOnly
    public void onInitiateClient(SkillContainer container) {
        container.getExecutor().getEntityDecorations().addTrailInfoModifier(this, (capabilityItem, trailInfo) -> {
            if (SwordmasterSkill.this.getAvailableWeaponCategories().contains(trailInfo.getWeaponCategory())) {
                TrailInfo.Builder builder = capabilityItem.unpackAsBuilder();
                builder.lifetime(capabilityItem.trailLifetime() + 2);
                builder.blockLight(capabilityItem.blockLight() + 10);
                if (capabilityItem.texturePath().equals(TrailInfo.GENERIC_TRAIL_TEXTURE)) builder.texture(TrailInfo.SWORDMASTER_SWING_TRAIL_TEX);

                return builder.create();
            }

            return capabilityItem;
        });
    }

    @Override @ClientOnly
    public List<Object> getTooltipArgsOfScreen(List<Object> list) {
        list.add(String.format("%.0f", this.speedBonus));
        StringBuilder sb = new StringBuilder();
        int i = 0;

        for (WeaponCategory weaponCategory : this.availableWeaponCategories) {
            sb.append(WeaponCategory.ENUM_MANAGER.toTranslated(weaponCategory));
            if (i < this.availableWeaponCategories.size() - 1) sb.append(", ");
            i++;
        }

        list.add(sb.toString());

        return list;
    }

    @Override
    public Set<WeaponCategory> getAvailableWeaponCategories() {
        return this.availableWeaponCategories;
    }
}