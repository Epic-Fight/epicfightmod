package yesman.epicfight.client.events.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings.ViewLimit;
import yesman.epicfight.api.client.input.action.EpicFightInputActions;
import yesman.epicfight.api.client.input.handlers.InputManager;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.neoevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.neoevent.RenderEnderDragonEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.gui.VersionNotifier;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.AimHelperRenderer;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FakeBlockRenderer;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.VanillaFakeBlockRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCreeperRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCustomHumanoidEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PDrownedRenderer;
import yesman.epicfight.client.renderer.patched.entity.PEnderDragonRenderer;
import yesman.epicfight.client.renderer.patched.entity.PEndermanRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHoglinRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.client.renderer.patched.entity.PIllagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PIronGolemRenderer;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PRavagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PSpiderRenderer;
import yesman.epicfight.client.renderer.patched.entity.PStrayRenderer;
import yesman.epicfight.client.renderer.patched.entity.PVexRenderer;
import yesman.epicfight.client.renderer.patched.entity.PVindicatorRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitchRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitherRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.entity.PZombieVillagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PresetRenderer;
import yesman.epicfight.client.renderer.patched.entity.WitherGhostCloneRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderFilledMap;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.item.RenderKatana;
import yesman.epicfight.client.renderer.patched.item.RenderShield;
import yesman.epicfight.client.renderer.patched.item.RenderTrident;
import yesman.epicfight.client.renderer.patched.item.RenderTwoHandedRangedWeapon;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.item.BowCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CrossbowCapability;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.ShieldCapability;
import yesman.epicfight.world.capabilities.item.TridentCapability;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@SuppressWarnings("rawtypes")
@OnlyIn(Dist.CLIENT)
public class RenderEngine implements IEventBasedEngine {
	private static final RenderEngine INSTANCE = new RenderEngine();
	
	public static RenderEngine getInstance() {
		return INSTANCE;
	}
	
	public final BattleModeGui battleModeHUD;
	public final VersionNotifier versionNotifier;
	public final Minecraft minecraft;
	
	private final BiMap<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
	private final Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;
	private final Map<Item, RenderItemBase> itemRendererMapByInstance;
	private final Map<Class<?>, RenderItemBase> itemRendererMapByClass;
	private final Map<UUID, BossPatch> bossEventOwners = new ConcurrentHashMap<> ();
	private final OverlayManager overlayManager;
	private FakeBlockRenderer fakeBlockRenderer;
	
	private Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers;
	private AimHelperRenderer aimHelper;
	private FirstPersonRenderer firstPersonRenderer;
	private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
	private int modelInitTimer;
	
	private boolean zoomingIn;
	private int zoomTick = 0;
	private int zoomTickO = 0;
	private final int maxZoomTicks = 6;
	private int zoomOutStandbyTicks = 0;
	
	private RenderEngine() {
		this.minecraft = Minecraft.getInstance();
		this.battleModeHUD = new BattleModeGui(this.minecraft);
		this.versionNotifier = new VersionNotifier(this.minecraft);
		this.entityRendererProvider = HashBiMap.create();
		this.entityRendererCache = new HashMap<> ();
		this.itemRendererMapByInstance = new HashMap<> ();
		this.itemRendererMapByClass = new HashMap<> ();
		this.overlayManager = new OverlayManager();
		this.fakeBlockRenderer = new VanillaFakeBlockRenderer();
	}
	
	public void initialize() {
		Map<ResourceLocation, Function<JsonElement, RenderItemBase>> builder = new HashMap<> ();
		builder.put(ResourceLocation.withDefaultNamespace("base"), RenderItemBase::new);
		builder.put(ResourceLocation.withDefaultNamespace("ranged"), RenderTwoHandedRangedWeapon::new);
		builder.put(ResourceLocation.withDefaultNamespace("map"), RenderFilledMap::new);
		builder.put(ResourceLocation.withDefaultNamespace("shield"), RenderShield::new);
		builder.put(ResourceLocation.withDefaultNamespace("trident"), RenderTrident::new);
		builder.put(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "uchigatana"), RenderKatana::new);
		
		ModLoader.postEvent(new PatchedRenderersEvent.RegisterItemRenderer(builder));
		
