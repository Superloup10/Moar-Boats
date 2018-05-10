package org.jglrxavpok.moarboats.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.storage.MapData
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.api.IControllable
import org.jglrxavpok.moarboats.client.gui.elements.GuiBinaryProperty
import org.jglrxavpok.moarboats.client.gui.elements.GuiToolButton
import org.jglrxavpok.moarboats.common.data.MapImageStripe
import org.jglrxavpok.moarboats.common.modules.HelmModule
import org.jglrxavpok.moarboats.common.modules.HelmModule.StripeLength
import org.jglrxavpok.moarboats.common.network.C10MapImageRequest
import org.jglrxavpok.moarboats.common.network.C12AddWaypoint
import org.lwjgl.input.Mouse

class GuiPathEditor(val player: EntityPlayer, val boat: IControllable, val mapData: MapData, val mapID: String): GuiScreen() {

    companion object {
        val maxZoom = 50f
        val minZoom = 1f
    }

    private var currentZoom = 1f
    private val mapScale = (1 shl mapData.scale.toInt())
    private val size = mapScale*128
    private val stripes = size/ StripeLength
    private val areaTexture = DynamicTexture(size, size)
    private val areaResLocation: ResourceLocation
    private var sentImageRequest = false
    private val stripesReceived = BooleanArray(stripes)
    private val mapHeight = IntArray(size*size)

    private val titleText = TextComponentTranslation("gui.path_editor.title", mapData.mapName)
    private val refreshButtonText = TextComponentTranslation("gui.path_editor.refresh")
    private val toolsText = TextComponentTranslation("gui.path_editor.tools")
    private val pathPropsText = TextComponentTranslation("gui.path_editor.path_properties")
    private val propertyLinesText = TextComponentTranslation("gui.path_editor.path_properties.lines")
    private val propertyPathfindingText = TextComponentTranslation("gui.path_editor.path_properties.path_finding")
    private val propertyLoopingText = TextComponentTranslation("gui.path_editor.path_properties.looping")
    private val propertyOneWayText = TextComponentTranslation("gui.path_editor.path_properties.one_way")
    private val toolMarkerText = TextComponentTranslation("gui.path_editor.tools.marker")
    private val toolEraserText = TextComponentTranslation("gui.path_editor.tools.eraser")

    private var buttonId = 0
    private val refreshMapButton = GuiButton(buttonId++, 0, 0, refreshButtonText.unformattedText)
    // Tools button
    private val markerButton = GuiToolButton(buttonId++, toolMarkerText.unformattedText, 0)
    private val eraserButton = GuiToolButton(buttonId++, toolEraserText.unformattedText, 1)
    private val toolButtonList = listOf(markerButton, eraserButton)

    // Properties buttons
    private val loopingButton = GuiBinaryProperty(buttonId++, Pair(propertyLoopingText.unformattedText, propertyOneWayText.unformattedText), Pair(2, 3))
    private val linesButton = GuiBinaryProperty(buttonId++, Pair(propertyLinesText.unformattedText, propertyPathfindingText.unformattedText), Pair(4, 5))
    private val propertyButtons = listOf(loopingButton, linesButton)

    init {
        val textureManager = Minecraft.getMinecraft().textureManager
        areaResLocation = textureManager.getDynamicTextureLocation("moarboats:path_editor_preview", areaTexture)
        mapHeight.fill(-1)
    }

    private var lastMouseX = 0
    private var lastMouseY = 0
    private var scrollX = size.toDouble()/2
    private var scrollZ = size.toDouble()/2
    private val world = player.world

    private val mapScreenSize = 200.0

    private val minX = mapData.xCenter-size/2
    private val minZ = mapData.zCenter-size/2

    private var toolButtonListEndY = 0
    private var menuX = 0
    private var menuY = 0
    private var horizontalBarY = 0

