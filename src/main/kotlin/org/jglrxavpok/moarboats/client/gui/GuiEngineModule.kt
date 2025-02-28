package org.jglrxavpok.moarboats.client.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiLockIconButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.client.config.GuiSlider
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.api.BoatModule
import org.jglrxavpok.moarboats.api.BoatModuleRegistry
import org.jglrxavpok.moarboats.api.IControllable
import org.jglrxavpok.moarboats.common.containers.ContainerBase
import org.jglrxavpok.moarboats.common.modules.BaseEngineModule
import org.jglrxavpok.moarboats.common.modules.BlockedByRedstone
import org.jglrxavpok.moarboats.common.modules.NoBlockReason
import org.jglrxavpok.moarboats.common.network.CChangeEngineMode
import org.jglrxavpok.moarboats.common.network.CChangeEngineSpeed
import org.lwjgl.opengl.GL11

class GuiEngineModule(playerInventory: InventoryPlayer, engine: BoatModule, boat: IControllable, container: ContainerBase):
        GuiModuleBase(engine, boat, playerInventory, container, isLarge = true) {

    companion object {
        val RedstoneDustStack = ItemStack(Items.REDSTONE)
    }

    override val moduleBackground = ResourceLocation(MoarBoats.ModID, "textures/gui/modules/${engine.id.resourcePath}.png")
    val barsTexture = ResourceLocation("minecraft:textures/gui/bars.png")
    val remainingCurrentItem = TextComponentTranslation("gui.engine.remainingCurrent")
    val estimatedTimeText = TextComponentTranslation("gui.engine.estimatedTime")
    private val lockInPlaceButton = GuiLockIconButton(0, 0, 0)
    private val lockText = TextComponentTranslation("gui.engine.lock")
    private val lockedByRedstone = TextComponentTranslation("gui.engine.blocked.redstone")
    private val foreverText = TextComponentTranslation("gui.engine.forever")
    private val speedSetting = TextComponentTranslation("gui.engine.powerSetting")
    private val minimumSpeedText = TextComponentTranslation("gui.engine.power.min")
    private val maximumSpeedText = TextComponentTranslation("gui.engine.power.max")
    private val normalSpeedText = TextComponentTranslation("gui.engine.power.normal")
    private val blockedByModuleText = TextComponentTranslation("gui.engine.blocked.module")
    private val unknownBlockReasonText = { str: String -> TextComponentTranslation("gui.engine.blocked.unknown", str) }
    private val imposedSpeedText = { str: String -> TextComponentTranslation("moarboats.gui.engine.imposed_boost", str) }
    private val engine = module as BaseEngineModule

    private lateinit var speedSlider: GuiSlider
    private val speedIconTexture = ResourceLocation(MoarBoats.ModID, "textures/gui/modules/engines/speed_setting.png")
    private val sliderCallback = GuiSlider.ISlider { slider ->
        MoarBoats.network.sendToServer(CChangeEngineSpeed(boat.entityID, module.id, slider.value.toFloat()/100f))
    }

    override fun initGui() {
        super.initGui()
        lockInPlaceButton.x = guiLeft + xSize - lockInPlaceButton.width - 5
        lockInPlaceButton.y = guiTop + 5
        addButton(lockInPlaceButton)

        val speedSettingMargins = 30
        val speedSettingHorizontalSize = xSize - speedSettingMargins*2

        speedSlider = GuiSlider(1, guiLeft + speedSettingMargins, guiTop + 90, speedSettingHorizontalSize, 20, "${speedSetting.unformattedText}: ", "%", -50.0, 50.0, 0.0, false, true, sliderCallback)
        addButton(speedSlider)
        speedSlider.value = (engine.speedProperty[boat].toDouble()) * 100f
    }

    override fun updateScreen() {
        super.updateScreen()
        speedSlider.updateSlider()
        lockInPlaceButton.isLocked = engine.stationaryProperty[boat]
    }

    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        when(button) {
            lockInPlaceButton -> {
                MoarBoats.network.sendToServer(CChangeEngineMode(boat.entityID, module.id, !engine.stationaryProperty[boat]))
            }
        }
    }

    override fun renderHoveredToolTip(mouseX: Int, mouseY: Int) {
        when {
            lockInPlaceButton.mousePressed(mc, mouseX, mouseY) -> drawHoveringText(lockText.unformattedText, mouseX, mouseY)
            else -> super.renderHoveredToolTip(mouseX, mouseY)
        }
    }

    override fun drawModuleForeground(mouseX: Int, mouseY: Int) {
        val remaining = engine.remainingTimeInPercent(boat)
        val estimatedTotalTicks = engine.estimatedTotalTicks(boat)
        val estimatedTime = estimatedTotalTicks / 20


        val infoY = 26
        fontRenderer.drawCenteredString(remainingCurrentItem.unformattedText, 88, infoY, 0xFFFFFFFF.toInt(), shadow = true)

        mc.renderEngine.bindTexture(barsTexture)
        val barIndex = 4
        val barSize = xSize*.85f
        val x = xSize/2f - barSize/2f
        drawBar(x, infoY+10f, barIndex, barSize, fill = if(remaining.isFinite()) remaining else 1f)
        if(estimatedTime.isInfinite()) {
            fontRenderer.drawCenteredString(estimatedTimeText.unformattedText, 88, infoY+18, 0xFFF0F0F0.toInt(), shadow = true)
            fontRenderer.drawCenteredString(foreverText.unformattedText, 88, infoY+28, 0xFF50A050.toInt())
        } else if(!estimatedTime.isNaN()) {
            fontRenderer.drawCenteredString(estimatedTimeText.unformattedText, 88, infoY+18, 0xFFF0F0F0.toInt(), shadow = true)
            fontRenderer.drawCenteredString("${estimatedTime.toInt()}s", 88, infoY+28, 0xFF50A050.toInt())
        }
        renderBlockReason(infoY+38)
        fontRenderer.drawCenteredString(speedSetting.unformattedText, 88, infoY+52, 0xFFF0F0F0.toInt(), shadow = true)
//        if(boat.isSpeedImposed()) {
            fontRenderer.drawCenteredString(imposedSpeedText("${(boat.imposedSpeed * 100.0).toInt()}").unformattedText, 88, infoY+42, 0xFFFFFF, shadow=true)
  //      }

        when {
            speedSlider.valueInt == -50 -> fontRenderer.drawCenteredString(minimumSpeedText.unformattedText, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
            speedSlider.valueInt == 50 -> fontRenderer.drawCenteredString(maximumSpeedText.unformattedText, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
            speedSlider.valueInt == 0 -> fontRenderer.drawCenteredString(normalSpeedText.unformattedText, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
        }

        renderSpeedIcon(0, 5, infoY + 40 + speedSlider.height)
        renderSpeedIcon(2, xSize - 25, infoY + 40 + speedSlider.height)
    }

    private fun renderBlockReason(y: Int) {
        when(boat.blockedReason) {
            NoBlockReason -> {}
            BlockedByRedstone -> renderPrettyReason(y, lockedByRedstone.unformattedText, RedstoneDustStack)
            else -> {
                if(boat.blockedReason is BoatModule) {
                    val blockingModule = boat.blockedReason as BoatModule
                    val itemstack = ItemStack(BoatModuleRegistry[blockingModule.id].correspondingItem)
                    renderPrettyReason(y, blockedByModuleText.unformattedText, itemstack)
                } else {
                    fontRenderer.drawCenteredString(unknownBlockReasonText(boat.blockedReason.toString()).unformattedText, 88, y, 0xFF0000)
                }
            }
        }
    }

    private fun renderPrettyReason(y: Int, text: String, itemStack: ItemStack) {
        fontRenderer.drawCenteredString(text, 88-16, y, 0xFF0000)
        val textWidth = fontRenderer.getStringWidth(text)
        val textX = 88-16 - textWidth/2
        zLevel = 100.0f
        itemRender.zLevel = 100.0f
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.color(1f, 1f, 1f)
        val itemX = textX+textWidth + 1
        val itemY = fontRenderer.FONT_HEIGHT/2 - 8 + y
        itemRender.renderItemAndEffectIntoGUI(itemStack, itemX, itemY)
        itemRender.zLevel = 0.0f
        zLevel = 0.0f
    }

    private fun renderSpeedIcon(ordinal: Int, x: Int, y: Int) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        val width = 20
        val height = 20
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        val margins = 0
        val minU = 10.0/32.0
        val maxU = 1.0
        val minV = ordinal * 16.0 / 64.0
        val maxV = (ordinal * 16.0 + 16.0) / 64.0
        bufferbuilder
                .pos((x+margins).toDouble(), (y+margins).toDouble(), 0.0)
                .tex(minU, minV)
                .endVertex()
        bufferbuilder
                .pos((x+width - margins*2).toDouble(), (y+margins).toDouble(), 0.0)
                .tex(maxU, minV)
                .endVertex()
        bufferbuilder
                .pos((x+width - margins*2).toDouble(), (y+height - margins*2).toDouble(), 0.0)
                .tex(maxU, maxV)
                .endVertex()
        bufferbuilder
                .pos((x+margins).toDouble(), (y+height - margins*2).toDouble(), 0.0)
                .tex(minU, maxV)
                .endVertex()

        mc.textureManager.bindTexture(speedIconTexture)
        GlStateManager.disableDepth()
        GlStateManager.disableCull()
        tessellator.draw()
        GlStateManager.enableCull()
        GlStateManager.enableDepth()
    }

    private fun drawBar(x: Float, y: Float, barIndex: Int, barSize: Float, fill: Float) {
        val barWidth = 182f
        val filledWidth = fill * barWidth

        val scale = barSize/barWidth
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, scale)
        GlStateManager.translate((x/scale).toDouble(), (y/scale).toDouble(), 0.0)
        drawTexturedModalRect(0, 0, 0, barIndex*10+5, (filledWidth).toInt(), 5)
        drawTexturedModalRect(filledWidth.toInt(), 0, filledWidth.toInt(), barIndex*10, (barWidth-filledWidth+1).toInt(), 5)
        GlStateManager.popMatrix()
    }

}