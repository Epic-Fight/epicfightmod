package yesman.epicfight.skill.dodge;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class StepSkill extends DodgeSkill {
	private static final UUID EVENT_UUID = UUID.fromString("23bd5c76-fe77-11ed-be56-0242ac120002");
	
	public StepSkill(DodgeSkill.Builder builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		container.getExecutor().getEventListener().addEventListener(EventType.COMBO_COUNTER_HANDLE_EVENT, EVENT_UUID, (event) -> {
			if (event.getCausal() == ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION && event.getAnimation().get().in(this.animations)) {
				event.setNextValue(event.getPrevValue());
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecutor().getEventListener().removeListener(EventType.COMBO_COUNTER_HANDLE_EVENT, EVENT_UUID);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Object getExecutionPacket(SkillContainer container, FriendlyByteBuf args) {
		int forward = args.readInt();
		int backward = args.readInt();
		int left = args.readInt();
		int right = args.readInt();
		int vertic = forward + backward;
		int horizon = left + right;
		int degree = vertic == 0 ? 0 : -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon);
		int animation;
		
		if (vertic == 0) {
			if (horizon == 0) {
				animation = 0;
			} else {
				animation = horizon >= 0 ? 2 : 3;
			}
		} else {
			animation = vertic >= 0 ? 0 : 1;
		}
		
		CPSkillRequest packet = new CPSkillRequest(container.getSlot());
		packet.getBuffer().writeInt(animation);
		packet.getBuffer().writeFloat(degree);
		
		return packet;
	}
}
