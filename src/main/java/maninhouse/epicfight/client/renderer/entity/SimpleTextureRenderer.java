package maninhouse.epicfight.client.renderer.entity;

import maninhouse.epicfight.capabilities.entity.LivingData;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleTextureRenderer<E extends LivingEntity, T extends LivingData<E>, M extends EntityModel<E>> extends ArmatureRenderer<E, T, M> {
	public final ResourceLocation textureLocation;
	
	public SimpleTextureRenderer(String texturePath) {
		this.textureLocation = new ResourceLocation(texturePath);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(E entityIn) {
		return this.textureLocation;
	}
}
