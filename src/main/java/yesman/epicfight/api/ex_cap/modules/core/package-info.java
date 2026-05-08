/**
 * The internal engine and registry system for Epic Fight's Extensible Capability (ExCap) API.
 * <p>
 * This package is organized into specialized sub-sectors to handle the full lifecycle
 * of weapon definitions:
 * </p>
 * <ul>
 * <li><b>{@code data}:</b> Contains immutable handles and templates (Entries) that
 * store raw weapon and combat data.</li>
 * <li><b>{@code managers}:</b> The primary entry points for registration. These classes
 * index all data and provide query methods for the combat engine.</li>
 * <li><b>{@code listeners}:</b> Handles the integration with Minecraft's data-reload
 * system, ensuring that changes to weapon properties can be updated dynamically.</li>
 * <li><b>{@code provider}:</b> Contains the core logic for applying these capabilities
 * to item stacks and evaluating conditional requirements at runtime.</li>
 * </ul>
 * <p><b>Implementation Note:</b> Most developers should interact with the registries
 * via the {@code managers} package. The other sub-packages are primarily intended for
 * internal engine use and advanced API extensions.</p>
 *
 * @since 1.21.1
 * @deprecated this intermediate package is no longer needed. It has been unpacked into ex_cap
 */
@Deprecated(forRemoval = true)
package yesman.epicfight.api.ex_cap.modules.core;