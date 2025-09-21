/*
 * This file is part of the TMC Exporter project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
 *
 * TMC Exporter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TMC Exporter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TMC Exporter.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tmcexporter.mixins.metric.server.network.inbound;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.NetworkStats;
import me.fallenbreath.tmcexporter.utils.NetworkUtils;
import net.minecraft.network.NetworkState;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DecoderHandler.class)
public abstract class DecoderHandlerMixin
{
	@Shadow @Final
	private NetworkState<?> state;

	@Inject(
			method = "decode",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/handler/NetworkStateTransitionHandler;onDecoded(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"
			)
	)
	private void network_recordInboundTrafficId(CallbackInfo ci, @Local(argsOnly = true) ChannelHandlerContext ctx, @Local Packet<?> packet)
	{
		Attribute<NetworkStats.PacketInfo> attr = ctx.channel().attr(NetworkStats.ATTR_PACKET_INFO_INBOUND);
		Optional.ofNullable(attr.get()).ifPresent(info -> {
			info.packetId = NetworkUtils.getPacketId(this.state, packet.getPacketType());
			info.packetName = this.state.id().getId() + "/" + packet.getPacketType().toString();
			NetworkStats.recordInbound(info);
		});
		attr.set(null);
	}
}
