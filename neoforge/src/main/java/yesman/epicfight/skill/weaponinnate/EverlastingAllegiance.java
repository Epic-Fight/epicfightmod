package yesman.epicfight.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

import java.util.List;

public class EverlastingAllegiance extends WeaponInnateSkill {
    public static void setThrownTridentEntityId(SkillContainer skillContainer, int entityId) {
        skillContainer.getDataManager().setDataSync(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID, entityId);
    }

    public static int getThrownTridentEntityId(SkillContainer skillContainer) {
        return skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID);
    }

    private AnimationAccessor<? extends StaticAnimation> callingAnimation;

    public EverlastingAllegiance(WeaponInnateSkill.Builder<?> builder) {
        super(builder);

        this.callingAnimation = Animations.EVERLASTING_ALLEGIANCE_CALL;
    }

    @Override
    public boolean checkExecuteCondition(SkillContainer container) {
        return container.getDataManager().getDataValue(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID) >= 0;
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return this.checkExecuteCondition(container);
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        super.executeOnServer(container, arguments);

        if (container.getExecutor().getOriginal().level().getEntity(container.getDataManager().getDataValue(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID)) instanceof ThrownTrident trident) {
            ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(trident, ThrownTridentPatch.class);
            tridentPatch.recalledBySkill();
            container.getExecutor().playAnimationSynchronized(this.callingAnimation, 0.0F);

            this.cancelOnServer(container, arguments);
        }
    }

    @Override @ClientOnly
    public void cancelOnClient(SkillContainer container, CompoundTag arguments) {
        super.cancelOnClient(container, arguments);

        if (container.getExecutor().getOriginal().level().getEntity(container.getDataManager().getDataValue(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID)) instanceof ThrownTrident trident) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(trident, ThrownTridentPatch.class).ifPresent(ThrownTridentPatch::recalledBySkill);
        }
    }

    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);

        int thrownTrident = container.getDataManager().getDataValue(EpicFightSkillDataKeys.THROWN_TRIDENT_ENTITY_ID);

        if (container.isDisabled() && thrownTrident >= 0) {
            container.setDisabled(false);
        } else if (!container.isDisabled() && thrownTrident < 0) {
            container.setDisabled(true);
        }
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Returning Trident:");

        return list;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        return this;
    }
}