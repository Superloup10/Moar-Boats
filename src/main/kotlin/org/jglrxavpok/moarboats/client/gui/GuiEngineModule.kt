package org.jglrxavpok.moarboats.client.gui

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.gui.widget.button.LockIconButton
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.client.config.GuiSlider
import net.minecraftforge.fml.client.config.GuiUtils.drawTexturedModalRect
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

class GuiEngineModule(playerInventory: PlayerInventory, engine: BoatModule, boat: IControllable, container: ContainerBase):
        GuiModuleBase<ContainerBase>(engine, boat, playerInventory, container, isLarge = true) {

    companion object {
        val RedstoneDustStack = ItemStack(Items.REDSTONE)
    }

    override val moduleBackground = ResourceLocation(MoarBoats.ModID, "textures/gui/modules/${engine.id.path}.png")
    private val engine = module as BaseEngineModule
    val barsTexture = ResourceLocation("minecraft:textures/gui/bars.png")
    val remainingCurrentItem = TranslationTextComponent("gui.engine.remainingCurrent")
    val estimatedTimeText = TranslationTextComponent("gui.engine.estimatedTime")
    private val lockInPlaceButton = LockIconButton(0, 0) {
        MoarBoats.network.sendToServer(CChangeEngineMode(boat.entityID, module.id, !(engine as BaseEngineModule).stationaryProperty[boat]))
    }
    private val lockText = TranslationTextComponent("gui.engine.lock")
    private val lockedByRedstone = TranslationTextComponent("gui.engine.blocked.redstone")
    private val foreverText = TranslationTextComponent("gui.engine.forever")
    private val speedSetting = TranslationTextComponent("gui.engine.powerSetting")
    private val minimumSpeedText = TranslationTextComponent("gui.engine.power.min")
    private val maximumSpeedText = TranslationTextComponent("gui.engine.power.max")
    private val normalSpeedText = TranslationTextComponent("gui.engine.power.normal")
    private val blockedByModuleText = TranslationTextComponent("gui.engine.blocked.module")
    private val unknownBlockReasonText = { str: String -> TranslationTextComponent("gui.engine.blocked.unknown", str) }
    private val imposedSpeedText = { str: String -> TranslationTextComponent("moarboats.gui.engine.imposed_boost", str) }

    private lateinit var speedSlider: GuiSlider
    private val speedIconTexture = ResourceLocation(MoarBoats.ModID, "textures/gui/modules/engines/speed_setting.png")
    private val sliderCallback = Button.IPressable { slider ->
        MoarBoats.network.sendToServer(CChangeEngineSpeed(boat.entityID, module.id, speedSlider.value.toFloat()/100f))
    }

    override fun init() {
        super.init()
        lockInPlaceButton.x = guiLeft + imageWidth - lockInPlaceButton.width - 5
        lockInPlaceButton.y = guiTop + 5
        addButton(lockInPlaceButton)

        val speedSettingMargins = 30
        val speedSettingHorizontalSize = imageWidth - speedSettingMargins*2

        speedSlider = GuiSlider(guiLeft + speedSettingMargins, guiTop + 90, speedSettingHorizontalSize, 20, "${speedSetting.coloredString}: ", "%", -50.0, 50.0, 0.0, false, true, sliderCallback)
        addButton(speedSlider)
        speedSlider.value = (engine.speedProperty[boat].toDouble()) * 100f
    }

    override fun tick() {
        super.tick()
        speedSlider.updateSlider()
        lockInPlaceButton.isLocked = engine.stationaryProperty[boat]
    }

    override fun renderTooltip(mouseX: Int, mouseY: Int) {
        when {
            lockInPlaceButton.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) -> drawHoveringText(lockText.coloredString, mouseX, mouseY)
            else -> super.renderTooltip(mouseX, mouseY)
        }
    }

    override fun drawModuleForeground(mouseX: Int, mouseY: Int) {
        val remaining = engine.remainingTimeInPercent(boat)
        val estimatedTotalTicks = engine.estimatedTotalTicks(boat)
        val estimatedTime = estimatedTotalTicks / 20


        val infoY = 26
        font.drawCenteredString(remainingCurrentItem.coloredString, 88, infoY, 0xFFFFFFFF.toInt(), shadow = true)

        mc.textureManager.bind(barsTexture)
        val barIndex = 4
        val barSize = imageWidth*.85f
        val x = imageWidth/2f - barSize/2f
        drawBar(x, infoY+10f, barIndex, barSize, fill = if(remaining.isFinite()) remaining else 1f)
        if(estimatedTime.isInfinite()) {
            font.drawCenteredString(estimatedTimeText.coloredString, 88, infoY+18, 0xFFF0F0F0.toInt(), shadow = true)
            font.drawCenteredString(foreverText.coloredString, 88, infoY+28, 0xFF50A050.toInt())
        } else if(!estimatedTime.isNaN()) {
            font.drawCenteredString(estimatedTimeText.coloredString, 88, infoY+18, 0xFFF0F0F0.toInt(), shadow = true)
            font.drawCenteredString("${estimatedTime.toInt()}s", 88, infoY+28, 0xFF50A050.toInt())
        }
        renderBlockReason(infoY+38)
        font.drawCenteredString(speedSetting.coloredString, 88, infoY+52, 0xFFF0F0F0.toInt(), shadow = true)
//        if(boat.isSpeedImposed()) {
            font.drawCenteredString(imposedSpeedText("${(boat.imposedSpeed * 100.0).toInt()}").coloredString, 88, infoY+42, 0xFFFFFF, shadow=true)
  //      }

        when {
            speedSlider.valueInt == -50 -> font.drawCenteredString(minimumSpeedText.coloredString, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
            speedSlider.valueInt == 50 -> font.drawCenteredString(maximumSpeedText.coloredString, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
            speedSlider.valueInt == 0 -> font.drawCenteredString(normalSpeedText.coloredString, 88, infoY + 70 + speedSlider.height, 0xFF0000F0.toInt())
        }

        renderSpeedIcon(0, 5, infoY + 40 + speedSlider.height)
        renderSpeedIcon(2, imageWidth - 25, infoY + 40 + speedSlider.height)
    }

    private fun renderBlockReason(y: Int) {
        when(boat.blockedReason) {
            NoBlockReason -> {}
            BlockedByRedstone -> renderPrettyReason(y, lockedByRedstone.coloredString, RedstoneDustStack)
            else -> {
                if(boat.blockedReason is BoatModule) {
                    val blockingModule = boat.blockedReason as BoatModule
                    val itemstack = ItemStack(BoatModuleRegistry[blockingModule.id].correspondingItem)
                    renderPrettyReason(y, blockedByModuleText.coloredString, itemstack)
                } else {
                    font.drawCenteredString(unknownBlockReasonText(boat.blockedReason.toString()).coloredString, 88, y, 0xFF0000)
                }
            }
        }
    }

    private fun renderPrettyReason(y: Int, text: String, itemStack: ItemStack) {
        font.drawCenteredString(text, 88-16, y, 0xFF0000)
        val textWidth = font.width(text)
        val textX = 88-16 - textWidth/2
        blitOffset = 100
        itemRenderer.blitOffset = 100.0f
        RenderHelper.turnOnGui()
        GlStateManager.color3f(1f, 1f, 1f)
        val itemX = textX+textWidth + 1
        val itemY = font.FONT_HEIGHT/2 - 8 + y
        itemRenderer.renderGuiItem(itemStack, itemX, itemY)
        itemRenderer.blitOffset = 0.0f
        blitOffset = 0
    }

    private fun renderSpeedIcon(ordinal: Int, x: Int, y: Int) {
        GlStateManager.color4f(1f, 1f, 1f, 1f)
        val width = 20
        val height = 20
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.builder
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        val margins = 0
        val minU = 10.0/32.0
        val maxU = 1.0
        val minV = ordinal * 16.0 / 64.0
        val maxV = (ordinal * 16.0 + 16.0) / 64.0
        bufferbuilder
                .vertex((x+margins).toDouble(), (y+margins).toDouble(), 0.0)
                .uv(minU, minV)
                .endVertex()
        bufferbuilder
                .vertex((x+width - margins*2).toDouble(), (y+margins).toDouble(), 0.0)
                .uv(maxU, minV)
                .endVertex()
        bufferbuilder
                .vertex((x+width - margins*2).toDouble(), (y+height - margins*2).toDouble(), 0.0)
                .uv(maxU, maxV)
                .endVertex()
        bufferbuilder
                .vertex((x+margins).toDouble(), (y+height - margins*2).toDouble(), 0.0)
                .uv(minU, maxV)
                .endVertex()

        mc.textureManager.bind(speedIconTexture)
        GlStateManager.disableDepthTest()
        GlStateManager.disableCull()
        tessellator.end()
        GlStateManager.enableCull()
        GlStateManager.enableDepthTest()
    }

    private fun drawBar(x: Float, y: Float, barIndex: Int, barSize: Float, fill: Float) {
        val barWidth = 182f
        val filledWidth = fill * barWidth

        val scale = barSize/barWidth
        GlStateManager.pushMatrix()
        GlStateManager.scalef(scale, scale, scale)
        GlStateManager.translated((x/scale).toDouble(), (y/scale).toDouble(), 0.0)
        drawTexturedModalRect(0, 0, 0, barIndex*10+5, (filledWidth).toInt(), 5, blitOffset.toFloat())
        drawTexturedModalRect(filledWidth.toInt(), 0, filledWidth.toInt(), barIndex*10, (barWidth-filledWidth+1).toInt(), 5, blitOffset.toFloat())
        GlStateManager.popMatrix()
    }

}