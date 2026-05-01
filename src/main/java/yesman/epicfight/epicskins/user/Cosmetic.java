package yesman.epicfight.epicskins.user;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.online.RemoteAssets;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public record Cosmetic(int seq, boolean unlocked, String title, String description, String resourceFile, Slot slot, @Nullable ResourceLocation textureLocation, boolean useIntParam1, boolean useBoolParam1) {
	public Cosmetic(JsonObject json) {
		this(
			  GsonHelper.getAsInt(json, "seq")
			, GsonHelper.getAsBoolean(json, "unlocked")
			, GsonHelper.getAsString(json, "title")
			, GsonHelper.getAsString(json, "explanation")
			, GsonHelper.getAsString(json, "fileLocation")
			, Slot.valueOf(ParseUtil.toUpperCase(GsonHelper.getAsString(json, "slot")))
			, RemoteAssets.getInstance().getRemoteTexture(GsonHelper.getAsString(json, "textureLocation"))
			, GsonHelper.getAsBoolean(json, "useIntParam1")
			, GsonHelper.getAsBoolean(json, "useBoolParam1")
		);
	}
	
	public AssetAccessor<? extends Mesh> getAsMesh(Consumer<Mesh> onDownloaded) {
		if (this.slot == Slot.CAPE) {
			if (this.seq == -1) {
				return AuthenticationHelperImpl.getInstance().playerInfo().getSkin().capeTexture() != null ? Meshes.CAPE_DEFAULT : null;
			} else {
				return RemoteAssets.getInstance().getRemoteMesh(this.seq, this.resourceFile, onDownloaded);
			}
		}
		
		return null;
	}

	public enum Slot {
		CAPE
	}
}
