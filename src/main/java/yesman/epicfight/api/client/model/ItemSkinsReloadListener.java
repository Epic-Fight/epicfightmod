package yesman.epicfight.api.client.model;

import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.client.events.engine.RenderEngine;

public class ItemSkinsReloadListener extends SimpleJsonResourceReloadListener {
	public static final ItemSkinsReloadListener INSTANCE = new ItemSkinsReloadListener();
	
	public ItemSkinsReloadListener() {
		super(new GsonBuilder().create(), "item_skins");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller) {
		RenderEngine.getInstance().reloadItemRenderers(objectIn);
	}
}