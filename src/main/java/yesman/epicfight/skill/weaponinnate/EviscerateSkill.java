package yesman.epicfight.skill.weaponinnate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;

import java.util.List;

public class EviscerateSkill extends WeaponInnateSkill {
    private final AssetAccessor<? extends AttackAnimation> first;
    private final AssetAccessor<? extends AttackAnimation> second;

    private float damageCap;

    public EviscerateSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);

        this.first = Animations.EVISCERATE_FIRST;
        this.second = Animations.EVISCERATE_SECOND;
    }

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        this.damageCap = parameters.getFloat("damage_cap");
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Animation.END,
            event -> {
                if (Animations.EVISCERATE_FIRST.equals(event.getAnimation())) {
                    List<LivingEntity> hurtEntities = container.getExecutor().getCurrentlyActuallyHitEntities();

                    if (!hurtEntities.isEmpty() && hurtEntities.getFirst().isAlive()) {
                        container.getExecutor().reserveAnimation(this.second);
                        container.getExecutor().getServerAnimator().getPlayerFor(null).reset();
                        container.getExecutor().getCurrentlyActuallyHitEntities().clear();
                    }
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
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "First Strike:");
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Second Strike:");
        return list;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        this.first.get().phases[0].addProperties(this.properties.get(0).entrySet());
        this.second.get().phases[0].addProperties(this.properties.get(1).entrySet());
        return this;
    }

    /// TODO: bad implementation but to avoid breaking changes
    /// make [ExtraDamageInstance] configurable via skill parameters
    public float getDamageCap() {
        return this.damageCap;
    }
}