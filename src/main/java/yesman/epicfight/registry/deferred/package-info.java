/**
 * This package houses the {@code DeferredRegister} instances for Epic Fight's weapon types
 * and registry entries.
 * <h2>Registry Load Order & Overriding Logic</h2>
 * The final runtime state of weapon data is determined by a multi-stage process.
 * Later stages in this list will override values set by previous stages:
 * <ol>
 * <li><b>Deferred Registration:</b> Classes within this package (e.g., {@code ItemPresetRegister})
 * initialize the base runtime map.</li>
 * <li><b>Legacy Event Registry:</b> The deprecated event-driven registration process runs,
 * overwriting entries in the runtime map.</li>
 * <li><b>Datapacks:</b> Data loaded via external JSON/Datapacks runs last. These provide
 * the final override, superseding both the deferred registers and legacy events.</li>
 * </ol>
 * @see yesman.epicfight.registry.deferred.holders
 */
package yesman.epicfight.registry.deferred;