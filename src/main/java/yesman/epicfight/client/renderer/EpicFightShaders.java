package yesman.epicfight.client.renderer;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public class EpicFightShaders {
	public static ShaderInstance positionColorNormalShader;
	
	@Nullable
	public static ShaderInstance getPositionColorNormalShader() {
		return positionColorNormalShader;
	}
	
	@SubscribeEvent
	public static void registerShadersEvent(RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL), reloadedShader -> {
			positionColorNormalShader = reloadedShader;
		});
	}
}
