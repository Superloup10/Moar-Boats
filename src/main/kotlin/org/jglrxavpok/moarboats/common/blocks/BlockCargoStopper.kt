package org.jglrxavpok.moarboats.common.blocks

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer
import net.minecraft.tags.FluidTags
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.IWorldReaderBase
import net.minecraft.world.World
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.entities.BasicBoatEntity
import org.jglrxavpok.moarboats.common.items.CargoStopperItem
import java.util.*

object BlockCargoStopper: RedstoneDiodeBlock(Block.Properties.create(Material.CIRCUITS).tickRandomly().hardnessAndResistance(0f).sound(SoundType.WOOD)) {
    init {
        registryName = ResourceLocation(MoarBoats.ModID, "cargo_stopper")
        this.registerDefaultState(stateDefinition.any().setValue(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).setValue(POWERED, false))
    }

    override fun ticksRandomly(state: BlockState) = true

    override fun canConnectRedstone(state: BlockState?, world: IBlockReader?, pos: BlockPos?, side: Direction?): Boolean {
        return side != null && side != Direction.DOWN && side != Direction.UP
    }

    override fun getCollisionShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun isValidPosition(state: BlockState, worldIn: IWorldReaderBase, pos: BlockPos): Boolean {
        return worldIn.getFluidState(pos.down()).isTagged(FluidTags.WATER)
    }

    override fun getDelay(state: BlockState?): Int {
        return 0
    }

    override fun getWeakPower(state: BlockState, blockAccess: IBlockReader, pos: BlockPos, side: Direction): Int {
        if(blockAccess is World) {
            val world = blockAccess
            val aabb = AxisAlignedBB(pos.offset(state.getValue(HorizontalBlock.FACING)))
            val entities = world.getEntitiesWithinAABB(BasicBoatEntity::class.java, aabb) { e -> e != null && e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent }
            val first = entities.firstOrNull()
            return first?.let {
                it.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map { capa -> calcRedstoneFromInventory(capa) }.orElse(0)
            } ?: 0
        }
        return 0
    }

    override fun tick(state: BlockState, worldIn: World, pos: BlockPos, random: Random) {
        val produceSignal = shouldBePowered(worldIn, pos, state)
        when {
            produceSignal && !state[POWERED] -> worldIn.setBlock(pos, state.with(POWERED, true).with(HorizontalBlock.FACING, state.getValue(HorizontalBlock.FACING)))
            !produceSignal && state[POWERED] -> worldIn.setBlock(pos, state.with(POWERED, false).with(HorizontalBlock.FACING, state.getValue(HorizontalBlock.FACING)))
        }
        worldIn.pendingBlockTicks.scheduleTick(pos, this, 2)
        notifyNeighbors(worldIn, pos, state)
    }

    private fun calcRedstoneFromInventory(inv: IItemHandler?): Int {
        if (inv == null) {
            return 0
        } else {
            var i = 0
            var f = 0.0f

            for (slotIndex in 0 until inv.slots) {
                val itemstack = inv.getStackInSlot(slotIndex)

                if (!itemstack.isEmpty) {
                    f += itemstack.count.toFloat() / Math.min(inv.getSlotLimit(slotIndex), itemstack.maxStackSize).toFloat()
                    ++i
                }
            }

            f /= inv.slots.toFloat()
            return MathHelper.floor(f * 14.0f) + if (i > 0) 1 else 0
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    override fun isFullCube(state: BlockState): Boolean {
        return false
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(HorizontalBlock.FACING, POWERED)
    }

    override fun onReplaced(state: BlockState, worldIn: World, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        super.onReplaced(state, worldIn, pos, newState, isMoving)
        this.notifyNeighbors(worldIn, pos, state)
    }

    /**
     * Called by BlockItems after a block is set in the world, to allow post-place logic
     */
    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
        //worldIn.pendingBlockTicks.scheduleTick(pos, this, 2)
        this.notifyNeighbors(worldIn, pos, state)
    }

    override fun shouldBePowered(worldIn: World, pos: BlockPos, state: BlockState): Boolean {
        return getWeakPower(state, worldIn, pos, state.getValue(HorizontalBlock.FACING)) > 0
    }

    override fun getItemDropped(state: BlockState, worldIn: World, pos: BlockPos, fortune: Int) = CargoStopperItem

    override fun getItem(worldIn: IBlockReader, pos: BlockPos, state: BlockState) = ItemStack(CargoStopperItem, 1)

    override fun getWeakChanges(state: BlockState?, world: IWorldReader?, pos: BlockPos?): Boolean {
        return true
    }

}