package yesman.epicfight.server.commands;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.server.commands.arguments.AnimationArgument;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AnimatorCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("epicfight")
					.then(Commands.literal("animator").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
							      .then(Commands.literal("play")
												.then(Commands.argument("targets", EntityArgument.entities())
														  	  .then(Commands.argument("animation", AnimationArgument.animation())
														  					.executes((commandContext) -> {
														  						return playAnimation( EntityArgument.getEntities(commandContext, "targets")
														  											, AnimationArgument.getAnimation(commandContext, "animation")
														  											, 0.0F
														  											);
														  					})
														  					.then(Commands.argument("transitionTimeModifier", FloatArgumentType.floatArg())
														  								  .executes((commandContext) -> {
														  									  return playAnimation( EntityArgument.getEntities(commandContext, "targets")
														  											  			  , AnimationArgument.getAnimation(commandContext, "animation")
														  											  			  , FloatArgumentType.getFloat(commandContext, "transitionTimeModifier")
														  											  			  );
														  								  })
														  						 )
														  		   )
													   )
							    	   )
							      .then(Commands.literal("soft_pause")
							    		  		.then(Commands.argument("targets", EntityArgument.entities())
							    		  				 	  .then(Commands.argument("paused", BoolArgumentType.bool())
							    		  				 			  		.executes((commandContext) -> {
							    		  				 			  			return softPause( EntityArgument.getEntities(commandContext, "targets")
						  											  			  				, BoolArgumentType.getBool(commandContext, "paused")
							    		  				 			  							);
							    		  				 			  		})
							    		  				 		   )
							    		  			 )
							    	   )
							      .then(Commands.literal("hard_pause")
							    		  		.then(Commands.argument("targets", EntityArgument.entities())
							    		  				 	  .then(Commands.argument("paused", BoolArgumentType.bool())
									    		  				 			.executes((commandContext) -> {
									    		  				 				return hardPause( EntityArgument.getEntities(commandContext, "targets")
								  											  			  		, BoolArgumentType.getBool(commandContext, "paused")
									    		  				 								);
							    		  				 			  		})
							    		  				 		   )
							    		  			 )
							    	   )
						 )
		);
	}
	
	public static int playAnimation(Collection<? extends Entity> targetEntities, AnimationAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		int successEntityNum = 0;
		
		for (Entity entity : targetEntities) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
			
			if (entitypatch != null) {
				successEntityNum++;
				entitypatch.playAnimationSynchronized(animation, transitionTimeModifier);
			}
		}
		
		return successEntityNum;
	}
	
	public static int softPause(Collection<? extends Entity> targetEntities, boolean paused) {
		int successEntityNum = 0;
		
		for (Entity entity : targetEntities) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
			
			if (entitypatch != null) {
				successEntityNum++;
				entitypatch.pauseAnimator(AbstractAnimatorControl.Action.SOFT_PAUSE, paused);
			}
		}
		
		return successEntityNum;
	}
	
	public static int hardPause(Collection<? extends Entity> targetEntities, boolean paused) {
		int successEntityNum = 0;
		
		for (Entity entity : targetEntities) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
			
			if (entitypatch != null) {
				successEntityNum++;
				entitypatch.pauseAnimator(AbstractAnimatorControl.Action.HARD_PAUSE, paused);
			}
		}
		
		return successEntityNum;
	}
}
