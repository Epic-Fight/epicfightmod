package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.ClientEngine;

@OnlyIn(Dist.CLIENT)
public class CombatKeyMapping extends KeyMapping {
	public CombatKeyMapping(String description, InputConstants.Type type, int code, String category) {
		super(description, type, code, category);
	}
	
	@Override
	public boolean isActiveAndMatches(InputConstants.@NotNull Key keyCode) {
        return super.isActiveAndMatches(keyCode) && ClientEngine.getInstance().isBattleMode();
    }
}