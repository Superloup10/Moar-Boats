package org.jglrxavpok.moarboats.client.renders

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.client.models.ModelRudder
import org.jglrxavpok.moarboats.common.modules.RudderModule

object RudderModuleRenderer : BoatModuleRenderer() {

    init {
        registryName = RudderModule.id
    }

    val rudderModel = ModelRudder()
    val textureLocation = ResourceLocation("textures/blocks/planks_oak.png")

    override fun renderModule(boat: ModularBoatEntity, module: BoatModule, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float, renderManager: RenderManager) {
        module as RudderModule
        GlStateManager.pushMatrix()
        GlStateManager.scale(-1f, -1f, -1f)
        GlStateManager.translate(0.0f, 0f, -0.5f*0.0625f)
        val angle = RudderModule.RudderAngleMultiplier[boat]*90f
        rudderModel.rudderBlade.rotateAngleY = angle
        renderManager.renderEngine.bindTexture(textureLocation)
        rudderModel.render(boat, 0f, 0f, 0f, 0f, 0f, 0.0625f)
        GlStateManager.popMatrix()
    }
}