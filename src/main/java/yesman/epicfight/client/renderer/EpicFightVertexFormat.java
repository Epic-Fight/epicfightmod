package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static org.lwjgl.opengl.GL46.*;

@OnlyIn(Dist.CLIENT)
public class EpicFightVertexFormat {

    @FunctionalInterface
    public interface IVertexFormatSetup{
        void invoke(VertexFormat vertexFormat, int... buffers);
    }

    public static IVertexFormatSetup vertexFormatSetupImpl = EpicFightVertexFormat::bindBufferFormat;

	/*public static final VertexFormatElement ELEMENT_POSITION = new EpicFightVertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3, SkinnedMesh::pointPositionsBuffer);
	public static final VertexFormatElement ELEMENT_UV0 = new EpicFightVertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2, SkinnedMesh::uvPositionsBuffer);
	public static final VertexFormatElement ELEMENT_NORMAL = new EpicFightVertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3, SkinnedMesh::normalPositionsBuffer);
	public static final VertexFormatElement ELEMENT_JOINTS = new EpicFightVertexFormatElement(0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.GENERIC, 3, SkinnedMesh::jointPositionsBuffer);
	public static final VertexFormatElement ELEMENT_WEIGHTS = new EpicFightVertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3, SkinnedMesh::weightPositionsBuffer);
	
	private static final Set<VertexFormatElement> FILTERTED_FORMATS = ImmutableSet.of(DefaultVertexFormat.ELEMENT_COLOR, DefaultVertexFormat.ELEMENT_UV1, DefaultVertexFormat.ELEMENT_UV2);
	
	private static final Map<VertexFormatElement, VertexFormatElement> VERTEX_FORMAT_MAPPING = ImmutableMap.of(
			DefaultVertexFormat.ELEMENT_POSITION, ELEMENT_POSITION,
			DefaultVertexFormat.ELEMENT_UV0, ELEMENT_UV0,
			DefaultVertexFormat.ELEMENT_NORMAL, ELEMENT_NORMAL
		);
	
	public static boolean keep(VertexFormatElement vertexFormatElement) {
		return !FILTERTED_FORMATS.contains(vertexFormatElement);
	}
	
	public static VertexFormatElement convert(VertexFormatElement vertexFormatElement) {
		return VERTEX_FORMAT_MAPPING.getOrDefault(vertexFormatElement, vertexFormatElement);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class AnimationVertexFormat extends VertexFormat {
		public AnimationVertexFormat(ImmutableMap<String, VertexFormatElement> attributesMap) {
			super(attributesMap);
		}
	}*/

    /*
    glBindBuffer(GL_ARRAY_BUFFER, positionOutputSSBO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
     */

    public static void bindAttrPointer(int vaao, int size, int binding_pos, int gl_type){
        glBindBuffer(GL_ARRAY_BUFFER, vaao);
        glVertexAttribPointer(binding_pos, size, gl_type, false, 0, 0);
        glEnableVertexAttribArray(binding_pos);
    }

    public static void bindAttrPointer(int vaao, int size, int binding_pos, int gl_type, int stride){
        glBindBuffer(GL_ARRAY_BUFFER, vaao);
        glVertexAttribPointer(binding_pos, size, gl_type, false, stride, 0);
        glEnableVertexAttribArray(binding_pos);
    }

    public static void bindIntAttrPointer(int vaao, int size, int binding_pos, int gl_type, int stride){
        glBindBuffer(GL_ARRAY_BUFFER, vaao);
        glVertexAttribIPointer(binding_pos, size, gl_type, stride, 0);
        glEnableVertexAttribArray(binding_pos);
    }


    /***
     * @param vertexFormat
     * @param buffers :  0 pos, 1 nor, 2 col, 3 uv, 4 layout, 5 light
     */
    public static void bindBufferFormat(VertexFormat vertexFormat, int... buffers){
        var elems = vertexFormat.getElements();

        //vertexFormat.setupBufferState();

        for(int i = 0; i < elems.size(); ++i) {
            var elem = elems.get(i);

            if(elem == DefaultVertexFormat.ELEMENT_POSITION){
                bindAttrPointer(buffers[0], 3, i, GL_FLOAT);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_UV){
                bindAttrPointer(buffers[3], 2, i, GL_FLOAT);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_COLOR){
                //bindAttrPointer(col, 4, i, GL_FLOAT);
                glBindBuffer(GL_ARRAY_BUFFER, buffers[2]);
                glVertexAttribPointer(i, 4, GL_FLOAT, true, 0, 0);
                glEnableVertexAttribArray(i);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_NORMAL){
                glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
                glVertexAttribPointer(i, 3, GL_BYTE, true, 4, 0);
                glEnableVertexAttribArray(i);
                //bindAttrPointer(nor, 3, i, GL_BYTE, 1);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_UV1){
                bindIntAttrPointer(buffers[4], 2, i, GL_UNSIGNED_SHORT, 0);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_UV2){
                bindIntAttrPointer(buffers[5], 2, i, GL_UNSIGNED_SHORT, 0);
            }
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static void clearBufferState(VertexFormat vertexFormat){
        vertexFormat.clearBufferState();
    }

}
