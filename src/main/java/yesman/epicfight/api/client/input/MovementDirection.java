package yesman.epicfight.api.client.input;

/// Represents player movement direction based on input.
///
/// forward, backward, left, right indicate the player's intended movement:
///
/// - `1` if that direction key is pressed (forward or left)
/// - `-1` if the opposite key is pressed (backward or right)
/// - `0` if neither key is pressed
///
/// Example uses:
/// - **Dodge skill:** calculate dash direction from input
/// - **Phantom ascent double jump:** move and rotate player based on input direction
public record MovementDirection(int forward, int backward, int left, int right) {
    public int vertical() {
        return forward + backward;
    }

    public int horizontal() {
        return left + right;
    }

    public static MovementDirection fromBooleans(boolean up, boolean down, boolean left, boolean right) {
        return new MovementDirection(
                up ? 1 : 0,
                down ? -1 : 0,
                left ? 1 : 0,
                right ? -1 : 0
        );
    }

    public static MovementDirection fromInputState(PlayerInputState inputState) {
        return fromBooleans(
                inputState.up(), inputState.down(), inputState.left(), inputState.right()
        );
    }
}
