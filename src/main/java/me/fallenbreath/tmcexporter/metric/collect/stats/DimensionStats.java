/*
 * This file is part of the TMC Exporter project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
 *
 * TechMetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TechMetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TechMetrics.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tmcexporter.metric.collect.stats;

import me.fallenbreath.tmcexporter.metric.collect.TimeCostRecorder;
import me.fallenbreath.tmcexporter.metric.collect.stats.dimension.*;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;
import me.fallenbreath.tmcexporter.metric.common.TileTickKey;
import net.minecraft.util.Identifier;

public class DimensionStats
{
	public final TimeCostRecorder<GamePhase> phaseCosts = new TimeCostRecorder<>();

	public final StatsMap<TileTickKey, TileTickStats> tileTick = new StatsMap<>(TileTickStats::new);
	public final StatsMap<Identifier, BlockEventStats> blockEvent = new StatsMap<>(BlockEventStats::new);
	public final StatsMap<Identifier, EntityStats> entity = new StatsMap<>(EntityStats::new);
	public final StatsMap<Identifier, TileEntityStats> tileEntity = new StatsMap<>(TileEntityStats::new);

	public final ChunkStats chunk = new ChunkStats();
}
