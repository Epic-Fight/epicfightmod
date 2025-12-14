package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;

public class SPChangeGamerule<Type, Config extends ForgeConfigSpec.ConfigValue<Type>, RuleValue extends GameRules.Value<RuleValue>> {
	private final ConfigurableGameRule<Type, Config, RuleValue> gamerule;
	private final Type value;
	
	public SPChangeGamerule() {
		this.gamerule = null;
		this.value = null;
	}
	
	public SPChangeGamerule(ConfigurableGameRule<Type, Config, RuleValue> gamerule, Type object) {
		this.gamerule = gamerule;
		this.value = object;
	}
	
	public static <Type, Config extends ForgeConfigSpec.ConfigValue<Type>, RuleValue extends GameRules.Value<RuleValue>> SPChangeGamerule<Type, Config, RuleValue> fromBytes(FriendlyByteBuf buf) {
		@SuppressWarnings("unchecked")
		ConfigurableGameRule<Type, Config, RuleValue> gamerule = (ConfigurableGameRule<Type, Config, RuleValue>)EpicFightGameRules.GAME_RULES.get(buf.readUtf());
		Type value = gamerule.getRuleType().bufferCodec().decode(buf);
		
		return new SPChangeGamerule<> (gamerule, value);
	}

	public static <Type, Config extends ForgeConfigSpec.ConfigValue<Type>, RuleValue extends GameRules.Value<RuleValue>> void toBytes(SPChangeGamerule<Type, Config, RuleValue> msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.gamerule.getRuleName());
		msg.gamerule.getRuleType().bufferCodec().encode(msg.value, buf);
	}
	
	public static <Type, Config extends ForgeConfigSpec.ConfigValue<Type>, RuleValue extends GameRules.Value<RuleValue>> void handle(SPChangeGamerule<Type, Config, RuleValue> msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			RuleValue ruleValue = Minecraft.getInstance().level.getGameRules().getRule(msg.gamerule.getRuleKey());
			msg.gamerule.getRuleType().setRule().accept(ruleValue, msg.value);
		});
		
		ctx.get().setPacketHandled(true);
	}
}