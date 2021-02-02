package com.mairwunnx.projectessentials.core.impl.commands

import com.mairwunnx.projectessentials.core.api.v1.MESSAGE_CORE_PREFIX
import com.mairwunnx.projectessentials.core.api.v1.commands.CommandBase
import com.mairwunnx.projectessentials.core.impl.vanilla.commands.TeleportCommand
import com.mairwunnx.projectessentials.core.api.v1.commands.back.BackLocationAPI
import com.mairwunnx.projectessentials.core.api.v1.extensions.getPlayer
import com.mairwunnx.projectessentials.core.api.v1.extensions.isPlayerSender
import com.mairwunnx.projectessentials.core.api.v1.messaging.MessagingAPI
import com.mairwunnx.projectessentials.core.api.v1.messaging.ServerMessagingAPI
import com.mairwunnx.projectessentials.core.api.v1.permissions.hasPermission
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import net.minecraft.world.server.TicketType
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.command.CommandSource
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.BlockPos

internal object BackLocationCommand : CommandBase(literal("back")) {
    override val name = "back"

    override fun process(context: CommandContext<CommandSource>): Int {
        if (context.isPlayerSender()) {
            val player = context.getPlayer()!!

            if (
                hasPermission(player, "ess.teleport.back", 2) ||
                hasPermission(player, "ess.back", 2)
            ) {
                val data = BackLocationAPI.take(player)

                if (data != null) {
                    val pos = data.position
                    val rot = data.rotation
                    val chunkpos = ChunkPos(BlockPos(pos.xPos, pos.yPos, pos.zPos))
                    data.world.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId())
                    player.stopRiding()
                    if (player.isSleeping()) {
                        player.stopSleepInBed(true, true)
                    }
                    if (data.world != player.serverWorld) {
                        TeleportCommand.changeDimension(player, data.world)
                    }
                    player.connection.setPlayerLocation(pos.xPos, pos.yPos, pos.zPos, rot.yaw, rot.pitch)
                    BackLocationAPI.revoke(player)
                    MessagingAPI.sendMessage(player, "$MESSAGE_CORE_PREFIX.back.success")
                } else {
                    MessagingAPI.sendMessage(player, "$MESSAGE_CORE_PREFIX.back.tickets_not_exists")
                }
                super.process(context)
            } else {
                MessagingAPI.sendMessage(player, "$MESSAGE_CORE_PREFIX.back.restricted")
            }
        } else {
            ServerMessagingAPI.throwOnlyPlayerCan()
        }
        return 0
    }
}
