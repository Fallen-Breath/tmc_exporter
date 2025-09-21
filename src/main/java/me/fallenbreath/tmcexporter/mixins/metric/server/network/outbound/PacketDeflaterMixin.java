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
import net.minecraft.network.handler.PacketDeflater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(PacketDeflater.class)
public abstract class PacketDeflaterMixin
{
	@ModifyVariable(
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lio/netty/buffer/ByteBuf;)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/network/handler/PacketDeflater;compressionThreshold:I",
					ordinal = 0
			)
	)
	private int network_recordOutboundRawSize(int uncompressedSize, @Local(argsOnly = true) ChannelHandlerContext ctx)
	{
		Optional.ofNullable(ctx.channel().attr(NetworkStats.ATTR_PACKET_INFO_OUTBOUND).get()).ifPresent(info -> {
			info.packetRawSize = uncompressedSize;
		});
		return uncompressedSize;
	}
}
