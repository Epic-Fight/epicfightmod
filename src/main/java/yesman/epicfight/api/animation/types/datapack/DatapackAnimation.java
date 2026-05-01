package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.JsonAssetLoader;

public interface DatapackAnimation<A extends StaticAnimation> extends AnimationAccessor<A> {
	void setAnimationClip(AnimationClip clip);
	void setRegistryName(ResourceLocation registryName);
	void setCreator(EditorAnimation fakeAnimation);
	EditorAnimation getCreator();
	EditorAnimation readAnimationFromJson(JsonAssetLoader.TransformFormat transformFormat, JsonArray rawAnimationJson);
	
	@Override
	default int id() {
		return -1;
	}
	
	@Override
	default boolean inRegistry() {
		return false;
	}
}