package yesman.epicfight.server.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;

public class SkillArgument implements ArgumentType<Skill> {
	private static final Collection<String> EXAMPLES = Arrays.asList("epicfight:dodge");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_SKILL = new DynamicCommandExceptionType((obj) -> {
		return Component.translatable("epicfight.skillNotFound", obj);
	});
	
	public static SkillArgument skill() {
		return new SkillArgument();
	}
	
	public static Skill getSkill(CommandContext<CommandSourceStack> commandContext, String name) {
		return commandContext.getArgument(name, Skill.class);
	}
	
	public Skill parse(StringReader p_98428_) throws CommandSyntaxException {
		ResourceLocation resourcelocation = ResourceLocation.read(p_98428_);
		Skill skill = SkillManager.getSkill(resourcelocation.toString());
		
		if (skill != null && !skill.getCategory().learnable()) {
			skill = null;
		}
		
		return Optional.ofNullable(skill).orElseThrow(() -> {
			return ERROR_UNKNOWN_SKILL.create(resourcelocation);
		});
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		final SkillCategory skillCategory = (commandContext.getNodes().size() > 5 && commandContext.getNodes().get(4).getNode() instanceof LiteralCommandNode<?> literalNode) ? nullParam(SkillSlot.ENUM_MANAGER.getOrThrow(literalNode.getLiteral())) : null;
		return SharedSuggestionProvider.suggestResource(SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && (skillCategory == null || skill.getCategory().equals(skillCategory))), suggestionsBuilder);
	}
	
	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
	private static SkillCategory nullParam(SkillSlot slot) {
		return slot == null ? null : slot.category();
	}
}