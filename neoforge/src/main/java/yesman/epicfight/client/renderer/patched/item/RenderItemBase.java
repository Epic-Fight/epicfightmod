package yesman.epicfight.client.renderer.patched.item;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.data.RenderModifier;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.model.armature.types.ToolHolderArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class RenderItemBase {
	protected static final Map<String, OpenMatrix4f> GLOBAL_MAINHAND_ITEM_TRANSFORMS = ImmutableMap.<String, OpenMatrix4f>builder()
		.put("Tool_L", new OpenMatrix4f().translate(0F, 0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS).unmodifiable())
		.put("Tool_R", new OpenMatrix4f().translate(0F, 0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS).unmodifiable())
		.put("Chest",
			new OpenMatrix4f(
				  3.3484866E-8F, -2.809714E-8F, -0.99999994F, 0.0F
				, -0.6427876F, -0.7660444F, 0.0F, 0.0F
				, -0.76604444F, 0.64278764F, -4.3711385E-8F, 0.0F
				, 0.25711504F, 0.30641776F, 0.14999999F, 1.0F
			).unmodifiable()
		)
		.put("Root", new OpenMatrix4f().unmodifiable())
		.build();
	
	protected static final Map<String, OpenMatrix4f> GLOBAL_OFFHAND_ITEM_TRANSFORMS = ImmutableMap.<String, OpenMatrix4f>builder()
		.put("Tool_L", new OpenMatrix4f().translate(0F, 0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS).unmodifiable())
		.put("Tool_R", new OpenMatrix4f().translate(0F, 0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS).unmodifiable())
		.put("Chest",
			new OpenMatrix4f(
				  3.3484866E-8F, 2.809714E-8F, 0.99999994F, 0.0F
				, 0.6427876F, -0.7660444F, 0.0F, 0.0F
				, 0.76604444F, 0.64278764F, -4.3711385E-8F, 0.0F
				, -0.25711504F, 0.30641776F, 0.15099998F, 1.0F
			).unmodifiable()
		)
		.put("Root", new OpenMatrix4f().unmodifiable())
		.build();
	
	protected static ItemRenderer itemRenderer;
	protected static ItemInHandRenderer itemInHandRenderer;
	
	public static void initItemRenderers(Minecraft minecraft) {
		if (itemRenderer != null || itemInHandRenderer != null) {
			throw new IllegalStateException("Already initialized item renderers");
		}
		
		itemRenderer = minecraft.getItemRenderer();
		itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
	}
	
	protected final Map<String, OpenMatrix4f> mainhandCorrectionTransforms;
	protected final Map<String, OpenMatrix4f> offhandCorrectionTransforms;
	private final TrailInfo trailInfo;
	private final boolean alwaysInHand;
	private final boolean forceVanillaFirstPerson;
	private final boolean appearedInAfterimage;
	
	public RenderItemBase(JsonElement jsonElement) {
		JsonObject jsonObj = jsonElement.getAsJsonObject();
		
		this.trailInfo = jsonObj.has("trail") ? TrailInfo.deserialize(jsonObj.get("trail")) : null;
		this.forceVanillaFirstPerson = jsonObj.has("force_vanilla_first_person") && GsonHelper.getAsBoolean(jsonObj, "force_vanilla_first_person");
		this.alwaysInHand = jsonObj.has("alwaysInHand") && GsonHelper.getAsBoolean(jsonObj, "alwaysInHand");
		this.appearedInAfterimage = jsonObj.has("appeared_in_afterimage") ? GsonHelper.getAsBoolean(jsonObj, "appeared_in_afterimage") : true;
		
		if (!jsonObj.has("transforms")) {
			// Set a global transformation
			this.mainhandCorrectionTransforms = GLOBAL_MAINHAND_ITEM_TRANSFORMS;
			this.offhandCorrectionTransforms = GLOBAL_OFFHAND_ITEM_TRANSFORMS;
		} else {
			JsonObject handEntry = jsonObj.get("transforms").getAsJsonObject();
			
			if (handEntry.has("mainhand")) {
				ImmutableMap.Builder<String, OpenMatrix4f> mainhandBuilder = ImmutableMap.builder();
				
				for (Map.Entry<String, JsonElement> entry : handEntry.get("mainhand").getAsJsonObject().entrySet()) {
					JsonObject transformEntry = entry.getValue().getAsJsonObject();
					OpenMatrix4f matrix = new OpenMatrix4f();
					
					if (transformEntry.has("translation")) {
						JsonArray values = transformEntry.get("translation").getAsJsonArray();
						matrix.translate(values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat());
					}
					
					if (transformEntry.has("rotation")) {
						JsonArray values = transformEntry.get("rotation").getAsJsonArray();
						matrix.rotateDeg(values.get(2).getAsFloat(), Vec3f.Z_AXIS);
						matrix.rotateDeg(values.get(1).getAsFloat(), Vec3f.Y_AXIS);
						matrix.rotateDeg(values.get(0).getAsFloat(), Vec3f.X_AXIS);
					}
					
					if (transformEntry.has("scale")) {
						JsonArray values = transformEntry.get("scale").getAsJsonArray();
						matrix.scale(values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat());
					}
					
					mainhandBuilder.put(entry.getKey(), matrix.unmodifiable());
				}
				
				this.mainhandCorrectionTransforms = mainhandBuilder.build();
			} else {
				this.mainhandCorrectionTransforms = GLOBAL_MAINHAND_ITEM_TRANSFORMS;
			}
			
			if (handEntry.has("offhand")) {
				ImmutableMap.Builder<String, OpenMatrix4f> offhandBuilder = ImmutableMap.builder();

				for (Map.Entry<String, JsonElement> entry : handEntry.get("offhand").getAsJsonObject().entrySet()) {
					JsonObject transformEntry = entry.getValue().getAsJsonObject();
					OpenMatrix4f matrix = new OpenMatrix4f();
					
					if (transformEntry.has("translation")) {
						JsonArray values = transformEntry.get("translation").getAsJsonArray();
						matrix.translate(values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat());
					}
					
					if (transformEntry.has("rotation")) {
						JsonArray values = transformEntry.get("rotation").getAsJsonArray();
						matrix.rotateDeg(values.get(2).getAsFloat(), Vec3f.Z_AXIS);
						matrix.rotateDeg(values.get(1).getAsFloat(), Vec3f.Y_AXIS);
						matrix.rotateDeg(values.get(0).getAsFloat(), Vec3f.X_AXIS);
					}
					
					if (transformEntry.has("scale")) {
						JsonArray values = transformEntry.get("scale").getAsJsonArray();
						matrix.scale(values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat());
					}
					
					offhandBuilder.put(entry.getKey(), matrix.unmodifiable());
				}
				
				this.offhandCorrectionTransforms = offhandBuilder.build();
			} else {
				this.offhandCorrectionTransforms = GLOBAL_OFFHAND_ITEM_TRANSFORMS;
			}
		}
	}

	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        CapabilityItem cap = entitypatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if (cap instanceof WeaponCapability wCap) {
            MoveSet set = wCap.getCurrentSet(entitypatch);
            if (set != null) {
                RenderModifier modifier = set.getRenderModifier();
                if (modifier != null && modifier.modify(
                        this, stack, entitypatch, hand, poses, buffer,
                        poseStack, packedLight, partialTicks,
                        itemRenderer, itemInHandRenderer)) {
                    return;
                }
            }
        }

        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		ItemDisplayContext transformType = (hand == InteractionHand.MAIN_HAND) ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		itemInHandRenderer.renderItem(entitypatch.getOriginal(), stack, transformType, hand == InteractionHand.OFF_HAND, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
	
	public final OpenMatrix4f transformHolder = new OpenMatrix4f();
	
	public OpenMatrix4f getCorrectionMatrix(LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses) {
		Joint parentJoint = null;
		
		if (this.alwaysInHand) {
			if (entitypatch.getArmature() instanceof ToolHolderArmature toolArmature) {
				parentJoint = hand == InteractionHand.MAIN_HAND ? toolArmature.rightToolJoint() : toolArmature.leftToolJoint();
			}
			
			if (parentJoint == null) {
				parentJoint = entitypatch.getArmature().rootJoint;
			}
		} else {
			parentJoint = entitypatch.getParentJointOfHand(hand);
		}
		
		switch (hand) {
		case MAIN_HAND -> this.transformHolder.load(this.mainhandCorrectionTransforms.getOrDefault(parentJoint.getName(), GLOBAL_MAINHAND_ITEM_TRANSFORMS.get(parentJoint.getName())));
		case OFF_HAND -> this.transformHolder.load(this.offhandCorrectionTransforms.getOrDefault(parentJoint.getName(), GLOBAL_OFFHAND_ITEM_TRANSFORMS.get(parentJoint.getName())));
		}
		
		this.transformHolder.mulFront(poses[parentJoint.getId()]);
		
		return this.transformHolder;
	}
	
	public TrailInfo trailInfo() {
		return this.trailInfo;
	}
	
	public boolean forceVanillaFirstPerson() {
		return this.forceVanillaFirstPerson;
	}
	
	public boolean appearedInAfterimage() {
		return this.appearedInAfterimage;
	}
}