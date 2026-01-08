package yesman.epicfight.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

public class GraspingSpireSkill extends WeaponInnateSkill {
    private AnimationAccessor<? extends AttackAnimation> first;
    private AnimationAccessor<? extends AttackAnimation> second;

    public GraspingSpireSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);

        this.first = Animations.GRASPING_SPIRAL_FIRST;
        this.second = Animations.GRASPING_SPIRAL_SECOND;
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Animation.END,
            event -> {
                if (this.first.equals(event.getAnimation())) {
                    skillContainer.getDataManager().setDataSync(EpicFightSkillDataKeys.LAST_HIT_COUNT, skillContainer.getExecutor().getCurrentlyActuallyHitEntities().size());
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,
            event -> {
                if (this.second.equals(event.getDamageSource().getAnimation())) {
                    event.getDamageSource().attachImpactModifier(ValueModifier.adder(skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.LAST_HIT_COUNT) * 0.4F));
                }
            },
            this
        );
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        container.getExecutor().playAnimationSynchronized(this.first, 0.0F);
        super.executeOnServer(container, arguments);
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);

        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Pierce:");
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Second Strike:");

        return list;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        this.first.get().phases[0].addProperties(this.properties.get(0).entrySet());
        this.second.get().phases[0].addProperties(this.properties.get(1).entrySet());

        return this;
    }
}