package yesman.epicfight.network;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface EntityPairingPacketType extends ExtensibleEnum {
	ExtensibleEnumManager<EntityPairingPacketType> ENUM_MANAGER = new ExtensibleEnumManager<> ("entity_pairing_packet_type");
	
	@SuppressWarnings("unchecked")
	default <T extends Enum<T>> T toEnum(Class<T> type) {
		return (T)this;
	}
	
	default <T extends Enum<T>> boolean is(Class<T> type) {
		return type.isAssignableFrom(this.getClass());
	}
}
