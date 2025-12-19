package yesman.epicfight.world.capabilities.entitypatch;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.joml.Vector4f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public final class EntityDecorations {
	private final Map<ResourceLocation, RenderAttributeModifier<Vec2i>> overlay = new HashMap<> ();
	private final Map<ResourceLocation, RenderAttributeModifier<Vector4f>> colors = new HashMap<> ();
	private final Map<ResourceLocation, RenderAttributeModifier<Vec2i>> lights = new HashMap<> ();
	private final Map<ResourceLocation, AnimationPropertyModifier<SoundEvent, CapabilityItem>> swingSound = new HashMap<> ();
	private final Map<ResourceLocation, AnimationPropertyModifier<SoundEvent, CapabilityItem>> hurtSound = new HashMap<> ();
	private final Map<ResourceLocation, AnimationPropertyModifier<TrailInfo, CapabilityItem>> trail = new HashMap<> ();
	private final Map<ResourceLocation, ParticleGenerator> particleGenerator = new HashMap<> ();
	private final Map<ResourceLocation, DecorationOverlay> decorationOverlays = new HashMap<> ();

    public static final ResourceLocation ADAPTIVE_SKIN_COLOR = EpicFightMod.identifier("adaptive_skin_color");
    public static final ResourceLocation ADAPTIVE_SKIN_OVERLAY = EpicFightMod.identifier("adaptive_skin_overlay");
    public static final ResourceLocation BERSERKER_PARTICLE = EpicFightMod.identifier("berserker_particle");
    public static final ResourceLocation BERSERKER_OVERLAY = EpicFightMod.identifier("berserker_overlay");
    public static final ResourceLocation BONEBREAKER_OVERLAY = EpicFightMod.identifier("bonebreaker_overlay");
    public static final ResourceLocation EMERGENCY_ESCAPE_TRANSPARENCY_MODIFIER = EpicFightMod.identifier("emergency_escape_transparency_modifier");
    public static final ResourceLocation HYPERVITALITY_OVERLAY = EpicFightMod.identifier("hypervitality_overlay");
    public static final ResourceLocation STAMINA_PILLAGER_ASHES_COLOR = EpicFightMod.identifier("stamina_pillager_ashes_color");
    public static final ResourceLocation STAMINA_PILLAGER_ASHES_OVERLAY = EpicFightMod.identifier("stamina_pillager_ashes_overlay");
    public static final ResourceLocation STAMINA_PILLAGER_ASHES_PARTICLE = EpicFightMod.identifier("stamina_pillager_ashes_particle");
    public static final ResourceLocation STAMINA_PILLAGER_FILLS_UP_OVERLAY = EpicFightMod.identifier("stamina_pillager_fills_up_overlay");
    public static final ResourceLocation STAMINA_PILLAGER_FILLS_UP_LIGHT = EpicFightMod.identifier("stamina_pillager_fills_up_light");
    public static final ResourceLocation FLASH_WHITE_OVERLAY = EpicFightMod.identifier("flash_white_overlay");
    public static final ResourceLocation FLASH_WHITE_LIGHT = EpicFightMod.identifier("flash_white_light");
    public static final ResourceLocation SWORDMASTER_SWING_SOUND = EpicFightMod.identifier("swordmaster_swing_sound_modifier");
    public static final ResourceLocation SWORDMASTER_TRAIL_MODIFIER = EpicFightMod.identifier("swordmaster_trail_modifier");
    public static final ResourceLocation VENGEANCE_OVERLAY = EpicFightMod.identifier("vengeance_overlay");
	
	public void addOverlayCoordModifier(ResourceLocation id, RenderAttributeModifier<Vec2i> overlayModifier) {
		this.overlay.put(id, overlayModifier);
	}
	
	public boolean removeOverlayCoordModifier(ResourceLocation id) {
		return this.overlay.remove(id) != null;
	}
	
	public void addColorModifier(ResourceLocation id, RenderAttributeModifier<Vector4f> colorModifier) {
		this.colors.put(id, colorModifier);
	}
	
	public boolean removeColorModifier(ResourceLocation id) {
		return this.colors.remove(id) != null;
	}
	
	public void addLightModifier(ResourceLocation id, RenderAttributeModifier<Vec2i> lightModifier) {
		this.lights.put(id, lightModifier);
	}
	
	public boolean removeLightModifier(ResourceLocation id) {
		return this.lights.remove(id) != null;
	}
	
	public void addSwingSoundModifier(ResourceLocation id, AnimationPropertyModifier<SoundEvent, CapabilityItem> swingSoundModifier) {
		this.swingSound.put(id, swingSoundModifier);
	}
	
	public boolean removeSwingSoundModifier(ResourceLocation id) {
		return this.swingSound.remove(id) != null;
	}
	
	public void addHurtSoundModifier(ResourceLocation id, AnimationPropertyModifier<SoundEvent, CapabilityItem> hurtSoundModifier) {
		this.hurtSound.put(id, hurtSoundModifier);
	}
	
	public boolean removeHurtSoundModifier(ResourceLocation id) {
		return this.hurtSound.remove(id) != null;
	}
	
	public void addTrailInfoModifier(ResourceLocation id, AnimationPropertyModifier<TrailInfo, CapabilityItem> trailInfoModifier) {
		this.trail.put(id, trailInfoModifier);
	}
	
	public boolean removeTrailInfoModifier(ResourceLocation id) {
		return this.trail.remove(id) != null;
	}
	
	public void addParticleGenerator(ResourceLocation id, ParticleGenerator particleGenerator) {
		this.particleGenerator.put(id, particleGenerator);
	}
	
	public boolean removeParticleGenerator(ResourceLocation id) {
		return this.particleGenerator.remove(id) != null;
	}
	
	public void addDecorationOverlay(ResourceLocation id, DecorationOverlay entityOverlay) {
		this.decorationOverlays.put(id, entityOverlay);
	}
	
	public void removeDecorationOverlay(ResourceLocation id) {
		this.decorationOverlays.remove(id);
	}
	
	public void modifyOverlay(Vec2i overlayCoord, float partialTick) {
		for (RenderAttributeModifier<Vec2i> modifier : this.overlay.values()) {
			if (!modifier.shouldRemove()) modifier.modifyValue(overlayCoord, partialTick);
		}
		
		overlayCoord.x = Mth.clamp(overlayCoord.x, 0, 15);
		overlayCoord.y = Mth.clamp(overlayCoord.y, 0, 15);
	}
	
	public void modifyColor(Vector4f vec, float partialTick) {
		for (RenderAttributeModifier<Vector4f> modifier : this.colors.values()) {
			if (!modifier.shouldRemove()) modifier.modifyValue(vec, partialTick);
		}
		
		vec.x = Mth.clamp(vec.x, 0.0F, 1.0F);
		vec.y = Mth.clamp(vec.y, 0.0F, 1.0F);
		vec.z = Mth.clamp(vec.z, 0.0F, 1.0F);
		vec.w = Mth.clamp(vec.w, 0.0F, 1.0F);
	}
	
	public void modifyLight(Vec2i mi, float partialTick) {
		for (RenderAttributeModifier<Vec2i> modifier : this.lights.values()) {
			if (!modifier.shouldRemove()) modifier.modifyValue(mi, partialTick);
		}
		
		mi.x = Mth.clamp(mi.x, 0, 15);
		mi.y = Mth.clamp(mi.y, 0, 15);
	}
	
	public Stream<DecorationOverlay> listDecorationOverlays() {
		return this.decorationOverlays.values().stream();
	}
	
	public SoundEvent getModifiedSwingSound(SoundEvent original, CapabilityItem item) {
		for (AnimationPropertyModifier<SoundEvent, CapabilityItem> v : this.swingSound.values()) {
			original = v.getModifiedValue(original, item);
		}
		
		return original;
	}
	
	public SoundEvent getModifiedHurtSound(SoundEvent original, CapabilityItem item) {
		for (AnimationPropertyModifier<SoundEvent, CapabilityItem> v : this.hurtSound.values()) {
			original = v.getModifiedValue(original, item);
		}
		
		return original;
	}
	
	public TrailInfo getModifiedTrailInfo(TrailInfo original, CapabilityItem item) {
		for (AnimationPropertyModifier<TrailInfo, CapabilityItem> v : this.trail.values()) {
			original = v.getModifiedValue(original, item);
		}
		
		return original;
	}
	
	public void tick() {
		this.colors.entrySet().removeIf(entry -> {
			entry.getValue().tick();
			return entry.getValue().shouldRemove();
		});
		
		this.overlay.entrySet().removeIf(entry -> {
			entry.getValue().tick();
			return entry.getValue().shouldRemove();
		});
		
		this.lights.entrySet().removeIf(entry -> {
			entry.getValue().tick();
			return entry.getValue().shouldRemove();
		});
		
		this.swingSound.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
		this.hurtSound.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
		this.trail.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
		
		this.particleGenerator.entrySet().removeIf(entry -> {
			entry.getValue().generateParticles();
			return entry.getValue().shouldRemove();
		});
		
		this.decorationOverlays.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
	}
	
	public interface RenderAttributeModifier<T> {
		public void modifyValue(T val, float partialTick);
		
		default boolean shouldRemove() {
			return false;
		}
		
		default void tick() {
		}
	}
	
	public interface AttachableRenderAttributeModifier<T> {
		boolean shouldRemove();
		
		public void onApplied();
		
		public void onRemoved();
	}
	
	public interface AnimationPropertyModifier<T, O> {
		public T getModifiedValue(T val, O object);
		
		default boolean shouldRemove() {
			return false;
		}
	}
	
	public interface ParticleGenerator {
		public void generateParticles();
		
		default boolean shouldRemove() {
			return false;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public interface DecorationOverlay {
        static ResourceLocation GENERIC = EpicFightMod.identifier("textures/common/white.png");
		static Vector4f NO_COLOR = new Vector4f(1.0F);
		
		default Vector4f color(float partialTick) {
			return NO_COLOR;
		}
		
		default RenderType getRenderType() {
			return EpicFightRenderTypes.overlayModel(GENERIC);
		}
		
		default boolean shouldRender() {
			return true;
		}
		
		default boolean shouldRemove() {
			return false;
		}
	}
}
