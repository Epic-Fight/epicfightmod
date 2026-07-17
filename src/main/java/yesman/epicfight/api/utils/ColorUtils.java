package yesman.epicfight.api.utils;

import net.minecraft.util.Mth;

/** Color interpolation helpers shared by non-visual client systems. */
public final class ColorUtils {
	private static final int[] HUE_COLORS = {
		0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
	};

	public static int interpolate(double value, int... colors) {
		int colorBlocks = colors.length - 1;
		for (int i = 0; i < colorBlocks; i++) {
			double min = 1.0D / colorBlocks * i;
			double max = 1.0D / colorBlocks * (i + 1);
			if (value >= min && value <= max) {
				double factor = (value - min) / (max - min);
				int start = colors[i];
				int end = colors[i + 1];
				int alpha = (int)Mth.lerp(factor, start >> 24 & 255, end >> 24 & 255);
				int red = (int)Mth.lerp(factor, start >> 16 & 255, end >> 16 & 255);
				int green = (int)Mth.lerp(factor, start >> 8 & 255, end >> 8 & 255);
				int blue = (int)Mth.lerp(factor, start & 255, end & 255);
				return alpha << 24 | red << 16 | green << 8 | blue;
			}
		}
		return 0;
	}

	public static int hue(double value) {
		return interpolate(value, HUE_COLORS);
	}

	private ColorUtils() {
	}
}
