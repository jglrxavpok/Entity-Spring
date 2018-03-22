package org.jglrxavpok.spring.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ModelSpring - jglrxavpok
 * Created using Tabula 7.0.0
 */
public class ModelSpring extends ModelBase {
    public ModelRenderer spring;

    public ModelSpring() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.spring = new ModelRenderer(this, 0, 0);
        this.spring.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.spring.addBox(-15.0F, -0.5F, -0.5F, 30, 1, 1, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.spring.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
