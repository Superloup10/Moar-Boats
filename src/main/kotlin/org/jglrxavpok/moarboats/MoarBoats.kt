package org.jglrxavpok.moarboats

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items as MCItems
import net.minecraft.init.Blocks as MCBlocks
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.item.ItemBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.registries.ForgeRegistry
import net.minecraftforge.registries.RegistryBuilder
import org.jglrxavpok.moarboats.api.BoatModuleEntry
import org.jglrxavpok.moarboats.common.*
import org.jglrxavpok.moarboats.common.modules.inventories.ChestModuleInventory
import org.jglrxavpok.moarboats.common.modules.inventories.EngineModuleInventory
import org.jglrxavpok.moarboats.common.modules.inventories.SimpleModuleInventory
import org.jglrxavpok.moarboats.api.BoatModuleRegistry
import org.jglrxavpok.moarboats.api.registerModule
import org.jglrxavpok.moarboats.common.blocks.BlockBoatBattery
import org.jglrxavpok.moarboats.common.blocks.BlockEnergyLoader
import org.jglrxavpok.moarboats.common.blocks.BlockEnergyUnloader
import org.jglrxavpok.moarboats.common.items.*
import org.jglrxavpok.moarboats.common.modules.*
import org.jglrxavpok.moarboats.common.tileentity.TileEntityEnergyLoader
import org.jglrxavpok.moarboats.common.tileentity.TileEntityEnergyUnloader

@Mod.EventBusSubscriber
@Mod(modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter", modid = MoarBoats.ModID, dependencies = "required-after:forgelin;",
        name = "Moar Boats", version = "2.1.0.2", updateJSON = "https://raw.githubusercontent.com/jglrxavpok/Moar-Boats/master/updateCheck.json")
object MoarBoats {
    const val ModID = "moarboats"

    lateinit var logger: Logger

    @SidedProxy(clientSide = "org.jglrxavpok.moarboats.client.Proxy", serverSide = "org.jglrxavpok.moarboats.server.Proxy")
    lateinit var proxy: MoarBoatsProxy

    val network = SimpleNetworkWrapper(ModID)
    lateinit var config: Configuration
        private set

    val CreativeTab = object: CreativeTabs("moarboats") {
        override fun getTabIconItem(): ItemStack {
            return ItemStack(ModularBoatItem)
        }

    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog
        config = Configuration(event.suggestedConfigurationFile)
        MBConfig.backing = config
        MBConfig.loadAll()
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(ItemEventHandler)
        proxy.preInit()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        proxy.init()
        DataSerializers.registerSerializer(ResourceLocationsSerializer)
        DataSerializers.registerSerializer(UniqueIDSerializer)
    }

    @JvmStatic
    @SubscribeEvent
    fun createRegistry(e: RegistryEvent.NewRegistry) {
        BoatModuleRegistry.forgeRegistry = RegistryBuilder<BoatModuleEntry>()
                .allowModification()
                .setName(ResourceLocation(ModID, "module_registry"))
                .setType(BoatModuleEntry::class.java)
                .create()
    }

    @SubscribeEvent
    fun registerModules(event: RegistryEvent.Register<BoatModuleEntry>) {
        event.registry.registerModule(ResourceLocation("moarboats:furnace_engine"), Item.getItemFromBlock(MCBlocks.FURNACE), FurnaceEngineModule, { boat, module -> EngineModuleInventory(boat, module) })
        event.registry.registerModule(ResourceLocation("moarboats:chest"), Item.getItemFromBlock(MCBlocks.CHEST), ChestModule, { boat, module -> ChestModuleInventory(boat, module) })
        event.registry.registerModule(ResourceLocation("moarboats:helm"), HelmItem, HelmModule, { boat, module -> SimpleModuleInventory(1, "helm", boat, module) })
        event.registry.registerModule(ResourceLocation("moarboats:fishing"), MCItems.FISHING_ROD, FishingModule, { boat, module -> SimpleModuleInventory(1, "fishing", boat, module) })
        event.registry.registerModule(SeatModule, SeatItem)
        event.registry.registerModule(AnchorModule, Item.getItemFromBlock(MCBlocks.ANVIL))
        event.registry.registerModule(SolarEngineModule, Item.getItemFromBlock(MCBlocks.DAYLIGHT_DETECTOR))
        event.registry.registerModule(CreativeEngineModule, CreativeEngineItem)
        event.registry.registerModule(IceBreakerModule, IceBreakerItem)
        event.registry.registerModule(SonarModule, Item.getItemFromBlock(MCBlocks.NOTEBLOCK))
        event.registry.registerModule(DispenserModule, Item.getItemFromBlock(MCBlocks.DISPENSER), { boat, module -> SimpleModuleInventory(3*5, "dispenser", boat, module) })
        event.registry.registerModule(DivingModule, DivingBottleItem)
        event.registry.registerModule(RudderModule, RudderItem)
        event.registry.registerModule(DropperModule, Item.getItemFromBlock(MCBlocks.DROPPER), { boat, module -> SimpleModuleInventory(3*5, "dropper", boat, module) })
        event.registry.registerModule(BatteryModule, Item.getItemFromBlock(BlockBoatBattery))
    }

    @SubscribeEvent
    fun registerBlocks(e: RegistryEvent.Register<Block>) {
        e.registry.registerAll(*Blocks.list.toTypedArray())
        GameRegistry.registerTileEntity(TileEntityEnergyUnloader::class.java, BlockEnergyUnloader.registryName)
        GameRegistry.registerTileEntity(TileEntityEnergyLoader::class.java, BlockEnergyLoader.registryName)
    }

    @SubscribeEvent
    fun registerItems(e: RegistryEvent.Register<Item>) {
        e.registry.registerAll(*Items.list.toTypedArray())
        for (block in Blocks.list) {
            e.registry.register(ItemBlock(block).setRegistryName(block.registryName).setUnlocalizedName(block.unlocalizedName))
        }
    }

    @SubscribeEvent
    fun registerEntities(e: RegistryEvent.Register<EntityEntry>) {
        e.registry.registerAll(*EntityEntries.list.toTypedArray())
    }
}