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

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TileTickKey
{
	public final TileTickType type;
	public final Identifier id;

	private TileTickKey(TileTickType type, Identifier id)
	{
		this.type = type;
		this.id = id;
	}

	public static TileTickKey of(TileTickType type, Identifier id)
	{
		return new TileTickKey(type, id);
	}

	@Nullable
	public static TileTickKey ofTileTickObject(Object object)
	{
		if (object instanceof Block)
		{
			return of(TileTickType.BLOCK, Registries.BLOCK.getId((Block)object));
		}
		else if (object instanceof Fluid)
		{
			return of(TileTickType.FLUID, Registries.FLUID.getId((Fluid)object));
		}
		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TileTickKey that = (TileTickKey)o;
		return type == that.type && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type, id);
	}

	@SuppressWarnings("EnhancedSwitchMigration")
	public WorldTickScheduler<?> getScheduler(ServerWorld world)
	{
		switch (this.type)
		{
			case BLOCK:
				return world.getBlockTickScheduler();
			case FLUID:
				return world.getFluidTickScheduler();
			default:
				throw new IllegalArgumentException("invalid type: " + this.type);
		}
	}
}
