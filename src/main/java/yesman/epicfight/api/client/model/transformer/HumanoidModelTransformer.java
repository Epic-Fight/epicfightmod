package yesman.epicfight.api.client.model.transformer;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;

public abstract class HumanoidModelTransformer {
	public abstract SkinnedMesh transformArmorModel(HumanoidModel<?> humanoidModel);

	public SkinnedMesh transformArmorModel(HumanoidModel<?> humanoidModel, LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot) {
		return transformArmorModel(humanoidModel);
	}
	
	public static abstract class PartTransformer<T> {
		public abstract void bakeCube(PoseStack poseStack, MeshPartDefinition partDefinition, T cube, List<SingleGroupVertexBuilder> vertices, Map<MeshPartDefinition, IntList> indices, IndexCounter indexCounter);

        @ApiStatus.Internal
		public static void triangluatePolygon(Map<MeshPartDefinition, IntList> indices, MeshPartDefinition partDefinition, IndexCounter indexCounter) {
			IntList list = indices.computeIfAbsent(partDefinition, (key) -> new IntArrayList());
			
			//Optimization: do not split vertices in a cube.
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.first());
			}
			
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.second());
			}
			
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.fourth());
			}
			
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.fourth());
			}
			
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.second());
			}
			
			for (int i = 0; i < 3; i++) {
				list.add(indexCounter.third());
			}
			
			indexCounter.count();
		}
		
		public static class IndexCounter {
			private int indexCounter = 0;
			
			private int first() {
				return this.indexCounter;
			}
			
			private int second() {
				return this.indexCounter + 1;
			}
			
			private int third() {
				return this.indexCounter + 2;
			}
			
			private int fourth() {
				return this.indexCounter + 3;
			}
			
			private void count() {
				this.indexCounter += 4;
			}
		}
	}
}