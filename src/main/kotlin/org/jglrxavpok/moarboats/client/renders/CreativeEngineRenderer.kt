package org.jglrxavpok.moarboats.client.renders

import net.minecraft.client.Minecraft
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.block.Blocks
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.common.modules.CreativeEngineModule

object CreativeEngineRenderer : BoatModuleRenderer() {

    init {
        registryName = CreativeEngineModule.id
    }

    override fun renderModule(boat: ModularBoatEntity, module: BoatModule, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float, EntityRendererManager: EntityRendererManager) {
        module as CreativeEngineModule
        GlStateManager.pushMatrix()
        GlStateManager.scalef(0.75f, 0.75f, 0.75f)
        GlStateManager.translatef(0.15f, -4f/16f, 0.5f)
        EntityRendererManager.textureManager.bind(AtlasTexture.LOCATION_BLOCKS)
        val block = Blocks.BEDROCK

        Minecraft.getInstance().blockRenderer.renderSingleBlock(block.defaultBlockState(), boat.brightness)

        GlStateManager.popMatrix()
    }
}