package yesman.epicfight.mixin.skinlayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.platform.NativeImage;

import dev.tr7zw.skinlayers.SkinUtil;
import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(value = SkinUtil.class, remap = false)
public interface MixinSkinUtil {
	@Invoker("getSkinTexture")
	public static NativeImage invokeGetSkinTexture(AbstractClientPlayer player) {
		throw new AssertionError();
	}
}
