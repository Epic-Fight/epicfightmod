package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

@OnlyIn(Dist.CLIENT)
public interface DatapackAnimation<A extends StaticAnimation> extends AnimationAccessor<A> {
	public void setAnimationClip(AnimationClip clip);
	public void setRegistryName(ResourceLocation registryName);
	public void setCreator(EditorAnimation fakeAnimation);
	public EditorAnimation getCreator();
	public EditorAnimation readAnimationFromJson(JsonArray rawAnimationJson);
	
	@Override
	default int id() {
		return -1;
	}
	
	@Override
	default boolean inRegistry() {
		return false;
	}
}