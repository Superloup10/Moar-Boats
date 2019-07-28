package org.jglrxavpok.moarboats.common

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.IInteractionObject
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import net.minecraftforge.fml.network.FMLPlayMessages
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.client.gui.GuiEnergy
import org.jglrxavpok.moarboats.client.gui.GuiMappingTable
import org.jglrxavpok.moarboats.client.gui.GuiWaypointEditor
import org.jglrxavpok.moarboats.common.containers.ContainerMappingTable
import org.jglrxavpok.moarboats.common.containers.EmptyContainer
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.common.modules.HelmModule
import org.jglrxavpok.moarboats.common.tileentity.TileEntityEnergy
import org.jglrxavpok.moarboats.common.tileentity.TileEntityMappingTable
import org.jglrxavpok.moarboats.extensions.use

object MoarBoatsGuiHandler: IGuiHandler {

    fun dispatchGui(container: FMLPlayMessages.OpenContainer): GuiScreen? {
        println("Dispatch: ${container.id}")
        val id = container.id
        if(id.namespace == MoarBoats.ModID) {
            val route = id.path.split("/")
            println("route: ${route.joinToString("/")}")
            val mc = Minecraft.getInstance()
            val player = mc.player
            val world = mc.world
            when(route[0]) {
                "gui" -> when(route[1]) {
                    "modules" -> {
                        val boatID = route[2].toInt()
                        val moduleIndex = route[3].toInt()
                        val boat = world.getEntityByID(boatID) as? ModularBoatEntity
                        boat?.let {
                            val module =
                            if(moduleIndex !in 0 until it.modules.size) {
                                it.findFirstModuleToShowOnGui()
                            } else {
                                it.modules[moduleIndex]
                            }
                            return module.createGui(player as EntityPlayerSP, it)
                        } ?: return null
                    }

                    "mapping_table" -> {
                        val x = route[2].toInt()
                        val y = route[3].toInt()
                        val z = route[4].toInt()
                        return BlockPos.PooledMutableBlockPos.retain(x, y, z).use {
                            val te = world.getTileEntity(it) as? TileEntityMappingTable ?: throw IllegalArgumentException("No Mapping Table at location!")
                            return@use GuiMappingTable(te, player.inventory)
                        }
                    }

                    "energy" -> {
                        val x = route[2].toInt()
                        val y = route[3].toInt()
                        val z = route[4].toInt()
                        return BlockPos.PooledMutableBlockPos.retain(x, y, z).use {
                            val te = world.getTileEntity(it) as? TileEntityEnergy ?: throw IllegalArgumentException("No Mapping Table at location!")
                            return@use GuiEnergy(te, player)
                        }
                    }
                }
            }
        }
        return null
    }

    class ModulesGuiInteraction(val boatID: Int, val moduleIndex: Int): IInteractionObject {
        override fun hasCustomName(): Boolean {
            return false // TODO
        }

        override fun getCustomName(): ITextComponent? {
            return null
        }

        override fun getName(): ITextComponent {
            return TextComponentTranslation("inventory.base.name")
        }

        override fun getGuiID(): String {
            return "${MoarBoats.ModID}:gui/modules/$boatID/$moduleIndex"
        }

        override fun createContainer(playerInventory: InventoryPlayer, player: EntityPlayer): Container? {
            val world = player.world
            val boat = world.getEntityByID(boatID) as? ModularBoatEntity ?: return null
            // y below 0 means that the menu should display the most interesting module first (generally engine > storage > navigation > misc.)
            val module = if(moduleIndex < 0) boat.findFirstModuleToShowOnGui() else boat.modules[moduleIndex]
            return module.createContainer(player, boat)
        }

    }

    open class TileEntityInteraction<TE: TileEntity>(val identifier: String, val x: Int, val y: Int, val z: Int, val containerGenerator: (TileEntity?, InventoryPlayer, EntityPlayer) -> Container? = { _0, _1, _2 -> null }): IInteractionObject {
        override fun hasCustomName(): Boolean {
            return false
        }

        override fun getCustomName(): ITextComponent? {
            return null
        }

        override fun getName(): ITextComponent {
            return TextComponentTranslation("${MoarBoats.ModID}.inventory.$identifier.name")
        }

        override fun getGuiID(): String {
            return "${MoarBoats.ModID}:gui/$identifier/$x/$y/$z"
        }

        override fun createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container? {
            return BlockPos.PooledMutableBlockPos.retain(x, y, z).use {
                val te = playerIn.world.getTileEntity(it) as? TE
                if(te != null) {
                    containerGenerator(te, playerInventory, playerIn)
                } else {
                    null
                }
            }
        }

    }

    class EnergyGuiInteraction(x: Int, y: Int, z: Int): TileEntityInteraction<TileEntityEnergy>("energy", x, y, z, { te, inv, player -> EmptyContainer(inv) })
    class MappingTableGuiInteraction(x: Int, y: Int, z: Int): TileEntityInteraction<TileEntityMappingTable>("mapping_table", x, y, z, { te, inv, player -> ContainerMappingTable(te as TileEntityMappingTable, inv) })

    override fun getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? {
        return when(ID) {
            PathEditor -> {
                val boatID = x
                val boat = world.getEntityByID(boatID) as? ModularBoatEntity ?: return null
                if(HelmModule in boat.modules) {

                } else {
                    null // NO HELM
                }
            }

           /* FluidGui -> {
                val pos = BlockPos.PooledMutableBlockPos.retain(x, y, z)
                val te = world.getTileEntity(pos)
                pos.close()
                when {
                    te == null -> null
                    te is TileEntityListenable && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).isPresent ->
                        GuiFluid(te, te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).orElseThrow(::NullPointerException), player)
                    else -> null
                }
            }*/

            WaypointEditor -> {
                val pos = BlockPos.PooledMutableBlockPos.retain(x, y, z)
                val te = world.getTileEntity(pos)
                pos.close()
                when (te) {
                    null -> null
                    is TileEntityMappingTable -> {
                        val stack = te.inventory.getStackInSlot(0)
                        val index = (Minecraft.getInstance().currentScreen as? GuiMappingTable)?.selectedIndex ?: 0
                        val pathNBT = (stack.item as org.jglrxavpok.moarboats.common.items.ItemPath).getWaypointData(stack, MoarBoats.getLocalMapStorage())
                        if(index !in 0 until pathNBT.size) {
                            null
                        } else {
                            GuiWaypointEditor(player, te, index)
                        }
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? {
        return when(ID) {

           /* FluidGui -> {
                val pos = BlockPos.PooledMutableBlockPos.retain(x, y, z)
                val te = world.getTileEntity(pos)
                pos.close()
                when {
                    te == null -> null
                    te is TileEntityListenable && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).isPresent ->
                        FluidContainer(te, te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).orElseThrow(::NullPointerException), player)
                    else -> null
                }
            }*/

            else -> null
        }
    }

    val ModulesGui: Int = 0
    val PathEditor: Int = 1
    val EnergyGui: Int = 2
    val FluidGui: Int = 3
    val MappingTableGui: Int = 4
    val WaypointEditor: Int = 5
}