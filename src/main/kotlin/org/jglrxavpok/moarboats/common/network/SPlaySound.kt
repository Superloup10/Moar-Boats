package org.jglrxavpok.moarboats.common.network

import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side

class SPlaySound(): IMessage {

    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    lateinit var soundEvent: SoundEvent
    lateinit var soundCategory: SoundCategory
    var volume: Float = 0f
    var pitch: Float = 0f

    constructor(x: Double, y: Double, z: Double, soundEvent: SoundEvent, soundCategory: SoundCategory, volume: Float, pitch: Float): this() {
        this.x = x
        this.y = y
        this.z = z
        this.soundEvent = soundEvent
        this.soundCategory = soundCategory
        this.volume = volume
        this.pitch = pitch
    }

    override fun fromBytes(buf: ByteBuf) {
        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()
        volume = buf.readFloat()
        pitch = buf.readFloat()
        val soundEventID = buf.readInt()
        soundEvent = SoundEvent.REGISTRY.getObjectById(soundEventID)!!
        soundCategory = SoundCategory.getByName(ByteBufUtils.readUTF8String(buf))
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeFloat(volume)
        buf.writeFloat(pitch)
        buf.writeInt(SoundEvent.REGISTRY.getIDForObject(soundEvent))
        ByteBufUtils.writeUTF8String(buf, soundCategory.getName())
    }

    object Handler: MBMessageHandler<SPlaySound, IMessage> {
        override val packetClass = SPlaySound::class
        override val receiverSide = Side.CLIENT

        override fun onMessage(message: SPlaySound, ctx: MessageContext): IMessage? {
            Minecraft.getMinecraft().world.playSound(
                    message.x,
                    message.y,
                    message.z,
                    message.soundEvent,
                    message.soundCategory,
                    message.volume,
                    message.pitch,
                    true)
            return null
        }
    }
}