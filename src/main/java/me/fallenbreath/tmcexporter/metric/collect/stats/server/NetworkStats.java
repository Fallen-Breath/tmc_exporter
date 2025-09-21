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

package me.fallenbreath.tmcexporter.metric.collect.stats.server;

import io.netty.util.AttributeKey;
import me.fallenbreath.tmcexporter.metric.exporter.NetworkMetricExporter;

/**
 * Notes: Concurrent access in network threads
 * <p>
 * See {@link me.fallenbreath.tmcexporter.metric.registry.NetworkMetrics}
 */
public class NetworkStats
{
	public static final AttributeKey<PacketInfo> ATTR_PACKET_INFO_INBOUND = AttributeKey.valueOf("me.fallenbreath.tmcexporter/packet_info_inbound");
	public static final AttributeKey<PacketInfo> ATTR_PACKET_INFO_OUTBOUND = AttributeKey.valueOf("me.fallenbreath.tmcexporter/packet_info_outbound");

	public static void recordInbound(PacketInfo info)
	{
		NetworkMetricExporter.recordPacketInfo("inbound", info);
	}

	public static void recordOutbound(PacketInfo info)
	{
		NetworkMetricExporter.recordPacketInfo("outbound", info);
	}

	public static class PacketInfo
	{
		public int totalSize = 0;  // len(varint(bufSize)) + packetBuf
		public int packetSize = 0;  // len(packetBuf)
		public int packetRawSize = -1;  // len(decompress(packetBuf))
		public int packetId;
		public String packetName;
	}
}
