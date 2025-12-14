package yesman.epicfight.server.commands;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPClearSkills;
import yesman.epicfight.network.server.SPRemoveSkillAndLearn;
import yesman.epicfight.server.commands.arguments.SkillArgument;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerSkillCommand {
	private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.epicfight.skill.add.failed"));
	private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.epicfight.skill.remove.failed"));
	private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.epicfight.skill.clear.failed"));
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		RequiredArgumentBuilder<CommandSourceStack, EntitySelector> addCommandBuilder = Commands.argument("targets", EntityArgument.players());
		RequiredArgumentBuilder<CommandSourceStack, EntitySelector> removeCommandBuilder = Commands.argument("targets", EntityArgument.players());
		
		for (SkillSlot skillSlot : SkillSlot.ENUM_MANAGER.universalValues()) {
			if (skillSlot.category().learnable()) {
				addCommandBuilder
					.then(Commands.literal(skillSlot.toString().toLowerCase(Locale.ROOT))
					.then(Commands.argument("skill", SkillArgument.skill())
					.executes((commandContext) -> {
						return addSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, SkillArgument.getSkill(commandContext, "skill"));
					})));
				
				removeCommandBuilder
					.then(Commands.literal(skillSlot.toString().toLowerCase(Locale.ROOT))
					.executes((commandContext) -> {
						return removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, null);
					})
					.then(Commands.argument("skill", SkillArgument.skill())
					.executes((commandContext) -> {
						return removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, SkillArgument.getSkill(commandContext, "skill"));
					})));
			}
		}
		
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skill").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
			.then(Commands.literal("clear").executes((commandContext) -> {
				return clearSkill(commandContext.getSource(), ImmutableList.of(commandContext.getSource().getPlayerOrException()));
			})
			.then(Commands.argument("targets", EntityArgument.players()).executes((commandContext) -> {
				return clearSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"));
			})))
			.then(Commands.literal("add")
			.then(addCommandBuilder))
			.then(Commands.literal("remove")
			.then(removeCommandBuilder));
		
		dispatcher.register(Commands.literal("epicfight").then(builder));
	}
	
	public static int clearSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets) throws CommandSyntaxException {
		int i = 0;
		
		for (ServerPlayer player : targets) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(playerpatch -> {
				playerpatch.getSkillCapability().clearContainersAndLearnedSkills(true);
				SPClearSkills clearpacket = new SPClearSkills(player.getId());
				
				EpicFightNetworkManager.sendToPlayer(clearpacket, player);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(clearpacket, player);
			});
			
			i++;
		}
		
		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.clear.success.single", targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.clear.success.multiple", i)), true);
			}
		} else {
			throw ERROR_CLEAR_FAILED.create();
		}
		
		return i;
	}
	
	public static int addSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
		int i = 0;
		
		for (ServerPlayer player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
			SkillContainer skillContainer = playerpatch.getSkillCapability().getSkillContainerFor(slot);
			
			if (skillContainer.setSkill(skill)) {
				if (skill.getCategory().learnable()) {
					playerpatch.getSkillCapability().addLearnedSkill(skill);
				}
				
				EpicFightNetworkManager.sendToPlayer(skillContainer.createSyncPacketToLocalPlayer(), player);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
				i++;
			}
		}

		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.add.success.single", skill.getDisplayName(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.add.success.multiple", skill.getDisplayName(), i)), true);
			}
		} else {
			throw ERROR_ADD_FAILED.create();
		}
		
		return i;
	}
	
	public static int removeSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
		int i = 0;
		
		for (ServerPlayer player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
			
			if (playerpatch != null) {
				if (skill == null) {
					SkillContainer skillContainer = playerpatch.getSkill(slot);
					skill = skillContainer.getSkill();
					
					if (skill != null) {
						skillContainer.setSkill(null);
						EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(slot, skill), player);
						EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
						i++;
					}
				} else {
					if (playerpatch.getSkillCapability().removeLearnedSkill(skill)) {
						SkillContainer skillContainer = playerpatch.getSkill(slot);
						
						if (skillContainer.getSkill() == skill) {
							skillContainer.setSkill(null);
							EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(slot, skill), player);
							EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
							i++;
						}
					}
				}
			}
		}
		
		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.remove.success.single", skill.getDisplayName(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.remove.success.multiple", skill.getDisplayName(), i)), true);
			}
		} else {
			throw ERROR_REMOVE_FAILED.create();
		}
		
		return i;
	}

	private static <T> Supplier<T> wrap(T value) {
		return () -> value;
	}
}
