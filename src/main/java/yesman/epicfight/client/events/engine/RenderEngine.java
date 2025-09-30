package yesman.epicfight.client.events.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings.ViewLimit;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.forgeevent.RenderEnderDragonEvent;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.gui.VersionNotifier;
import yesman.epicfight.client.gui.screen.config.UISetupScreen;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.AimHelperRenderer;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.ImaginaryBlockRenderer;
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
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@SuppressWarnings("rawtypes")
@OnlyIn(Dist.CLIENT)
public class RenderEngine {
	public final BattleModeGui battleModeUI;
	public final VersionNotifier versionNotifier;
	public final Minecraft minecraft;
	
	private final Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers;
	private final BiMap<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
	private final Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;
	private final Map<Item, RenderItemBase> itemRendererMapByInstance;
	private final Map<Class<?>, RenderItemBase> itemRendererMapByClass;
	private final Map<UUID, BossPatch> bossEventOwners = Maps.newConcurrentMap();
	private final OverlayManager overlayManager;
	private final ImaginaryBlockRenderer imaginaryBlockRenderer;
	
	private AimHelperRenderer aimHelper;
	private FirstPersonRenderer firstPersonRenderer;
	private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
	private boolean zoomingIn;
	private int modelInitTimer;
	
	private final int maxZoomCount = 20;
	private int zoomOutStandbyTicks = 0;
	private int zoomCount = 0;
	
	public RenderEngine() {
		Events.renderEngine = this;
		
		this.minecraft = Minecraft.getInstance();
		this.battleModeUI = new BattleModeGui(this.minecraft);
		this.versionNotifier = new VersionNotifier(this.minecraft);
		this.entityRendererProvider = HashBiMap.create();
		this.entityRendererCache = Maps.newHashMap();
		this.itemRendererMapByInstance = Maps.newHashMap();
		this.itemRendererMapByClass = Maps.newHashMap();
		this.overlayManager = new OverlayManager();
		this.imaginaryBlockRenderer = new ImaginaryBlockRenderer();
		
		Map<ResourceLocation, Function<JsonElement, RenderItemBase>> builder = Maps.newHashMap();
		
		builder.put(ResourceLocation.withDefaultNamespace("base"), RenderItemBase::new);
		builder.put(ResourceLocation.withDefaultNamespace("ranged"), RenderTwoHandedRangedWeapon::new);
		builder.put(ResourceLocation.withDefaultNamespace("map"), RenderFilledMap::new);
		builder.put(ResourceLocation.withDefaultNamespace("shield"), RenderShield::new);
		builder.put(ResourceLocation.withDefaultNamespace("trident"), RenderTrident::new);
		builder.put(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "uchigatana"), RenderKatana::new);
		
		ModLoader.get().postEvent(new PatchedRenderersEvent.RegisterItemRenderer(builder));
		
