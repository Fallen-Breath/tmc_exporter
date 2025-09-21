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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelHandlerContext;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.NetworkStats;
import net.minecraft.network.handler.PacketInflater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PacketInflater.class)
public abstract class PacketInflaterMixin
{
	@ModifyExpressionValue(
			method = "decode",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/encoding/VarInts;read(Lio/netty/buffer/ByteBuf;)I",
					ordinal = 0
			)
	)
	private int network_recordInboundRawSize(int uncompressedSize, @Local(argsOnly = true) ChannelHandlerContext ctx)
	{
		Optional.ofNullable(ctx.channel().attr(NetworkStats.ATTR_PACKET_INFO_INBOUND).get()).ifPresent(info -> {
			info.packetRawSize = uncompressedSize;
		});
		return uncompressedSize;
	}
}
