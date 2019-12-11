package org.jglrxavpok.spring.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jglrxavpok.spring.common.SpringEntity;

import javax.annotation.Nullable;

public class RenderSpringEntity extends EntityRenderer<SpringEntity> {

    public RenderSpringEntity(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(SpringEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if(entity.dominant != null && entity.dominated != null) {
            GlStateManager.pushMatrix();

            Vec3d anchorThis = SpringEntity.calculateAnchorPosition(entity.dominant, SpringEntity.SpringSide.DOMINATED);
            GlStateManager.translated(anchorThis.x - renderManager.info.getRenderViewEntity().posX, anchorThis.y - renderManager.info.getRenderViewEntity().posY - renderManager.info.getRenderViewEntity().getEyeHeight(), anchorThis.z - renderManager.info.getRenderViewEntity().posZ);
            GlStateManager.rotatef(-entityYaw, 0f, 1f, 0f);
            renderSpring(entity);
            GlStateManager.popMatrix();
        }
    }

    private void renderSpring(SpringEntity spring) {
        Vec3d anchorThis = SpringEntity.calculateAnchorPosition(spring.dominant, SpringEntity.SpringSide.DOMINATED);
        Vec3d anchorOther = SpringEntity.calculateAnchorPosition(spring.dominated, SpringEntity.SpringSide.DOMINANT);
        double offsetX = anchorOther.x - anchorThis.x;
        double offsetY = anchorOther.y - anchorThis.y;
        double offsetZ = anchorOther.z - anchorThis.z;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tess.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        int l = 32;

        for (int i1 = 0; i1 <= l; ++i1)
        {
            float f11 = (float)i1 / l;
            bufferbuilder
                    .pos(offsetX * (double)f11, offsetY * (double)(f11 * f11 + f11) * 0.5D + 0.25D, offsetZ * (double)f11);
            if(i1 % 2 == 0) {
                bufferbuilder.color(0x80, (int)((1f-f11)*0x80), (int)((1f-f11)*0x80), 255);
            } else {
                bufferbuilder.color(0x20, (int)((1f-f11)*0x20), (int)((1f-f11)*0x20), 255);
            }

            bufferbuilder.endVertex();
        }

        GlStateManager.lineWidth(5f);
        tess.draw();
        GlStateManager.lineWidth(1f);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(SpringEntity entity) {
        return null;
    }
}
