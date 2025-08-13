package yesman.epicfight.client.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.joml.Vector4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.api.exception.ShaderParsingException;
//import yesman.epicfight.client.renderer.EpicFightVertexFormat.AnimationVertexFormat;
import yesman.epicfight.client.renderer.shader.AnimationShaderInstance;
import yesman.epicfight.client.renderer.shader.ShaderParser;
import yesman.epicfight.client.renderer.shader.VanillaAnimationShader;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public abstract class EpicFightRenderTypes extends RenderType {
	public static RenderType makeTriangulated(RenderType renderType) {
		if (renderType.mode() == VertexFormat.Mode.TRIANGLES) {
			return renderType;
		}
		
		if (renderType instanceof CompositeRenderType compositeRenderType) {
			return new CompositeRenderType(renderType.name, renderType.format, VertexFormat.Mode.TRIANGLES, renderType.bufferSize(), renderType.affectsCrumbling(), renderType.sortOnUpload, compositeRenderType.state);
		} else {
			return renderType;
		}
	}
	
	private static final Map<String, Map<ResourceLocation, RenderType>> TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE = new HashMap<> ();
	
	private static final Function<RenderType, RenderType> TRIANGULATED_RENDER_TYPES = Util.memoize(renderType -> {
		if (renderType.mode() == VertexFormat.Mode.TRIANGLES) {
			return renderType;
		}
		
		if (renderType instanceof CompositeRenderType compositeRenderType) {
			if (TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.containsKey(renderType.name)) {
				Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.get(renderType.name);
				
				if (compositeRenderType.state.textureState instanceof TextureStateShard texStateShard) {
					ResourceLocation texLocation = texStateShard.texture.orElse(null);
					
					if (renderTypesByTexture.containsKey(texLocation)) {
						return renderTypesByTexture.get(texLocation);
					}
				}
			}
			
			return new CompositeRenderType(renderType.name, renderType.format,
					VertexFormat.Mode.TRIANGLES, renderType.bufferSize(),
					renderType.affectsCrumbling(), renderType.sortOnUpload,
					compositeRenderType.state);

		} else {
			return renderType;
		}
	});
	
	public static RenderType getTriangulated(RenderType renderType) {
		return TRIANGULATED_RENDER_TYPES.apply(renderType);
	}
	
	/**
	 * Cache all Texture - RenderType entries to replace texture by MeshPart
	 */
	public static void addRenderType(String name, ResourceLocation textureLocation, RenderType renderType) {
		Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.computeIfAbsent(name, (k) -> Maps.newHashMap());
		renderTypesByTexture.put(textureLocation, renderType);
	}
	
	// Custom shards
	protected static final RenderStateShard.ShaderStateShard PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);
	
	@OnlyIn(Dist.CLIENT)
	public static class ShaderColorStateShard extends RenderStateShard {
		private Vector4f color;
		
		public ShaderColorStateShard(Vector4f color) {
			super(
				"shader_color",
				() -> {
					RenderSystem.setShaderColor(color.x, color.y, color.z, color.w);
				},
				() -> {
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				}
			);
			
			this.color = color;
		}
		
		public void setColor(float r, float g, float b, float a) {
			this.color.set(r, g, b, a);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class MutableCompositeState extends CompositeState {
		private ShaderColorStateShard shaderColorState = new ShaderColorStateShard(new Vector4f(1.0F));
		
		public MutableCompositeState(
			EmptyTextureStateShard pTextureState, ShaderStateShard pShaderState,
			TransparencyStateShard pTransparencyState, DepthTestStateShard pDepthState, CullStateShard pCullState,
			LightmapStateShard pLightmapState, OverlayStateShard pOverlayState, LayeringStateShard pLayeringState,
			OutputStateShard pOutputState, TexturingStateShard pTexturingState, WriteMaskStateShard pWriteMaskState,
			LineStateShard pLineState, ColorLogicStateShard pColorLogicState, RenderType.OutlineProperty pOutlineProperty
		) {
			super(
				pTextureState, pShaderState, pTransparencyState, pDepthState, pCullState, pLightmapState, pOverlayState,
				pLayeringState, pOutputState, pTexturingState, pWriteMaskState, pLineState, pColorLogicState, pOutlineProperty
			);
			
			List<RenderStateShard> list = new ArrayList<> (this.states);
			list.add(this.shaderColorState);
			this.states = ImmutableList.copyOf(list);
		}
		
		public void setShaderColor(int r, int g, int b, int a) {
			this.shaderColorState.setColor(r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F);
		}
		
		public void setShaderColor(float r, float g, float b, float a) {
			this.shaderColorState.setColor(r, g, b, a);
		}
		
		public static EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder mutableStateBuilder() {
	        return new EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder();
	    }
		
		@OnlyIn(Dist.CLIENT)
		public static class MutableCompositeStateBuilder {
			private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
			private RenderStateShard.ShaderStateShard shaderState = RenderStateShard.NO_SHADER;
			private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
			private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
			private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
			private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
			private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
			private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
			private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
			private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
			private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
			private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;
			private RenderStateShard.ColorLogicStateShard colorLogicState = RenderStateShard.NO_COLOR_LOGIC;

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard pTextureState) {
				this.textureState = pTextureState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setShaderState(RenderStateShard.ShaderStateShard pShaderState) {
				this.shaderState = pShaderState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard pTransparencyState) {
				this.transparencyState = pTransparencyState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard pDepthTestState) {
				this.depthTestState = pDepthTestState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setCullState(RenderStateShard.CullStateShard pCullState) {
				this.cullState = pCullState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard pLightmapState) {
				this.lightmapState = pLightmapState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard pOverlayState) {
				this.overlayState = pOverlayState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard pLayerState) {
				this.layeringState = pLayerState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard pOutputState) {
				this.outputState = pOutputState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard pTexturingState) {
				this.texturingState = pTexturingState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard pWriteMaskState) {
				this.writeMaskState = pWriteMaskState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLineState(RenderStateShard.LineStateShard pLineState) {
				this.lineState = pLineState;
				return this;
			}

			public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setColorLogicState(RenderStateShard.ColorLogicStateShard pColorLogicState) {
				this.colorLogicState = pColorLogicState;
				return this;
			}
			
			public EpicFightRenderTypes.MutableCompositeState createCompositeState(boolean pOutline) {
				return this.createCompositeState(pOutline ? RenderType.OutlineProperty.AFFECTS_OUTLINE : RenderType.OutlineProperty.NONE);
			}
			
			public EpicFightRenderTypes.MutableCompositeState createCompositeState(RenderType.OutlineProperty pOutlineState) {
				return new EpicFightRenderTypes.MutableCompositeState(
					this.textureState,
					this.shaderState,
					this.transparencyState,
					this.depthTestState,
					this.cullState,
					this.lightmapState,
					this.overlayState,
					this.layeringState,
					this.outputState,
					this.texturingState,
					this.writeMaskState,
					this.lineState,
					this.colorLogicState,
					pOutlineState
				);
			}
		}
	}
	
	private static final RenderType ENTITY_UI_COLORED = 
		create(
			  EpicFightMod.MODID + ":ui_color"
			, DefaultVertexFormat.POSITION_COLOR
			, VertexFormat.Mode.QUADS
			, 256
			, true
			, false
			, RenderType.CompositeState.builder()
				.setShaderState(POSITION_COLOR_SHADER)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTMAP)
				.setOverlayState(NO_OVERLAY)
				.createCompositeState(true)
		);
	
	private static final Function<ResourceLocation, RenderType> ENTITY_UI_TEXTURE = Util.memoize(
		(textureLocation) -> create( 
			  EpicFightMod.MODID + ":ui_texture"
			, DefaultVertexFormat.POSITION_TEX
			, VertexFormat.Mode.QUADS
			, 256
			, true
			, false
			, RenderType.CompositeState.builder()
				.setShaderState(POSITION_TEX_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(textureLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(NO_LIGHTMAP)
				.setOverlayState(NO_OVERLAY)
				.createCompositeState(true)
		)
	);
	
	private static final RenderType OBB = create(
		  EpicFightMod.MODID + ":debug_collider"
		, DefaultVertexFormat.POSITION_COLOR_NORMAL
		, VertexFormat.Mode.LINE_STRIP
		, 256
		, false
		, false
		, RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	
	private static final RenderType DEBUG_QUADS = create(
		  EpicFightMod.MODID + ":debug_quad"
		, DefaultVertexFormat.POSITION_COLOR
		, VertexFormat.Mode.QUADS
		, 256
		, false
		, false
		, RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(NO_TRANSPARENCY)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	
	private static final RenderType GUI_TRIANGLE = create(
		  EpicFightMod.MODID + ":gui_triangle"
		, DefaultVertexFormat.POSITION_COLOR
		, VertexFormat.Mode.TRIANGLES
		, 256
		, false
		, false
		, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_GUI_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(LEQUAL_DEPTH_TEST)
			.createCompositeState(false)
	);
	
	private static final Function<ResourceLocation, RenderType> OVERLAY_MODEL = Util.memoize(texLocation -> {
		return create(
			EpicFightMod.MODID + ":overlay_model",
			DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.TRIANGLES,
			256,
			false,
			false,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texLocation, false, false))
				.setWriteMaskState(COLOR_WRITE)
				.setCullState(NO_CULL)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
			);
		}
	);
	
	private static final RenderType ENTITY_AFTERIMAGE_WHITE = 
		create(
			EpicFightMod.MODID + ":entity_afterimage",
			DefaultVertexFormat.PARTICLE,
			VertexFormat.Mode.TRIANGLES,
			256,
			true,
			true,
			RenderType.CompositeState.builder()
				.setShaderState(PARTICLE_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "textures/common/white.png"), false, false))
				.setCullState(NO_CULL)
				.setWriteMaskState(COLOR_WRITE)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
		);
	
	private static final RenderType ITEM_AFTERIMAGE_WHITE = 
		create(
			EpicFightMod.MODID + ":item_afterimage",
			DefaultVertexFormat.PARTICLE,
			VertexFormat.Mode.QUADS,
			256,
			true,
			true,
			RenderType.CompositeState.builder()
				.setShaderState(PARTICLE_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "textures/common/white.png"), false, false))
				.setCullState(NO_CULL)
				.setWriteMaskState(COLOR_WRITE)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
		);
	
	private static final Function<ResourceLocation, RenderType> ENTITY_PARTICLE = Util.memoize(texLocation -> {
		return create(
			EpicFightMod.MODID + ":entity_particle",
			DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.TRIANGLES,
			256,
			true,
			true,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texLocation, false, false))
				.setWriteMaskState(COLOR_WRITE)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
		);
	});
	
	private static final RenderType ITEM_PARTICLE = 
		create(
			EpicFightMod.MODID + ":entity_particle",
			DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.QUADS,
			256,
			true,
			true,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
				.setWriteMaskState(COLOR_WRITE)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
		);
	
	private static final Function<ResourceLocation, RenderType> ENTITY_PARTICLE_STENCIL = Util.memoize(texLocation -> {
		return create(
			EpicFightMod.MODID + ":entity_particle_stencil",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.TRIANGLES,
			256,
			false,
			false,
			RenderType.CompositeState.builder()
				.setShaderState(POSITION_TEX_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texLocation, false, false))
				.setWriteMaskState(DEPTH_WRITE)
				.createCompositeState(false)
		);
	});
	
	private static final RenderType ITEM_PARTICLE_STENCIL = 
		create(
			EpicFightMod.MODID + ":item_particle_stencil",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256,
			false,
			false,
			RenderType.CompositeState.builder()
				.setShaderState(POSITION_TEX_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
				.setWriteMaskState(DEPTH_WRITE)
				.createCompositeState(false)
		);
	
	private static RenderType replaceTextureShard(ResourceLocation texToReplace, RenderType renderType) {
		if (renderType instanceof CompositeRenderType compositeRenderType && compositeRenderType.state.textureState instanceof TextureStateShard texStateShard) {
			CompositeState textureReplacedState = new CompositeState(
				  new RenderStateShard.TextureStateShard(texToReplace, texStateShard.blur, texStateShard.mipmap)
				, compositeRenderType.state.shaderState
				, compositeRenderType.state.transparencyState
				, compositeRenderType.state.depthTestState
				, compositeRenderType.state.cullState
				, compositeRenderType.state.lightmapState
				, compositeRenderType.state.overlayState
				, compositeRenderType.state.layeringState
				, compositeRenderType.state.outputState
				, compositeRenderType.state.texturingState
				, compositeRenderType.state.writeMaskState
				, compositeRenderType.state.lineState
				, compositeRenderType.state.colorLogicState
				, compositeRenderType.state.outlineProperty
			);
			
			return new CompositeRenderType(renderType.name, renderType.format, compositeRenderType.mode(), renderType.bufferSize(), renderType.affectsCrumbling(), renderType.sortOnUpload, textureReplacedState);
		} else {
			return null;
		}
	}
	
	public static RenderType replaceTexture(ResourceLocation texLocation, RenderType renderType) {
		if (TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.containsKey(renderType.name)) {
			Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.get(renderType.name);
			
			if (renderTypesByTexture.containsKey(texLocation)) {
				return renderTypesByTexture.get(texLocation);
			}
		}
		
		RenderType textureReplacedRenderType = replaceTextureShard(texLocation, renderType);
		
		if (textureReplacedRenderType == null) {
			return renderType;
		}
		
		Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.computeIfAbsent(textureReplacedRenderType.name, k -> Maps.newHashMap());
		renderTypesByTexture.put(texLocation, textureReplacedRenderType);
		
		return textureReplacedRenderType;
	}
	
	public static RenderType entityUIColor() {
		return ENTITY_UI_COLORED;
	}
	
	public static RenderType entityUITexture(ResourceLocation resourcelocation) {
		return ENTITY_UI_TEXTURE.apply(resourcelocation);
	}
	
	public static RenderType debugCollider() {
		return OBB;
	}
	
	public static RenderType debugQuads() {
		return DEBUG_QUADS;
	}
	
	public static RenderType guiTriangle() {
		return GUI_TRIANGLE;
	}
	
	public static RenderType overlayModel(ResourceLocation textureLocation) {
		return OVERLAY_MODEL.apply(textureLocation);
	}
	
	public static RenderType entityAfterimageStencil(ResourceLocation textureLocation) {
		return ENTITY_PARTICLE_STENCIL.apply(textureLocation);
	}
	
	public static RenderType itemAfterimageStencil() {
		return ITEM_PARTICLE_STENCIL;
	}
	
	public static RenderType entityAfterimageTranslucent(ResourceLocation textureLocation) {
		return ENTITY_PARTICLE.apply(textureLocation);
	}
	
	public static RenderType itemAfterimageTranslucent() {
		return ITEM_PARTICLE;
	}
	
	public static RenderType entityAfterimageWhite() {
		return ENTITY_AFTERIMAGE_WHITE;
	}
	
	public static RenderType itemAfterimageWhite() {
		return ITEM_AFTERIMAGE_WHITE;
	}
	
	private static final Map<Entity, CompositeRenderType> WORLD_RENDERTYPES_COLORED_GLINT = new HashMap<> ();
	
	public static void freeUnusedWorldRenderTypes() {
		WORLD_RENDERTYPES_COLORED_GLINT.entrySet().removeIf(entry -> entry.getKey().isRemoved());
	}
	
	public static void clearWorldRenderTypes() {
		WORLD_RENDERTYPES_COLORED_GLINT.clear();
	}
	
	public static RenderType coloredGlintWorldRendertype(Entity owner, float r, float g, float b) {
		CompositeRenderType glintRenderType = WORLD_RENDERTYPES_COLORED_GLINT.computeIfAbsent(
			owner,
			k -> create(
				EpicFightMod.MODID + ":colored_glint",
				DefaultVertexFormat.POSITION_TEX,
				VertexFormat.Mode.TRIANGLES,
				256,
				false,
				false,
				EpicFightRenderTypes.MutableCompositeState.mutableStateBuilder()
					.setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "textures/entity/overlay/glint_white.png"), true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(ENTITY_GLINT_TEXTURING)
					.createCompositeState(false)
			));
		
		((MutableCompositeState)glintRenderType.state).setShaderColor(r, g, b, 1.0F);
		
		return glintRenderType;
	}
	
	public static RenderType coloredGlintWorldRendertype(Entity owner, int r, int g, int b) {
		return coloredGlintWorldRendertype(owner, r / 255.0F, g / 255.0F, b / 255.0F);
	}
	
	/*****************************************
	 *         Animation shader part         *
	 *****************************************/
	private static Map<ResourceLocation, Resource> SHADER_LIBS;
	//private static final List<ShaderTransformer> ANIMATION_SHADERS_TRANSFORMERS = Lists.newArrayList();
	private static final Map<String, AnimationShaderInstance> ANIMATION_SHADERS = Maps.newConcurrentMap();
	/*private static final Function<VertexFormat, VertexFormat> ANIMATION_VERTEX_FORMATS = Util.memoize((vertexFormat) -> {
		if (vertexFormat instanceof AnimationVertexFormat) {
			return vertexFormat;
		}
		
		ImmutableMap.Builder<String, VertexFormatElement> vertexFormatElements = ImmutableMap.builder();
		
		vertexFormat.getElementMapping().entrySet().stream().filter((entry) -> EpicFightVertexFormat.keep(entry.getValue()))
															.map((entry) -> Pair.of(entry.getKey(), EpicFightVertexFormat.convert(entry.getValue())))
															.forEach((pair) -> vertexFormatElements.put(pair.getFirst(), pair.getSecond()));
		
		vertexFormatElements.put("Joints", EpicFightVertexFormat.ELEMENT_JOINTS);
		vertexFormatElements.put("Weights", EpicFightVertexFormat.ELEMENT_WEIGHTS);
		
		VertexFormat animationVertexFormat = new AnimationVertexFormat(vertexFormatElements.build());
		
		return animationVertexFormat;
	});
	*/
	/*public static AnimationShaderInstance getAnimationShader(ShaderInstance shaderInstance) {
		if (shaderInstance instanceof AnimationShaderInstance animationShaderInstance) {
			return animationShaderInstance;
		}
		
		try {
			if (!ANIMATION_SHADERS.containsKey(shaderInstance.getName())) {
				AnimationShaderInstance animationShaderInstance = null;
				
				for (ShaderTransformer shaderTransformer : ANIMATION_SHADERS_TRANSFORMERS) {
					if (shaderTransformer.predicate().test(shaderInstance)) {
						animationShaderInstance = shaderTransformer.transformer().apply(shaderInstance);
						break;
					}
				}
				
				if (animationShaderInstance == null) {
					animationShaderInstance = ShaderTransformer.VANILLA_TRANSFORMER.transformer.apply(shaderInstance);
				}
				
				if (animationShaderInstance != null) {
					ANIMATION_SHADERS.put(shaderInstance.getName(), animationShaderInstance);
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			EpicFightMod.LOGGER.warn("Failed to create shader with " + e.getMessage() + ". Automatically switches animation shader mode off.");
			Minecraft.getInstance().levelRenderer.allChanged();
			Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("epicfight.messages.shader_transform_fail", shaderInstance.getName()).withStyle(ChatFormatting.RED));
			
			ClientConfig.animationShaderLockedByException = true;
			ClientConfig.activateAnimationShader = false;
			ClientConfig.saveChanges();
		}
		
		return ANIMATION_SHADERS.get(shaderInstance.getName());
	}
	
	public static AnimationShaderInstance getAnimationShader(RenderType renderType) {
		if (renderType instanceof CompositeRenderType compositeRenderType) {
			Optional<Supplier<ShaderInstance>> shaderInstanceOptional = compositeRenderType.state.shaderState.shader;
			
			if (shaderInstanceOptional.isPresent()) {
				return getAnimationShader(shaderInstanceOptional.get().get());
			}
		}
		
		return null;
	}
	
	public static VertexFormat getAnimationVertexFormat(VertexFormat vertexFormat) {
		if (vertexFormat instanceof AnimationVertexFormat) {
			return vertexFormat;
		}
		
		return ANIMATION_VERTEX_FORMATS.apply(vertexFormat);
	}*/
	
	/*public static void registerShaderTransformer(Predicate<ShaderInstance> predicate, Function<ShaderInstance, AnimationShaderInstance> transformer) {
		ANIMATION_SHADERS_TRANSFORMERS.add(new ShaderTransformer(predicate, transformer));
	}*/
	
	@SubscribeEvent
	public static void registerShadersEvent(RegisterShadersEvent event) throws IOException {
		ANIMATION_SHADERS.clear();
		
		Map<ResourceLocation, Resource> shaderLibs = ((ResourceManager)((GameRenderer.ResourceCache) event.getResourceProvider()).original()).listResources("shaders/include", (rl) -> {
			String s = rl.getPath();
			return s.endsWith(".glsl");
		});
		
		SHADER_LIBS = ImmutableMap.copyOf(shaderLibs); 
		ClientConfig.animationShaderLockedByException = false;
	}
	
	public static void clearAnimationShaderInstance(String shaderName) {
		if (!ANIMATION_SHADERS.containsKey(shaderName)) {
			return;
		}
		
		AnimationShaderInstance animationShaderInstance = ANIMATION_SHADERS.get(shaderName);
		animationShaderInstance._clear();
		animationShaderInstance._close();
		ANIMATION_SHADERS.remove(shaderName);
	}
	
	/*@OnlyIn(Dist.CLIENT)
	private record ShaderTransformer(Predicate<ShaderInstance> predicate, Function<ShaderInstance, AnimationShaderInstance> transformer) {
		public static final ShaderTransformer VANILLA_TRANSFORMER = new ShaderTransformer((shaderInstance) -> true, (shaderInstance) -> {
			ShaderParser shaderParser = null;
			
			try {
				ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
				ResourceLocation shaderLocation = ResourceLocation.parse(shaderInstance.getName());
				shaderParser = new ShaderParser(resourceManager, shaderInstance.getName());
				boolean hasNormalAttribute = shaderParser.hasAttribute("Normal");
				boolean isEyesShader = "rendertype_eyes".equals(shaderLocation.getPath());
				
				if (shaderParser.hasAttribute("Color")) {
					shaderParser.addUniform("Color", ShaderParser.GLSLType.VEC4, "in .* Color;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.THROW, new Double[] {1.0D, 1.0D, 1.0D, 1.0D});
				}
				
				if (shaderParser.hasAttribute("UV1") && !isEyesShader) {
					shaderParser.addUniform("UV1", ShaderParser.GLSLType.IVEC2, "in .* UV1;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.THROW, new Integer[] {0, 0});
				}
				
				if (shaderParser.hasAttribute("UV2") && !isEyesShader) {
					shaderParser.addUniform("UV2", ShaderParser.GLSLType.IVEC2, "in .* UV2;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.THROW, new Integer[] {0, 0});
				}
				
				shaderParser.remove("Color", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
				shaderParser.remove("UV1", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
				shaderParser.remove("UV2", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
				shaderParser.addAttribute("Joints", ShaderParser.ExceptionHandler.THROW, ShaderParser.GLSLType.IVEC3);
				shaderParser.addAttribute("Weights", ShaderParser.ExceptionHandler.THROW, ShaderParser.GLSLType.VEC3);
				
				if (hasNormalAttribute && !isEyesShader) {
					shaderParser.addUniform("Normal_Mv_Matrix", ShaderParser.GLSLType.MATRIX3F, ShaderParser.ExceptionHandler.THROW, null);
				}
				
				shaderParser.addUniformArray("Poses", ShaderParser.GLSLType.MATRIX4F, ShaderParser.ExceptionHandler.THROW, null, ShaderParser.SHADER_ARRAY_LIMIT);
				shaderParser.replaceScript("Position", "Position_a", -1, ShaderParser.ExceptionHandler.THROW, "gl_Position", "in vec3 Position;");
				
				if (hasNormalAttribute && !isEyesShader) {
					shaderParser.replaceScript("Normal", "Normal_a", -1, ShaderParser.ExceptionHandler.THROW, "uniform mat3 Normal_Mv_Matrix;", "in vec3 Normal;");
				}
				
				shaderParser.insertToScript("in vec3 Position;", "\nvec3 Position_a = vec3(0.0);", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
				
				if (hasNormalAttribute && !isEyesShader) {
					shaderParser.insertToScript("in vec3 Normal;", "\nvec3 Normal_a = vec3(0.0);", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
				}
				
				shaderParser.insertToScript("void main\\(\\) \\{",
										    "void setAnimationPosition() {\n"
										  + "    for(int i=0;i<3;i++)\n"
										  + "    {\n"
										  + "        mat4 jointTransform = Poses[Joints[i]];\n"
										  + "        vec4 posePosition = jointTransform * vec4(Position, 1.0);\n"
										  + "        Position_a += vec3(posePosition.xyz) * Weights[i];\n"
										  + "    }\n"
										  + "}\n"
										  + "\n", 0, ShaderParser.InsertPosition.PRECEDING, ShaderParser.ExceptionHandler.THROW);
				
				if (hasNormalAttribute && !isEyesShader) {
					shaderParser.insertToScript("void main\\(\\) \\{",
											    "void setAnimationNormal() {\n"
											  + "    \n"
											  + "    for(int i=0;i<3;i++)\n"
											  + "    {\n"
											  + "        mat4 jointTransform = Poses[Joints[i]];\n"
											  + "        vec4 poseNormal = jointTransform * vec4(Normal, 1.0);\n"
											  + "        Normal_a += vec3(poseNormal.xyz) * Weights[i];\n"
											  + "    }\n"
											  + "    \n"
											  + "    Normal_a = Normal_Mv_Matrix * Normal_a;\n"
											  + "}\n", 0, ShaderParser.InsertPosition.PRECEDING, ShaderParser.ExceptionHandler.THROW);
					
					shaderParser.insertToScript("void main\\(\\) \\{", "\n    setAnimationNormal();", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
				}
				
				shaderParser.insertToScript("void main\\(\\) \\{", "\n    setAnimationPosition();", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
				
				Map<ResourceLocation, Resource> cache = Maps.newHashMap();
				cache.putAll(SHADER_LIBS);
				shaderParser.addToResourceCache(cache);
				GameRenderer.ResourceCache resourceProvider = new GameRenderer.ResourceCache(resourceManager, cache);
				
				return new VanillaAnimationShader(resourceProvider, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, shaderLocation.getPath()), EpicFightRenderTypes.getAnimationVertexFormat(shaderInstance.getVertexFormat()));
			} catch (IOException | ShaderParsingException e) {
				e.printStackTrace();
				
				if (shaderParser != null) {
					EpicFightMod.LOGGER.warn("Shader Script\n " + shaderParser.getOriginalScript());
				}
				
				throw new RuntimeException("Can't create animation shader", e);
			}
		});
	}*/
	
	//Util class
	private EpicFightRenderTypes() {
		super(null, null, null, -1, false, false, null, null);
	}
}