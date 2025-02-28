package org.jglrxavpok.moarboats.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fluids.Fluid
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.api.IControllable
import org.jglrxavpok.moarboats.common.containers.EmptyContainer
import org.jglrxavpok.moarboats.common.modules.IFluidBoatModule
import org.lwjgl.opengl.GL11

class GuiTankModule(playerInventory: InventoryPlayer, module: BoatModule, boat: IControllable): GuiModuleBase(module, boat, playerInventory, EmptyContainer(playerInventory)) {

    val tankModule = module as IFluidBoatModule

    init {
        shouldRenderInventoryName = false
    }

    override val moduleBackground: ResourceLocation = ResourceLocation(MoarBoats.ModID, "textures/gui/fluid.png")

    override fun drawModuleForeground(mouseX: Int, mouseY: Int) {
        super.drawModuleForeground(mouseX, mouseY)
        val localX = mouseX - guiLeft
        val localY = mouseY - guiTop
        if(localX in 60..(60+55) && localY in 6..(6+75)) {
            val fluidName = tankModule.getFluidInside(boat)?.getLocalizedName(tankModule.getContents(boat)!!) ?: "nothing"
            drawHoveringText(TextComponentTranslation(MoarBoats.ModID+".tank_level", tankModule.getFluidAmount(boat), tankModule.getCapacity(boat), fluidName).unformattedText, localX, localY)
        }
    }

    override fun drawModuleBackground(mouseX: Int, mouseY: Int) {
        super.drawModuleBackground(mouseX, mouseY)
        mc.textureManager.bindTexture(moduleBackground)
        GlStateManager.disableCull()
        val fluid = tankModule.getFluidInside(boat)
        if(fluid != null) {
            renderFluidInGui(guiLeft+56, guiTop+80, fluid, tankModule.getFluidAmount(boat), tankModule.getCapacity(boat), horizontalTilesCount = 4)
        }
    }

    companion object {
        fun renderFluidInGui(leftX: Int, bottomY: Int, fluid: Fluid, fluidAmount: Int, fluidCapacity: Int, horizontalTilesCount: Int) {
            val energyHeight = (73 * (fluidAmount/fluidCapacity.toFloat())).toInt()
            val mc = Minecraft.getMinecraft()
            mc.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
            val sprite = mc.textureMapBlocks.getAtlasSprite(fluid.still.toString())
            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
            val maxXOffset = horizontalTilesCount
            for(xOffset in 0 until maxXOffset) {
                val maxYOffset = energyHeight/16
                for(yOffset in 0 until maxYOffset) {
                    buffer.pos(leftX+xOffset*16.0, bottomY-yOffset*16.0, 0.0).tex(sprite.minU.toDouble(), sprite.minV.toDouble()).endVertex()
                    buffer.pos(leftX+xOffset*16.0+16.0, bottomY-yOffset*16.0, 0.0).tex(sprite.maxU.toDouble(), sprite.minV.toDouble()).endVertex()
                    buffer.pos(leftX+xOffset*16.0+16.0, bottomY-yOffset*16-16.0, 0.0).tex(sprite.maxU.toDouble(), sprite.maxV.toDouble()).endVertex()
                    buffer.pos(leftX+xOffset*16.0, bottomY-yOffset*16-16.0, 0.0).tex(sprite.minU.toDouble(), sprite.maxV.toDouble()).endVertex()
                }

                // add little part on top
                val remainingHeight = energyHeight % 16
                val deltaH = remainingHeight/16.0
                val minV = sprite.minV.toDouble()
                val maxV = sprite.maxV.toDouble() * deltaH + (1.0-deltaH) * minV
                buffer.pos(leftX+xOffset*16.0, bottomY-maxYOffset*16.0, 0.0).tex(sprite.minU.toDouble(), minV).endVertex()
                buffer.pos(leftX+xOffset*16.0+16.0, bottomY-maxYOffset*16.0, 0.0).tex(sprite.maxU.toDouble(), minV).endVertex()
                buffer.pos(leftX+xOffset*16.0+16.0, bottomY-maxYOffset*16.0-remainingHeight, 0.0).tex(sprite.maxU.toDouble(), maxV).endVertex()
                buffer.pos(leftX+xOffset*16.0, bottomY-maxYOffset*16.0-remainingHeight, 0.0).tex(sprite.minU.toDouble(), maxV).endVertex()
            }
            tessellator.draw()

        }
    }
}