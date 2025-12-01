package yesman.epicfight.api.client.model;

import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public interface MeshPartDefinition {
	String partName();
	Mesh.RenderProperties renderProperties();
	Supplier<Matrix4f> getModelPartAnimationProvider();
}