/**
 * Defines the data structures and templates for the Extended Capability (ExCap) system.
 * <p>
 * This package contains the immutable "blueprints" of the combat engine. These classes
 * act as containers for raw animation data, hitboxes, and logic predicates before they
 * are processed into active game behaviors.
 * </p>
 *
 * <h3>Core Components:</h3>
 * <ul>
 * specific configuration builder.</li>
 * <li><b>Templates:</b> The {@link yesman.epicfight.api.ex_cap.data.Moveset} class, which represents a fully
 * baked set of animations and skills ready for runtime execution.</li>
 * <li><b>Rendering:</b> Interfaces like {@link yesman.epicfight.api.ex_cap.data.modifier.RenderModifier} that define how
 * weapons should be visually handled during specific combat states.</li>
 * </ul>
 *
 * <p><b>Safety Note:</b> Objects in this package are designed to be read-only once
 * registered. To modify an existing entry, developers should use the
 * {@code ModifyWeaponPresetEvent} rather than attempting to mutate these objects directly.</p>
 *
 */
package yesman.epicfight.api.ex_cap.data;