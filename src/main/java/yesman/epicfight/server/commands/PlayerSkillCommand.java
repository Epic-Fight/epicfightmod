package yesman.epicfight.server.commands;

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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPClearSkills;
import yesman.epicfight.network.server.SPRemoveSkillAndLearn;
import yesman.epicfight.server.commands.arguments.SkillArgument;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

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
					.executes((commandContext) -> addSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, commandContext.getArgument("skill", Holder.class)))));
				removeCommandBuilder
					.then(Commands.literal(skillSlot.toString().toLowerCase(Locale.ROOT))
					.executes((commandContext) -> removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, null))
					.then(Commands.argument("skill", SkillArgument.skill())
					.executes((commandContext) -> removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, commandContext.getArgument("skill", Holder.class)))));
			}
		}
		
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skill").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
			.then(Commands.literal("clear").executes((commandContext) -> clearSkill(commandContext.getSource(), ImmutableList.of(commandContext.getSource().getPlayerOrException())))
			.then(Commands.argument("targets", EntityArgument.players()).executes((commandContext) -> clearSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets")))))
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
				playerpatch.getPlayerSkills().clearContainersAndLearnedSkills(true);
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
	
	public static int addSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, @NotNull Holder<Skill> skill) throws CommandSyntaxException {
		int i = 0;
		
		for (ServerPlayer player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
			SkillContainer skillContainer = playerpatch.getPlayerSkills().getSkillContainerFor(slot);
			
			if (skillContainer.setSkill(skill.value())) {
				if (skill.value().getCategory().learnable()) {
					playerpatch.getPlayerSkills().addLearnedSkill(skill.value());
				}
				
				EpicFightNetworkManager.sendToPlayer(skillContainer.createSyncPacketToLocalPlayer(), player);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
				i++;
			}
		}

		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.add.success.single", skill.getRegisteredName(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.add.success.multiple", skill.getRegisteredName(), i)), true);
			}
		} else {
			throw ERROR_ADD_FAILED.create();
		}
		
		return i;
	}
	
	public static int removeSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, @Nullable Holder<Skill> skill) throws CommandSyntaxException {
		int i = 0;
        Holder<Skill> removedSkill = null;

		for (ServerPlayer player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
			
			if (playerpatch != null) {
				if (skill == null) {
					SkillContainer skillContainer = playerpatch.getSkill(slot);

					if (skillContainer.getSkill() != null) {
                        removedSkill = skillContainer.getSkill().holder();
						skillContainer.setSkill(null);
						EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(removedSkill, slot), player);
						EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
						i++;
					}
				} else {
                    SkillContainer skillContainer = playerpatch.getSkill(slot);

                    if (skillContainer.getSkill().equals(skill.value())) {
                        playerpatch.getPlayerSkills().removeLearnedSkill(skill.value());
                        removedSkill = skill;
                        skillContainer.setSkill(null);
                        EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(skill, slot), player);
                        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
                        i++;
                    }
				}
			}
		}
		
		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.remove.success.single", removedSkill.value().getTranslationKey(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess(wrap(Component.translatable("commands.epicfight.skill.remove.success.multiple", skill.getRegisteredName(), i)), true);
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
