/**
 * Contains the standard, pre-defined assets and presets for the Epic Fight engine.
 * <p>
 * This package serves as a library of "Ready-to-Use" components. Modders should look
 * here first before creating custom animations or colliders, as these assets are
 * optimized and balanced for the core Epic Fight experience.
 * </p>
 * * <h3>Primary Asset Groups:</h3>
 * <ul>
 * <li><b>Builders:</b> Pre-configured hitboxes (e.g., {@code ColliderPreset.UCHIGATANA}), sounds, particles, and
 * all the base features of a weapon.</li>
 * <li><b>Movesets:</b> The base animation sets (e.g., {@code Movesets.sword1HMS})
 * used by vanilla-equivalent weapons.</li>
 * <li><b>Conditionals:</b> Common logic (e.g., {@code MainConditionals.DUAL_SWORDS})
 * for standard combat behaviors.</li>
 * </ul>
 * <p><b>Usage Note:</b> Assets in this package are intended to be passed directly
 * into {@link yesman.epicfight.api.ex_cap.core.data.BuilderEntry} instances during
 * weapon definition.</p>
 */
package yesman.epicfight.gameasset;