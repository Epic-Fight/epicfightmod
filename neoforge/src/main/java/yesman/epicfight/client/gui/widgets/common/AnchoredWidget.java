package yesman.epicfight.client.gui.widgets.common;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;


/// A utility interface to ease the re-positioning widgets in proper place based on Anchor system.
///
/// Note: This interface supposes implementing any class that inherits [AbstractWidget] or [AbstractSelectionList].
public interface AnchoredWidget {
    /// Gets the first position parameter for the horizontal
    int getX1();

    /// Gets the second position parameter for the horizontal
    int getX2();

    /// Gets the first position parameter for the vertical
    int getY1();

    /// Gets the second position parameter for the vertical
    int getY2();

    /// Sets the first position parameter for the horizontal
    void setX1(int i);

    /// Sets the second position parameter for the horizontal
    void setX2(int i);

    /// Sets the second position parameter for the vertical
    void setY1(int i);

    /// Sets the first position parameter for the horizontal
    void setY2(int i);

    /// Gets the anchor type for the horizontal
    HorizontalAnchorType getHorizontalAnchorType();

    /// Gets the anchor type for the vertical
    VerticalAnchorType getVerticalAnchorType();

    /// Relocate the widget with the given anchor types
    default void relocate(ScreenRectangle screenRectangle) {
        if (this instanceof AbstractWidget abstractWidget) {
            int x = this.getHorizontalAnchorType().positioner().startsAt(screenRectangle, this);
            int width = this.getHorizontalAnchorType().scaler().withSize(screenRectangle, this);
            int y = this.getVerticalAnchorType().positioner().startsAt(screenRectangle, this);
            int height = this.getVerticalAnchorType().scaler().withSize(screenRectangle, this);
            abstractWidget.setX(x);
            abstractWidget.setY(y);
            abstractWidget.setWidth(width);
            abstractWidget.setHeight(height);
        } else {
            throw new IllegalStateException("AnchoredWidget must implement either AbstractWidget or AbstractSelectionList");
        }
    }

    enum HorizontalAnchorType {
        
        /// Anchors the widget on the left side with a spacing by {@link #getX1()}, and has a width by {@link #getX2()} toward the right
        LEFT_WIDTH((rect, widget) -> rect.left() + widget.getX1(), (rect, widget) -> widget.getX2()),

        /// Anchors the widget on the right side with a spacing by {@link #getX1()}, and has a width by {@link #getX2()} toward the left
        RIGHT_WIDTH((rect, widget) -> rect.right() - (widget.getX1() + widget.getX2()), (rect, widget) -> widget.getX2()),

        /// Anchors the widget in the center with a spacing by {@link #getX1()} (positive to right, negative to left), and has a width by {@link #getX2()} toward both left and right
        CENTER_WIDTH((rect, widget) -> rect.getCenterInAxis(ScreenAxis.HORIZONTAL) + widget.getX1() - widget.getX2() / 2, (rect, widget) -> widget.getX2()),

        /// Anchors the widget on the left and right side with each {@link #getX1()} {@link #getX2()} spacing, which has a width by {@link #getX2()} - {@link #getX1()}
        LEFT_RIGHT((rect, widget) -> rect.left() + widget.getX1(), (rect, widget) -> (rect.right() - widget.getX2()) - (rect.left() + widget.getX1()));

        final Positioner positioner;
        final Scaler scaler;

        HorizontalAnchorType(Positioner positioner, Scaler scaler) {
            this.positioner = positioner;
            this.scaler = scaler;
        }

        public Positioner positioner() {
            return this.positioner;
        }

        public Scaler scaler() {
            return this.scaler;
        }
    }

    enum VerticalAnchorType {
        /// Anchors the widget on the top side with a spacing by {@link #getY1()}, and has a height by {@link #getY2()} toward the bottom
        TOP_HEIGHT((rect, widget) -> rect.top() + widget.getY1(), (rect, widget) -> widget.getY2()),

        /// Anchors the widget on the bottom side with a spacing by {@link #getY1()}, and has a height by {@link #getY2()} toward the top
        BOTTOM_HEGIHT((rect, widget) -> rect.bottom() - (widget.getY1() + widget.getY2()), (rect, widget) -> widget.getY2()),

        /// Anchors the widget in the center with a spacing by {@link #getY1()} (positive to bottom, negative to top), and has a height by {@link #getY2()} toward both top and bottom
        CENTER_HEIGHT((rect, widget) -> rect.getCenterInAxis(ScreenAxis.VERTICAL) + widget.getY1() - widget.getY2() / 2, (rect, widget) -> widget.getY2()),

        /// Anchors the widget on the top and bottom side with each {@link #getY1()} {@link #getY2()} spacing, which has a height by {@link #getY2()} - {@link #getY1()}
        TOP_BOTTOM((rect, widget) -> rect.top() + widget.getY1(), (rect, widget) -> (rect.bottom() - widget.getY2()) - (rect.top() + widget.getY1()));

        final Positioner positioner;
        final Scaler scaler;

        VerticalAnchorType(Positioner positioner, Scaler scaler) {
            this.positioner = positioner;
            this.scaler = scaler;
        }

        public Positioner positioner() {
            return this.positioner;
        }

        public Scaler scaler() {
            return this.scaler;
        }
    }

    /// Determines the widget's starting position, left for X and top for Y
    @FunctionalInterface
    interface Positioner {
        int startsAt(ScreenRectangle screenRectangle, AnchoredWidget anchoredWidget);
    }

    /// Determines the widget's width and height
    @FunctionalInterface
    interface Scaler {
        int withSize(ScreenRectangle screenRectangle, AnchoredWidget anchoredWidget);
    }

    /// For the common anchor implementation, you can copy and paste this code
    /// at the bottom of the class
    /// ***************************************************************
    /// [AnchoredWidget] implementations                              *
    /// ***************************************************************
    /*
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
    private final AnchoredWidget.VerticalAnchorType verticalAnchorType;

    @Override
    public int getX1() {
        return this.x1;
    }

    @Override
    public int getX2() {
        return this.x2;
    }

    @Override
    public int getY1() {
        return this.y1;
    }

    @Override
    public int getY2() {
        return this.y2;
    }

    @Override
    public void setX1(int i) {
        this.x1 = i;
    }

    @Override
    public void setX2(int i) {
        this.x2 = i;
    }

    @Override
    public void setY1(int i) {
        this.y1 = i;
    }

    @Override
    public void setY2(int i) {
        this.y2 = i;
    }

    @Override
    public HorizontalAnchorType getHorizontalAnchorType() {
        return this.horizontalAnchorType;
    }

    @Override
    public VerticalAnchorType getVerticalAnchorType() {
        return this.verticalAnchorType;
    }
    */
}
