package yesman.epicfight.client.gui.widgets.common;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/// A utility interface to ease the data binding process when initializing,
/// and the widget state is changed, which is generally fragmented in minecraft's
/// widget classes in 1.20.1
public interface DataBoundWidget<T> {
    /// Returns the provider of original data that the widget holds
    /// Note: the returned object could throw an exception under specific conditions
    ///
    /// e.g. When tires to get value from [CompoundTag] with certain key, but
    /// the key does not exist in the compound
    ///
    /// It may require some exception handling code to properly use this method
    @NotNull
    Callable<T> getDataProvider();

    /// Called when the state of the widget is changed by user input.
    /// Typically used to modify the bound data
    @NotNull
    Consumer<T> valueChangeCallback();

    /// Returns the setter of the widget to change the state
    @NotNull
    Consumer<T> valueSetter();

    /// Returns the original data
    default T getDataValue() throws Exception {
        return this.getDataProvider().call();
    }

    /// Reset to the empty value
    /// Note: This method doesn't set the widget state to the value provided by {@link #getDataProvider()}
    /// but to mostly each data type's default value. (boolean = false, int = 0, double = 0.0)
    ///
    /// Example usage: when adding a new entry in datapack editor screen
    void reset();

    /// For the common data binding implementation, you can copy and paste this code
    /// at the bottom of the class

    /// *****************************************************************
    /// [DataBoundWidget] implementations                               *
    /// *****************************************************************
    /*
    private final Callable<T> dataProvider;
    private final Consumer<T> onValueChanged;

    @Override
    public Callable<T> getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public Consumer<T> valueChangeCallback() {
        return this.onValueChanged;
    }

    @Override @NotNull
    public Consumer<T> valueSetter() {
        /// TODO return a setter of the widget
        /// return ComboBox.this::setValue; See the example here: {@link ComboBox#setValue}
    }

    @Override
    public void reset() {
        /// TODO assign the default data type
        /// e.g.
        /// this.value = 0.0D; (for double type)
        /// this.value = 0; (for integer type)
        /// this.value = false; (for boolean type)
    }
    */
}
