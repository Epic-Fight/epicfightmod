package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.model.Armature;

public class DatapackBasicAttackAnimation extends DatapackAttackAnimation {
	public DatapackBasicAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, ListTag phases) {
		super(convertTime, path, armature, phases);
	}
	
	public DatapackBasicAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, Phase... phases) {
		super(convertTime, path, armature, phases);
	}
	
	@Override
	public EditorAnimation readAnimationFromJson(JsonAssetLoader.TransformFormat transformFormat, JsonArray rawAnimationJson) {
		EditorAnimation fakeAnimation = new EditorAnimation(this.registryName().toString(), this.armature, this.clip, transformFormat, rawAnimationJson);
		fakeAnimation.setAnimationClass(EditorAnimation.AnimationType.BASIC_ATTACK);
		fakeAnimation.setParameter("convertTime", this.transitionTime);
		fakeAnimation.setParameter("path", this.registryName().toString());
		fakeAnimation.setParameter("armature", this.armature);
		
		ListTag listTag = new ListTag();
		
		for (AttackAnimation.Phase phase : this.phases) {
			CompoundTag compTag = new CompoundTag();
			
			compTag.putFloat("antic", phase.antic);
			compTag.putFloat("preDelay", phase.preDelay);
			compTag.putFloat("contact", phase.contact);
			compTag.putFloat("recovery", phase.recovery);
			compTag.putString("hand", phase.hand.toString());
			
			if (phase.colliders[0].getSecond() != null) {
				compTag.put("collider", phase.colliders[0].getSecond().serialize(new CompoundTag()));
			}
			
			compTag.putString("joint", this.armature.toString() +"."+ phase.colliders[0].getFirst().getName());
			
			listTag.add(compTag);
		}
		
		fakeAnimation.setParameter("phases", listTag);
		
		this.getProperty(ClientAnimationProperties.TRAIL_EFFECT).ifPresent((trailInfos) -> {
			JsonArray trailArray = new JsonArray();
			
			for (TrailInfo trailInfo : trailInfos) {
				JsonObject trailObj = new JsonObject();
				trailObj.addProperty("start_time", trailInfo.startTime());
				trailObj.addProperty("end_time", trailInfo.endTime());
				trailObj.addProperty("joint", trailInfo.joint());
				trailObj.addProperty("item_skin_hand", trailInfo.hand().toString());
				trailArray.add(trailObj);
			}
			
			fakeAnimation.getPropertiesJson().add("trail_effects", trailArray);
			fakeAnimation.addProperty(ClientAnimationProperties.TRAIL_EFFECT, trailInfos);
		});
		
		this.fakeAnimation = fakeAnimation;
		
		return fakeAnimation;
	}
}
