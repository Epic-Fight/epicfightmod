package yesman.epicfight.network;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public interface EntityPairingPacketType extends ExtendableEnum {
	ExtendableEnumManager<EntityPairingPacketType> ENUM_MANAGER = new ExtendableEnumManager<> ("entity_pairing_packet_type");
	
	@SuppressWarnings("unchecked")
	default <T extends Enum<T>> T toEnum(Class<T> type) {
		return (T)this;
	}
	
	default <T extends Enum<T>> boolean is(Class<T> type) {
		return type.isAssignableFrom(this.getClass());
	}
}
