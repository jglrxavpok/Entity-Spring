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

    public static final ResourceLocation SPRING_TEXTURE = new ResourceLocation(EntitySpringMod.MODID, "textures/entity/spring.png");
    private ModelSpring springModel = new ModelSpring();

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
        int l = 16;

        for (int i1 = 0; i1 <= l; ++i1)
        {
            float f11 = (float)i1 / l;
            bufferbuilder
                    .pos(offsetX * (double)f11, offsetY * (double)(f11 * f11 + f11) * 0.5D + 0.25D, offsetZ * (double)f11)
                    .color(128, (int)(128 * f11), (int)(128 * f11), 255)
                .endVertex();
        }

        GlStateManager.glLineWidth(20f);
        tess.draw();
        GlStateManager.glLineWidth(1f);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void setLookAlongQuaternion(Quaternion quat, float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
        quat.setIdentity();
        // Normalize direction
        float invDirLength = (float)(1.0 / Math.sqrt((dirX * dirX + dirY * dirY + dirZ * dirZ)));
        float dirnX = -dirX * invDirLength;
        float dirnY = -dirY * invDirLength;
        float dirnZ = -dirZ * invDirLength;
        // left = up x dir
        float leftX;
        float leftY;
        float leftZ;
                leftX = upY * dirnZ - upZ * dirnY;
        leftY = upZ * dirnX - upX * dirnZ;
        leftZ = upX * dirnY - upY * dirnX;
        // normalize left
        float invLeftLength = (float) (1.0 / Math.sqrt((leftX * leftX + leftY * leftY + leftZ * leftZ)));
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirnY * leftZ - dirnZ * leftY;
        float upnY = dirnZ * leftX - dirnX * leftZ;
        float upnZ = dirnX * leftY - dirnY * leftX;

        /* Convert orthonormal basis vectors to quaternion */
        float x;
        float y;
        float z;
        float w;
        double t;
        double tr = (leftX + upnY + dirnZ);
        if (tr >= 0.0) {
            t = Math.sqrt(tr + 1.0);
            w = (float) (t * 0.5);
            t = 0.5 / t;
            x = (float) ((dirnY - upnZ) * t);
            y = (float) ((leftZ - dirnX) * t);
            z = (float) ((upnX - leftY) * t);
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = Math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (float) (t * 0.5);
                t = 0.5 / t;
                y = (float) ((leftY + upnX) * t);
                z = (float) ((dirnX + leftZ) * t);
                w = (float) ((dirnY - upnZ) * t);
            } else if (upnY > dirnZ) {
                t = Math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((leftY + upnX) * t);
                z = (float) ((upnZ + dirnY) * t);
                w = (float) ((leftZ - dirnX) * t);
            } else {
                t = Math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((dirnX + leftZ) * t);
                y = (float) ((upnZ + dirnY) * t);
                w = (float) ((upnX - leftY) * t);
            }
        }
        /* Multiply */
        quat.set(quat.w * x + quat.x * w + quat.y * z - quat.z * y,
                quat.w * y - quat.x * z + quat.y * w + quat.z * x,
                quat.w * z + quat.x * y - quat.y * x + quat.z * w,
                quat.w * w - quat.x * x - quat.y * y - quat.z * z);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySpring entity) {
        return SPRING_TEXTURE;
    }
}
