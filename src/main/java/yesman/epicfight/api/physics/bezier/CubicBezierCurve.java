package yesman.epicfight.api.physics.bezier;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/// Get more informations about cubic bezier curve here:
/// [...](https://omaraflak.medium.com/bézier-interpolation-8033e9a262c2)
public class CubicBezierCurve {
	private static final DoubleList MATRIX_CONSTANTS = new DoubleArrayList();
	
	static {
		MATRIX_CONSTANTS.add(0.5D);
	}
	
	private static void getBezierEquationCoefficients(DoubleList points, DoubleList aList, DoubleList bList) {
		DoubleList results = new DoubleArrayList();
		int size = points.size();
		
		results.add(points.getDouble(0) + points.getDouble(1) * 2);
		
		for (int idx = 1; idx < size - 2; idx++) {
			results.add(points.getDouble(idx) * 4 + points.getDouble(idx + 1) * 2);
		}
		
		results.add(points.getDouble(size - 2) * 8 + points.getDouble(size - 1));
		
		int storedConstsSize = MATRIX_CONSTANTS.size();
		int coordSize = results.size();
		
		if (storedConstsSize < coordSize - 1) {
			for (int i = 0; i < (coordSize - 1) - storedConstsSize; i++) {
				double lastConst = MATRIX_CONSTANTS.getDouble(MATRIX_CONSTANTS.size() - 1);
				MATRIX_CONSTANTS.add(1.0D / (4.0D - lastConst));
			}
		}
		
		DoubleList convertedResults = new DoubleArrayList();
		
		for (int idx = 0; idx < coordSize; idx++) {
			if (idx == 0) {
				convertedResults.add(results.getDouble(idx) * 0.5D);
			} else if (idx == coordSize - 1) {
				convertedResults.add((results.getDouble(idx) - 2 * convertedResults.getDouble(idx - 1)) / (7.0D - 2.0 * MATRIX_CONSTANTS.getDouble(idx - 1)));
			} else {
				convertedResults.add((results.getDouble(idx) - convertedResults.getDouble(idx - 1)) / (4.0D - MATRIX_CONSTANTS.getDouble(idx - 1)));
			}
		}
		
		for (int idx = coordSize - 1; idx >= 0; idx--) {
			if (idx == coordSize - 1) {
				aList.add(0, convertedResults.getDouble(idx));
			} else {
				aList.add(0, convertedResults.getDouble(idx) - aList.getDouble(0) * MATRIX_CONSTANTS.getDouble(idx));
			}
		}
		
		for (int i = 0; i < coordSize; i++) {
			if (i == coordSize - 1) {
				bList.add((aList.getDouble(i) + points.getDouble(i + 1)) * 0.5D);
			} else {
				bList.add(2 * points.getDouble(i + 1) - aList.getDouble(i + 1));
			}
		}
	}
	
	private static double cubicBezier(double start, double end, double a, double b, double t) {
		return Math.pow((1 - t), 3) * start + 3 * t * Math.pow((1 - t), 2) * a + 3 * t * t * (1 - t) * b + t * t * t * end; 
	}
	
	public static List<Vec3> getBezierInterpolatedPoints(List<Vec3> points, int interpolatedResults) {
		return getBezierInterpolatedPoints(points, 0, points.size() - 1, interpolatedResults);
	}
	
	/**
	 * Requires at least 3 points
	 * @param points : control points of the bezier curve
	 * @param sliceBegin : first control point to calculate the interpolation
	 * @param sliceEnd : last control point to calculate the interpolation
	 * @param interpolatedResults : the number of interpolated vertices between control points
	 * @return
	 */
	public static List<Vec3> getBezierInterpolatedPoints(List<Vec3> points, int sliceBegin, int sliceEnd, int interpolatedResults) {
		if (points.size() < 3) {
			return null;
		}
		
		sliceBegin = Math.max(sliceBegin, 0);
		sliceEnd = Math.min(sliceEnd, points.size() - 1);
		
		int size = points.size();
		List<Vec3> interpolatedPoints = Lists.newArrayList();
		DoubleList x = new DoubleArrayList();
		DoubleList y = new DoubleArrayList();
		DoubleList z = new DoubleArrayList();
		
		for (int idx = 0; idx < size; idx++) {
			x.add(points.get(idx).x);
			y.add(points.get(idx).y);
			z.add(points.get(idx).z);
		}
		
		DoubleList x_a = new DoubleArrayList();
		DoubleList x_b = new DoubleArrayList();
		DoubleList y_a = new DoubleArrayList();
		DoubleList y_b = new DoubleArrayList();
		DoubleList z_a = new DoubleArrayList();
		DoubleList z_b = new DoubleArrayList();
		
		getBezierEquationCoefficients(x, x_a, x_b);
		getBezierEquationCoefficients(y, y_a, y_b);
		getBezierEquationCoefficients(z, z_a, z_b);
		
		for (int i = sliceBegin; i < sliceEnd; i++) {
			if (!interpolatedPoints.isEmpty()) {
				interpolatedPoints.remove(interpolatedPoints.size() - 1);
			}
			
			Vec3 start = points.get(i);
			Vec3 end = points.get(i + 1);
			double x_av = x_a.getDouble(i);
			double x_bv = x_b.getDouble(i);
			double y_av = y_a.getDouble(i);
			double y_bv = y_b.getDouble(i);
			double z_av = z_a.getDouble(i);
			double z_bv = z_b.getDouble(i);
			
			for (int j = 0; j < interpolatedResults + 1; j++) {
				double t = (double)j / (double)interpolatedResults;
				
				interpolatedPoints.add(new Vec3(cubicBezier(start.x, end.x, x_av, x_bv, t)
											  , cubicBezier(start.y, end.y, y_av, y_bv, t)
											  , cubicBezier(start.z, end.z, z_av, z_bv, t)));
			}
		}
		
		return interpolatedPoints;
	}
}