    override fun initGui() {
        super.initGui()
        addButton(refreshMapButton)

        menuX = (width/2+mapScreenSize/2 + 5).toInt()
        menuY = (height/2-mapScreenSize/2).toInt()
        var yOffset = 0
        val spacing = 10

        yOffset += spacing // tools label
        toolButtonList.forEach { button ->
            button.x = menuX
            button.y = menuY+yOffset
            addButton(button)
            yOffset += button.height
            yOffset += spacing
            button.selected = false
        }

        horizontalBarY = yOffset+menuY-spacing
        toolButtonListEndY = yOffset+menuY+spacing/2 // horizontal bar
        yOffset += spacing+spacing/2 // properties label + horizontal bar

        propertyButtons.forEach { button ->
            button.x = menuX
            button.y = menuY+yOffset
            addButton(button)
            yOffset += button.height
            yOffset += spacing
        }

        markerButton.selected = true

        refreshMapButton.x = width/2-refreshMapButton.width/2
        refreshMapButton.y = height-refreshMapButton.height-2
    }

    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        when(button) {
            refreshMapButton -> {
                sentImageRequest = false
                mapHeight.fill(-1)
                stripesReceived.fill(false)
            }

            in toolButtonList -> {
                toolButtonList.forEach {
                    it.selected = false
                }
                (button as GuiToolButton).selected = true
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        if(mouseButton == 1) {
            lastMouseX = mouseX
            lastMouseY = mouseY
        } else if(mouseButton == 0) {
            val mapX = width/2-mapScreenSize/2
            val mapY = height/2-mapScreenSize/2
            val posOnMapX = mouseX - mapX
            val posOnMapY = mouseY - mapY
            if(posOnMapX in 0.0..mapScreenSize
                    && posOnMapY in 0.0..mapScreenSize) {
                handleClickOnMap(posOnMapX, posOnMapY)
            }
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)

        if(clickedMouseButton == 1) {

            val dx = mouseX-lastMouseX
            val dy = mouseY-lastMouseY

            scrollX -= dx/currentZoom
            scrollZ -= dy/currentZoom
            lastMouseX = mouseX
            lastMouseY = mouseY
        }
    }

    private fun handleClickOnMap(x: Double, y: Double) {
        when {
            markerButton.selected -> addWaypointOnMap(x, y)
            eraserButton.selected -> removeWaypointOnMap(x, y)
        }
    }

    /**
     * Please free the returned blockpos
     */
    private fun pixelsToWorldCoords(x: Double, y: Double): BlockPos.PooledMutableBlockPos {
        val result = BlockPos.PooledMutableBlockPos.retain()

        val invZoom = (1.0/currentZoom)
        val viewportSize = invZoom*size
        val minU = ((scrollX.toDouble()-viewportSize/2)/size).coerceAtLeast(0.0)
        val maxU = ((scrollX.toDouble()+viewportSize/2)/size).coerceAtMost(1.0)
        val minV = ((scrollZ.toDouble()-viewportSize/2)/size).coerceAtLeast(0.0)
        val maxV = ((scrollZ.toDouble()+viewportSize/2)/size).coerceAtMost(1.0)

        val blockX = (mapData.xCenter + ((x/mapScreenSize * (maxU-minU) + minU) - 0.5)*size).toInt()
        val blockZ = (mapData.zCenter + ((y/mapScreenSize * (maxV-minV) + minV) - 0.5)*size).toInt()
        val indexX = (blockX-minX).coerceIn(0 until size)
        val indexZ = (blockZ-minZ).coerceIn(0 until size)
        val blockY = mapHeight[indexX + indexZ*size]
        result.setPos(blockX, blockY, blockZ)
        return result
    }

    private fun worldCoordsToPixels(x: Int, z: Int): Pair<Int, Int> {
        TODO()
    }

    private fun removeWaypointOnMap(x: Double, y: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addWaypointOnMap(x: Double, y: Double) {
        val pos = pixelsToWorldCoords(x, y)
        MoarBoats.network.sendToServer(C12AddWaypoint(pos, boat.entityID, mapID))
        pos.release()
        mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 2.5f))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        val invZoom = 1.0/currentZoom
        /*val low = (size/2 * invZoom).toInt()
        val upperBound = low + ((size-size*invZoom).toInt()).coerceAtLeast(0)
        scrollX = scrollX.coerceIn(low .. upperBound)
        scrollZ = scrollZ.coerceIn(low .. upperBound)*/
        val viewportSize = invZoom*size
        scrollX = scrollX.coerceIn(viewportSize/2 .. size-viewportSize/2)
        scrollZ = scrollZ.coerceIn(viewportSize/2 .. size-viewportSize/2)

        val mapX = width/2-mapScreenSize/2
        val mapY = height/2-mapScreenSize/2
        renderMap(mapX, mapY, 0.0, mapScreenSize)

        super.drawScreen(mouseX, mouseY, partialTicks)
        fontRenderer.drawStringWithShadow(toolsText.unformattedText, menuX.toFloat(), menuY.toFloat(), 0xFFF0F0F0.toInt())

        mc.textureManager.bindTexture(GuiToolButton.WidgetsTextureLocation)
        Gui.drawModalRectWithCustomSizedTexture(menuX, horizontalBarY, 0f, 100f, 120, 20, 120f, 120f)
        fontRenderer.drawStringWithShadow(pathPropsText.unformattedText, menuX.toFloat(), toolButtonListEndY.toFloat(), 0xFFF0F0F0.toInt())

        drawCenteredString(fontRenderer, titleText.unformattedText, width/2, 10, 0xFFF0F0F0.toInt())

        renderTool(mouseX, mouseY, mapX, mapY)



        val posOnMapX = mouseX - mapX
        val posOnMapY = mouseY - mapY
        if(posOnMapX in 0.0..mapScreenSize
                && posOnMapY in 0.0..mapScreenSize) {
            val pos = pixelsToWorldCoords(posOnMapX, posOnMapY)
            drawHoveringText("X: ${pos.x} Y: ${pos.y} Z: ${pos.z}", mouseX, mouseY)
            pos.release()
        }
    }

