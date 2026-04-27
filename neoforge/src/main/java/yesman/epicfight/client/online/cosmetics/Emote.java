package yesman.epicfight.client.online.cosmetics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.EmoteAnimation;
import yesman.epicfight.api.asset.AssetAccessor;

import java.util.Optional;

public record Emote(AssetAccessor<? extends EmoteAnimation> animation, String title, float snapshotTimeStamp, PreviewCameraTransform previewCameraTransform) {
    public static final Codec<Emote> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("animation").forGetter(emote -> emote.animation.registryName().toString()),
            Codec.STRING.fieldOf("title").forGetter(Emote::title),
            Codec.FLOAT.optionalFieldOf("snapshot_time_stamp").forGetter(emote -> Optional.of(emote.snapshotTimeStamp)),
            PreviewCameraTransform.CODEC.fieldOf("preview_camera_transform").forGetter(Emote::previewCameraTransform)
        )
        .apply(instance, (animationName, title, snapshotTimeStampOptional, previewCameraTransform) -> {
            return new Emote(AnimationManager.trasientAccessor(ResourceLocation.parse(animationName)), title, snapshotTimeStampOptional.orElse(0.0F), previewCameraTransform);
        })
    );

    public record PreviewCameraTransform(double zoom, float xRot, float yRot, float xMove, float yMove) {
        public static final Codec<PreviewCameraTransform> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("zoom").forGetter(PreviewCameraTransform::zoom),
                Codec.FLOAT.fieldOf("x_rotation").forGetter(PreviewCameraTransform::xRot),
                Codec.FLOAT.fieldOf("y_rotation").forGetter(PreviewCameraTransform::yRot),
                Codec.FLOAT.fieldOf("x_translation").forGetter(PreviewCameraTransform::xMove),
                Codec.FLOAT.fieldOf("y_translation").forGetter(PreviewCameraTransform::yMove)
            )
            .apply(instance, PreviewCameraTransform::new)
        );
    }
}
