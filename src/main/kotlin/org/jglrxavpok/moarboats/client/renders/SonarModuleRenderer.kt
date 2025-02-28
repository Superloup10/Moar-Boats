package org.jglrxavpok.moarboats.client.renders

import net.minecraft.block.BlockLiquid
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.init.Blocks
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.common.modules.SonarModule
import org.jglrxavpok.moarboats.common.modules.SurroundingsMatrix
import org.jglrxavpok.moarboats.extensions.toDegrees

object SonarModuleRenderer : BoatModuleRenderer() {

    init {
        registryName = SonarModule.id
    }

    private val testMatrix = SurroundingsMatrix(32)

    override fun renderModule(boat: ModularBoatEntity, module: BoatModule, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float, renderManager: RenderManager) {
        module as SonarModule
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.75f, 0.75f, 0.75f)
        GlStateManager.scale(-1f, 1f, 1f)

        for(xOffset in arrayOf(-1.25f, 1.0f)) {
            for(zOffset in arrayOf(-0.625f, 0.875f)) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(xOffset, 4f/16f, zOffset)
                GlStateManager.scale(0.25f, 0.25f, 0.25f)
                renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
                val block = Blocks.NOTEBLOCK
                Minecraft.getMinecraft().blockRendererDispatcher.renderBlockBrightness(block.defaultState, boat.brightness)
                GlStateManager.popMatrix()
            }
        }

        // render gradient
        if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            GlStateManager.rotate(-(180.0f - entityYaw - 90f), 0.0f, 1.0f, 0.0f)
            testMatrix.compute(boat.world, boat.positionX, boat.positionY, boat.positionZ).removeNotConnectedToCenter()
            val gradient = testMatrix.computeGradient()
            testMatrix.forEach { xOffset, zOffset, potentialState ->
                if(potentialState != null) {
                    val gradientVal = gradient[testMatrix.pos2index(xOffset, zOffset)]
                    if(gradientVal.x.toInt() != 0 || gradientVal.y.toInt() != 0) {
                        GlStateManager.pushMatrix()
                        GlStateManager.scale(0.25f, 0.25f, 0.25f)
                        GlStateManager.translate(xOffset.toDouble(), 1.0, zOffset.toDouble())

                        val angle = Math.atan2(gradientVal.y.toDouble(), gradientVal.x.toDouble()).toDegrees()
                        GlStateManager.rotate(angle.toFloat(), 0f, 1f, 0f)
                        GlStateManager.scale(0.1f, 0.1f, gradientVal.length() * 0.1f)
                        if(potentialState.block is BlockLiquid) {
                            Minecraft.getMinecraft().blockRendererDispatcher.renderBlockBrightness(Blocks.EMERALD_BLOCK.defaultState, boat.brightness)
                        } else {
                            Minecraft.getMinecraft().blockRendererDispatcher.renderBlockBrightness(potentialState, boat.brightness)
                        }

                        GlStateManager.popMatrix()
                    }
                }
            }

        }

        GlStateManager.popMatrix()
    }
}