    private fun renderTool(mouseX: Int, mouseY: Int, mapX: Double, mapY: Double) {
        val localX = mouseX-mapX
        val localY = mouseY-mapY
        if(localX < 0 || localX >= mapScreenSize
        || localY < 0 || localY >= mapScreenSize)
            return

        val iconIndex = when {
            markerButton.selected -> 0
            eraserButton.selected -> 1
            else -> 0
        }

        val toolX = iconIndex % GuiToolButton.ToolIconCountPerLine
        val toolY = iconIndex / GuiToolButton.ToolIconCountPerLine
        val minU = toolX.toFloat() * 20f
        val minV = toolY.toFloat() * 20f
        mc.textureManager.bindTexture(GuiToolButton.WidgetsTextureLocation)
        val toolScreenX = mouseX-10
        val toolScreenY = mouseY-15
        Gui.drawModalRectWithCustomSizedTexture(toolScreenX, toolScreenY, minU, minV, 20, 20, GuiToolButton.WidgetsTextureSize, GuiToolButton.WidgetsTextureSize)
    }

    private fun renderMap(x: Double, y: Double, margins: Double, mapSize: Double) {
        val mc = Minecraft.getMinecraft()
        GlStateManager.pushMatrix()
        GlStateManager.translate(x+margins, y+margins, 0.0)
        GlStateManager.scale(0.0078125f, 0.0078125f, 0.0078125f)
        GlStateManager.scale(mapSize-margins*2, mapSize-margins*2, 0.0)

        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        mc.textureManager.bindTexture(areaResLocation)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE)
        GlStateManager.disableAlpha()

        val invZoom = (1.0/currentZoom)
        val viewportSize = invZoom*size
        val minU = ((scrollX.toDouble()-viewportSize/2)/size).coerceAtLeast(0.0)
        val maxU = ((scrollX.toDouble()+viewportSize/2)/size).coerceAtMost(1.0)
        val minV = ((scrollZ.toDouble()-viewportSize/2)/size).coerceAtLeast(0.0)
        val maxV = ((scrollZ.toDouble()+viewportSize/2)/size).coerceAtMost(1.0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos(0.0, 128.0, -0.009999999776482582).tex(minU, maxV).endVertex()
        bufferbuilder.pos(128.0, 128.0, -0.009999999776482582).tex(maxU, maxV).endVertex()
        bufferbuilder.pos(128.0, 0.0, -0.009999999776482582).tex(maxU, minV).endVertex()
        bufferbuilder.pos(0.0, 0.0, -0.009999999776482582).tex(minU, minV).endVertex()
        tessellator.draw()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()

        GlStateManager.enableBlend()
        GlStateManager.translate(0.0, 0.0, 1.0)

        GlStateManager.enableAlpha()

        GlStateManager.popMatrix()
    }

    override fun updateScreen() {
        super.updateScreen()
        if(!sentImageRequest) {
            MoarBoats.network.sendToServer(C10MapImageRequest(mapID, boat.entityID, HelmModule.id))
            sentImageRequest = true
        }

        for(stripeIndex in 0 until stripes) {
            val received = stripesReceived[stripeIndex]
            if(!received) {
                val storage = world.mapStorage
                if(storage != null) {
                    val id = "moarboats:map_preview/$mapID/$stripeIndex"
                    val stripe = storage.getOrLoadData(MapImageStripe::class.java, id) as? MapImageStripe
                    if(stripe != null) {
                        val textureStripe = stripe.textureStripe
                        val offset = stripeIndex * StripeLength * size
                        for(i in 0 until StripeLength * size) {
                            areaTexture.textureData[i+offset] = textureStripe[i]
                        }
                        areaTexture.updateDynamicTexture()
                        stripesReceived[stripeIndex] = true
                    }
                }
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val dwheel = Mouse.getEventDWheel()
        val zoomFactor = when {
            dwheel > 0 -> 1f+5f/20f
            dwheel < 0 -> 1f-5f/20f
            else -> 1f
        }
        currentZoom *= zoomFactor
        currentZoom = currentZoom.coerceIn(minZoom..maxZoom)
    }
}