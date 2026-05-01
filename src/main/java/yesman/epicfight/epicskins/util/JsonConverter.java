package yesman.epicfight.epicskins.util;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonConverter {
	public static JsonElement parseJson(String jsonAsString) {
		JsonReader jsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonAsString.getBytes()), StandardCharsets.UTF_8));
		jsonReader.setLenient(true);
		
		return Streams.parse(jsonReader);
	}
}
