package yesman.epicfight.api.utils;

import org.joml.Vector4f;
import org.joml.Vector4i;

import net.minecraft.util.Mth;

public abstract class ColorUtil {
	public static int packToARGB(int r, int g, int b, int a) {
		int ia = a << 24;
		int ir = r << 16;
		int ig = g << 8;
		int ib = b;
		
		return ir | ig | ib | ia;
	}
	
	// Thread-unsafe
	private static final Vector4i COLOR_HOLDER_I = new Vector4i();
	
	public static Vector4i unpackToARGBI(int packedColor) {
		int a = (packedColor & 0xFF000000) >>> 24;
		int r = (packedColor & 0x00FF0000) >>> 16;
		int g = (packedColor & 0x0000FF00) >>> 8;
		int b = (packedColor & 0x000000FF);
		
		COLOR_HOLDER_I.x = r;
		COLOR_HOLDER_I.y = g;
		COLOR_HOLDER_I.z = b;
		COLOR_HOLDER_I.w = a;
		
		return COLOR_HOLDER_I;
	}
	
	// Thread-unsafe
	private static final Vector4f COLOR_HOLDER_F = new Vector4f();
	
	public static Vector4f unpackToARGBF(int packedColor) {
		int a = (packedColor & 0xFF000000) >>> 24;
		int r = (packedColor & 0x00FF0000) >>> 16;
		int g = (packedColor & 0x0000FF00) >>> 8;
		int b = (packedColor & 0x000000FF);
		
		COLOR_HOLDER_F.x = r / 255.0F;
		COLOR_HOLDER_F.y = g / 255.0F;
		COLOR_HOLDER_F.z = b / 255.0F;
		COLOR_HOLDER_F.w = a / 255.0F;
		
		return COLOR_HOLDER_F;
	}
	
	public static int mixPackedARGB(double delta, int c1, int c2) {
		ColorUtil.unpackToARGBI(c1);
		int startR = COLOR_HOLDER_I.x;
		int startG = COLOR_HOLDER_I.y;
		int startB = COLOR_HOLDER_I.z;
		int startA = COLOR_HOLDER_I.w;
		
		ColorUtil.unpackToARGBI(c2);
		int endR = COLOR_HOLDER_I.x;
		int endG = COLOR_HOLDER_I.y;
		int endB = COLOR_HOLDER_I.z;
		int endA = COLOR_HOLDER_I.w;
		
		int mixR = (int) Mth.lerp(delta, startR, endR);
		int mixG = (int) Mth.lerp(delta, startG, endG);
		int mixB = (int) Mth.lerp(delta, startB, endB);
		int mixA = (int) Mth.lerp(delta, startA, endA);
		
		return ColorUtil.packToARGB(mixR, mixG, mixB, mixA);
	}
}