		this.itemRenderers = ImmutableMap.copyOf(builder);
	}
	
	public void reloadEntityRenderers(EntityRendererProvider.Context context) {
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
		this.entityRendererProvider.put(EpicFightEntities.WITHER_SKELETON_MINION.get(), (entityType) -> new PWitherSkeletonMinionRenderer(context, entityType).initLayerLast(context, entityType));
		this.entityRendererProvider.put(EpicFightEntities.WITHER_GHOST_CLONE.get(), (entityType) -> new WitherGhostCloneRenderer());
		
		this.firstPersonRenderer = new FirstPersonRenderer(context, EntityType.PLAYER);
		this.basicHumanoidRenderer = new PHumanoidRenderer<>(Meshes.BIPED, context, EntityType.PLAYER);
		this.aimHelper = new AimHelperRenderer();
		
		ModLoader.get().postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider, context));
		
		this.resetRenderers();
	}
	
	public void reloadItemRenderers(Map<ResourceLocation, JsonElement> objects) {
		//Clear item renderers
		this.itemRendererMapByInstance.clear();
		this.itemRendererMapByClass.clear();
		
		for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String pathString = rl.getPath();
			ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), pathString);
			
			if (!ForgeRegistries.ITEMS.containsKey(registryName)) {
				EpicFightMod.LOGGER.warn("Failed to load item skin: no item named " + registryName);
				continue;
			}
			
			Item item = ForgeRegistries.ITEMS.getValue(registryName);
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
		RenderTwoHandedRangedWeapon bowRenderer = new RenderTwoHandedRangedWeapon(objects.get(ForgeRegistries.ITEMS.getKey(Items.BOW)).getAsJsonObject());
		RenderTwoHandedRangedWeapon crossbowRenderer = new RenderTwoHandedRangedWeapon(objects.get(ForgeRegistries.ITEMS.getKey(Items.CROSSBOW)).getAsJsonObject());
		RenderTrident tridentRenderer = new RenderTrident(objects.get(ForgeRegistries.ITEMS.getKey(Items.TRIDENT)).getAsJsonObject());
		RenderFilledMap mapRenderer = new RenderFilledMap(objects.get(ForgeRegistries.ITEMS.getKey(Items.FILLED_MAP)).getAsJsonObject());
		RenderShield shieldRenderer = new RenderShield(objects.get(ForgeRegistries.ITEMS.getKey(Items.SHIELD)).getAsJsonObject());
		
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
		
		ModLoader.get().postEvent(new PatchedRenderersEvent.Modify(this.entityRendererCache));
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
			EntityType<?> presetEntityType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(rendererName));
			
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
			this.zoomCount = this.zoomCount == 0 ? 1 : this.zoomCount;
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
	
	private void setRangedWeaponThirdPerson(ViewportEvent.ComputeCameraAngles event, CameraType pov, double partialTicks) {
		if (ClientEngine.getInstance().getPlayerPatch() == null) {
			return;
		}
		
		Camera camera = event.getCamera();
		Entity entity = minecraft.getCameraEntity();
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
			float intpol = pov == CameraType.THIRD_PERSON_BACK ? ((float) zoomCount / (float) maxZoomCount) : 0;
			Vec3f interpolatedCorrection = new Vec3f(AIMING_CORRECTION.x * intpol, AIMING_CORRECTION.y * intpol, AIMING_CORRECTION.z * intpol);
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
				HitResult raytraceresult = minecraft.level.clip(new ClipContext(new Vec3(entityPosX + f, entityPosY + f1, entityPosZ + f2), new Vec3(d00 + f + f2, d11 + f1, d22 + f2), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

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
	
	public void correctCamera(ViewportEvent.ComputeCameraAngles event, float partialTicks) {
		LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
		Camera camera = event.getCamera();
		CameraType cameraType = this.minecraft.options.getCameraType();
		
		if (localPlayerPatch != null) {
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
					
					camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
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
						float clampedYRot = Mth.clamp(event.getYaw(), localPlayerPatch.getYRot() + viewLimit.yRotMin(), localPlayerPatch.getYRot() + viewLimit.yRotMax());
						
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
	
	public void upSlideSkillUI() {
		this.battleModeUI.slideUp();
	}
	
	public void downSlideSkillUI() {
		this.battleModeUI.slideDown();
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
	
	public void initHUD() {
		this.battleModeUI.init();
		this.versionNotifier.init();
	}
	
	public void freeUnusedSources() {
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
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
	public static class Events {
		static RenderEngine renderEngine;
		
		@SubscribeEvent
		public static void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
			LivingEntity livingentity = event.getEntity();
			
			if (livingentity.level() == null) {
				return;
			}
			
			if (renderEngine.hasRendererFor(livingentity)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				float originalYRot = 0.0F;
				
				//Draw the player in inventory
				if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localplayerpatch) {
					if (entitypatch.overrideRender()) {
						originalYRot = localplayerpatch.getModelYRot();
						localplayerpatch.setModelYRotInGui(livingentity.getYRot());
						event.getPoseStack().translate(0, 0.1D, 0);
						renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getMultiBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
						event.setCanceled(true);
						localplayerpatch.disableModelYRotInGui(originalYRot);
					}
					
					return;
				}
				
				if (entitypatch != null && entitypatch.overrideRender()) {
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getMultiBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
					
					if (renderEngine.shouldRenderVanillaModel()) {
						event.getPoseStack().translate(renderEngine.modelInitTimer > 0 ? 10000.0F : 1.5F, 0.0F, 0.0F);
						--renderEngine.modelInitTimer;
					} else {
						event.setCanceled(true);
					}
				}
			}
			
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null && !renderEngine.minecraft.options.hideGui && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.level())) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				
				for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
					if (entityIndicator.shouldDraw(livingentity, entitypatch, playerpatch, event.getPartialTick())) {
						entityIndicator.draw(livingentity, entitypatch, playerpatch, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void itemTooltip(ItemTooltipEvent event) {
			if (ClientConfig.showEpicFightAttributesInTooltip && event.getEntity() != null && event.getEntity().level().isClientSide) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class).ifPresent(playerpatch -> {
					CapabilityItem cap = EpicFightCapabilities.getItemStackCapabilityOr(event.getItemStack(), null);
					
					if (cap != null) {
						if (ControlEngine.isKeyDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP)) {
							Skill weaponInnateSkill = cap.getInnateSkill(playerpatch, event.getItemStack());

							if (weaponInnateSkill != null) {
								event.getToolTip().clear();
								List<Component> skilltooltip = weaponInnateSkill.getTooltipOnItem(event.getItemStack(), cap, playerpatch);

								for (Component s : skilltooltip) {
									event.getToolTip().add(s);
								}
							}
						} else {
							List<Component> tooltip = event.getToolTip();
							cap.modifyItemTooltip(event.getItemStack(), event.getToolTip(), playerpatch);
							
							for (int i = 0; i < tooltip.size(); i++) {
								Component textComp = tooltip.get(i);
								
								if (!textComp.getSiblings().isEmpty()) {
									Component sibling = textComp.getSiblings().get(0);
									
									if (sibling instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContent) {
										if (translatableContent.getArgs().length > 1 && translatableContent.getArgs()[1] instanceof MutableComponent mutableComponent$2) {
											if (mutableComponent$2.getContents() instanceof TranslatableContents translatableContent$2) {
												if (translatableContent$2.getKey().equals(Attributes.ATTACK_SPEED.getDescriptionId())) {
													float weaponSpeed = (float)playerpatch.getWeaponAttribute(Attributes.ATTACK_SPEED, event.getItemStack());
													tooltip.remove(i);
													tooltip.add(i, Component.literal(String.format(" %.2f ", playerpatch.getModifiedAttackSpeed(cap, weaponSpeed)))
															.append(Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId())));
													
												} else if (translatableContent$2.getKey().equals(Attributes.ATTACK_DAMAGE.getDescriptionId())) {
													float weaponDamage = (float)playerpatch.getWeaponAttribute(Attributes.ATTACK_DAMAGE, event.getItemStack());
													float damageBonus = EnchantmentHelper.getDamageBonus(event.getItemStack(), MobType.UNDEFINED);
													String damageFormat = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(playerpatch.getModifiedBaseDamage(weaponDamage) + damageBonus);
													
													tooltip.remove(i);
													tooltip.add(i, Component.literal(String.format(" %s ", damageFormat))
																			.append(Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId()))
																			.withStyle(ChatFormatting.DARK_GREEN));
												}
											}
										}
									}
								}
							}
							
							Skill weaponInnateSkill = cap.getInnateSkill(playerpatch, event.getItemStack());
							
							if (weaponInnateSkill != null) {
								event.getToolTip().add(Component.translatable("inventory.epicfight.guide_innate_tooltip", EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP.getKey().getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
							}
						}
					}
				});
			}
		}
		
		@SubscribeEvent
		public static void cameraSetupEvent(ViewportEvent.ComputeCameraAngles event) {
			if (renderEngine.zoomCount > 0 && ClientConfig.aimingPovCorrection) {
				renderEngine.setRangedWeaponThirdPerson(event, renderEngine.minecraft.options.getCameraType(), event.getPartialTick());
				
				if (renderEngine.zoomOutStandbyTicks > 0) {
					renderEngine.zoomOutStandbyTicks--;
				} else {
					renderEngine.zoomCount = renderEngine.zoomingIn ? renderEngine.zoomCount + 1 : renderEngine.zoomCount - 1;
				}
				
				renderEngine.zoomCount = Math.min(renderEngine.maxZoomCount, renderEngine.zoomCount);
			}
			
			renderEngine.correctCamera(event, (float)event.getPartialTick());
		}
		
		@SubscribeEvent
		public static void renderGui(RenderGuiEvent.Pre event) {
			Window window = Minecraft.getInstance().getWindow();
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null) {
				playerpatch.getSkillCapability().listSkillContainers().forEach(skillContainer -> {
					if (skillContainer.getSkill() != null) {
						skillContainer.getSkill().onScreen(playerpatch, window.getGuiScaledWidth(), window.getGuiScaledHeight());
					}
				});
				
				renderEngine.overlayManager.renderTick(window.getGuiScaledWidth(), window.getGuiScaledHeight());
				
				if (Minecraft.renderNames() && !(Minecraft.getInstance().screen instanceof UISetupScreen)) {
					renderEngine.battleModeUI.renderTick();
				}
				
				//Shows the epic fight version in beta
				renderEngine.versionNotifier.render(event.getGuiGraphics(), true);
			}
		}
		
		@SubscribeEvent
		public static void renderGameOverlayPost(CustomizeGuiOverlayEvent.BossEventProgress event) {
			if (event.getBossEvent().getName().getString().equals("Ender Dragon")) {
				if (renderEngine.bossEventOwners.containsKey(event.getBossEvent().getId())) {
					LivingEntityPatch<?> entitypatch = renderEngine.bossEventOwners.get(event.getBossEvent().getId()).cast();
					float stunShield = entitypatch.getStunShield();
					
					if (stunShield > 0) {
						float progression = stunShield / entitypatch.getMaxStunShield();
						int x = event.getX();
						int y = event.getY();
						
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						event.getGuiGraphics().blit(BossHealthOverlay.GUI_BARS_LOCATION, x, y + 6, 183, 2, 0, 45.0F, 182, 6, 255, 255);
						event.getGuiGraphics().blit(BossHealthOverlay.GUI_BARS_LOCATION, x + (int)(183 * progression), y + 6, (int)(183 * (1.0F - progression)), 2, 0, 39.0F, 182, 6, 255, 255);
					}
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void renderHand(RenderHandEvent event) {
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null) {
				boolean isBattleMode = playerpatch.isEpicFightMode();
				
				if (isBattleMode && ClientConfig.enableAnimatedFirstPersonModel) {
					RenderItemBase mainhandItemSkin = renderEngine.getItemRenderer(playerpatch.getOriginal().getMainHandItem());
					RenderItemBase offhandItemSkin = renderEngine.getItemRenderer(playerpatch.getOriginal().getOffhandItem());
					boolean useEpicFightModel = (mainhandItemSkin == null || !mainhandItemSkin.forceVanillaFirstPerson()) && (offhandItemSkin == null || !offhandItemSkin.forceVanillaFirstPerson());
					
					if (useEpicFightModel) {
						if (event.getHand() == InteractionHand.MAIN_HAND) {
							renderEngine.firstPersonRenderer.render(
								  playerpatch.getOriginal()
								, playerpatch
								, (LivingEntityRenderer)renderEngine.minecraft.getEntityRenderDispatcher().getRenderer(playerpatch.getOriginal())
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
		
		@SubscribeEvent
		public static void renderWorldLast(RenderLevelStageEvent event) {
			if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
				if (ClientConfig.aimingPovCorrection && renderEngine.zoomCount > 0 && renderEngine.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
					renderEngine.aimHelper.doRender(event.getPoseStack(), event.getPartialTick());
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		@SubscribeEvent
		public static void renderEnderDragonEvent(RenderEnderDragonEvent event) {
			EnderDragon livingentity = event.getEntity();
			
			if (renderEngine.hasRendererFor(livingentity)) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(livingentity, EnderDragonPatch.class).ifPresent(enderdragonpatch -> {
					event.setCanceled(true);
					renderEngine.getEntityRenderer(livingentity).render(livingentity, enderdragonpatch, event.getRenderer(), event.getBuffers(), event.getPoseStack(), event.getLight(), event.getPartialRenderTick());
				});
			}
		}
		
		@SubscribeEvent
		public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
			if (!ClientConfig.enableMineBlockGuide) {
				return;
			}
			
			EpicFightCapabilities.getUnparameterizedEntityPatch(renderEngine.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
				if (playerpatch.canPlayAttackAnimation()) {
					event.setCanceled(true);
				} else {
					renderEngine.imaginaryBlockRenderer.render(event.getCamera(), event.getPoseStack(), event.getMultiBufferSource(), renderEngine.minecraft.level, event.getTarget().getBlockPos(), 1.0F, 0, 0, 0.5F);					
				}
			});
		}
		
		@SubscribeEvent
		public static void renderTickEvent(TickEvent.RenderTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				EntityUI.HEALTH_BAR.reset();
			} else {
				EntityUI.HEALTH_BAR.remove();
			}
		}
		
		@SubscribeEvent
		public static void clientTickEvent(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				renderEngine.freeUnusedSources();
			}
		}
		
		@SubscribeEvent
		public static void levelTickEvent(TickEvent.LevelTickEvent event) {
			if (event.level.isClientSide() && event.phase == TickEvent.Phase.END) {
				EntityUI.HEALTH_BAR.tick();
			}
		}
	}
}
