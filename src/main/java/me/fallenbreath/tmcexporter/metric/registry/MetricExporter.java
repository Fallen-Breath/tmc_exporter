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

package me.fallenbreath.tmcexporter.metric.registry;

import me.fallenbreath.tmcexporter.metric.collect.stats.DimensionStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.PerTickStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.StaticStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.ServerStats;
import me.fallenbreath.tmcexporter.metric.common.Dimension;
import net.minecraft.server.MinecraftServer;

public class MetricExporter
{
	private final MinecraftServer server;

	public MetricExporter(MinecraftServer server)
	{
		this.server = server;
	}

	public void export(StaticStats staticStats, PerTickStats perTickStats)
	{
		this.exportServerStats(perTickStats.server);
		perTickStats.dimensions.forEach(this::exportDimensionStats);
	}

	private void exportServerStats(ServerStats serverStats)
	{
		ServerMetrics.SERVER_TICK_COUNT.inc(serverStats.tickedServerTick);
		ServerMetrics.GAME_TICK_COUNT.inc(serverStats.tickedGameTick);
		ServerMetrics.PLAYER_COUNT.set(serverStats.playerCount);

		TimeCostMetrics.GAME_TICK_COST.observe(serverStats.tickCostNs / 1e9);
		serverStats.phaseCosts.forEach((phase, costNs) -> {
			TimeCostMetrics.GAME_PHASE_COST.labelValues("", phase.toString()).observe(costNs / 1e9);
		});
	}

	private void exportDimensionStats(Dimension dimension, DimensionStats dimensionStats)
	{
		dimensionStats.phaseCosts.forEach((phase, costNs) -> {
			TimeCostMetrics.GAME_PHASE_COST.labelValues(dimension.toString(), phase.toString()).observe(costNs / 1e9);
		});
	}
}
