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

package me.fallenbreath.tmcexporter.metric.collect;

import com.google.common.collect.Lists;
import me.fallenbreath.tmcexporter.metric.collect.stats.DimensionStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.PerTickStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.StaticStats;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.ServerStats;
import me.fallenbreath.tmcexporter.metric.common.ChunkStatus;
import me.fallenbreath.tmcexporter.metric.common.Dimension;
import me.fallenbreath.tmcexporter.metric.common.TileTickKey;
import me.fallenbreath.tmcexporter.metric.common.TileTickType;
import me.fallenbreath.tmcexporter.metric.exporter.MetricExporter;
import me.fallenbreath.tmcexporter.mixins.access.ThreadedAnvilChunkStorageAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.blockentity.ChunkAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.blockentity.WorldAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.entity.ServerEntityManagerAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.entity.ServerWorldAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.tiletick.ChunkTickSchedulerAccessor;
import me.fallenbreath.tmcexporter.mixins.metric.dimension.tiletick.WorldTickSchedulerAccessor;
import me.fallenbreath.tmcexporter.utils.BlockEntityUtils;
import me.fallenbreath.tmcexporter.utils.IdentifierUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.entity.EntityTrackingStatus;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.Nullable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class MetricCollector
{
	private static final MetricCollector INSTANCE = new MetricCollector();

	// lifecycle: whole server
	private MinecraftServer server = null;
	private StaticStats staticStats = null;

	// lifecycle: per tick
	private PerTickStats perTickStats = null;

	public static MetricCollector getInstance()
	{
		return INSTANCE;
	}

	public boolean isInitialized()
	{
		return this.server != null;
	}

	public void init(MinecraftServer server)
	{
		this.server = server;
		this.staticStats = new StaticStats(server);
		this.perTickStats = new PerTickStats(this.server);
	}

	public void tickStart()
	{
		if (!this.isInitialized())
		{
			return;
		}

		this.perTickStats.server.tickStartNs = System.nanoTime();
	}

	public void tickEnd()
	{
		if (!this.isInitialized())
		{
			return;
		}

		this.perTickStats.server.tickEndNs = System.nanoTime();
		this.fillThisTickStats();
		this.exportAndReset();
	}

	private void exportAndReset()
	{
		MetricExporter exporter = new MetricExporter(this.server);
		exporter.export(this.staticStats, this.perTickStats);
		this.perTickStats = new PerTickStats(this.server);
	}

	private void fillThisTickStats()
	{
		this.fillServerStats(this.perTickStats.server);
		for (ServerWorld world : this.server.getWorlds())
		{
			this.fillDimensionStats(world, Objects.requireNonNull(this.perTickStats.dimensions.get(Dimension.of(world))));
		}
	}

	private void fillServerStats(ServerStats stats)
	{
		ServerWorld overworld = this.server.getWorld(World.OVERWORLD);

		stats.tickCounter = this.server.getTicks();
		stats.uptimeNs = System.nanoTime() - this.staticStats.startupTimeStampNs;
		stats.gameTime = overworld != null ? overworld.getTime() : 0;
		stats.dayTime = overworld != null ? overworld.getTimeOfDay() : 0;
		stats.tickCostNs = stats.tickEndNs - stats.tickStartNs;
		stats.playerCount = this.server.getCurrentPlayerCount();
		stats.playerMax = this.server.getMaxPlayerCount();

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
		stats.jvmName = Optional.ofNullable(System.getProperty("java.runtime.name")).orElse("");
		stats.jvmVersion = Optional.ofNullable(System.getProperty("java.runtime.version")).orElse(Optional.ofNullable(System.getProperty("java.version")).orElse(""));
		stats.jvmMemoryFree = Runtime.getRuntime().freeMemory();
		stats.jvmMemoryAllocated = Runtime.getRuntime().totalMemory();
		stats.jvmMemoryMax = Runtime.getRuntime().maxMemory();
		stats.jvmMemoryNonHeap = nonHeapMemoryUsage.getUsed();
	}

	private void fillDimensionStats(ServerWorld world, DimensionStats stats)
	{
		ServerChunkLoadingManager tacs = world.getChunkManager().chunkLoadingManager;
		List<WorldChunk> loadedChunks = Lists.newArrayList();
		for (ChunkHolder chunkHolder : ((ThreadedAnvilChunkStorageAccessor)tacs).getCurrentChunkHolders().values())
		{
			WorldChunk chunk = chunkHolder.getWorldChunk();
			if (chunk != null)
			{
				loadedChunks.add(chunk);
			}

			ChunkStatus status = ChunkStatus.fromLevel(chunkHolder.getLevel());
			stats.chunk.loaded.put(status, stats.chunk.loaded.getOrDefault(status, 0) + 1);
		}

		for (TileTickType tileTickType : TileTickType.values())
		{
			for (ChunkTickScheduler<?> cts : ((WorldTickSchedulerAccessor<?>)tileTickType.getSchedulerFromWorld(world)).getChunkTickSchedulers().values())
			{
				ChunkTickSchedulerAccessor<?> ctsAccess = (ChunkTickSchedulerAccessor<?>)cts;
				List<? extends Tick<?>> ticks = ctsAccess.getTicks();
				Queue<? extends OrderedTick<?>> tickQueue = ctsAccess.getTickQueue();
				if (ticks != null)
				{
					for (Tick<?> t : ticks)
					{
						stats.tileTick.access(TileTickKey.ofTileTickObject(t.type()), tts -> tts.amount++);
					}
				}
				if (tickQueue != null)
				{
					for (OrderedTick<?> t : tickQueue)
					{
						stats.tileTick.access(TileTickKey.ofTileTickObject(t.type()), tts -> tts.amount++);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		SectionedEntityCache<Entity> entityCache = ((ServerEntityManagerAccessor<Entity>)((ServerWorldAccessor)world).getEntityManager()).getCache();
		for (Long chunkPosLong : entityCache.getChunkPositions())
		{
			entityCache.getTrackingSections(chunkPosLong).forEach(section -> {
				section.stream().forEach(entity -> {
					Identifier eId = IdentifierUtils.of(entity.getType());
					stats.entity.access(eId, es -> {
						es.total++;
						if (section.getStatus() == EntityTrackingStatus.TICKING)
						{
							es.ticking++;
						}
					});
				});
			});
		}

		for (WorldChunk chunk : loadedChunks)
		{
			for (BlockEntity blockEntity : ((ChunkAccessor)chunk).getBlockEntities().values())
			{
				stats.tileEntity.access(
						IdentifierUtils.of(blockEntity.getType()),
						tes -> tes.total++
				);
			}
		}
		((WorldAccessor)world).getBlockEntityTickers().forEach(ticker -> {
			BlockEntity blockEntity = BlockEntityUtils.getBlockEntityFromTickInvoker(ticker);
			if (blockEntity != null)
			{
				stats.tileEntity.access(
						IdentifierUtils.of(blockEntity.getType()),
						tes -> tes.ticking++
				);
			}
		});
	}

	public static Optional<ServerStats> getServerStats()
	{
		return getInstance().isInitialized() ? Optional.of(getInstance().perTickStats.server) : Optional.empty();
	}

	public static Optional<DimensionStats> getDimStats(@Nullable World world)
	{
		if (!getInstance().isInitialized() || world == null)
		{
			return Optional.empty();
		}
		// XXX: warn if dimensionStats.get returns null?
		return Optional.ofNullable(getInstance().perTickStats.dimensions.get(Dimension.of(world)));
	}
}
