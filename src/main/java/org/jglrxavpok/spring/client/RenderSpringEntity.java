package org.jglrxavpok.spring.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jglrxavpok.spring.EntitySpringMod;
import org.jglrxavpok.spring.common.EntitySpring;
import org.lwjgl.util.vector.Quaternion;

import javax.annotation.Nullable;

public class RenderSpringEntity extends Render<EntitySpring> {

    public RenderSpringEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpring entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if(entity.dominant != null && entity.dominated != null) {
            GlStateManager.pushMatrix();

            Vec3d anchorThis = EntitySpring.calculateAnchorPosition(entity.dominant, EntitySpring.SpringSide.DOMINATED);
            GlStateManager.translate(anchorThis.x - renderManager.viewerPosX, anchorThis.y - renderManager.viewerPosY, anchorThis.z - renderManager.viewerPosZ);
            GlStateManager.rotate(-entityYaw, 0f, 1f, 0f);
            renderSpring(entity);
            GlStateManager.popMatrix();
        }
    }

    private void renderSpring(EntitySpring spring) {
        Vec3d anchorThis = EntitySpring.calculateAnchorPosition(spring.dominant, EntitySpring.SpringSide.DOMINATED);
        Vec3d anchorOther = EntitySpring.calculateAnchorPosition(spring.dominated, EntitySpring.SpringSide.DOMINANT);
        double offsetX = anchorOther.x - anchorThis.x;
        double offsetY = anchorOther.y - anchorThis.y;
        double offsetZ = anchorOther.z - anchorThis.z;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
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

        GlStateManager.glLineWidth(5f);
        tess.draw();
        GlStateManager.glLineWidth(1f);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySpring entity) {
        return null;
    }
}
