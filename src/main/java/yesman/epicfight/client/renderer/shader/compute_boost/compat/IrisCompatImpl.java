package yesman.epicfight.client.renderer.shader.compute_boost.compat;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import yesman.epicfight.client.renderer.EpicFightVertexFormat;
import yesman.epicfight.client.renderer.shader.compute_boost.loader.ShaderRegistries;

import static org.lwjgl.opengl.GL46.*;
import static yesman.epicfight.client.renderer.EpicFightVertexFormat.bindAttrPointer;
import static yesman.epicfight.client.renderer.EpicFightVertexFormat.bindIntAttrPointer;

public class IrisCompatImpl implements IIrisCompatContext{

    public static void init(){
        if(ShaderRegistries.IrisLoaded){
            IIrisCompatContext.CTX.INSTANCE = new IrisCompatImpl();
            EpicFightVertexFormat.vertexFormatSetupImpl = IrisCompatImpl::bindBufferFormat;
        }
    }

    @Override
    public short getBlock() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
    }

    @Override
    public short getEntity() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
    }

    @Override
    public short getItem() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
    }

    /***
     * @param vertexFormat
     * @param buffers :  0 pos, 1 nor, 2 col, 3 uv_org, 4 uv1, 5 uv2,
     *                6 iris_entity_id, 7 iris_midUV, 8 iris_tangent
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
                glBindBuffer(GL_ARRAY_BUFFER, buffers[2]);
                glVertexAttribPointer(i, 4, GL_FLOAT, true, 0, 0);
                glEnableVertexAttribArray(i);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_NORMAL){
                glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
                glVertexAttribPointer(i, 3, GL_BYTE, true, 4, 0);
                glEnableVertexAttribArray(i);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_UV1){
                bindIntAttrPointer(buffers[4], 2, i, GL_UNSIGNED_SHORT, 0);
            }
            else if(elem == DefaultVertexFormat.ELEMENT_UV2){
                bindIntAttrPointer(buffers[5], 2, i, GL_UNSIGNED_SHORT, 0);
            }

            // iris part
            else if(elem == IrisVertexFormats.ENTITY_ID_ELEMENT){
                bindIntAttrPointer(buffers[6], 3, i, GL_UNSIGNED_SHORT, 4);
            }
            else if(elem == IrisVertexFormats.MID_TEXTURE_ELEMENT){
                bindAttrPointer(buffers[7], 2, i, GL_FLOAT);
            }
            else if(elem == IrisVertexFormats.TANGENT_ELEMENT){
                glBindBuffer(GL_ARRAY_BUFFER, buffers[8]);
                glVertexAttribPointer(i, 4, GL_BYTE, false, 0, 0);
                glEnableVertexAttribArray(i);
            }
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
