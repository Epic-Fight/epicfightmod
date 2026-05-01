package yesman.epicfight.client.events.engine;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.api.client.event.types.render.RenderEnderDragonEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.model.Meshes;
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
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FakeBlockRenderer;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.VanillaFakeBlockRenderer;
import yesman.epicfight.client.renderer.patched.entity.*;
import yesman.epicfight.client.renderer.patched.item.*;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
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

    private FirstPersonRenderer firstPersonRenderer;
    private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
    private int modelInitTimer;

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

    public void reloadFakeBlockRenderer(FakeBlockRenderer fakeBlockRenderer) {
        this.fakeBlockRenderer = fakeBlockRenderer;
    }

    public void reloadItemRenderers(Map<ResourceLocation, JsonElement> objects) {
        //Clear item renderers
        this.itemRendererMapByInstance.clear();
        this.itemRendererMapByClass.clear();

        Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers = new HashMap<> ();
        itemRenderers.put(ResourceLocation.withDefaultNamespace("base"), RenderItemBase::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("ranged"), RenderTwoHandedRangedWeapon::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("map"), RenderFilledMap::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("shield"), RenderShield::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("trident"), RenderTrident::new);
        itemRenderers.put(EpicFight.identifier("uchigatana"), RenderKatana::new);

        EpicFightClientEventHooks.Registry.PATCHED_ITEM.post(new RegisterPatchedRenderersEvent.Item(itemRenderers));

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation rl = entry.getKey();
            String pathString = rl.getPath();
            ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), pathString);

            if (!BuiltInRegistries.ITEM.containsKey(registryName)) {
                EpicFight.LOGGER.warn("Failed to load item skin: no item named " + registryName);
                continue;
            }

            Item item = BuiltInRegistries.ITEM.get(registryName);
            Function<JsonElement, RenderItemBase> rendererProvider;

            if (entry.getValue().getAsJsonObject().has("renderer")) {
                ResourceLocation rendererName = ResourceLocation.parse(entry.getValue().getAsJsonObject().get("renderer").getAsString());

                if (itemRenderers.containsKey(rendererName)) {
                    rendererProvider = itemRenderers.get(rendererName);
                } else {
                    EpicFight.LOGGER.warn("No renderer named " + rendererName);
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

        EpicFightClientEventHooks.Registry.MODIFY_PATCHED_ENTITY.post(new RegisterPatchedRenderersEvent.ModifyEntity(this.entityRendererCache));
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
        availableRendererEntities.add(EpicFight.identifier("custom"));

        return availableRendererEntities;
    }

    public void setModelInitializerTimer(int tick) {
        this.modelInitTimer = tick;
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
        EpicFightCameraAPI.getInstance().zoomOut(0);
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

    /// More strict type sensitive hit result getter by instanceof
    public static BlockHitResult asBlockHitResult(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return null;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult;
        }

        return null;
    }

    /// More strict type sensitive hit result getter by instanceof
    public static EntityHitResult asEntityHitResult(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return null;
        }

        if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHitResult) {
            return entityHitResult;
        }

        return null;
    }

    /******************
     * Forge EventHook listeners
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
        if (!this.minecraft.options.hideGui && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.level())) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
                LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);

                for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
                    if (entityIndicator.shouldDraw(livingentity, entityPatch, playerpatch, event.getPartialTick())) {
                        entityIndicator.draw(livingentity, entityPatch, playerpatch, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
                    }
                }
            });
        }
    }

    private void epicfight$itemTooltip(ItemTooltipEvent event) {
        if (ClientConfig.showEpicFightAttributesInTooltip && event.getEntity() != null && event.getEntity().level().isClientSide) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class).ifPresent(playerpatch -> {
                EpicFightCapabilities.getItemCapability(event.getItemStack()).ifPresent(itemCapability -> {
                    if (InputManager.isActionPhysicallyActive(EpicFightInputAction.WEAPON_INNATE_SKILL_TOOLTIP)) {
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
                                                tooltip.add(i, Component.literal(String.format(" %.2f ", playerpatch.getModifiedAttackSpeedOfItem(itemCapability, weaponSpeed)))
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

    private static final Vector3f CAMERA_ROTATION_EULER = new Vector3f();
    private static final OpenMatrix4f PLAYER_ROTATION = new OpenMatrix4f();

    private void epicfight$computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            // First person camera correction
            if (ClientConfig.enableFirstPersonCameraMove && this.minecraft.options.getCameraType().isFirstPerson() && playerpatch.isEpicFightMode() && !playerpatch.getFirstPersonLayer().isOff()) {
                float partialTick = (float)event.getPartialTick();
                EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();

                if (cameraApi.isLerpingFpv()) {
                    float xRot = cameraApi.getLerpedFpvXRot(partialTick);
                    float yRot = cameraApi.getLerpedFpvYRot(partialTick);
                    this.minecraft.cameraEntity.setXRot(xRot);
                    this.minecraft.cameraEntity.setYRot(yRot);
                } else {
                    AnimationSubFileReader.PovSettings.ViewLimit viewLimit = playerpatch.getPovSettings().viewLimit();

                    if (viewLimit != null) {
                        float clampedXRot = Mth.clamp(event.getPitch(), viewLimit.xRotMin(), viewLimit.xRotMax());
                        float bodyY = MathUtils.findNearestRotation(event.getYaw(), playerpatch.getYRot());
                        float clampedYRot = Mth.clamp(event.getYaw(), bodyY + viewLimit.yRotMin(), bodyY + viewLimit.yRotMax());

                        if (Float.compare(clampedXRot, event.getPitch()) != 0 || Float.compare(clampedYRot, event.getYaw()) != 0) {
                            cameraApi.fixFpvRotation(clampedXRot, playerpatch.getYRot(), 5);
                        }
                    }
                }

                if (playerpatch.hasCameraAnimation()) {
                    float time = Mth.lerp(partialTick, playerpatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(), playerpatch.getFirstPersonLayer().animationPlayer.getElapsedTime());
                    JointTransform cameraTransform;

                    if (playerpatch.getFirstPersonLayer().animationPlayer.getAnimation().get().isLinkAnimation() || playerpatch.getPovSettings() == null) {
                        cameraTransform = playerpatch.getFirstPersonLayer().getLinkCameraTransform().getInterpolatedTransform(time);
                    } else {
                        cameraTransform = playerpatch.getPovSettings().cameraTransform().getInterpolatedTransform(time);
                    }

                    float xRot = playerpatch.getOriginal().getXRot();
                    float yRot = playerpatch.getOriginal().getYRot();

                    Vec3f translation = OpenMatrix4f.transform3v(OpenMatrix4f.ofRotationDegree(yRot, Vec3f.Y_AXIS, PLAYER_ROTATION).rotate(xRot, Vec3f.X_AXIS), cameraTransform.translation(), null);
                    Quaternionf rot = cameraTransform.rotation();
                    rot.getEulerAnglesXYZ(CAMERA_ROTATION_EULER);

                    CAMERA_ROTATION_EULER.x = (float)Math.toDegrees(CAMERA_ROTATION_EULER.x);
                    CAMERA_ROTATION_EULER.y = (float)Math.toDegrees(CAMERA_ROTATION_EULER.y);
                    CAMERA_ROTATION_EULER.z = (float)Math.toDegrees(CAMERA_ROTATION_EULER.z);

                    event.getCamera().move(translation.x, translation.y, translation.z);
                    event.setPitch(event.getPitch() + CAMERA_ROTATION_EULER.x);
                    event.setYaw(event.getYaw() + CAMERA_ROTATION_EULER.y);
                    event.setRoll(event.getRoll() + CAMERA_ROTATION_EULER.z);
                }
            }
        });
    }

    private void epicfight$renderGuiPre(RenderGuiEvent.Pre event) {
        Window window = Minecraft.getInstance().getWindow();
        LocalPlayerPatch playerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();;

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
        LocalPlayerPatch playerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();;

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
            BlockHitResult blockHitResult = RenderEngine.asBlockHitResult(this.minecraft.hitResult);

            if (ClientConfig.mineBlockGuideOption.showBlockHighlight() && blockHitResult != null) {
                EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
                    if (!playerpatch.canPlayAttackAnimation() && playerpatch.isEpicFightMode()) {
                        this.fakeBlockRenderer.render(event.getCamera(), event.getPoseStack(), this.minecraft.renderBuffers().bufferSource(), this.minecraft.level, blockHitResult.getBlockPos(), 1.0F, 1.0F, 1.0F, 0.4F);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void epicfight$renderEnderDragon(RenderEnderDragonEvent event) {
        EnderDragon livingentity = event.getEntity();

        if (this.hasRendererFor(livingentity)) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(livingentity, EnderDragonPatch.class).ifPresent(enderdragonpatch -> {
                event.cancel();
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

    private void epicfight$clientTick$Pre(ClientTickEvent.Pre event) {
        EpicFightCameraAPI.getInstance().preClientTick();
        EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(this.battleModeHUD::tick);
        this.freeUnusedSources();
    }

    private void epicfight$clientTick$Post(ClientTickEvent.Post event) {
        EpicFightCameraAPI.getInstance().postClientTick();
    }

    private void epicfight$levelTickPost(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }

        EntityUI.HEALTH_BAR.tick();
    }

    private void epicfight$renderBlockHighlight(RenderHighlightEvent.Block event) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            if (playerpatch.canPlayAttackAnimation()) {
                event.setCanceled(true);
            }
        });
    }

    /**********************
     * Forge EventHook listeners end
     **********************/

    /**********************
     * Mod EventHook listeners
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

        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.post(new RegisterPatchedRenderersEvent.AddEntity(this.entityRendererProvider, context));

        this.resetRenderers();
    }
    /**********************
     * Mod EventHook listeners end
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
        gameEventBus.addListener(this::epicfight$renderTickPre);
        gameEventBus.addListener(this::epicfight$renderTickPost);
        gameEventBus.addListener(this::epicfight$clientTick$Pre);
        gameEventBus.addListener(this::epicfight$clientTick$Post);
        gameEventBus.addListener(this::epicfight$levelTickPost);
        gameEventBus.addListener(this::epicfight$renderBlockHighlight);

        EpicFightClientEventHooks.Render.RENDER_ENDER_DRAGON.registerEvent(this::epicfight$renderEnderDragon);
    }

    @Override
    public void modEventBus(IEventBus modEventBus) {
        modEventBus.addListener(this::epicfight$addLayers);
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#zoomIn()} instead
     */
    @Deprecated(forRemoval = true)
    public void zoomIn() {
        EpicFightCameraAPI.getInstance().zoomIn();
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#zoomOut(int)} instead
     */
    @Deprecated(forRemoval = true)
    public void zoomOut(int zoomOutTicks) {
        EpicFightCameraAPI.getInstance().zoomOut(zoomOutTicks);
    }
}
