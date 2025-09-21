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

import me.fallenbreath.tmcexporter.TmcExporterMod;
import me.fallenbreath.tmcexporter.metric.collect.TimeCostRecorder;
import me.fallenbreath.tmcexporter.metric.collect.stats.DimensionStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.PerTickStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.StaticStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.dimension.ChunkStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.ServerStats;
import me.fallenbreath.tmcexporter.metric.common.Dimension;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;
import me.fallenbreath.tmcexporter.metric.registry.DimensionMetrics;
import me.fallenbreath.tmcexporter.metric.registry.ServerMetrics;
import me.fallenbreath.tmcexporter.metric.registry.TimeCostMetrics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BiConsumer;

public class MetricExporter
{
	private final MinecraftServer server;

	public MetricExporter(MinecraftServer server)
	{
		this.server = server;
	}

	public void export(StaticStats staticStats, PerTickStats perTickStats)
	{
		this.exportStaticStats(staticStats);
		this.exportServerStats(perTickStats.server);
		perTickStats.dimensions.forEach(this::exportDimensionStats);
	}

	private void exportStaticStats(StaticStats staticStats)
	{
		ServerMetrics.SERVER_INFO.labelValues(staticStats.worldName).set(1);
		ServerMetrics.EXPORTER_INFO.labelValues(TmcExporterMod.MOD_NAME, TmcExporterMod.MOD_VERSION).set(1);
	}

	private static void forEachPhaseCost(TimeCostRecorder<GamePhase> phaseCosts, boolean inWorld, BiConsumer<GamePhase, Long> consumer)
	{
		for (GamePhase gamePhase : GamePhase.ALL)
		{
			if (gamePhase.isInsideWorld() == inWorld)
			{
				consumer.accept(gamePhase, phaseCosts.getCost(gamePhase));
			}
		}
	}

	private void exportServerStats(ServerStats serverStats)
	{
		ServerMetrics.SERVER_UPTIME.set(serverStats.uptimeNs / 1e9);
		ServerMetrics.SERVER_TICK_COUNT.inc(serverStats.tickedServerTick);

		ServerWorld overworld = this.server.getOverworld();
		ServerMetrics.GAME_TICK_COUNT.inc(serverStats.tickedGameTick);
		ServerMetrics.GAME_GAME_TIME.set(overworld.getTime());
		ServerMetrics.GAME_DAY_TIME.set(overworld.getTimeOfDay());

		ServerMetrics.PLAYER_COUNT.set(serverStats.playerCount);
		ServerMetrics.PLAYER_MAX.set(serverStats.playerMax);

		ServerMetrics.JVM_INFO.labelValues(serverStats.jvmName, serverStats.jvmVersion).set(1);
		ServerMetrics.JVM_MEMORY_FREE.set(serverStats.jvmMemoryFree);
		ServerMetrics.JVM_MEMORY_ALLOCATED.set(serverStats.jvmMemoryAllocated);
		ServerMetrics.JVM_MEMORY_MAX.set(serverStats.jvmMemoryMax);
		ServerMetrics.JVM_MEMORY_NON_HEAP.set(serverStats.jvmMemoryNonHeap);

		TimeCostMetrics.GAME_TICK_COST.observe(serverStats.tickCostNs / 1e9);
		forEachPhaseCost(serverStats.phaseCosts, false, (phase, costNs) -> {
			TimeCostMetrics.GAME_PHASE_COST.labelValues("", phase.toString()).observe(costNs / 1e9);
		});
	}

	private void exportDimensionStats(Dimension dimension, DimensionStats dimensionStats)
	{
		forEachPhaseCost(dimensionStats.phaseCosts, true, (phase, costNs) -> {
			TimeCostMetrics.GAME_PHASE_COST.labelValues(dimension.toString(), phase.toString()).observe(costNs / 1e9);
		});
		dimensionStats.blockEvent.forEach((key, stats) -> {
			DimensionMetrics.BLOCK_EVENT_COUNT.labelValues(key.toString()).set(stats.amount);
			DimensionMetrics.BLOCK_EVENT_TICKED.labelValues(key.toString()).inc(stats.ticked);
			DimensionMetrics.BLOCK_EVENT_ADDED.labelValues(key.toString()).inc(stats.added);
			DimensionMetrics.BLOCK_EVENT_REMOVED.labelValues(key.toString()).inc(stats.removed);
		});
		{
			ChunkStats stats = dimensionStats.chunk;
			stats.loaded.forEach((status, amount) -> {
				DimensionMetrics.CHUNK_COUNT.labelValues(status.toString()).set(amount);
			});
			DimensionMetrics.CHUNK_ADDED.labelValues().inc(stats.added);
			DimensionMetrics.CHUNK_REMOVED.labelValues().inc(stats.removed);
		}
		dimensionStats.entity.forEach((key, stats) -> {
			DimensionMetrics.ENTITY_COUNT.labelValues(key.toString(), "0").set(Math.max(0, stats.total - stats.ticking));
			DimensionMetrics.ENTITY_COUNT.labelValues(key.toString(), "1").set(stats.ticking);
			DimensionMetrics.ENTITY_ADDED.labelValues(key.toString()).inc(stats.added);
			DimensionMetrics.ENTITY_REMOVED.labelValues(key.toString()).inc(stats.removed);
		});
		dimensionStats.tileEntity.forEach((key, stats) -> {
			DimensionMetrics.TILE_ENTITY_COUNT.labelValues(key.toString(), "0").set(Math.max(0, stats.total - stats.ticking));
			DimensionMetrics.TILE_ENTITY_COUNT.labelValues(key.toString(), "1").set(stats.ticking);
			DimensionMetrics.TILE_ENTITY_ADDED.labelValues(key.toString()).inc(stats.added);
			DimensionMetrics.TILE_ENTITY_REMOVED.labelValues(key.toString()).inc(stats.removed);
		});
		dimensionStats.tileTick.forEach((key, stats) -> {
			DimensionMetrics.TILE_TICK_COUNT.labelValues(key.id.toString(), key.type.toString()).set(stats.amount);
			DimensionMetrics.TILE_TICK_SCHEDULED.labelValues(key.id.toString(), key.type.toString()).inc(stats.schedule_succeeded);
			DimensionMetrics.TILE_TICK_TICKED.labelValues(key.id.toString(), key.type.toString()).inc(stats.ticked);
		});
	}
}
