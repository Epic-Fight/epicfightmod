/**
 * Provides centralized registry management for the Extended Capability (ExCap) system.
 * <p>
 * This package contains the three primary managers used to index and retrieve
 * weapon data. These managers replace the legacy event-based registration system
 * used in previous versions, facilitating a more direct and type-safe workflow.
 * </p>
 * * <h3>Registry Roles:</h3>
 * <ul>
 * <li><b>{@link yesman.epicfight.api.ex_cap.managers.ItemPresetManager}:</b> Manages weapon capability templates and
 * handles the surgical modification of weapon presets.</li>
 * <li><b>{@link yesman.epicfight.api.ex_cap.managers.MovesetManager}:</b> Indexes combat animation sets and handles
 * the inheritance hierarchy/merging logic.</li>
 * <li><b>{@link yesman.epicfight.api.ex_cap.managers.ConditionalManager}:</b> Stores the logic predicates that determine
 * style swaps and runtime state changes.</li>
 * </ul>
 * * <p><b>Design Principle:</b> All managers follow a "Register-Once" pattern. Once
 * an entry is registered, it is automatically available to the engine, listeners,
 * and other mods via the {@link yesman.epicfight.api.ex_cap.core.data} entries.</p>
 *
 * @see yesman.epicfight.api.ex_cap.managers.ItemPresetManager
 * @see yesman.epicfight.api.ex_cap.managers.MovesetManager
 * @see yesman.epicfight.api.ex_cap.managers.ConditionalManager
 */
package yesman.epicfight.api.ex_cap.managers;