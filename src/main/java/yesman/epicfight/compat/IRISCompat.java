package yesman.epicfight.compat;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.shader.AnimationShaderInstance;
import yesman.epicfight.config.ClientConfig;

public class IRISCompat implements ICompatModule {
	@OnlyIn(Dist.CLIENT)
	private static final Map<String, Supplier<AnimationShaderInstance>> IRIS_SHADER_PROVIDERS = Maps.newHashMap();
	
	@OnlyIn(Dist.CLIENT)
	public static void putIrisShaderProvider(String name, Supplier<AnimationShaderInstance> shaderSupplier) {
		EpicFightRenderTypes.clearAnimationShaderInstance(name);
		IRIS_SHADER_PROVIDERS.put(name, shaderSupplier);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void clearIrisShaders() {
		ClientConfig.animationShaderLockedByException = false;
		IRIS_SHADER_PROVIDERS.clear();
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		/*EpicFightRenderTypes.registerShaderTransformer((shaderInstance) -> (shaderInstance instanceof net.irisshaders.iris.pipeline.programs.ExtendedShader), (shaderInstance) -> {
			return IRIS_SHADER_PROVIDERS.get(shaderInstance.getName()).get();
		});*/
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
}
