package yesman.epicfight.api.utils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import yesman.epicfight.api.utils.math.Vec3f;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ParseUtil {
	public static Integer[] toIntArray(JsonArray array) {
		List<Integer> result = Lists.newArrayList();
		
		for (JsonElement je : array) {
			result.add(je.getAsInt());
		}
		
		return result.toArray(new Integer[0]);
	}
	
	public static Float[] toFloatArray(JsonArray array) {
		List<Float> result = Lists.newArrayList();
		
		for (JsonElement je : array) {
			result.add(je.getAsFloat());
		}
		
		return result.toArray(new Float[0]);
	}
	
	public static int[] toIntArrayPrimitive(JsonArray array) {
		IntList result = new IntArrayList();
		
		for (JsonElement je : array) {
			result.add(je.getAsInt());
		}
		
		return result.toIntArray();
	}
	
	public static float[] toFloatArrayPrimitive(JsonArray array) {
		FloatList result = new FloatArrayList();
		
		for (JsonElement je : array) {
			result.add(je.getAsFloat());
		}
		
		return result.toFloatArray();
	}
	
	public static int[] unwrapIntWrapperArray(Number[] wrapperArray) {
		int[] iarray = new int[wrapperArray.length];
		
		for (int i = 0; i < wrapperArray.length; i++) {
			iarray[i] = (int)wrapperArray[i];
		}
		
		return iarray;
	}
	
	public static float[] unwrapFloatWrapperArray(Number[] wrapperArray) {
		float[] farray = new float[wrapperArray.length];
		
		for (int i = 0; i < wrapperArray.length; i++) {
			farray[i] = (float)wrapperArray[i];
		}
		
		return farray;
	}
	
	public static JsonObject farrayToJsonObject(float[] array, int stride) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("stride", stride);
		jsonObject.addProperty("count", array.length / stride);
		JsonArray jsonArray = new JsonArray();
		
		for (float element : array) {
			jsonArray.add(element);
		}
		
		jsonObject.add("array", jsonArray);
		
		return jsonObject;
	}
	
	public static JsonObject iarrayToJsonObject(int[] array, int stride) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("stride", stride);
		jsonObject.addProperty("count", array.length / stride);
		JsonArray jsonArray = new JsonArray();
		
		for (int element : array) {
			jsonArray.add(element);
		}
		
		jsonObject.add("array", jsonArray);
		
		return jsonObject;
	}
	
	public static Vec3f toVector3f(JsonArray array) {
		float[] result = toFloatArrayPrimitive(array);
		
		if (result.length < 3) {
			throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
		}
		
		return new Vec3f(result[0], result[1], result[2]);
	}
	
	public static Vec3 toVector3d(JsonArray array) {
		DoubleList result = new DoubleArrayList();
		
		for (JsonElement je : array) {
			result.add(je.getAsDouble());
		}
		
		if (result.size() < 3) {
			throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
		}
		
		return new Vec3(result.getDouble(0), result.getDouble(1), result.getDouble(2));
	}
	
	public static <T> String nullOrToString(T obj, Function<T, String> toString) {
		return obj == null ? "" : toString.apply(obj);
	}
	
	public static <T, V> V nullOrApply(T obj, Function<T, V> apply) {
		if (obj == null) {
			return null;
		}
		
		try {
			return apply.apply(obj);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static <T> T nvl(T a, T b) {
		return a == null ? b : a;
	}
	
	/**
	 * Tries to get a value from the first supplier. If failed, it returns a result from the second supplier
	 * @param tryTask: A supplier that has a chance to throw an error or return null value
	 * @param or: A supplier that always return non-null value
	 */
	public static <T> T tryGetOr(Supplier<T> tryTask, Supplier<T> or) {
		try {
			T t = tryTask.get();
			Objects.requireNonNull(t);
			return t;
		} catch (Exception e) {
			T t = or.get();
			Objects.requireNonNull(t);
			return t;
		}
	}
	
	public static String snakeToSpacedCamel(Object obj) {
		if (obj == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean upperNext = true;
		String toStr = obj.toString().toLowerCase(Locale.ROOT);
		
		for (String sElement : toStr.split("")) {
			if (upperNext) {
				sElement = sElement.toUpperCase(Locale.ROOT);
				upperNext = false;
			}
			
			if ("_".equals(sElement)) {
				upperNext = true;
				sb.append(" ");
			} else {
				sb.append(sElement);
			}
		}
		
		return sb.toString();
	}
	
	public static boolean compareNullables(@Nullable Object obj1, @Nullable Object obj2) {
		if (obj1 == null) {
			if (obj2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			return obj1.equals(obj2);
		}
	}
	
	public static String nullParam(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	
	public static <T> String getRegistryName(T obj, Registry<T> registry) {
		return obj == null ? "" : registry.getKey(obj).toString();
	}
	
	public static <T extends Tag> T getOrSupply(CompoundTag compTag, String name, Supplier<T> tag) {
		return getOrDefaultTag(compTag, name, tag.get());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Tag> T getOrDefaultTag(CompoundTag compTag, String name, T tag) {
		if (compTag.contains(name)) {
			return (T)compTag.get(name);
		}
		
		compTag.put(name, tag);
		
		return tag;
	}
	
	public static <T> boolean isParsableAllowingMinus(String s, Function<String, T> parser) {
		if ("-".equals(s)) {
			return true;
		}
		
		try {
			parser.apply(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static <T> boolean isParsable(String s, Function<String, T> parser) {
		try {
			parser.apply(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static <T> String valueOfOmittingType(T value) {
		try {
			return String.valueOf(value).replaceAll("[df]", "");
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static <T> T parseOrGet(String value, Function<String, T> parseFunction, T defaultValue) {
		try {
			return parseFunction.apply(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static <K, V> Set<Pair<K, V>> mapEntryToPair(Set<Map.Entry<K, V>> entrySet) {
		return entrySet.stream().map((entry) -> Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
	}
	
	public static <T> List<T> remove(Collection<T> collection, T object) {
		List<T> copied = new ArrayList<> (collection);
		copied.remove(object);
		return copied;
	}
	
	public static <T extends Enum<T>> T enumValueOfOrNull(Class<T> enumCls, String enumName) {
		try {
			return Enum.valueOf(enumCls, enumName.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}
	
	public static JsonObject convertToJsonObject(CompoundTag compoundtag) {
		JsonObject root = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, compoundtag).getOrThrow().getAsJsonObject();
		
		compoundtag.getAllKeys().forEach(key -> {
			Tag val = compoundtag.get(key);
			
			if (val instanceof ByteTag byteTag && (byteTag.getAsByte() == 0 || byteTag.getAsByte() == 1)) {
				root.remove(key);
				root.addProperty(key, byteTag.getAsByte() == 1);
			}
		});
		
		return root;
	}
	
	public static String toLowerCase(String s) {
		return s.toLowerCase(Locale.ROOT);
	}
	
	public static String toUpperCase(String s) {
		return s.toUpperCase(Locale.ROOT);
	}
	
	public static String getBytesSHA256Hash(byte[] bytes) {
		String hashString = "";
		
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(bytes);
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer();
			
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xFF) + 0x100, 16).substring(1));
			}
			
			hashString = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			hashString = null;
		}
		
		return hashString;
	}
	
	public static int parseCharacterToNumber(char c) {
		if (c < '0' || c > '9') {
			throw new IllegalArgumentException(c + "is not a character represents number");
		}
		
		return c - '0';
	}

	public static <T> T orElse(T value, Supplier<T> defaultVal) {
		Objects.requireNonNull(defaultVal);
		
		return value == null ? defaultVal.get() : value;
	}
	
	public static CompoundTag parseTagOrThrow(JsonElement jsonElement) {
		try {
			return TagParser.parseTag(jsonElement.toString());
		} catch (CommandSyntaxException e) {
			throw new RuntimeException("Can't parse element:", e);
		}
	}
	
	public static String packListString(List<String> strings, String separator) {
		StringBuilder sb = new StringBuilder();
		MutableInt mi = new MutableInt(1);
		
		strings.forEach(string -> {
			sb.append(string);
			if (mi.intValue() < strings.size()) sb.append(separator);
			mi.add(1);
		});
		
		return sb.toString();
	}

    public static boolean canParseToResourceLocation(String str) {
        if (StringUtil.isNullOrEmpty(str)) {
            return true;
        }

        String[] astring = new String[]{"minecraft", str};
        int i = str.indexOf(ResourceLocation.NAMESPACE_SEPARATOR);

        if (i >= 0) {
            astring[1] = str.substring(i + 1);

            if (i >= 1) {
                astring[0] = str.substring(0, i);
            }
        }

        return ResourceLocation.isValidNamespace(astring[0]) && ResourceLocation.isValidPath(astring[1]);
    }

	private ParseUtil() {}
}