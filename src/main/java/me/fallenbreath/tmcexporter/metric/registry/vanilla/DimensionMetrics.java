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

package me.fallenbreath.tmcexporter.metric.registry.vanilla;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import me.fallenbreath.tmcexporter.metric.registry.MetricRegistry;

public class DimensionMetrics
{
	public static final Gauge BLOCK_EVENT_COUNT = MetricRegistry.gauge("dimension_block_event_count", "", new String[]{"id"});
	public static final Counter BLOCK_EVENT_TICKED = MetricRegistry.counter("dimension_block_event_ticked", "", new String[]{"id"});
	public static final Counter BLOCK_EVENT_ADDED = MetricRegistry.counter("dimension_block_event_added", "", new String[]{"id"});
	public static final Counter BLOCK_EVENT_REMOVED = MetricRegistry.counter("dimension_block_event_removed", "", new String[]{"id"});

	public static final Gauge CHUNK_COUNT = MetricRegistry.gauge("dimension_chunk_count", "", new String[]{"status"});
	public static final Counter CHUNK_ADDED = MetricRegistry.counter("dimension_chunk_added", "", null);
	public static final Counter CHUNK_REMOVED = MetricRegistry.counter("dimension_chunk_removed", "", null);

	public static final Gauge ENTITY_COUNT = MetricRegistry.gauge("dimension_entity_count", "", new String[]{"id", "ticking"});
	public static final Counter ENTITY_ADDED = MetricRegistry.counter("dimension_entity_added", "", new String[]{"id"});
	public static final Counter ENTITY_REMOVED = MetricRegistry.counter("dimension_entity_removed", "", new String[]{"id"});

	public static final Gauge TILE_ENTITY_COUNT = MetricRegistry.gauge("dimension_tile_entity_count", "", new String[]{"id", "ticking"});
	public static final Counter TILE_ENTITY_ADDED = MetricRegistry.counter("dimension_tile_entity_added", "", new String[]{"id"});
	public static final Counter TILE_ENTITY_REMOVED = MetricRegistry.counter("dimension_tile_entity_removed", "", new String[]{"id"});

	public static final Gauge TILE_TICK_COUNT = MetricRegistry.gauge("dimension_tile_tick_count", "", new String[]{"id", "type"});
	public static final Counter TILE_TICK_SCHEDULED = MetricRegistry.counter("dimension_tile_tick_scheduled", "", new String[]{"id", "type"});
	public static final Counter TILE_TICK_TICKED = MetricRegistry.counter("dimension_tile_tick_ticked", "", new String[]{"id", "type"});
}
