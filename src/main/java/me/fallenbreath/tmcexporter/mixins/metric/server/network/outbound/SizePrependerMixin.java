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
import io.netty.util.Attribute;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.NetworkStats;
import net.minecraft.network.handler.SizePrepender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SizePrepender.class)
public abstract class SizePrependerMixin
{
	@Inject(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lio/netty/buffer/ByteBuf;)V",
			at = @At(
					value = "INVOKE",
					target = "Lio/netty/buffer/ByteBuf;ensureWritable(I)Lio/netty/buffer/ByteBuf;",
					ordinal = 0,
					remap = false
			),
			remap = false
	)
	private void network_recordTrafficSize(
			CallbackInfo ci,
			@Local(argsOnly = true) ChannelHandlerContext ctx,
			@Local(ordinal = 0) int packetSize,
			@Local(ordinal = 1) int headerSize
	)
	{
		Attribute<NetworkStats.PacketInfo> attr = ctx.channel().attr(NetworkStats.ATTR_PACKET_INFO_OUTBOUND);
		Optional.ofNullable(attr.get()).ifPresent(info -> {
			info.totalSize = packetSize + headerSize;
			info.packetSize = packetSize;
			if (info.packetRawSize == -1)
			{
				info.packetRawSize = packetSize;  // fallback
			}
			NetworkStats.recordOutbound(info);
		});
		attr.set(null);
	}
}
