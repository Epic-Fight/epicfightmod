package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMask.JointMaskSet;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Map;

public class DatapackStaticAnimation extends StaticAnimation implements DatapackAnimation<DatapackStaticAnimation> {
	protected EditorAnimation fakeAnimation;
	protected ResourceLocation registryName;
	
	public DatapackStaticAnimation(float convertTime, boolean isRepeat, String path, AssetAccessor<? extends Armature> armature) {
		super(convertTime, isRepeat, path, armature);
		
		((DatapackAnimation<DatapackStaticAnimation>)this).setRegistryName(ResourceLocation.parse(path));
		this.accessor = this;
	}
	
	@Override
	public void setCreator(EditorAnimation fakeAnimation) {
		this.fakeAnimation = fakeAnimation;
	}
	
	@Override
	public EditorAnimation getCreator() {
		return this.fakeAnimation;
	}
	
	@Override
	public void setAnimationClip(AnimationClip clip) {
		this.animationClip = clip;
	}
	
	@Override
	public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
		animationPlayer.setPlayAnimation(this);
		animationPlayer.tick(entitypatch);
	}
	
	@Override
	public DatapackStaticAnimation get() {
		return this;
	}

	@Override
	public void setRegistryName(ResourceLocation registryName) {
		this.registryName = registryName;
	}
	
	@Override
	public ResourceLocation registryName() {
		return this.registryName;
	}

	@Override
	public boolean isPresent() {
		return true;
	}
	
	@Override
	public EditorAnimation readAnimationFromJson(JsonAssetLoader.TransformFormat transformFormat, JsonArray rawAnimationJson) {
		EditorAnimation fakeAnimation = new EditorAnimation(this.registryName().toString(), this.armature, this.animationClip, transformFormat, rawAnimationJson);
		fakeAnimation.setAnimationClass(EditorAnimation.AnimationType.STATIC);
		fakeAnimation.setParameter("convertTime", this.transitionTime);
		fakeAnimation.setParameter("isRepeat", this.isRepeat());
		fakeAnimation.setParameter("path", this.registryName().toString());
		fakeAnimation.setParameter("armature", this.armature);
		final JsonObject propertiesJson = fakeAnimation.getPropertiesJson();
		
		this.getProperty(ClientAnimationProperties.MULTILAYER_ANIMATION).ifPresentOrElse((multilayer) -> {
			JsonObject multilayerJson = new JsonObject();
			JsonObject baseJson = new JsonObject();
			baseJson.addProperty("priority", multilayer.getPriority().toString());
			final JsonArray baseMasks = new JsonArray();
			
			this.getProperty(ClientAnimationProperties.JOINT_MASK).ifPresent((jointMaskEntry) -> {
				for (Map.Entry<LivingMotion, JointMaskSet> entry : jointMaskEntry.getEntries()) {
					JsonObject maskObj = new JsonObject();
					maskObj.addProperty("livingmotion", entry.getKey().toString());
					maskObj.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
					baseMasks.add(maskObj);
				}
				
				JsonObject maskObj = new JsonObject();
				JointMaskSet defaultMask = jointMaskEntry.getDefaultMask();
				maskObj.addProperty("livingmotion", LivingMotions.ALL.toString());
				maskObj.addProperty("type", JointMaskReloadListener.getKey(defaultMask).toString());
				baseMasks.add(maskObj);
			});
			
			baseJson.add("masks", baseMasks);
			JsonObject compositeJson = new JsonObject();
			compositeJson.addProperty("priority", this.getPriority().toString());
			final JsonArray compositeMasks = new JsonArray();
			
			multilayer.getProperty(ClientAnimationProperties.JOINT_MASK).ifPresent((jointMaskEntry) -> {
				for (Map.Entry<LivingMotion, JointMaskSet> entry : jointMaskEntry.getEntries()) {
					JsonObject maskObj = new JsonObject();
					maskObj.addProperty("livingmotion", entry.getKey().toString());
					maskObj.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
					compositeMasks.add(maskObj);
				}
				
				JsonObject maskObj = new JsonObject();
				JointMaskSet defaultMask = jointMaskEntry.getDefaultMask();
				maskObj.addProperty("livingmotion", LivingMotions.ALL.toString());
				maskObj.addProperty("type", JointMaskReloadListener.getKey(defaultMask).toString());
				compositeMasks.add(maskObj);
			});
			
			baseJson.add("masks", compositeMasks);
			multilayerJson.add("base", baseJson);
			multilayerJson.add("composite", compositeJson);
			propertiesJson.add("multilayer", multilayerJson);
		}, () -> {
			final JsonArray masks = new JsonArray();
			
			this.getProperty(ClientAnimationProperties.JOINT_MASK).ifPresent((jointMaskEntry) -> {
				for (Map.Entry<LivingMotion, JointMaskSet> entry : jointMaskEntry.getEntries()) {
					JsonObject maskObj = new JsonObject();
					maskObj.addProperty("livingmotion", entry.getKey().toString());
					maskObj.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
					masks.add(maskObj);
				}
				
				JsonObject maskObj = new JsonObject();
				JointMaskSet defaultMask = jointMaskEntry.getDefaultMask();
				maskObj.addProperty("livingmotion", LivingMotions.ALL.toString());
				maskObj.addProperty("type", JointMaskReloadListener.getKey(defaultMask).toString());
				masks.add(maskObj);
			});
			
			if (!masks.isEmpty()) {
				propertiesJson.add("masks", masks);
			}
			
			propertiesJson.addProperty("layer", this.getLayerType().toString());
			propertiesJson.addProperty("priority", this.getPriority().toString());
		});
		
		this.fakeAnimation = fakeAnimation;
		
		return fakeAnimation;
	}
}
