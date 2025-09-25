package yesman.epicfight.api.client.model;

import java.util.function.Supplier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

@OnlyIn(Dist.CLIENT)
public interface MeshPartDefinition {
	String partName();
	Mesh.RenderProperties renderProperties();
	Supplier<OpenMatrix4f> getModelPartAnimationProvider();
}