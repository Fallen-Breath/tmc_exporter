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

package me.fallenbreath.tmcexporter.metric.exporter;

import me.fallenbreath.tmcexporter.metric.collect.stats.server.NetworkStats;
import me.fallenbreath.tmcexporter.metric.registry.NetworkMetrics;

public class NetworkMetricExporter
{
	public static void recordPacketInfo(String direction, NetworkStats.PacketInfo info)
	{
		NetworkMetrics.PACKET_COUNT.labelValues(direction, String.valueOf(info.packetId), info.packetName).inc();
		NetworkMetrics.PACKET_RAW_SIZE.labelValues(direction, String.valueOf(info.packetId), info.packetName).observe(info.packetRawSize + info.headerSize);
		NetworkMetrics.PACKET_TRANSFER_SIZE.labelValues(direction, String.valueOf(info.packetId), info.packetName).observe(info.packetSize + info.headerSize);
	}
}
