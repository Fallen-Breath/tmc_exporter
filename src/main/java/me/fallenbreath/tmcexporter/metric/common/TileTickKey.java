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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ticks.LevelTicks;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TileTickKey
{
	public final TileTickType type;
	public final ResourceLocation id;

	private TileTickKey(TileTickType type, ResourceLocation id)
	{
		this.type = type;
		this.id = id;
	}

	public static TileTickKey of(TileTickType type, ResourceLocation id)
	{
		return new TileTickKey(type, id);
	}

	@Nullable
	public static TileTickKey ofTileTickObject(Object object)
	{
		if (object instanceof Block)
		{
			return of(TileTickType.BLOCK, BuiltInRegistries.BLOCK.getKey((Block)object));
		}
		else if (object instanceof Fluid)
		{
			return of(TileTickType.FLUID, BuiltInRegistries.FLUID.getKey((Fluid)object));
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
	public LevelTicks<?> getScheduler(ServerLevel world)
	{
		switch (this.type)
		{
			case BLOCK:
				return world.getBlockTicks();
			case FLUID:
				return world.getFluidTicks();
			default:
				throw new IllegalArgumentException("invalid type: " + this.type);
		}
	}
}
