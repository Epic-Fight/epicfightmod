package yesman.epicfight.client.online.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.online.EpicSkins;
import yesman.epicfight.client.online.RemoteAssets;

public record Cape(int seq, EpicSkins.Slot slot, int intParam1, boolean boolParam1, boolean useIntParam1,
                   boolean useBoolParam1, String fileLocation, ResourceLocation textureLocation) {
    // intParam1 is normally used to color
// boolParam1 is normally used to decide cape's vanilla texture
    public Cape(JsonObject json) throws JsonSyntaxException {
        this(
            GsonHelper.getAsInt(json, "cosmeticSeq")
            , EpicSkins.Slot.valueOf(ParseUtil.toUpperCase(GsonHelper.getAsString(json, "slot")))
            , GsonHelper.getAsInt(json, "intParam1")
            , GsonHelper.getAsBoolean(json, "boolParam1")
            , GsonHelper.getAsBoolean(json, "useIntParam1")
            , GsonHelper.getAsBoolean(json, "useBoolParam1")
            , GsonHelper.getAsString(json, "fileLocation")
            , RemoteAssets.getInstance().getRemoteTexture(GsonHelper.getAsString(json, "textureLocation"))
        );
    }
}
