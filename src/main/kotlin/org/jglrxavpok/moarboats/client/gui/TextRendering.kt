package org.jglrxavpok.moarboats.client.gui

import net.minecraft.client.gui.Font

fun Font.drawCenteredString(text: String, x: Int, y: Int, color: Int, shadow: Boolean = false) {
    val textWidth = getStringWidth(text)
    val textX = x - textWidth/2
    if(shadow)
        drawStringWithShadow(text, textX.toFloat(), y.toFloat(), color)
    else
        drawString(text, textX.toFloat(), y.toFloat(), color)
}