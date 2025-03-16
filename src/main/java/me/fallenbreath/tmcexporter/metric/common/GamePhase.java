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

package me.fallenbreath.tmcexporter.metric.common;

import com.google.common.collect.Lists;

import java.util.List;

public class GamePhase
{
	public static final List<GamePhase> ALL = Lists.newArrayList();

	public static final GamePhase UNKNOWN = new GamePhase("unknown", false);

	public static final GamePhase SPAWNING = new GamePhase("spawning", true);
	public static final GamePhase CHUNK_MANAGING = new GamePhase("chunk_managing", true);
	public static final GamePhase CHUNK_TICK = new GamePhase("chunk_tick", true);
	public static final GamePhase TILE_TICK = new GamePhase("tile_tick", true);
	public static final GamePhase RAID = new GamePhase("raid", true);
	public static final GamePhase BLOCK_EVENT = new GamePhase("block_event", true);
	public static final GamePhase ENTITY = new GamePhase("entity", true);
	public static final GamePhase BLOCK_ENTITY = new GamePhase("block_entity", true);

	public static final GamePhase AUTO_SAVE = new GamePhase("auto_save", false);
	public static final GamePhase ASYNC_TASK = new GamePhase("async_task", false);
	public static final GamePhase COMMAND_FUNCTION = new GamePhase("command_function", false);
	public static final GamePhase NETWORK = new GamePhase("network", false);
	public static final GamePhase CONSOLE = new GamePhase("console", false);

	private final String name;
	private final boolean insideWorld;

	public GamePhase(String name, boolean insideWorld)
	{
		this.name = name;
		this.insideWorld = insideWorld;
		if (!name.equals("unknown"))
		{
			ALL.add(this);
		}
	}

	public boolean isInsideWorld()
	{
		return this.insideWorld;
	}

	public String getName()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
