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

import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.fallenbreath.tmcexporter.TmcExporterMod;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.Objects;
import java.util.function.BiConsumer;

public class TimeCostRecorder<K>
{
	public final Object2LongOpenHashMap<K> timeCost = new Object2LongOpenHashMap<>();

	private final Deque<K> currentKeys = Queues.newArrayDeque();
	private long startTimeNs;

	public TimeCostRecorder()
	{
		this.reset();
	}

	public void forEach(BiConsumer<K, Long> consumer)
	{
		this.timeCost.forEach(consumer);
	}

	public void push(K newKey)
	{
		long now = System.nanoTime();
		if (!this.currentKeys.isEmpty() && this.startTimeNs > 0)
		{
			K key = Objects.requireNonNull(this.currentKeys.peekLast());
			this.timeCost.addTo(key, now - this.startTimeNs);
		}
		this.currentKeys.addLast(newKey);
		this.startTimeNs = now;
	}

	public void pop(@Nullable K expectedKey)
	{
		K key = null;
		if (!this.currentKeys.isEmpty() && this.startTimeNs > 0)
		{
			long now = System.nanoTime();
			key = Objects.requireNonNull(this.currentKeys.pollLast());
			this.timeCost.addTo(key, now - this.startTimeNs);
			this.startTimeNs = now;
		}
		if (expectedKey != null && !Objects.equals(key, expectedKey))
		{
			TmcExporterMod.LOGGER.warn("popK assert failed: should be {} but popped {}", expectedKey, key);
		}
	}

	public void pop()
	{
		this.pop(null);
	}

	public void switchTo(K newKey)
	{
		this.pop();
		this.push(newKey);
	}

	public void reset()
	{
		this.currentKeys.clear();
		this.startTimeNs = 0;
	}
}
