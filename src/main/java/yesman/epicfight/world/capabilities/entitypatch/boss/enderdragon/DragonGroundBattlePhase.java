package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

import java.util.List;

public class DragonGroundBattlePhase extends PatchedDragonPhase {
	private final List<Player> recognizedPlayers = Lists.newArrayList();
	private PathFinder pathFinder;
	private int aggroCounter;
	private int noPathWarningCounter;
	CombatBehaviors<EnderDragonPatch> combatBehaviors;
	
	public DragonGroundBattlePhase(EnderDragon dragon) {
		super(dragon);
		
		if (!dragon.level().isClientSide()) {
			this.combatBehaviors = MobCombatBehaviors.ENDER_DRAGON.build(this.dragonpatch);
			NodeEvaluator nodeEvaluator = new WalkNodeEvaluator();
			nodeEvaluator.setCanPassDoors(true);
		    this.pathFinder = new PathFinder(nodeEvaluator, 100);
		}
	}
	
	@Override
	public void begin() {
		this.dragonpatch.setGroundPhase();
	}
	
	@Override
	public void doServerTick() {
		LivingEntity target = this.dragon.getTarget();
		
		if (target != null) {
			if (isValidTarget(target) && isInEndSpikes(target)) {
				EntityState state = this.dragonpatch.getEntityState();
				this.combatBehaviors.tick();
				--this.aggroCounter;
				
				if (this.combatBehaviors.hasActivatedMove()) {
					if (state.canBasicAttack()) {
						CombatBehaviors.Behavior<EnderDragonPatch> result = this.combatBehaviors.tryProceed();
						
						if (result != null) {
							result.execute(this.dragonpatch);
						}
					}
				} else {
					if (!state.inaction()) {
						CombatBehaviors.Behavior<EnderDragonPatch> result = this.combatBehaviors.selectRandomBehaviorSeries();
						
						if (result != null) {
							result.execute(this.dragonpatch);
						} else {
							if (this.dragon.tickCount % 20 == 0) {
								if (!this.checkTargetPath(target)) {
									if (this.noPathWarningCounter++ >= 3) {
										this.fly();
									}
								} else {
									this.noPathWarningCounter = 0;
								}
							}
							
							double dx = target.getX() - this.dragon.getX();
							double dz = target.getZ() - this.dragon.getZ();
							float yRot = 180.0F - (float) Math.toDegrees(Mth.atan2(dx, dz));
							this.dragon.setYRot(MathUtils.rotlerp(this.dragon.getYRot(), yRot, 6.0F));
							Vec3 forward = this.dragon.getForward().scale(-0.25F);
							this.dragon.move(MoverType.SELF, forward);
						}
					} else {
						if (this.aggroCounter < 0) {
							this.aggroCounter = 200;
							this.searchNearestTarget();
						}
					}
				}
			} else {
				if (!this.dragonpatch.getEntityState().inaction()) {
					this.searchNearestTarget();
				}
			}
		} else {
			this.searchNearestTarget();
			
			if (this.dragon.getTarget() == null && !this.dragonpatch.getEntityState().inaction()) {
				this.dragonpatch.playAnimationSynchronized(Animations.DRAGON_GROUND_TO_FLY, 0.0F);
				this.dragon.getPhaseManager().setPhase(PatchedPhases.FLYING);
				((DragonFlyingPhase)this.dragon.getPhaseManager().getCurrentPhase()).enableAirstrike();
			}
		}
	}
	
	@Override
	public float onHurt(DamageSource damagesource, float amount) {
        if (damagesource.getDirectEntity() instanceof AbstractArrow) {
            damagesource.getDirectEntity().igniteForSeconds(1);
            return 0.0F;
        } else {
            return super.onHurt(damagesource, amount);
        }
	}
	
	private void refreshNearbyPlayers(double within) {
		this.recognizedPlayers.clear();
		this.recognizedPlayers.addAll(this.getPlayersNearbyWithin(within));
	}
	
	@SuppressWarnings("deprecation")
	private boolean checkTargetPath(LivingEntity target) {
		BlockPos blockpos = this.dragon.blockPosition();
		
		while (this.dragon.level().getBlockState(blockpos).blocksMotion()) {
			blockpos = blockpos.above();
		}
		
		while (!this.dragon.level().getBlockState(blockpos.below()).blocksMotion()) {
			blockpos = blockpos.below();
		}
		
        int sight = 60;
        PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.dragon.level(), blockpos.offset(-sight, -sight, -sight), blockpos.offset(sight, sight, sight));
        
        Path path = this.pathFinder.findPath(pathnavigationregion, this.dragon, ImmutableSet.of(target.blockPosition()), sight, 0, 1.0F);
        
        BlockPos pathEnd = path.getNode(path.getNodeCount() - 1).asBlockPos();
        BlockPos targetPos = path.getTarget();
        double xd = Math.abs(pathEnd.getX() - targetPos.getX());
        double yd = Math.abs(pathEnd.getY() - targetPos.getY());
        double zd = Math.abs(pathEnd.getZ() - targetPos.getZ());
        
        return xd < this.dragon.getBbWidth() && yd < this.dragon.getBbHeight() && zd < this.dragon.getBbWidth();
	}
	
	private void searchNearestTarget() {
		this.refreshNearbyPlayers(60.0F);
		
		if (!this.recognizedPlayers.isEmpty()) {
			int nearestPlayerIndex = 0;
			double nearestDistance = this.recognizedPlayers.get(0).distanceToSqr(this.dragon);
			
			for (int i = 1; i < this.recognizedPlayers.size(); i++) {
				double distance = this.recognizedPlayers.get(i).distanceToSqr(this.dragon);
				
				if (distance < nearestDistance) {
					nearestPlayerIndex = i;
					nearestDistance = distance;
				}
			}
			
			Player nearestPlayer = this.recognizedPlayers.get(nearestPlayerIndex);
			
			if (isValidTarget(nearestPlayer) && isInEndSpikes(nearestPlayer)) {
				this.dragonpatch.setAttakTargetSync(nearestPlayer);
				return;
			}
		}
		
		this.dragonpatch.setAttakTargetSync(null);
	}
	
	public void fly() {
		this.combatBehaviors.execute(6);
	}
	
	public void resetFlyCooldown() {
		this.combatBehaviors.resetCooldown(6, false);
	}
	
	@Override
	public boolean isSitting() {
		return true;
	}
	
	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return PatchedPhases.GROUND_BATTLE;
	}
}