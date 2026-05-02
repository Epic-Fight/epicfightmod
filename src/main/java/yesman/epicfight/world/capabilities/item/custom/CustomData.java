package yesman.epicfight.world.capabilities.item.custom;

public record CustomData<T>(T defaultValue) {

    public T get() {
        return defaultValue;
    }

}
