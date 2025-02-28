package org.jglrxavpok.moarboats.common.items

import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.blocks.BlockUnpoweredCargoStopper
import org.jglrxavpok.moarboats.common.blocks.BlockUnpoweredWaterborneComparator
import org.jglrxavpok.moarboats.common.blocks.BlockUnpoweredWaterborneConductor

object CargoStopperItem : WaterborneItem() {

    override val correspondingBlock = BlockUnpoweredCargoStopper
    private val descriptionText = TextComponentTranslation(MoarBoats.ModID+".tile.cargo_stopper.description")

    init {
        creativeTab = MoarBoats.CreativeTab
        unlocalizedName = "cargo_stopper"
        registryName = ResourceLocation(MoarBoats.ModID, "cargo_stopper")
        maxStackSize = 64
    }

    override fun addInformation(stack: ItemStack?, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag?) {
        tooltip.add(descriptionText.unformattedText)
    }
}