package org.jglrxavpok.moarboats.client.renders

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.client.models.ModelBoatLinkerAnchor
import org.jglrxavpok.moarboats.client.models.ModelModularBoat
import org.jglrxavpok.moarboats.common.entities.BasicBoatEntity
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity


class RenderModularBoat(renderManager: RenderManager): Render<ModularBoatEntity>(renderManager) {

    companion object {
        val TextureLocation = ResourceLocation(MoarBoats.ModID, "textures/entity/modularboat.png")
        val RopeAnchorTextureLocation = ResourceLocation(MoarBoats.ModID, "textures/entity/ropeanchor.png")
    }

    val model = ModelModularBoat()
    val ropeAnchorModel = ModelBoatLinkerAnchor()

    override fun getEntityTexture(entity: ModularBoatEntity) = TextureLocation

    override fun doRender(entity: ModularBoatEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        bindTexture(TextureLocation)
        GlStateManager.pushMatrix()
        GlStateManager.disableCull()
        if(entity.isEntityInLava())
            setTranslation(entity, x, y+0.20f, z)
        else
            setTranslation(entity, x, y, z)
        setRotation(entity, entityYaw, partialTicks)
        GlStateManager.enableRescaleNormal()
        setScale()
        model.noWater.showModel = false
        val color = entity.color.colorComponentValues
        GlStateManager.color(color[0], color[1], color[2])
        model.render(entity, 0f, 0f, entity.ticksExisted.toFloat(), 0f, 0f, 1f)
        GlStateManager.color(1f, 1f, 1f)
        renderLink(entity, x, y, z, entityYaw, partialTicks)
        removeScale()
        entity.modules.forEach {
            BoatModuleRenderingRegistry.getValue(it.id)?.renderModule(entity, it, x, y, z, entityYaw, partialTicks, renderManager)
        }
        GlStateManager.disableRescaleNormal()
        GlStateManager.enableCull()
        GlStateManager.popMatrix()
    }

    private fun renderLink(boatEntity: ModularBoatEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        bindTexture(RopeAnchorTextureLocation)
        // front
        if(boatEntity.hasLink(BasicBoatEntity.FrontLink)) {
            boatEntity.getLinkedTo(BasicBoatEntity.FrontLink)?.let {
                GlStateManager.pushMatrix()
                GlStateManager.translate(17f, -4f, 0f)
                renderActualLink(boatEntity, it, BasicBoatEntity.FrontLink, entityYaw)
                bindTexture(RopeAnchorTextureLocation)
                ropeAnchorModel.render(boatEntity, 0f, 0f, boatEntity.ticksExisted.toFloat(), 0f, 0f, 1f)
                GlStateManager.popMatrix()
            }
        }

        // back
        if(boatEntity.hasLink(BasicBoatEntity.BackLink)) {
            boatEntity.getLinkedTo(BasicBoatEntity.BackLink)?.let {
                GlStateManager.pushMatrix()
                GlStateManager.translate(-17f, -4f, 0f)
                renderActualLink(boatEntity, it, BasicBoatEntity.BackLink, entityYaw)
                bindTexture(RopeAnchorTextureLocation)
                ropeAnchorModel.render(boatEntity, 0f, 0f, boatEntity.ticksExisted.toFloat(), 0f, 0f, 1f)
                GlStateManager.popMatrix()
            }
        }
    }

    private fun renderActualLink(thisBoat: BasicBoatEntity, targetEntity: Entity, sideFromThisBoat: Int, entityYaw: Float) {
        val anchorThis = thisBoat.calculateAnchorPosition(sideFromThisBoat)
        val anchorOther = (targetEntity as? BasicBoatEntity)?.calculateAnchorPosition(1-sideFromThisBoat)
                ?: targetEntity.positionVector
        val offsetX = anchorOther.x - anchorThis.x
        val offsetY = anchorOther.y - anchorThis.y
        val offsetZ = anchorOther.z - anchorThis.z

        GlStateManager.pushMatrix()
        removeScale()
        GlStateManager.scale(-1.0f, 1.0f, 1f)
        GlStateManager.rotate((180.0f - entityYaw - 90f), 0.0f, -1.0f, 0.0f)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        val tess = Tessellator.getInstance()
        val bufferbuilder = tess.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        val l = 32

        for (i1 in 0..l) {
            val f11 = i1.toFloat() / l
            bufferbuilder
                    .pos(offsetX * f11.toDouble(), offsetY * (f11 * f11 + f11).toDouble() * 0.5, offsetZ * f11.toDouble())
            bufferbuilder.color(138, 109, 68, 255)

            bufferbuilder.endVertex()
        }

        GlStateManager.glLineWidth(5f)
        tess.draw()
        GlStateManager.glLineWidth(1f)
        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    private fun removeScale() {
        val scale = 0.0625f
        val invScale = 1f/scale
        GlStateManager.scale(invScale, invScale, invScale)
        GlStateManager.scale(1.0f, -1.0f, 1.0f)
    }

    private fun setScale() {
        val scale = 0.0625f
        GlStateManager.scale(scale, scale, scale)
        GlStateManager.scale(1.0f, -1.0f, 1.0f)
    }

    private fun setTranslation(entity: ModularBoatEntity, x: Double, y: Double, z: Double) {
        GlStateManager.translate(x, y + 0.375f, z)
    }

    private fun setRotation(entity: ModularBoatEntity, entityYaw: Float, partialTicks: Float) {
        GlStateManager.rotate(180.0f - entityYaw - 90f, 0.0f, 1.0f, 0.0f)
        val timeSinceHit = entity.timeSinceHit - partialTicks
        var damage = entity.damageTaken - partialTicks

        if (damage < 0.0f) {
            damage = 0.0f
        }

        if (timeSinceHit > 0.0f) {
            GlStateManager.rotate(MathHelper.sin(timeSinceHit) * timeSinceHit * damage / 10.0f * entity.forwardDirection, 1.0f, 0.0f, 0.0f)
        }

        GlStateManager.scale(-1.0f, 1.0f, 1.0f)
    }

    override fun isMultipass() = true

    override fun renderMultipass(entity: ModularBoatEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.disableCull()
        bindTexture(TextureLocation)
        setTranslation(entity, x, y, z)
        setRotation(entity, entityYaw, partialTicks)
        setScale()
        model.noWater.showModel = true

        GlStateManager.colorMask(false, false, false, false)
        model.noWater.render(1f)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.enableCull()
        GlStateManager.popMatrix()
    }
}