		this.itemRenderers = ImmutableMap.copyOf(builder);
	}
	
	public void reloadFakeBlockRenderer(FakeBlockRenderer fakeBlockRenderer) {
		this.fakeBlockRenderer = fakeBlockRenderer;
	}
	
	public void reloadItemRenderers(Map<ResourceLocation, JsonElement> objects) {
		//Clear item renderers
		this.itemRendererMapByInstance.clear();
		this.itemRendererMapByClass.clear();
		
		for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String pathString = rl.getPath();
			ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), pathString);
			
			if (!BuiltInRegistries.ITEM.containsKey(registryName)) {
				EpicFightMod.LOGGER.warn("Failed to load item skin: no item named " + registryName);
				continue;
			}
			
			Item item = BuiltInRegistries.ITEM.get(registryName);
			Function<JsonElement, RenderItemBase> rendererProvider;
			
			if (entry.getValue().getAsJsonObject().has("renderer")) {
				ResourceLocation rendererName = ResourceLocation.parse(entry.getValue().getAsJsonObject().get("renderer").getAsString());
				
				if (itemRenderers.containsKey(rendererName)) {
					rendererProvider = itemRenderers.get(rendererName);
				} else {
					EpicFightMod.LOGGER.warn("No renderer named " + rendererName);
					rendererProvider = RenderItemBase::new;
				}
			} else {
				rendererProvider = RenderItemBase::new;
			}
			
			RenderItemBase itemRenderer = rendererProvider.apply(entry.getValue());
			this.itemRendererMapByInstance.put(item, itemRenderer);
		}
		
		RenderItemBase baseRenderer = new RenderItemBase(new JsonObject());
		RenderTwoHandedRangedWeapon bowRenderer = new RenderTwoHandedRangedWeapon(objects.get(BuiltInRegistries.ITEM.getKey(Items.BOW)).getAsJsonObject());
		RenderTwoHandedRangedWeapon crossbowRenderer = new RenderTwoHandedRangedWeapon(objects.get(BuiltInRegistries.ITEM.getKey(Items.CROSSBOW)).getAsJsonObject());
		RenderTrident tridentRenderer = new RenderTrident(objects.get(BuiltInRegistries.ITEM.getKey(Items.TRIDENT)).getAsJsonObject());
		RenderFilledMap mapRenderer = new RenderFilledMap(objects.get(BuiltInRegistries.ITEM.getKey(Items.FILLED_MAP)).getAsJsonObject());
		RenderShield shieldRenderer = new RenderShield(objects.get(BuiltInRegistries.ITEM.getKey(Items.SHIELD)).getAsJsonObject());
		
		// Render by item classes
		this.itemRendererMapByClass.put(BowItem.class, bowRenderer);
		this.itemRendererMapByClass.put(CrossbowItem.class, crossbowRenderer);
		this.itemRendererMapByClass.put(ShieldItem.class, baseRenderer);
		this.itemRendererMapByClass.put(TridentItem.class, tridentRenderer);
		this.itemRendererMapByClass.put(ShieldItem.class, shieldRenderer);
		
		// Render by capability classes
		this.itemRendererMapByClass.put(BowCapability.class, bowRenderer);
		this.itemRendererMapByClass.put(CrossbowCapability.class, crossbowRenderer);
		this.itemRendererMapByClass.put(TridentCapability.class, tridentRenderer);
		this.itemRendererMapByClass.put(MapCapability.class, mapRenderer);
		this.itemRendererMapByClass.put(ShieldCapability.class, shieldRenderer);
	}
	
	public void resetRenderers() {
		this.entityRendererCache.clear();
		
		for (Map.Entry<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
			this.entityRendererCache.put(entry.getKey(), entry.getValue().apply(entry.getKey()));
		}
		
		ModLoader.postEvent(new PatchedRenderersEvent.Modify(this.entityRendererCache));
	}
	
	@SuppressWarnings("unchecked")
	public void registerCustomEntityRenderer(EntityType<?> entityType, String rendererName, CompoundTag compound) {
		if (StringUtil.isNullOrEmpty(rendererName)) {
			return;
		}
		
		EntityRenderDispatcher erd = this.minecraft.getEntityRenderDispatcher();
		EntityRendererProvider.Context context = new EntityRendererProvider.Context(erd, this.minecraft.getItemRenderer(), this.minecraft.getBlockRenderer(), erd.getItemInHandRenderer(), this.minecraft.getResourceManager(), this.minecraft.getEntityModels(), this.minecraft.font);
		
		if ("player".equals(rendererName)) {
			this.entityRendererCache.put(entityType, this.basicHumanoidRenderer);
		} else if ("epicfight:custom".equals(rendererName)) {
			if (compound.getBoolean("humanoid")) {
				this.entityRendererCache.put(entityType, new PCustomHumanoidEntityRenderer<> (Meshes.getOrCreate(ResourceLocation.parse(compound.getString("model")), (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)), context, entityType));
			} else {
				this.entityRendererCache.put(entityType, new PCustomEntityRenderer(Meshes.getOrCreate(ResourceLocation.parse(compound.getString("model")), (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)), context));
			}
		} else {
			EntityType<?> presetEntityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(rendererName));
			
			if (this.entityRendererProvider.containsKey(presetEntityType)) {
				PatchedEntityRenderer renderer = this.entityRendererProvider.get(presetEntityType).apply(entityType);
				
				if (!(this.minecraft.getEntityRenderDispatcher().renderers.get(entityType) instanceof LivingEntityRenderer) && (renderer instanceof PatchedLivingEntityRenderer patchedLivingEntityRenderer)) {
					this.entityRendererCache.put(entityType, new PresetRenderer(context, entityType, (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)context.getEntityRenderDispatcher().renderers.get(presetEntityType), patchedLivingEntityRenderer.getDefaultMesh()));
				} else {
					this.entityRendererCache.put(entityType, this.entityRendererProvider.get(presetEntityType).apply(entityType));
				}
			} else {
				throw new IllegalArgumentException("Datapack Mob Patch Crash: Invalid Renderer type " + rendererName);
			}
		}
	}
	
	public RenderItemBase getItemRenderer(ItemStack itemstack) {
		RenderItemBase renderItem = this.itemRendererMapByInstance.get(itemstack.getItem());
		
		if (renderItem == null) {
			renderItem = this.findMatchingRendererByClass(itemstack.getItem().getClass());
			
			if (renderItem == null) {
				CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemstack);
				renderItem = this.findMatchingRendererByClass(itemCap.getClass());
			}
			
			if (renderItem == null) {
				// Get generic renderer
				renderItem = this.itemRendererMapByInstance.get(Items.AIR);
			}
			
			this.itemRendererMapByInstance.put(itemstack.getItem(), renderItem);
		}
		
		return renderItem;
	}

	private RenderItemBase findMatchingRendererByClass(Class<?> clazz) {
		RenderItemBase renderer = null;
		
		for (; clazz != null && renderer == null; clazz = clazz.getSuperclass()) {
			renderer = this.itemRendererMapByClass.get(clazz);
		}
		
		return renderer;
	}
	
	@SuppressWarnings("unchecked")
	public void renderEntityArmatureModel(LivingEntity livingEntity, LivingEntityPatch<?> entitypatch, EntityRenderer<? extends Entity> renderer, MultiBufferSource buffer, PoseStack matStack, int packedLight, float partialTicks) {
		this.getEntityRenderer(livingEntity).render(livingEntity, entitypatch, renderer, buffer, matStack, packedLight, partialTicks);
	}
	
	public PatchedEntityRenderer getEntityRenderer(Entity entity) {
		return this.getEntityRenderer(entity.getType());
	}
	
	public PatchedEntityRenderer getEntityRenderer(EntityType entityType) {
		return this.entityRendererCache.get(entityType);
	}
	
	public boolean hasRendererFor(Entity entity) {
		return this.entityRendererCache.computeIfAbsent(entity.getType(), (key) -> this.entityRendererProvider.containsKey(key) ? this.entityRendererProvider.get(entity.getType()).apply(entity.getType()) : null) != null;
	}
	
	public Set<ResourceLocation> getRendererEntries() {
		Set<ResourceLocation> availableRendererEntities = this.entityRendererProvider.keySet().stream().map((entityType) -> EntityType.getKey(entityType)).collect(Collectors.toSet());
		availableRendererEntities.add(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "custom"));
		
		return availableRendererEntities;
	}
	
	public void zoomIn() {
		// Nothing happens if player is already zooming-in
		if (!this.zoomingIn) {
			this.zoomingIn = true;
			this.zoomTick = this.zoomTick == 0 ? 1 : this.zoomTick;
		}
	}
	
	public void zoomOut(int zoomOutTicks) {
		// Nothing happens if player is already zooming-out
		if (this.zoomingIn) {
			this.zoomingIn = false;
			this.zoomOutStandbyTicks = zoomOutTicks;
		}
	}
	
	public void setModelInitializerTimer(int tick) {
		this.modelInitTimer = tick;
	}
	
	private static final Vec3f AIMING_CORRECTION = new Vec3f(-1.5F, 0.0F, 1.25F);
	
	public void setRangedWeaponThirdPerson(Camera camera, CameraType pov, double partialTicks) {
		if (this.zoomTickO < 1) {
			return;
		}
		
		if (ClientEngine.getInstance().getPlayerPatch() == null) {
			return;
		}
		
		Entity entity = this.minecraft.getCameraEntity();
		Vec3 vector = camera.getPosition();
		double totalX = vector.x();
		double totalY = vector.y();
		double totalZ = vector.z();
		
		if (pov == CameraType.THIRD_PERSON_BACK) {
			double posX = vector.x();
			double posY = vector.y();
			double posZ = vector.z();
			double entityPosX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
			double entityPosY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks + entity.getEyeHeight();
			double entityPosZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
			float zoomAmount = pov == CameraType.THIRD_PERSON_BACK ? (float)(Mth.lerp(partialTicks, this.zoomTickO, this.zoomTick)) / (float)this.maxZoomTicks : 0;
			Vec3f interpolatedCorrection = new Vec3f(AIMING_CORRECTION.x * zoomAmount, AIMING_CORRECTION.y * zoomAmount, AIMING_CORRECTION.z * zoomAmount);
			OpenMatrix4f rotationMatrix = ClientEngine.getInstance().getPlayerPatch().getMatrix((float)partialTicks);
			Vec3f rotateVec = OpenMatrix4f.transform3v(rotationMatrix, interpolatedCorrection, null);
			double d3 = Math.sqrt((rotateVec.x * rotateVec.x) + (rotateVec.y * rotateVec.y) + (rotateVec.z * rotateVec.z));
			double smallest = d3;
			double d00 = posX + rotateVec.x;
			double d11 = posY - rotateVec.y;
			double d22 = posZ + rotateVec.z;
			
			for (int i = 0; i < 8; ++i) {
				float f = (float) ((i & 1) * 2 - 1);
				float f1 = (float) ((i >> 1 & 1) * 2 - 1);
				float f2 = (float) ((i >> 2 & 1) * 2 - 1);
				f = f * 0.1F;
				f1 = f1 * 0.1F;
				f2 = f2 * 0.1F;
				HitResult raytraceresult = this.minecraft.level.clip(new ClipContext(new Vec3(entityPosX + f, entityPosY + f1, entityPosZ + f2), new Vec3(d00 + f + f2, d11 + f1, d22 + f2), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

				if (raytraceresult != null) {
					double d7 = raytraceresult.getLocation().distanceTo(new Vec3(entityPosX, entityPosY, entityPosZ));
					if (d7 < smallest) {
						smallest = d7;
					}
				}
			}
			
			float dist = d3 == 0 ? 0 : (float) (smallest / d3);
			totalX += rotateVec.x * dist;
			totalY -= rotateVec.y * dist;
			totalZ += rotateVec.z * dist;
		}
		
		camera.setPosition(totalX, totalY, totalZ);
	}
	
	private static final OpenMatrix4f PLAYER_ROTATION = new OpenMatrix4f();
	private static final Vector3f CAMERA_ROTATION_EULER = new Vector3f();
	
	@SuppressWarnings("deprecation")
	public void correctCamera(ViewportEvent.ComputeCameraAngles event, float partialTicks) {
		LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
		Camera camera = event.getCamera();
		CameraType cameraType = this.minecraft.options.getCameraType();
		
		if (localPlayerPatch != null) {
			// Lock on correction
			if (localPlayerPatch.getTarget() != null && localPlayerPatch.isTargetLockedOn()) {
				float xRot = localPlayerPatch.getLerpedLockOnX(partialTicks);
				float yRot = localPlayerPatch.getLerpedLockOnY(partialTicks);
				
				if (cameraType.isMirrored()) {
					yRot += 180.0F;
					xRot *= -1.0F;
				}
				
				camera.setRotation(yRot, xRot);
				event.setPitch(xRot);
				event.setYaw(yRot);
				
				if (!cameraType.isFirstPerson()) {
					Entity cameraEntity = this.minecraft.cameraEntity;
					camera.setPosition(
						  Mth.lerp(partialTicks, cameraEntity.xo, cameraEntity.getX())
						, Mth.lerp(partialTicks, cameraEntity.yo, cameraEntity.getY()) + Mth.lerp(partialTicks, camera.eyeHeightOld, camera.eyeHeight)
						, Mth.lerp(partialTicks, cameraEntity.zo, cameraEntity.getZ())
					);
					
					camera.move(-camera.getMaxZoom(4.0F), 0.0F, 0.0F);
				}
			}
			
			// First person camera correction
			if (ClientConfig.enablePovAction && cameraType.isFirstPerson() && localPlayerPatch.isEpicFightMode() && !localPlayerPatch.getFirstPersonLayer().isOff()) {
				if (localPlayerPatch.isLerpingFpv()) {
					float xRot = localPlayerPatch.getLerpedFpvXRot(partialTicks);
					float yRot = localPlayerPatch.getLerpedFpvYRot(partialTicks);
					this.minecraft.cameraEntity.setXRot(xRot);
					this.minecraft.cameraEntity.setYRot(yRot);
				} else {
					ViewLimit viewLimit = localPlayerPatch.getPovSettings().viewLimit();
					
					if (viewLimit != null) {
						float clampedXRot = Mth.clamp(event.getPitch(), viewLimit.xRotMin(), viewLimit.xRotMax());
						float bodyY = MathUtils.findNearestRotation(event.getYaw(), localPlayerPatch.getYRot());
						float clampedYRot = Mth.clamp(event.getYaw(), bodyY + viewLimit.yRotMin(), bodyY + viewLimit.yRotMax());
						
						if (Float.compare(clampedXRot, event.getPitch()) != 0 || Float.compare(clampedYRot, event.getYaw()) != 0) {
							localPlayerPatch.fixFpvRotation(clampedXRot, localPlayerPatch.getYRot());
						}
					}
				}
				
				if (localPlayerPatch.hasCameraAnimation()) {
					float time = Mth.lerp(partialTicks, localPlayerPatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(), localPlayerPatch.getFirstPersonLayer().animationPlayer.getElapsedTime());
					JointTransform cameraTransform;
					
					if (localPlayerPatch.getFirstPersonLayer().animationPlayer.getAnimation().get().isLinkAnimation() || localPlayerPatch.getPovSettings() == null) {
						cameraTransform = localPlayerPatch.getFirstPersonLayer().getLinkCameraTransform().getInterpolatedTransform(time);
					} else {
						cameraTransform = localPlayerPatch.getPovSettings().cameraTransform().getInterpolatedTransform(time);
					}
					
					float xRot = localPlayerPatch.getOriginal().getXRot();
					float yRot = localPlayerPatch.getOriginal().getYRot();
					
					Vec3f translation = OpenMatrix4f.transform3v(OpenMatrix4f.ofRotationDegree(yRot, Vec3f.Y_AXIS, PLAYER_ROTATION).rotate(xRot, Vec3f.X_AXIS), cameraTransform.translation(), null);
					Quaternionf rot = cameraTransform.rotation();
					rot.getEulerAnglesXYZ(CAMERA_ROTATION_EULER);
					
					CAMERA_ROTATION_EULER.x = (float)Math.toDegrees(CAMERA_ROTATION_EULER.x);
					CAMERA_ROTATION_EULER.y = (float)Math.toDegrees(CAMERA_ROTATION_EULER.y);
					CAMERA_ROTATION_EULER.z = (float)Math.toDegrees(CAMERA_ROTATION_EULER.z);
					
					camera.move(translation.x, translation.y, translation.z);
					
					event.setPitch(event.getPitch() + CAMERA_ROTATION_EULER.x);
					event.setYaw(event.getYaw() + CAMERA_ROTATION_EULER.y);
					event.setRoll(event.getRoll() + CAMERA_ROTATION_EULER.z);
				}
			}
		}
	}
	
	public OverlayManager getOverlayManager() {
		return this.overlayManager;
	}
	
	public FirstPersonRenderer getFirstPersonRenderer() {
		return firstPersonRenderer;
	}
	
	public boolean shouldRenderVanillaModel() {
		return ClientEngine.getInstance().isVanillaModelDebuggingMode() || this.modelInitTimer > 0;
	}
	
	public void addBossEventOwner(UUID uuid, BossPatch bosspatch) {
		this.bossEventOwners.put(uuid, bosspatch);
	}
	
	public void removeBossEventOwner(UUID uuid, BossPatch bosspatch) {
		this.bossEventOwners.remove(uuid);
	}
	
	public void initHUD(LocalPlayerPatch playerpatch) {
		this.battleModeHUD.init(playerpatch);
		this.versionNotifier.init();
	}
	
	private void freeUnusedSources() {
		this.bossEventOwners.entrySet().removeIf((entry) -> {
			Entity entity = entry.getValue().cast().getOriginal();
			return !entity.isAlive() || entity.isRemoved();
		});
		
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				EpicFightRenderTypes.freeUnusedWorldRenderTypes();
			});
		} else {
			EpicFightRenderTypes.freeUnusedWorldRenderTypes();
		}
	}
	
	public void clear() {
		this.zoomOut(0);
		this.bossEventOwners.clear();
		
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				this.resetRenderers();
				EpicFightRenderTypes.clearWorldRenderTypes();
			});
		} else {
			this.resetRenderers();
			EpicFightRenderTypes.clearWorldRenderTypes();
		}
	}
	
	public static boolean hitResultEquals(@Nullable HitResult hitResult, HitResult.Type hitType) {
		return hitResult == null ? false : hitType.equals(hitResult.getType());
	}
	
	public static boolean hitResultNotEquals(@Nullable HitResult hitResult, HitResult.Type hitType) {
		return hitResult == null ? true : !hitType.equals(hitResult.getType());
	}
	
	/******************
	 * Forge Event listeners
	 ******************/
	private void epicfight$renderLivingPre(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
		LivingEntity livingentity = event.getEntity();
		
		if (livingentity.level() == null) {
			return;
		}
		
		if (this.hasRendererFor(livingentity)) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
			float originalYRot = 0.0F;
			
			//Draw the player in inventory
			if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
				if (entitypatch.overrideRender()) {
					originalYRot = localPlayerPatch.getModelYRot();
					localPlayerPatch.setModelYRotInGui(livingentity.getYRot());
					event.getPoseStack().translate(0, 0.1D, 0);
					boolean compusteShaderSetting = ClientConfig.activateComputeShader;
					
					// Disable compute shader
					ClientConfig.activateComputeShader = false;
					this.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getMultiBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
					ClientConfig.activateComputeShader = compusteShaderSetting;
					
					event.setCanceled(true);
					localPlayerPatch.disableModelYRotInGui(originalYRot);
				}
				
				return;
			}
			
			if (entitypatch != null && entitypatch.overrideRender()) {
				this.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getMultiBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
				
				if (this.shouldRenderVanillaModel()) {
					event.getPoseStack().translate(this.modelInitTimer > 0 ? 10000.0F : 1.5F, 0.0F, 0.0F);
					--this.modelInitTimer;
				} else {
					event.setCanceled(true);
				}
			}
		}
		
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch != null && !this.minecraft.options.hideGui && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.level())) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
			
			for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
				if (entityIndicator.shouldDraw(livingentity, entitypatch, playerpatch, event.getPartialTick())) {
					entityIndicator.draw(livingentity, entitypatch, playerpatch, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
				}
			}
		}
	}
	
	private void epicfight$itemTooltip(ItemTooltipEvent event) {
		if (ClientConfig.showEpicFightAttributesInTooltip && event.getEntity() != null && event.getEntity().level().isClientSide) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class).ifPresent(playerpatch -> {
				EpicFightCapabilities.getItemCapability(event.getItemStack()).ifPresent(itemCapability -> {
					if (InputManager.isActionPhysicallyActive(EpicFightInputActions.WEAPON_INNATE_SKILL_TOOLTIP)) {
						Skill weaponInnateSkill = itemCapability.getInnateSkill(playerpatch, event.getItemStack());
						
						if (weaponInnateSkill != null) {
							event.getToolTip().clear();
							List<Component> skilltooltip = weaponInnateSkill.getTooltipOnItem(event.getItemStack(), itemCapability, playerpatch);

							for (Component s : skilltooltip) {
								event.getToolTip().add(s);
							}
						}
					} else {
						List<Component> tooltip = event.getToolTip();
						itemCapability.modifyItemTooltip(event.getItemStack(), event.getToolTip(), playerpatch);
						
						for (int i = 0; i < tooltip.size(); i++) {
							Component textComp = tooltip.get(i);
							
							if (!textComp.getSiblings().isEmpty()) {
								Component sibling = textComp.getSiblings().get(0);
								
								if (sibling instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContent) {
									if (translatableContent.getArgs().length > 1 && translatableContent.getArgs()[1] instanceof MutableComponent mutableComponent$2) {
										if (mutableComponent$2.getContents() instanceof TranslatableContents translatableContent$2) {
											if (translatableContent$2.getKey().equals(Attributes.ATTACK_SPEED.value().getDescriptionId())) {
												float weaponSpeed = (float)playerpatch.getWeaponAttribute(Attributes.ATTACK_SPEED, event.getItemStack());
												tooltip.remove(i);
												tooltip.add(i, Component.literal(String.format(" %.2f ", playerpatch.getModifiedAttackSpeed(itemCapability, weaponSpeed)))
														.append(Component.translatable(Attributes.ATTACK_SPEED.value().getDescriptionId())));
												
											} else if (translatableContent$2.getKey().equals(Attributes.ATTACK_DAMAGE.value().getDescriptionId())) {
												float weaponDamage = (float)playerpatch.getWeaponAttribute(Attributes.ATTACK_DAMAGE, event.getItemStack());
												String damageFormat = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(playerpatch.getModifiedBaseDamage(weaponDamage));
												
												tooltip.remove(i);
												tooltip.add(i, Component.literal(String.format(" %s ", damageFormat))
																		.append(Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()))
																		.withStyle(ChatFormatting.DARK_GREEN));
											}
										}
									}
								}
							}
						}
						
						Skill weaponInnateSkill = itemCapability.getInnateSkill(playerpatch, event.getItemStack());
						
						if (weaponInnateSkill != null) {
							event.getToolTip().add(Component.translatable("inventory.epicfight.guide_innate_tooltip", EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP.getKey().getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
						}
					}
				});
			});
		}
	}
	
	private void epicfight$computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		this.correctCamera(event, (float)event.getPartialTick());
	}
	
	private void epicfight$renderGuiPre(RenderGuiEvent.Pre event) {
		Window window = Minecraft.getInstance().getWindow();
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch != null) {
			playerpatch.getPlayerSkills().listSkillContainers().filter(skillContainer -> skillContainer.getSkill() != null).forEach(skillContainer -> {
				skillContainer.getSkill().onScreen(playerpatch, window.getGuiScaledWidth(), window.getGuiScaledHeight());
			});
			
			this.overlayManager.renderTick(window.getGuiScaledWidth(), window.getGuiScaledHeight());
			
			//Shows the epic fight version in beta
			this.versionNotifier.render(event.getGuiGraphics(), true);
		}
	}
	
	private static final ResourceLocation YELLOWBAR_BACKGROUND = ResourceLocation.withDefaultNamespace("boss_bar/yellow_background");
	private static final ResourceLocation YELLOWBAR_PROGRESS = ResourceLocation.withDefaultNamespace("boss_bar/yellow_progress");
	
	private void epicfight$bossEventProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
		if (event.getBossEvent().getName().getString().equals("Ender Dragon")) {
			if (this.bossEventOwners.containsKey(event.getBossEvent().getId())) {
				LivingEntityPatch<?> entitypatch = this.bossEventOwners.get(event.getBossEvent().getId()).cast();
				float stunShield = entitypatch.getStunShield();
				
				if (stunShield > 0) {
					float progression = stunShield / entitypatch.getMaxStunShield();
					
					int x = event.getX();
					int y = event.getY();
					
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					event.getGuiGraphics().blitSprite(YELLOWBAR_BACKGROUND, 182, 5, 0, 0, x, y + 6, 182, 5);
					event.getGuiGraphics().blitSprite(YELLOWBAR_PROGRESS, 182, 5, 0, 0, x, y + 6, (int)(182 * progression), 5);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void epicfight$renderHand(RenderHandEvent event) {
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch != null) {
			if (playerpatch.isEpicFightMode() && ClientConfig.enableAnimatedFirstPersonModel) {
				RenderItemBase mainhandItemSkin = this.getItemRenderer(playerpatch.getOriginal().getMainHandItem());
				RenderItemBase offhandItemSkin = this.getItemRenderer(playerpatch.getOriginal().getOffhandItem());
				boolean useEpicFightModel = (mainhandItemSkin == null || !mainhandItemSkin.forceVanillaFirstPerson()) && (offhandItemSkin == null || !offhandItemSkin.forceVanillaFirstPerson());
				
				if (useEpicFightModel) {
					if (event.getHand() == InteractionHand.MAIN_HAND) {
						this.firstPersonRenderer.render(
							  playerpatch.getOriginal()
							, playerpatch
							, (LivingEntityRenderer)this.minecraft.getEntityRenderDispatcher().getRenderer(playerpatch.getOriginal())
							, event.getMultiBufferSource()
							, event.getPoseStack()
							, event.getPackedLight()
							, event.getPartialTick()
						);
					}
					
					event.setCanceled(true);
				}
			}
		}
	}
	
	private void epicfight$renderAfterLevel(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
			if (ClientConfig.mineBlockGuideOption.showBlockHighlight() && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
					if (!playerpatch.canPlayAttackAnimation()) {
						this.fakeBlockRenderer.render(event.getCamera(), event.getPoseStack(), this.minecraft.renderBuffers().bufferSource(), this.minecraft.level, ((BlockHitResult)this.minecraft.hitResult).getBlockPos(), 1.0F, 1.0F, 1.0F, 0.4F);					
					}
				});
			}
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			if (ClientConfig.aimingPovCorrection && this.zoomTick > 0 && this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
				float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
				this.aimHelper.doRender(event.getPoseStack(), partialTick);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void epicfight$renderEnderDragon(RenderEnderDragonEvent event) {
		EnderDragon livingentity = event.getEntity();
		
		if (this.hasRendererFor(livingentity)) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(livingentity, EnderDragonPatch.class).ifPresent(enderdragonpatch -> {
				event.setCanceled(true);
				this.getEntityRenderer(livingentity).render(livingentity, enderdragonpatch, event.getRenderer(), event.getBuffers(), event.getPoseStack(), event.getLight(), event.getPartialRenderTick());
			});
		}
	}
	
	private void epicfight$renderTickPre(RenderFrameEvent.Pre event) {
		EntityUI.HEALTH_BAR.reset();
	}
	
	private void epicfight$renderTickPost(RenderFrameEvent.Post event) {
		EntityUI.HEALTH_BAR.remove();
	}
	
	private void epicfight$clientTickPre(ClientTickEvent.Pre event) {
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch != null) {
			this.battleModeHUD.tick(playerpatch);
		}
		
		this.freeUnusedSources();
	}
	
	private void epicfight$levelTickPost(LevelTickEvent.Post event) {
		if (!event.getLevel().isClientSide()) {
			return;
		}
		
		this.zoomTickO = this.zoomTick;
		
		EntityUI.HEALTH_BAR.tick();
		
		if (this.zoomTick > 0 && ClientConfig.aimingPovCorrection) {
			if (this.zoomOutStandbyTicks > 0) {
				this.zoomOutStandbyTicks--;
			} else {
				this.zoomTick = this.zoomingIn ? this.zoomTick + 1 : this.zoomTick - 1;
			}
			
			this.zoomTick = Math.clamp(this.zoomTick, 0, this.maxZoomTicks);
		}
	}
	
	private void epicfight$renderBlockHighlight(RenderHighlightEvent.Block event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
			if (playerpatch.canPlayAttackAnimation()) {
				event.setCanceled(true);
			}
		});
	}
	
	private void epicfight$renderGuiLayer$Pre(RenderGuiLayerEvent.Pre event) {
		if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
			CameraType cameraType = this.minecraft.options.getCameraType();
			
			// Cancel if a modified crosshair is rendered
			if (event.isCanceled() && cameraType.isFirstPerson() || !cameraType.isFirstPerson()) {
				return;
			}
			
			MutableBoolean itemAction = new MutableBoolean(true); // true: combat, false: mine
			
			if (ClientConfig.mineBlockGuideOption.switchCrosshair()) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
					itemAction.setValue(playerpatch.canPlayAttackAnimation());
				});
			}
			
			if (!itemAction.booleanValue()) {
				event.setCanceled(true);
				
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

				event.getGuiGraphics().blit(EntityUI.BATTLE_ICON, (event.getGuiGraphics().guiWidth() - 15) / 2, (event.getGuiGraphics().guiHeight() - 15) / 2, 0, 240, 15, 15);
				
				RenderSystem.defaultBlendFunc();
				RenderSystem.disableBlend();
			}
		}
	}
	
	/**********************
	 * Forge Event listeners end
	 **********************/
	
	/**********************
	 * Mod Event listeners
	 **********************/
	private void epicfight$addLayers(EntityRenderersEvent.AddLayers event) {
		EntityRendererProvider.Context context = event.getContext();
		
		this.entityRendererProvider.clear();
		this.entityRendererProvider.put(EntityType.CREEPER, (entityType) -> new PCreeperRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ENDERMAN, (entityType) -> new PEndermanRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ZOMBIE, (entityType) -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ZOMBIE_VILLAGER, (entityType) -> new PZombieVillagerRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ZOMBIFIED_PIGLIN, (entityType) -> new PHumanoidRenderer<>(Meshes.PIGLIN, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.HUSK, (entityType) -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.SKELETON, (entityType) -> new PHumanoidRenderer<>(Meshes.SKELETON, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.WITHER_SKELETON, (entityType) -> new PHumanoidRenderer<>(Meshes.SKELETON, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.STRAY, (entityType) -> new PStrayRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.PLAYER, (entityType) -> new PPlayerRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.SPIDER, (entityType) -> new PSpiderRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.CAVE_SPIDER, (entityType) -> new PSpiderRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.IRON_GOLEM, (entityType) -> new PIronGolemRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.VINDICATOR, (entityType) -> new PVindicatorRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.EVOKER, (entityType) -> new PIllagerRenderer<> (context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.WITCH, (entityType) -> new PWitchRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.DROWNED, (entityType) -> new PDrownedRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.PILLAGER, (entityType) -> new PIllagerRenderer<> (context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.RAVAGER, (entityType) -> new PRavagerRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.VEX, (entityType) -> new PVexRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.PIGLIN, (entityType) -> new PHumanoidRenderer<>(Meshes.PIGLIN, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.PIGLIN_BRUTE, (entityType) -> new PHumanoidRenderer<>(Meshes.PIGLIN, context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.HOGLIN, (entityType) -> new PHoglinRenderer<> (context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ZOGLIN, (entityType) -> new PHoglinRenderer<> (context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EntityType.ENDER_DRAGON, (entityType) -> new PEnderDragonRenderer());
		this.entityRendererProvider.put(EntityType.WITHER, (entityType) -> new PWitherRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), (entityType) -> new PWitherSkeletonMinionRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), (entityType) -> new WitherGhostCloneRenderer());
		
		this.firstPersonRenderer = new FirstPersonRenderer(context, EntityType.PLAYER);
		this.basicHumanoidRenderer = new PHumanoidRenderer<>(Meshes.BIPED, context, EntityType.PLAYER);
		this.aimHelper = new AimHelperRenderer();
		
		ModLoader.postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider, context));
		
		this.resetRenderers();
	}
	/**********************
	 * Mod Event listeners end
	 **********************/
	
	@Override
	public void gameEventBus(IEventBus gameEventBus) {
		gameEventBus.addListener(this::epicfight$bossEventProgress);
		gameEventBus.addListener(this::epicfight$renderLivingPre);
		gameEventBus.addListener(this::epicfight$itemTooltip);
		gameEventBus.addListener(this::epicfight$computeCameraAngles);
		gameEventBus.addListener(this::epicfight$renderGuiPre);
		gameEventBus.addListener(this::epicfight$renderHand);
		gameEventBus.addListener(this::epicfight$renderAfterLevel);
		gameEventBus.addListener(this::epicfight$renderEnderDragon);
		gameEventBus.addListener(this::epicfight$renderTickPre);
		gameEventBus.addListener(this::epicfight$renderTickPost);
		gameEventBus.addListener(this::epicfight$clientTickPre);
		gameEventBus.addListener(this::epicfight$levelTickPost);
		gameEventBus.addListener(this::epicfight$renderBlockHighlight);
		gameEventBus.addListener(this::epicfight$renderGuiLayer$Pre);
	}

	@Override
	public void modEventBus(IEventBus modEventBus) {
		modEventBus.addListener(this::epicfight$addLayers);
	}
}
