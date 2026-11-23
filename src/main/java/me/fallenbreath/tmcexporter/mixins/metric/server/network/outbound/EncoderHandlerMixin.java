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

package me.fallenbreath.tmcexporter.mixins.metric.server.network.outbound;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelHandlerContext;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.NetworkStats;
import me.fallenbreath.tmcexporter.utils.NetworkUtils;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public abstract class EncoderHandlerMixin
{
	@Shadow @Final
	private ProtocolInfo<?> protocolInfo;

	@Inject(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
			at = @At(
					value = "INVOKE",
					target = "Lio/netty/buffer/ByteBuf;readableBytes()I",
					remap = false
			)
	)
	private void network_recordOutboundTrafficId(CallbackInfo ci, @Local(argsOnly = true) ChannelHandlerContext ctx, @Local(argsOnly = true) Packet<?> packet)
	{
		NetworkStats.PacketInfo info = new NetworkStats.PacketInfo();
		info.packetId = NetworkUtils.getPacketId(this.protocolInfo, packet.type());
		info.packetName = this.protocolInfo.id().id() + "/" + packet.type().toString();
		ctx.channel().attr(NetworkStats.ATTR_PACKET_INFO_OUTBOUND).set(info);
	}
}
