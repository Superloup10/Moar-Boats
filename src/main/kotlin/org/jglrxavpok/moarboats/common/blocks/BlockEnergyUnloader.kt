package org.jglrxavpok.moarboats.common.blocks

import net.minecraft.block.Block
import net.minecraft.block.state.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.DirectionProperty
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.chunk.BlockStateContainer
import net.minecraftforge.fml.network.NetworkHooks
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.MoarBoatsGuiHandler
import org.jglrxavpok.moarboats.common.tileentity.TileEntityEnergyUnloader
import java.util.function.Predicate

val Facing: DirectionProperty = DirectionProperty.create("facing") {true}

object BlockEnergyUnloader: MoarBoatsBlock() {

    init {
        registryName = ResourceLocation(MoarBoats.ModID, "boat_energy_discharger")
        defaultBlockState() = stateContainer.baseState.with(Facing, Direction.UP)
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(Facing)
    }

    override fun hasTileEntity() = true
    override fun hasTileEntity(state: BlockState) = true

    override fun createTileEntity(state: BlockState?, world: IBlockReader?): TileEntity? {
        return TileEntityEnergyUnloader()
    }

    override fun getStateForPlacement(context: BlockItemUseContext): BlockState? {
        return this.defaultBlockState().with(Facing, context.nearestLookingDirection)
    }

    override fun onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, playerIn: PlayerEntity, hand: Hand?, facing: Direction?, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        if(worldIn.isClientSide)
            return true
        NetworkHooks.openGui(playerIn as ServerPlayerEntity, MoarBoatsGuiHandler.EnergyGuiInteraction(pos.x, pos.y, pos.z))
        return true
    }

    override fun hasComparatorInputOverride(state: BlockState): Boolean {
        return true
    }

    override fun getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int {
        return (worldIn.getBlockEntity(pos) as? TileEntityEnergyUnloader)?.getRedstonePower() ?: 0
    }

}