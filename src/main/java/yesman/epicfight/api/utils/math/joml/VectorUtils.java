package yesman.epicfight.api.utils.math.joml;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.main.EpicFightMod;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class VectorUtils
{
    public static Vector3f lerp(Vector3f start, Vector3f end, float delta, Vector3f dest) {
        dest.x = start.x + (end.x - start.x) * delta;
        dest.y = start.y + (end.y - start.y) * delta;
        dest.z = start.z + (end.z - start.z) * delta;
        return dest;
    }

    public static double getAngleBetween(Vector3f a, Vector3f b) {
        double cos = (a.x * b.x + a.y * b.y + a.z * b.z);
        return Math.toDegrees(Math.acos(cos));
    }

    public static Vector3f average(Collection<Vector3f> vectors, Vector3f dest) {


        dest.set(0.0F, 0.0F, 0.0F);

        for (Vector3f v : vectors) {
            dest.add(v);
        }

        dest.mul(1.0F / vectors.size());

        return dest;
    }
    public static int getNearest(Vector3f from, List<Vector3f> vectors) {
        float minLength = Float.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < vectors.size(); i++) {


            float distSqr = from.distanceSquared(vectors.get(i));

            if (distSqr < minLength) {
                minLength = distSqr;
                index = i;
            }
        }

        return index;
    }
    public static Quaternionf getRotatorBetween(Vector3f a, Vector3f b, Quaternionf dest) {
        if (dest == null) {
            dest = new Quaternionf();
        }

        Vector3f axis = a.cross(b, new Vector3f()).normalize();
        float dotDivLength = a.dot(b) / (a.length() * b.length());

        if (!Float.isFinite(dotDivLength)) {
            EpicFightMod.LOGGER.info("Warning : given vector's length is zero");
            (new IllegalArgumentException()).printStackTrace();
            dotDivLength = 1.0F;
        }

        float radian = (float) java.lang.Math.acos(java.lang.Math.min(1.0F, dotDivLength));
        dest.setAngleAxis(radian, axis.x, axis.y, axis.z);

        return dest;
    }

    public static void invalidate(Vector3f invalid) {
        invalid.set(Float.NaN, Float.NaN, Float.NaN);
    }

    public static boolean validate(Vector3f validation) {
        return Float.isFinite(validation.x) && Float.isFinite(validation.y) && Float.isFinite(validation.z);
    }

    public static Vector3f project(Vector3f from, Vector3f to, Vector3f dest) {


        float dot = to.dot(from);
        float normalScale = 1.0F / ((to.x * to.x) + (to.y * to.y) + (to.z * to.z));

        dest.x = dot * to.x * normalScale;
        dest.y = dot * to.y * normalScale;
        dest.z = dot * to.z * normalScale;

        return dest;
    }

    public static int getNearest(Vector3f from, Vector3f... vectors) {
        float minLength = Float.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < vectors.length; i++) {

            float distSqr = from.distanceSquared(vectors[i]);

            if (distSqr < minLength) {
                minLength = distSqr;
                index = i;
            }
        }

        return index;
    }

    public static int getLeastAngleVectorIdx(Vector3f src, Vector3f... candidates) {
        int leastVectorIdx = -1;
        int current = 0;
        float maxDot = -10000.0F;

        for (Vector3f normzlizedVec : Stream.of(candidates).map(Vector3f::normalize).toList()) {
            float dot = src.dot(normzlizedVec);

            if (maxDot < dot) {
                maxDot = dot;
                leastVectorIdx = current;
            }

            current++;
        }

        return leastVectorIdx;
    }
}
