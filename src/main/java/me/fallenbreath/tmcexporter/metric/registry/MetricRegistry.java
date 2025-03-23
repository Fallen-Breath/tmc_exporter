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
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MetricRegistry
{
	public static final PrometheusRegistry REGISTRY = new PrometheusRegistry();

	private static final String NAMESPACE = "tmcexporter";

	private static String createName(String name)
	{
		return NAMESPACE + "_" + name;
	}

	public static Counter counter(String name, String help, @Nullable String[] labels)
	{
		Counter.Builder builder = Counter.builder().name(createName(name)).help(help);
		if (labels != null && labels.length > 0)
		{
			builder.labelNames(labels);
		}
		return builder.register(REGISTRY);
	}

	public static Gauge gauge(String name, String help, @Nullable String[] labels)
	{
		Gauge.Builder builder = Gauge.builder().name(createName(name)).help(help);
		if (labels != null && labels.length > 0)
		{
			builder.labelNames(labels);
		}
		return builder.register(REGISTRY);
	}

	public static Histogram histogram(String name, String help, Consumer<Histogram.Builder> builderConsumer)
	{
		Histogram.Builder builder = Histogram.builder().name(createName(name)).help(help);
		builderConsumer.accept(builder);
		return builder.register(REGISTRY);
	}

	public static Histogram histogram(String name, String help)
	{
		return histogram(name, help, builder -> {});
	}
}
