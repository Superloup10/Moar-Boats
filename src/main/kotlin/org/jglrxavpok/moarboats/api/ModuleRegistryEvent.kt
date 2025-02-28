package org.jglrxavpok.moarboats.api

import net.minecraftforge.fml.common.eventhandler.Event

public class ModuleRegistryEvent(val entry: BoatModuleEntry): Event() {

    val module = entry.module
    val item = entry.correspondingItem

    override fun isCancelable(): Boolean {
        return true
    }
}