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

package me.fallenbreath.tmcexporter.metric.collect.stats.server;

import me.fallenbreath.tmcexporter.metric.collect.TimeCostRecorder;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;

public class ServerStats
{
	// tick counters
	public int tickedServerTick;
	public int tickedGameTick;

	// basic
	public int tickCounter;
	public long uptimeNs;
	public long gameTime;
	public long dayTime;

	// tick costs
	public long tickStartNs;
	public long tickEndNs;
	public long tickCostNs;
	public final TimeCostRecorder<GamePhase> phaseCosts = new TimeCostRecorder<>();

	// players
	public long playerCount;
	public long playerMax;

	// jvm status
	public String jvmName;
	public String jvmVersion;
	public long jvmMemoryFree;
	public long jvmMemoryAllocated;
	public long jvmMemoryMax;
	public long jvmMemoryNonHeap;
}
