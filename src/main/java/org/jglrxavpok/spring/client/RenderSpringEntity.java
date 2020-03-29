package org.jglrxavpok.spring.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jglrxavpok.spring.common.SpringEntity;

public class RenderSpringEntity extends EntityRenderer<SpringEntity> {

    public RenderSpringEntity(EntityRendererManager renderManager) {
        super(renderManager);
    }


    @Override
    public void render(SpringEntity entity, float entityYaw, float p_225623_3_, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        if(entity.dominant != null && entity.dominated != null) {
            matrixStack.push();

            Vec3d anchorThis = SpringEntity.calculateAnchorPosition(entity.dominant, SpringEntity.SpringSide.DOMINATED);
            matrixStack.translate(-entity.getPosX(), -entity.getPosY(), -entity.getPosZ());
            matrixStack.translate(anchorThis.x, anchorThis.y, anchorThis.z);
            matrixStack.rotate(Vector3f.YP.rotationDegrees(-entityYaw));
            renderSpring(entity, matrixStack, buffers, light);
            matrixStack.pop();
        }
    }

    @Override
    public ResourceLocation getEntityTexture(SpringEntity entity) {
        return null;
    }

    private void renderSpring(SpringEntity spring, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light) {
        Vec3d anchorThis = SpringEntity.calculateAnchorPosition(spring.dominant, SpringEntity.SpringSide.DOMINATED);
        Vec3d anchorOther = SpringEntity.calculateAnchorPosition(spring.dominated, SpringEntity.SpringSide.DOMINANT);
        double offsetX = anchorOther.x - anchorThis.x;
        double offsetY = anchorOther.y - anchorThis.y;
        double offsetZ = anchorOther.z - anchorThis.z;

        matrixStack.push();
        IVertexBuilder bufferbuilder = buffers.getBuffer(RenderType.LINES);
        int l = 32;

        for (int i1 = 1; i1 <= l; ++i1)
        {
            float step = (float)i1 / l;
            float stepMinus1 = (float)(i1-1) / l;
            line(matrixStack, offsetX, offsetY, offsetZ, bufferbuilder, i1-1, stepMinus1);
            line(matrixStack, offsetX, offsetY, offsetZ, bufferbuilder, i1, step);
        }
        matrixStack.pop();
    }

    private void line(MatrixStack matrixStack, double offsetX, double offsetY, double offsetZ, IVertexBuilder bufferbuilder, int i1, float step) {
        bufferbuilder
                .pos(matrixStack.getLast().getMatrix(), (float)(offsetX * (double)step), (float)(offsetY * (double)(step * step + step) * 0.5D + 0.25D), (float)(offsetZ * (double)step));
        if(i1 % 2 == 0) {
            bufferbuilder.color(0x80, (int)((1f-step)*0x80), (int)((1f-step)*0x80), 255);
        } else {
            bufferbuilder.color(0x20, (int)((1f-step)*0x20), (int)((1f-step)*0x20), 255);
        }
        bufferbuilder.endVertex();
    }
}
