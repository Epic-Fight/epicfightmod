package yesman.epicfight.api.client.model.transformer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CommonMethods
{
    public static Matrix4f createInvertedParentTransform(PoseStack poseStack)
    {
        return new Matrix4f(poseStack.last().pose()).setTranslation(poseStack.last().pose().getTranslation(new Vector3f()).mul(0.0625f)).invert();
    }
}
