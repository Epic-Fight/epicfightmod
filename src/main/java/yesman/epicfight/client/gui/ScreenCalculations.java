package yesman.epicfight.client.gui;

import java.util.function.BiFunction;

import yesman.epicfight.api.utils.math.Vec2i;

public class ScreenCalculations {
	private static final BiFunction<Integer, Integer, Integer> ORIGIN = ((screenLength, value) -> value);
	private static final BiFunction<Integer, Integer, Integer> SCREEN_EDGE = ((screenLength, value) -> screenLength - value);
	private static final BiFunction<Integer, Integer, Integer> CENTER = ((screenLength, value) -> screenLength / 2 + value);
	private static final BiFunction<Integer, Integer, Integer> CENTER_SAVE = ((screenLength, value) -> value - screenLength / 2);
	
	public enum HorizontalBasis {
		LEFT(ScreenCalculations.ORIGIN, ScreenCalculations.ORIGIN), RIGHT(ScreenCalculations.SCREEN_EDGE, ScreenCalculations.SCREEN_EDGE), CENTER(ScreenCalculations.CENTER, ScreenCalculations.CENTER_SAVE);
		
		public final BiFunction<Integer, Integer, Integer> positionGetter;
		public final BiFunction<Integer, Integer, Integer> saveCoordGetter;
		
		HorizontalBasis(BiFunction<Integer, Integer, Integer> positionGetter, BiFunction<Integer, Integer, Integer> saveCoordGetter) {
			this.positionGetter = positionGetter;
			this.saveCoordGetter = saveCoordGetter;
		}
	}
	
	public enum VerticalBasis {
		TOP(ScreenCalculations.ORIGIN, ScreenCalculations.ORIGIN), BOTTOM(ScreenCalculations.SCREEN_EDGE, ScreenCalculations.SCREEN_EDGE), CENTER(ScreenCalculations.CENTER, ScreenCalculations.CENTER_SAVE);
		
		public final BiFunction<Integer, Integer, Integer> positionGetter;
		public final BiFunction<Integer, Integer, Integer> saveCoordGetter;
		
		VerticalBasis(BiFunction<Integer, Integer, Integer> positionGetter, BiFunction<Integer, Integer, Integer> saveCoordGetter) {
			this.positionGetter = positionGetter;
			this.saveCoordGetter = saveCoordGetter;
		}
	}
	
	@FunctionalInterface
	public interface StartCoordGetter {
		Vec2i get(int x, int y, int width, int height, int icons, HorizontalBasis horBasis, VerticalBasis verBasis);
	}
	
	private static final StartCoordGetter START_HORIZONTAL = (x, y, width, height, icons, horBasis, verBasis) -> {
		if (horBasis == HorizontalBasis.CENTER) {
			return new Vec2i(x - width * (icons - 1) / 2, y);
		} else {
			return new Vec2i(x, y);
		}
	};
	
	private static final StartCoordGetter START_VERTICAL = (x, y, width, height, icons, horBasis, verBasis) -> {
		if (verBasis == VerticalBasis.CENTER) {
			return new Vec2i(x, y - height * (icons - 1) / 2);
		} else {
			return new Vec2i(x, y);
		}
	};
	
	@FunctionalInterface
	public interface NextCoordGetter {
		Vec2i getNext(HorizontalBasis horBasis, VerticalBasis verBasis, Vec2i prevCoord, int width, int height);
	}
	
	private static final NextCoordGetter NEXT_HORIZONTAL = (horBasis, verBasis, oldPos, width, height) -> {
		if (horBasis == HorizontalBasis.LEFT || horBasis == HorizontalBasis.CENTER) {
			return new Vec2i(oldPos.x + width, oldPos.y);
		} else {
			return new Vec2i(oldPos.x - width, oldPos.y);
		}
	};
	
	private static final NextCoordGetter NEXT_VERTICAL = (horBasis, verBasis, oldPos, width, height) -> {
		if (verBasis == VerticalBasis.TOP || verBasis == VerticalBasis.CENTER) {
			return new Vec2i(oldPos.x, oldPos.y + height);
		} else {
			return new Vec2i(oldPos.x, oldPos.y - height);
		}
	};
	
	public enum AlignDirection {
		HORIZONTAL(START_HORIZONTAL, NEXT_HORIZONTAL), VERTICAL(START_VERTICAL, NEXT_VERTICAL);
		
		public final StartCoordGetter startCoordGetter;
		public final NextCoordGetter nextPositionGetter;
		
		AlignDirection(StartCoordGetter startCoordGetter, NextCoordGetter nextPositionGetter) {
			this.startCoordGetter = startCoordGetter;
			this.nextPositionGetter = nextPositionGetter;
		}
	}
}