package yesman.epicfight.server.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerStaminaCommand {
	private static final SimpleCommandExceptionType ERROR_MODIFYING_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.epicfight.stamina.success.failed"));
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("stamina").requires((commandSourceStack) -> commandSourceStack.hasPermission(2));
		
		builder.then(Commands.literal("get").then(Commands.argument("target", EntityArgument.player()).executes((command) -> {
			return getStamina(command.getSource(), EntityArgument.getPlayer(command, "target"));
		})));
		
		for (Operation operation : Operation.values()) {
			builder.then(Commands.literal(operation.name().toLowerCase(Locale.ROOT)).then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
				return setStamina(command, Collections.singleton(command.getSource().getPlayerOrException()), operation, DoubleArgumentType.getDouble(command, "value"));
			})).then(Commands.argument("target", EntityArgument.players()).then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
	            return setStamina(command, EntityArgument.getPlayers(command, "target"), operation, DoubleArgumentType.getDouble(command, "value"));
	        }))));
		}
		
		dispatcher.register(Commands.literal("epicfight").then(builder));
	}
	
	private static int getStamina(CommandSourceStack command, ServerPlayer serverplayer) {
		ServerPlayerPatch serverplayerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
		
		if (serverplayerpatch == null) {
			command.sendFailure(Component.translatable("commands.epicfight.stamina.failed.no_stamina"));
			
			return 0;
		} else {
			double stamina = serverplayerpatch.getStamina();
			
			command.sendSuccess(() -> {
				return Component.translatable("commands.epicfight.stamina.value.get.success", serverplayer.getName(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(stamina));
			}, false);
			
			return (int)stamina;
		}
	}
	
	private static int setStamina(CommandContext<CommandSourceStack> command, Collection<ServerPlayer> players, Operation operation, double value) {
		int i = 0;
		double returnVal = 0.0D;
		
		for (ServerPlayer serverplayer : players) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
			
			if (playerpatch != null) {
				double stamina = operation.func.apply((double)playerpatch.getStamina(), value);
				
				playerpatch.resetActionTick();
				playerpatch.setStamina((float)stamina);
				returnVal = playerpatch.getStamina();
				++i;
			}
		}
		
		if (i == 0) {
			ERROR_MODIFYING_FAILED.create();
		} else {
			if (i == 1) {
				command.getSource().sendSuccess(wrap(Component.translatable("commands.epicfight.stamina.success.self", players.iterator().next().getDisplayName(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(returnVal))), true);
			} else {
				for (ServerPlayer serverplayer : players) {
					if (command.getSource().getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
						serverplayer.sendSystemMessage(Component.translatable("commands.epicfight.stamina.success.other", String.valueOf(i)));
					}
				}
				
				command.getSource().sendSuccess(wrap(Component.translatable("commands.epicfight.stamina.success.other", String.valueOf(i))), true);
			}
		}
		
		return i;
	}
	
	private static enum Operation {
		ADD((value, operand) -> value + operand), SUBTRACT((value, operand) -> value - operand), SET((value, operand) -> operand);
		
		BiFunction<Double, Double, Double> func;
		
		Operation(BiFunction<Double, Double, Double> func) {
			this.func = func;
		}
	}
	
	private static <T> Supplier<T> wrap(T value) {
		return () -> value;
	}
}