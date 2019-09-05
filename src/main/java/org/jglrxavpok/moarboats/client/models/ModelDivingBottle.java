package org.jglrxavpok.moarboats.client.models;


import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;

/**
 * ModelDivingBottle - jglrxavpok
 * Created using Tabula 7.0.0
 */
public class ModelDivingBottle extends ModelBase {
    public RendererModel body;
    public RendererModel nozzle_attach;
    public RendererModel nozzle;

    public ModelDivingBottle() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.body = new RendererModel(this, 0, 0);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.body.addBox(-4.0F, 0.0F, -4.0F, 8, 16, 8, 0.0F);
        this.nozzle = new RendererModel(this, 24, 0);
        this.nozzle.setRotationPoint(0.0F, -5.5F, 0.0F);
        this.nozzle.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
        this.nozzle_attach = new RendererModel(this, 0, 0);
        this.nozzle_attach.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.nozzle_attach.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.body.render(f5);
        this.nozzle.render(f5);
        this.nozzle_attach.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(RendererModel RendererModel, float x, float y, float z) {
        RendererModel.xRot = x;
        RendererModel.yRot = y;
        RendererModel.zRot = z;
    }
}
