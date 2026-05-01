package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Locale;
import java.util.NoSuchElementException;

public class DatapackAttackAnimation extends AttackAnimation implements DatapackAnimation<DatapackAttackAnimation> {
	private static Phase[] convertListTagToPhases(ListTag listTag, Armature armature) {
		float start = 0.0F;
		Phase[] phases = new Phase[listTag.size()];
		
		int i = 0;
		
		for (Tag phaseTag : listTag) {
			CompoundTag phaseCompTag = (CompoundTag)phaseTag;
			
			if (!phaseCompTag.contains("antic")) {
				throw new NoSuchElementException("Phase" + i + ": Antic not specified");
			}
			
			if (!phaseCompTag.contains("preDelay")) {
				throw new NoSuchElementException("Phase" + i + ": Pre-Delay not specified");
			}
			
			if (!phaseCompTag.contains("contact")) {
				throw new NoSuchElementException("Phase" + i + ": Contact not specified");
			}
			
			if (!phaseCompTag.contains("recovery")) {
				throw new NoSuchElementException("Phase" + i + ": Recovery not specified");
			}
			
			if (!phaseCompTag.contains("hand")) {
				throw new NoSuchElementException("Phase" + i + ": Hand not specified");
			}
			
			if (!phaseCompTag.contains("joint")) {
				throw new NoSuchElementException("Phase" + i + ": Joint not specified");
			}
			
			float antic = phaseCompTag.getFloat("antic");
			float preDelay = phaseCompTag.getFloat("preDelay");
			float contact = phaseCompTag.getFloat("contact");
			float recovery = phaseCompTag.getFloat("recovery");
			InteractionHand hand = InteractionHand.valueOf(phaseCompTag.getString("hand").toUpperCase(Locale.ROOT));
			
			String armature$joint = phaseCompTag.getString("joint");
			String joinName = armature$joint.substring(armature$joint.lastIndexOf('.') + 1);
			Joint joint =  armature.searchJointByName(joinName);
			Collider collider = null;
			
			try {
				collider = ColliderPreset.deserializeSimpleCollider(phaseCompTag.getCompound("collider"));
			} catch (Exception e) {
			}
			
			phases[i] = new Phase(start, antic, preDelay, contact, recovery, recovery, hand, joint, collider);
			start = recovery;
			i++;
		}
		
		return phases;
	}
	
	protected AnimationClip clip;
	protected EditorAnimation fakeAnimation;
	protected ResourceLocation registryName;
	
	public DatapackAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, ListTag phases) {
		super(convertTime, path, armature, convertListTagToPhases(phases, armature.get()));
		
		((DatapackAnimation<DatapackAttackAnimation>)this).setRegistryName(ResourceLocation.parse(path));
		this.accessor = this;
	}
	
	public DatapackAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, Phase... phases) {
		super(convertTime, path, armature, phases);
		
		((DatapackAnimation<DatapackAttackAnimation>)this).setRegistryName(ResourceLocation.parse(path));
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
		this.clip = clip;
	}
	
	@Override
	public AnimationClip getAnimationClip() {
		return this.clip;
	}
	
	@Override
	public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
		animationPlayer.setPlayAnimation(this);
		animationPlayer.tick(entitypatch);
	}
	
	@Override
	public EditorAnimation readAnimationFromJson(JsonAssetLoader.TransformFormat transformFormat, JsonArray rawAnimationJson) {
		EditorAnimation fakeAnimation = new EditorAnimation(this.registryName().toString(), this.armature, this.clip, transformFormat, rawAnimationJson);
		fakeAnimation.setAnimationClass(EditorAnimation.AnimationType.ATTACK);
		fakeAnimation.setParameter("convertTime", this.transitionTime);
		fakeAnimation.setParameter("path", this.registryName().toString());
		fakeAnimation.setParameter("armature", this.armature);
		
		ListTag listTag = new ListTag();
		
		for (AttackAnimation.Phase phase : this.phases) {
			CompoundTag compTag = new CompoundTag();
			
			compTag.putFloat("start", phase.start);
			compTag.putFloat("antic", phase.antic);
			compTag.putFloat("preDelay", phase.preDelay);
			compTag.putFloat("contact", phase.contact);
			compTag.putFloat("recovery", phase.recovery);
			compTag.putFloat("end", phase.end);
			compTag.putString("hand", phase.hand.toString());
			
			if (phase.colliders[0].getSecond() != null) {
				compTag.put("collider", phase.colliders[0].getSecond().serialize(new CompoundTag()));
			}
			
			compTag.putString("joint", this.armature.registryName() +"."+ phase.colliders[0].getFirst().getName());
			
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

	@Override
	public DatapackAttackAnimation get() {
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
}
