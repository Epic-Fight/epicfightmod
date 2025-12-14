package yesman.epicfight.gameasset;

import yesman.epicfight.data.conditions.entity.HealthPoint;
import yesman.epicfight.network.server.SPMoveAndPlayAnimation;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.DragonFlyingPhase;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.PatchedPhases;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.IronGolemPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.RavagerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SpiderPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitchPatch;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;

public class MobCombatBehaviors {
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_ONEHAND_TOOLS = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_ONEHAND1).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_ONEHAND2).withinEyeHeight().withinDistance(0.0D, 2.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_DUAL_SWORD = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SWORD_DUAL1).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SWORD_DUAL2).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SWORD_DUAL3).withinEyeHeight().withinDistance(0.0D, 2.5D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false).cooldown(60)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SWORD_DUAL2).randomChance(0.3F).withinEyeHeight().withinDistance(0.0D, 2.5D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false).cooldown(100)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SWORD_DUAL3).withinEyeHeight().withinDistance(0.0D, 3.5D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_FIST = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.ZOMBIE_ATTACK1).withinEyeHeight().withinDistance(0.0D, 1.8D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.ZOMBIE_ATTACK2).withinEyeHeight().withinDistance(0.0D, 1.8D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.ZOMBIE_ATTACK3).withinEyeHeight().withinDistance(0.0D, 1.8D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_GREATSWORD = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.GREATSWORD_AUTO1).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.GREATSWORD_AUTO2).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_GREATSWORD).randomChance(0.3F).withinEyeHeight().withinDistance(0.0D, 3.0D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(50.0F).canBeInterrupted(false).looping(false).cooldown(100)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_GREATSWORD).withinEyeHeight().withinDistance(0.0D, 3.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_KATANA = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_UCHIGATANA1).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_UCHIGATANA2).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_UCHIGATANA3).withinEyeHeight().withinDistance(0.0D, 2.5D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_LONGSWORD = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD1).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD2).withinEyeHeight().withinDistance(0.0D, 2.5D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false).cooldown(60)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD2).randomChance(0.4F).withinEyeHeight().withinDistance(0.0D, 2.5D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_TACHI = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD1).withinEyeHeight().withinDistance(0.0D, 2.5D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD2).withinEyeHeight().withinDistance(0.0D, 2.5D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false).cooldown(60)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_LONGSWORD2).randomChance(0.4F).withinEyeHeight().withinDistance(0.0D, 2.5D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false).cooldown(100)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_TACHI).withinEyeHeight().withinDistance(0.0D, 4.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_SPEAR_ONEHAND = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_ONEHAND).withinEyeHeight().withinDistance(0.0D, 3.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_SPEAR_TWOHAND = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND1).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND2).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND3).withinEyeHeight().withinDistance(0.0D, 3.0D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(50.0F).canBeInterrupted(false).looping(false).cooldown(100)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND3).withinEyeHeight().withinDistance(0.0D, 3.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_ONEHAND_DAGGER = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_ONEHAND1).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_ONEHAND2).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_ONEHAND3).withinEyeHeight().withinDistance(0.0D, 2.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> HUMANOID_TWOHAND_DAGGER = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_TWOHAND1).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_TWOHAND2).withinEyeHeight().withinDistance(2.0D, 5.0D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false).cooldown(80)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_DAGGER_TWOHAND2).withinEyeHeight().withinDistance(2.0D, 5.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> MOUNT_HUMANOID_BEHAVIORS = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.SWORD_MOUNT_ATTACK).withinEyeHeight().withinDistance(0.0D, 1.8D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> DROWNED_TRIDENT = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_SPEAR_ONEHAND).withinEyeHeight().withinDistance(0.0D, 1.8D))
		).newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false).cooldown(60)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.BIPED_MOB_THROW).withinEyeHeight().withinDistance(5.0D, 10.0D))
		);
	
	public static final CombatBehaviors.Builder<WitchPatch> WITCH = CombatBehaviors.<WitchPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<WitchPatch>builder().weight(100.0F).canBeInterrupted(true).looping(false).cooldown(50)
				.nextBehavior(Behavior.<WitchPatch>builder().animationBehavior(Animations.BIPED_MOB_THROW).custom((witchpatch) -> !witchpatch.getOriginal().isDrinkingPotion()).withinEyeHeight().withinDistance(0.0D, 10.0D))
		);
	
	public static final CombatBehaviors.Builder<EnderDragonPatch> ENDER_DRAGON = CombatBehaviors.<EnderDragonPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK1).randomChance(0.1F).withinDistance(0.0D, 7.0D).withinAngle(0.0F, 60.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK3).withinDistance(0.0D, 7.0D))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK2))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK2).withinDistance(0.0D, 5.0D).withinAngle(0.0F, 60.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK3))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK1).randomChance(0.4F).withinDistance(0.0D, 7.0D))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).cooldown(200).simultaneousCooldown(3).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK4).withinDistance(15.0D, 20.0D).withinAngle(0.0F, 40.0F))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(100.0F).cooldown(100).simultaneousCooldown(2).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_BACKJUMP_PREPARE).withinDistance(0.0D, 4.0D).withinAngle(90.0F, 180.0F))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(100.0F).cooldown(240).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_FIREBALL).withinDistance(15.0D, 30.0D).withinAngleHorizontal(0.0F, 10.0F))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(1000.0F).cooldown(0).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().health(0.3F, HealthPoint.Comparator.LESS_RATIO).custom((mobpatch) -> mobpatch.getNearbyCrystals() > 0)
				.behavior((mobpatch) -> {
					mobpatch.getOriginal().getPhaseManager().setPhase(PatchedPhases.CRYSTAL_LINK);
				}))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(10.0F).cooldown(1600).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().health(0.5F, HealthPoint.Comparator.LESS_RATIO).custom((mobpatch) -> mobpatch.getNearbyCrystals() > 0)
				.behavior((mobpatch) -> {
					mobpatch.playAnimationSynchronized(Animations.DRAGON_GROUND_TO_FLY, 0.0F);
					mobpatch.getOriginal().getPhaseManager().setPhase(PatchedPhases.FLYING);
					((DragonFlyingPhase)mobpatch.getOriginal().getPhaseManager().getCurrentPhase()).enableAirstrike();
				}))
		);
	
	public static final CombatBehaviors.Builder<EndermanPatch> ENDERMAN = CombatBehaviors.<EndermanPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(40.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_KNEE).withinEyeHeight().withinDistance(0.0D, 1.6D))
		).newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(40.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_KICK_COMBO).withinEyeHeight().withinDistance(0.0D, 2.0D))
		).newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(10.0F).cooldown(100).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_KICK1).randomChance(0.16F).withinEyeHeight().withinDistance(1.6D, 5.0D))
		).newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(10.0F).cooldown(100).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_KICK2).randomChance(0.16F).withinEyeHeight().withinDistance(0.0D, 4.0D))
		);
	
	public static final CombatBehaviors.Builder<EndermanPatch> ENDERMAN_TELEPORT = CombatBehaviors.<EndermanPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_TP_KICK1).randomChance(0.1F).withinEyeHeight().withinDistance(8.0F, 100.0F).packetProvider(SPMoveAndPlayAnimation::new))
		).newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_TP_KICK2).randomChance(0.1F).withinEyeHeight().withinDistance(8.0F, 100.0F).packetProvider(SPMoveAndPlayAnimation::new))
		);
	
	public static final CombatBehaviors.Builder<EndermanPatch> ENDERMAN_RAGE = CombatBehaviors.<EndermanPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<EndermanPatch>builder().weight(10.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<EndermanPatch>builder().animationBehavior(Animations.ENDERMAN_GRASP).withinEyeHeight().withinDistance(0.0D, 2.0D))
		);
	
	public static final CombatBehaviors.Builder<MobPatch<?>> HOGLIN = CombatBehaviors.builder()
		.newBehaviorSeries(
			BehaviorSeries.builder().weight(10.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.builder().animationBehavior(Animations.HOGLIN_ATTACK).withinEyeHeight().withinAngleHorizontal(0.0F, 20.0F).withinDistance(0.0D, 4.0D))
		);
	
	public static final CombatBehaviors.Builder<IronGolemPatch> IRON_GOLEM = CombatBehaviors.<IronGolemPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<IronGolemPatch>builder().weight(10.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.<IronGolemPatch>builder().animationBehavior(Animations.GOLEM_ATTACK2).withinEyeHeight().withinDistance(0.0D, 2.0D))
		).newBehaviorSeries(
			BehaviorSeries.<IronGolemPatch>builder().weight(10.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<IronGolemPatch>builder().animationBehavior(Animations.GOLEM_ATTACK3).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<IronGolemPatch>builder().animationBehavior(Animations.GOLEM_ATTACK4).withinEyeHeight())
				.nextBehavior(Behavior.<IronGolemPatch>builder().animationBehavior(Animations.GOLEM_ATTACK1).randomChance(0.3F).withinEyeHeight().withinDistance(0.0D, 2.0D))
		);
	
	public static final CombatBehaviors.Builder<RavagerPatch> RAVAGER = CombatBehaviors.<RavagerPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<RavagerPatch>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<RavagerPatch>builder().animationBehavior(Animations.RAVAGER_ATTACK1).withinEyeHeight().withinDistance(0.0D, 2.25D))
				.nextBehavior(Behavior.<RavagerPatch>builder().animationBehavior(Animations.RAVAGER_ATTACK2).withinEyeHeight().withinDistance(0.0D, 2.25D))
		).newBehaviorSeries(
			BehaviorSeries.<RavagerPatch>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<RavagerPatch>builder().animationBehavior(Animations.RAVAGER_ATTACK3).randomChance(0.1F).withinEyeHeight().withinDistance(0.0D, 2.4D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> SKELETON_SWORD = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.WITHER_SKELETON_ATTACK1).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.WITHER_SKELETON_ATTACK2).withinEyeHeight().withinDistance(0.0D, 2.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.WITHER_SKELETON_ATTACK3).withinEyeHeight().withinDistance(0.0D, 2.0D))
		);
	
	public static final CombatBehaviors.Builder<SpiderPatch<?>> SPIDER = CombatBehaviors.<SpiderPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<SpiderPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.<SpiderPatch<?>>builder().animationBehavior(Animations.SPIDER_ATTACK).withinEyeHeight().withinDistance(0.0D, 2.0D))
		).newBehaviorSeries(
			BehaviorSeries.<SpiderPatch<?>>builder().weight(30.0F).canBeInterrupted(true).looping(false)
				.nextBehavior(Behavior.<SpiderPatch<?>>builder().animationBehavior(Animations.SPIDER_JUMP_ATTACK).withinEyeHeight().withinDistance(0.0D, 2.5D))
		);
	
	public static final CombatBehaviors.Builder<WitherPatch> WITHER = CombatBehaviors.<WitherPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<WitherPatch>builder().weight(3000.0F)
				.nextBehavior(Behavior.<WitherPatch>builder().emptyBehavior().health(150.0F, HealthPoint.Comparator.GREATER_ABSOLUTE))
		).newBehaviorSeries(
			BehaviorSeries.<WitherPatch>builder().weight(100.0F).cooldown(200).looping(false).canBeInterrupted(false)
				.nextBehavior(Behavior.<WitherPatch>builder().animationBehavior(Animations.WITHER_CHARGE).withinDistance(6.0D, 10.0D).withinAngleHorizontal(0.0D, 30.0D))
		).newBehaviorSeries(
			BehaviorSeries.<WitherPatch>builder().weight(500.0F).cooldown(60).looping(false).canBeInterrupted(false)
				.nextBehavior(Behavior.<WitherPatch>builder().animationBehavior(Animations.WITHER_SWIRL).health(150.0F, HealthPoint.Comparator.LESS_ABSOLUTE).withinDistance(0.0D, 2.5D).withinAngle(0.0D, 60.0D))
		).newBehaviorSeries(
			BehaviorSeries.<WitherPatch>builder().weight(500.0F).cooldown(180).looping(false).canBeInterrupted(false)
				.nextBehavior(Behavior.<WitherPatch>builder().animationBehavior(Animations.WITHER_BACKFLIP).health(150.0F, HealthPoint.Comparator.LESS_ABSOLUTE).withinDistance(0.0D, 2.5D).withinAngle(0.0D, 20.0D))
		).newBehaviorSeries(
			BehaviorSeries.<WitherPatch>builder().weight(50.0F).cooldown(200).looping(false).canBeInterrupted(false)
				.nextBehavior(Behavior.<WitherPatch>builder().animationBehavior(Animations.WITHER_BEAM).withinDistance(3.0D, 20.0D).withinAngleHorizontal(0.0D, 20.0D))
		);
	
	public static final CombatBehaviors.Builder<HumanoidMobPatch<?>> VINDICATOR_ONEHAND = CombatBehaviors.<HumanoidMobPatch<?>>builder()
		.newBehaviorSeries(
			BehaviorSeries.<HumanoidMobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.VINDICATOR_SWING_AXE1).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.VINDICATOR_SWING_AXE2).withinEyeHeight().withinDistance(0.0D, 3.0D))
				.nextBehavior(Behavior.<HumanoidMobPatch<?>>builder().animationBehavior(Animations.VINDICATOR_SWING_AXE3).withinEyeHeight().withinDistance(0.0D, 3.0D))
		);
}