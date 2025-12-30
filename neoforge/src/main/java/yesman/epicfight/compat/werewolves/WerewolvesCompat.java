package yesman.epicfight.compat.werewolves;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import de.teamlapen.werewolves.client.model.WerewolfEarsModel;
import de.teamlapen.werewolves.client.render.layer.HumanWerewolfLayer;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.werewolves.mixin.MixinHumanWerewolfLayer;

public class WerewolvesCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
		
	}

	@Override
	public void onGameEventBus(IEventBus eventBus) {
        EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE.registerEvent(event -> {
            WerewolfForm form = WerewolfPlayer.get(event.getPlayerPatch().getOriginal()).getForm();

            if (form == WerewolfForm.SURVIVALIST || form == WerewolfForm.BEAST) {
                event.cancel();
            }
        });
	}
	
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Registry.MODIFY_PATCHED_ENTITY.registerEvent(event -> {
            if (event.get(EntityType.PLAYER) instanceof PPlayerRenderer playerrenderer) {
                playerrenderer.addPatchedLayerAlways(HumanWerewolfLayer.class, new EpicFightHumanWerewolfLayer<> ());
            }
        });
	}
	
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Render.VALIDATE_PLAYER_MODEL_TO_RENDER.registerEvent(event -> {
            WerewolfForm form = WerewolfPlayer.get(event.getPlayerPatch().getOriginal()).getForm();

            if (form == WerewolfForm.SURVIVALIST || form == WerewolfForm.BEAST) {
                event.setShouldRender(false);
            }
        });
	}
	
	public static class EpicFightHumanWerewolfLayer<A extends HumanoidModel<AbstractClientPlayer>> extends PatchedLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, HumanWerewolfLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>, A>> {
		private SkinnedMesh mesh;
		private SkinnedMesh slimMesh;
		
		@Override
		protected void renderLayer(
			  AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch
			, AbstractClientPlayer entityliving
			, HumanWerewolfLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>, A> vanillaLayer
			, PoseStack poseStack
			, MultiBufferSource buffer
			, int packedLight
			, OpenMatrix4f[] poses
			, float bob
			, float yRot
			, float xRot
			, float partialTicks
		) {
			@SuppressWarnings("unchecked")
			MixinHumanWerewolfLayer<AbstractClientPlayer, A> accessor = (MixinHumanWerewolfLayer<AbstractClientPlayer, A>)vanillaLayer;
			String modelType = entityliving.getSkin().model().id();
			
			A vanillaModel = accessor.getModel();
			
			if (vanillaModel instanceof WerewolfEarsModel werewolfEars) {
				werewolfEars.head.loadPose(werewolfEars.head.getInitialPose());
				werewolfEars.hat.loadPose(werewolfEars.hat.getInitialPose());
				werewolfEars.body.loadPose(werewolfEars.body.getInitialPose());
				werewolfEars.leftArm.loadPose(werewolfEars.leftArm.getInitialPose());
				werewolfEars.rightArm.loadPose(werewolfEars.rightArm.getInitialPose());
				werewolfEars.leftLeg.loadPose(werewolfEars.leftLeg.getInitialPose());
				werewolfEars.rightLeg.loadPose(werewolfEars.rightLeg.getInitialPose());
			}
			
			SkinnedMesh mesh;
			
			if ("default".equals(modelType)) {
				if (this.mesh == null) {
					this.mesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(vanillaModel);
				}
				
				mesh = this.mesh;
			} else {
				if (this.slimMesh == null) {
					this.slimMesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(vanillaModel);
				}
				
				mesh = this.slimMesh;
			}
			
			Helper.asIWerewolf(entityliving).filter(werewolf -> werewolf.getForm() == WerewolfForm.HUMAN).ifPresent(werewolf -> {
	            ResourceLocation texture = accessor.getTextures().get(werewolf.getSkinType() % accessor.getTextures().size());
	            RenderType rendertype = EpicFightRenderTypes.getTriangulated(RenderType.entityCutoutNoCull(texture));
	            mesh.draw(poseStack, buffer, rendertype, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, LivingEntityRenderer.getOverlayCoords(entityliving, 0.0F), entitypatch.getArmature(), poses);
	        });
		}
	}
}
