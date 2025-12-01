package yesman.epicfight.api.utils.math.joml;

import com.google.common.collect.Lists;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.List;

public class Matrix4fUtils {

    public static List<Float> toList(Matrix4f matrix) {
        List<Float> elements = Lists.newArrayList();

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                elements.add(matrix.get(i, j));
            }
        }

        return elements;
    }

    public static Matrix4f store(Matrix4f matrix4f, FloatBuffer buf) {
        //Use a for-loop for better readability
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                buf.put(matrix4f.get(i, j));
            }
        }
        return matrix4f;
    }

    public static Vec3 transform(Matrix4f matrix, Vec3 src) {
        Vector4f buffer = matrix.transform(new Vector4f((float)src.x, (float)src.y, (float)src.z, 1.0f));
        float x = buffer.x;
        float y = buffer.y;
        float z = buffer.z;

        return new Vec3(x, y ,z);
    }

    public static Matrix4f mulMatrices(Matrix4f... srcs) {
        Matrix4f result = new Matrix4f();

        for (Matrix4f src : srcs) {
            result.mul(src);
        }

        return result;
    }

    public static Matrix4f mulMatricesLocal(Matrix4f... srcs) {
        Matrix4f result = new Matrix4f();

        for (Matrix4f src : srcs) {
            result.mulLocal(src);
        }

        return result;
    }

    public static Matrix4f[] allocateArray(int size) {
        Matrix4f[] matrixArray = new Matrix4f[size];

        for (int i = 0; i < size; i++) {
            matrixArray[i] = new Matrix4f();
        }

        return matrixArray;
    }

    public static Matrix4f mulAsOrigin(Matrix4f left, Matrix4f right, Matrix4f dest) {
        float x = right.m30();
        float y = right.m31();
        float z = right.m32();

        Matrix4f result = left.mul(right, dest);
        result.m30(x);
        result.m31(y);
        result.m32(z);

        return result;
    }

    public static Matrix4f mulAsOriginInverse(Matrix4f left, Matrix4f right, Matrix4f dest) {
        return mulAsOrigin(right, left, dest);
    }

    public static Matrix4f mulBoth(Matrix4f left, Matrix4f right, Matrix4f dest)
    {
        if (dest == null)
            dest = new Matrix4f();
        left.mul(right, dest);
        return dest;
    }

    public static Vector3f transform3v(Matrix4f matrix, Vector3f src, Vector3f dest) {


        Vector4f buffer = matrix.transform(new Vector4f(src, 1));
        dest.set(buffer.x, buffer.y, buffer.z);

        return dest;
    }

    public static Matrix4f removeScale(Matrix4f src, Matrix4f dest) {

        Vector3f buffer = new Vector3f();
        buffer.set(src.m00(), src.m01(), src.m02());
        float xScale = buffer.length();

        buffer.set(src.m10(), src.m11(), src.m12());
        float yScale = buffer.length();

        buffer.set(src.m20(), src.m21(), src.m22());
        float zScale = buffer.length();

        dest.set(src);
        dest.scale(1.0F / xScale, 1.0F / yScale, 1.0F / zScale);

        return dest;
    }
}
