/**
 * Provides the core API for Epic Fight's Extensible Capability (ExCap) system in 1.21.1.
 * * <p>The ExCap system has been redesigned to prioritize developer experience (DX)
 * through a <b>Self-Registering Fluent API</b>. This architecture eliminates the
 * need for manual event-based data linking and multi-step registration.</p>
 * <h3>Primary Workflow:</h3>
 * <ul>
 * <li><b>Definition:</b> Use {@code BuilderManager.register()},
 * {@code MovesetManager.register()}, and {@code ConditionalManager.register()}
 * to define and index mod content in a single step.</li>
 * <li><b>Modification:</b> Use the {@code ModifyWeaponPresetEvent} to surgically
 * alter existing weapon definitions from other mods or the base mod.</li>
 * </ul>
 * <h3>Best Practices:</h3>
 * <ul>
 * <li>Avoid direct instantiation of {@code Entry} classes; always use the
 * provided {@code Manager} registration methods.</li>
 * <li>Prefer passing direct object references (e.g., {@code Movesets.SWORD_MS})
 * over ResourceLocations where possible to ensure compile-time safety.</li>
 * </ul>
 * @since 1.21.1
 * @version 2.0
 */
package yesman.epicfight.api.ex_cap;

