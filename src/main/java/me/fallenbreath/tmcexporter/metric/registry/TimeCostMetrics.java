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

import io.prometheus.metrics.core.metrics.Histogram;

public class TimeCostMetrics
{
	private static final double[] DETAILED_TICK_COST_UPPER_BOUNDS = new double[]{
			0.002, 0.005, 0.010, 0.015, 0.020, 0.030, 0.040, 0.050,
			0.060, 0.080, 0.100, 0.150, 0.200, 0.300, 0.500,
			1.000, 2.000, 5.000, 10.000,
	};

	public static final Histogram GAME_TICK_COST = MetricRegistry.histogram(
			"game_tick_cost",
			"",
			builder -> builder.classicUpperBounds(DETAILED_TICK_COST_UPPER_BOUNDS).classicOnly()
	);
	public static final Histogram GAME_PHASE_COST = MetricRegistry.histogram(
			"game_phase_cost",
			"",
			builder -> builder.labelNames("dimension", "phase").classicUpperBounds().classicOnly()
	);
}
