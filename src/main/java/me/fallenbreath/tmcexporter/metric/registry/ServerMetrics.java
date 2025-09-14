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

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;

public class ServerMetrics
{
	public static final Gauge SERVER_INFO = MetricRegistry.gauge("server_information", "", new String[]{"world_name"});
	public static final Gauge SERVER_UPTIME = MetricRegistry.gauge("server_uptime", "", null);
	public static final Counter SERVER_TICK_COUNT = MetricRegistry.counter("server_tick_count", "", null);

	public static final Counter GAME_TICK_COUNT = MetricRegistry.counter("game_tick_count", "", null);
	public static final Gauge GAME_GAME_TIME = MetricRegistry.gauge("game_game_time", "", null);
	public static final Gauge GAME_DAY_TIME = MetricRegistry.gauge("game_day_time", "", null);

	public static final Gauge PLAYER_COUNT = MetricRegistry.gauge("player_count", "", null);
	public static final Gauge PLAYER_MAX = MetricRegistry.gauge("player_max", "", null);

	public static final Gauge JVM_INFO = MetricRegistry.gauge("jvm_information", "", new String[]{"name", "version"});
	public static final Gauge JVM_MEMORY_FREE = MetricRegistry.gauge("jvm_memory_free", "", null);
	public static final Gauge JVM_MEMORY_ALLOCATED = MetricRegistry.gauge("jvm_memory_allocated", "", null);
	public static final Gauge JVM_MEMORY_MAX = MetricRegistry.gauge("jvm_memory_max", "", null);
	public static final Gauge JVM_MEMORY_NON_HEAP = MetricRegistry.gauge("jvm_memory_non_heap", "", null);

	public static final Gauge EXPORTER_INFO = MetricRegistry.gauge("exporter_information", "", new String[]{"name", "version"});
}
