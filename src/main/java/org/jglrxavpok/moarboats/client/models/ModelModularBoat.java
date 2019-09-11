package org.jglrxavpok.moarboats.client.models;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;

/**
 * ModelBoat - Either Mojang or a mod author
 * Created using Tabula 7.0.0
 */
public class ModelModularBoat extends Model {
    public RendererModel boatSides3;
    public RendererModel boatSides2;
    public RendererModel boatSides1;
    public RendererModel noWater;
    public RendererModel boatSides5;
    public RendererModel boatSides4;
    public RendererModel frontAnchor;
    public RendererModel backAnchor;

    public ModelModularBoat() {
        this.textureWidth = 128;
        this.textureHeight = 64;
        this.frontAnchor = new RendererModel(this, 40, 19);
        this.frontAnchor.setRotationPoint(17.0F, -5.6F, 0.0F);
        this.frontAnchor.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2, 0.0F);
        this.noWater = new RendererModel(this, 0, 0);
        this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
        this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.setRotateAngle(noWater, 1.5707963705062866F, 0.0F, 0.0F);
        this.backAnchor = new RendererModel(this, 48, 19);
        this.backAnchor.setRotationPoint(-17.0F, -5.0F, 0.0F);
        this.backAnchor.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2, 0.0F);
        this.boatSides5 = new RendererModel(this, 0, 43);
        this.boatSides5.setRotationPoint(0.0F, 4.0F, 9.0F);
        this.boatSides5.addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.boatSides2 = new RendererModel(this, 0, 19);
        this.boatSides2.setRotationPoint(-15.0F, 4.0F, 4.0F);
        this.boatSides2.addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
        this.setRotateAngle(boatSides2, 0.0F, 4.71238899230957F, 0.0F);
        this.boatSides4 = new RendererModel(this, 0, 35);
        this.boatSides4.setRotationPoint(0.0F, 4.0F, -9.0F);
        this.boatSides4.addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
        this.setRotateAngle(boatSides4, 0.0F, 3.1415927410125732F, 0.0F);
        this.boatSides3 = new RendererModel(this, 0, 27);
        this.boatSides3.setRotationPoint(15.0F, 4.0F, 0.0F);
        this.boatSides3.addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
        this.setRotateAngle(boatSides3, 0.0F, 1.5707963705062866F, 0.0F);
        this.boatSides1 = new RendererModel(this, 0, 0);
        this.boatSides1.setRotationPoint(0.0F, 3.0F, 1.0F);
        this.boatSides1.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
        this.setRotateAngle(boatSides1, 1.5707963705062866F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.frontAnchor.render(f5);
        this.noWater.render(f5);
        this.backAnchor.render(f5);
        this.boatSides5.render(f5);
        this.boatSides2.render(f5);
        this.boatSides4.render(f5);
        this.boatSides3.render(f5);
        this.boatSides1.render(f5);
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
