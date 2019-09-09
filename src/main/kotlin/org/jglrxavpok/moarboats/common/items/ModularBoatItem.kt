package org.jglrxavpok.moarboats.common.items

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.DyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.*
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceFluidMode
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.jglrxavpok.moarboats.MoarBoats
import org.jglrxavpok.moarboats.common.entities.AnimalBoatEntity
import org.jglrxavpok.moarboats.common.entities.BasicBoatEntity
import org.jglrxavpok.moarboats.common.entities.ModularBoatEntity
import org.jglrxavpok.moarboats.extensions.Fluids

class ModularBoatItem(val dyeColor: DyeColor): BaseBoatItem() {

    companion object {
        val White = ModularBoatItem(DyeColor.WHITE)
        val Orange = ModularBoatItem(DyeColor.ORANGE)
        val Magenta = ModularBoatItem(DyeColor.MAGENTA)
        val LightBlue = ModularBoatItem(DyeColor.LIGHT_BLUE)
        val Yellow = ModularBoatItem(DyeColor.YELLOW)
        val Lime = ModularBoatItem(DyeColor.LIME)
        val Pink = ModularBoatItem(DyeColor.PINK)
        val Gray = ModularBoatItem(DyeColor.GRAY)
        val LightGray = ModularBoatItem(DyeColor.LIGHT_GRAY)
        val Cyan = ModularBoatItem(DyeColor.CYAN)
        val Purple = ModularBoatItem(DyeColor.PURPLE)
        val Blue = ModularBoatItem(DyeColor.BLUE)
        val Brown = ModularBoatItem(DyeColor.BROWN)
        val Green = ModularBoatItem(DyeColor.GREEN)
        val Red = ModularBoatItem(DyeColor.RED)
        val Black = ModularBoatItem(DyeColor.BLACK)
        // in same order as DyeColor
        val AllVersions = arrayOf(
                White,
                Orange,
                Magenta,
                LightBlue,
                Yellow,
                Lime,
                Pink,
                Gray,
                LightGray,
                Cyan,
                Purple,
                Blue,
                Brown,
                Green,
                Red,
                Black
        )

        operator fun get(color: DyeColor): ModularBoatItem {
            return AllVersions[color.ordinal]
        }
    }

    init {
        registryName = ResourceLocation(MoarBoats.ModID, "modular_boat_${dyeColor.translationKey}")
    }

    override fun createBoat(levelIn: World, raytraceresult: RayTraceResult, inUsualFluid: Boolean, itemstack: ItemStack, playerIn: PlayerEntity): BasicBoatEntity {
        val color = dyeColor
        return ModularBoatEntity(
                levelIn,
                raytraceresult.hitVec.x,
                if (inUsualFluid) raytraceresult.hitVec.y - 0.12 else raytraceresult.hitVec.y,
                raytraceresult.hitVec.z,
                color,
                ModularBoatEntity.OwningMode.PlayerOwned,
                playerIn.gameProfile.id).apply {
                    readAdditional(itemstack.getOrCreateChildTag("boat_data"))
                }
    }

}

object AnimalBoatItem: BaseBoatItem() {

    init {
        registryName = ResourceLocation(MoarBoats.ModID, "animal_boat")
    }

    override fun createBoat(levelIn: World, raytraceresult: RayTraceResult, inUsualFluid: Boolean, itemstack: ItemStack, playerIn: PlayerEntity): BasicBoatEntity {
        return AnimalBoatEntity(levelIn, raytraceresult.hitVec.x, if (inUsualFluid) raytraceresult.hitVec.y - 0.12 else raytraceresult.hitVec.y, raytraceresult.hitVec.z)
    }
}

abstract class BaseBoatItem: Item(Item.Properties().tab(MoarBoats.CreativeTab).maxStackSize(1)) {

    override fun onItemRightClick(levelIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        val itemstack = playerIn.getItemInHand(handIn)
        val f1 = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * 1.0f
        val f2 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * 1.0f
        val d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * 1.0
        val d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * 1.0 + playerIn.getEyeHeight().toDouble()
        val d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * 1.0
        val vec3d = Vec3d(d0, d1, d2)
        val f3 = MathHelper.cos(-f2 * 0.017453292f - Math.PI.toFloat())
        val f4 = MathHelper.sin(-f2 * 0.017453292f - Math.PI.toFloat())
        val f5 = -MathHelper.cos(-f1 * 0.017453292f)
        val f6 = MathHelper.sin(-f1 * 0.017453292f)
        val f7 = f4 * f5
        val f8 = f3 * f5
        val vec3d1 = vec3d.add(f7.toDouble() * 5.0, f6.toDouble() * 5.0, f8.toDouble() * 5.0)
        val raytraceresult = levelIn.rayTraceBlocks(vec3d, vec3d1, RayTraceFluidMode.ALWAYS)

        if (raytraceresult == null) {
            return ActionResult(ActionResultType.PASS, itemstack)
        } else {
            val vec3d2 = playerIn.getLook(1.0f)
            val list = levelIn.getEntitiesWithinAABBExcludingEntity(playerIn, playerIn.boundingBox.expand(vec3d2.x * 5.0, vec3d2.y * 5.0, vec3d2.z * 5.0).grow(1.0))

            val flag = list.indices
                    .map { list[it] }
                    .filter { it.canBeCollidedWith() }
                    .map { it.boundingBox.grow(it.collisionBorderSize.toDouble()) }
                    .any { it.contains(vec3d) }

            if (flag) {
                return ActionResult(ActionResultType.PASS, itemstack)
            } else if (raytraceresult.type != RayTraceResult.Type.BLOCK) {
                return ActionResult(ActionResultType.PASS, itemstack)
            } else {
                val inUsualFluid = Fluids.isUsualLiquidBlock(levelIn, raytraceresult.blockPos)
                val entityboat = createBoat(levelIn, raytraceresult, inUsualFluid, itemstack, playerIn)
                entityboat.rotationYaw = playerIn.rotationYaw

                return if (levelIn.getCollisionBoxes(entityboat, entityboat.boundingBox.grow(-0.1), 0.0, 0.0, 0.0).count() != 0L) {
                    ActionResult(ActionResultType.FAIL, itemstack)
                } else {
                    if (!levelIn.isClientSide) {
                        levelIn.spawnEntity(entityboat)
                    }

                    if (!playerIn.isCreative) {
                        itemstack.shrink(1)
                    }

                    playerIn.addStat(StatList.ITEM_USED[this])
                    ActionResult(ActionResultType.SUCCESS, itemstack)
                }
            }
        }
    }

    abstract fun createBoat(levelIn: World, raytraceresult: RayTraceResult, inUsualFluid: Boolean, itemstack: ItemStack, playerIn: PlayerEntity): BasicBoatEntity
}