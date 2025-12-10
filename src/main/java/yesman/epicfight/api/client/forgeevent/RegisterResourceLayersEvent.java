package yesman.epicfight.api.client.forgeevent;

import java.util.Map;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RegisterResourceLayersEvent<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LivingEntityRenderer<E, M>, AM extends SkinnedMesh> extends Event {
	private final Map<ResourceLocation, LayerUtil.LayerProvider<E, T, M, R, AM>> layersbyid;
	
	public RegisterResourceLayersEvent(Map<ResourceLocation, LayerUtil.LayerProvider<E, T, M, R, AM>> layersbyid) {
		this.layersbyid = layersbyid;
	}
	
	public void register(ResourceLocation rl, LayerUtil.LayerProvider<E, T, M, R, AM> layerAdder) {
		this.layersbyid.put(rl, layerAdder);
	}
}