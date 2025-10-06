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

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

//#if MC >= 11600
import net.minecraft.registry.RegistryKey;
//#else
//$$ import net.minecraft.world.dimension.DimensionType;
//#endif

public class Dimension
{
	//#if MC >= 11600
	private final RegistryKey<World> dimensionType;
	//#else
	//$$ private final DimensionType dimensionType;
	//#endif


	private Dimension(
			//#if MC >= 11600
			RegistryKey<World> dimensionType
			//#else
			//$$ DimensionType dimensionType
			//#endif
	)
	{
		this.dimensionType = dimensionType;
	}

	public static Dimension of(World world)
	{
		return new Dimension(
				//#if MC >= 11600
				world.getRegistryKey()
				//#else
				//$$ world.getDimension().getType()
				//#endif
		);
	}

	public static Dimension of(
			//#if MC >= 11600
			RegistryKey<World> dimensionType
			//#else
			//$$ DimensionType dimensionType
			//#endif
	)
	{
		return new Dimension(dimensionType);
	}

	public static Dimension of(Entity entity)
	{
		return of(entity.getEntityWorld());
	}

	public Identifier getIdentifier()
	{
		//#if MC >= 11600
		return this.dimensionType.getValue();
		//#else
		//$$ return DimensionType.getId(this.dimensionType);
		//#endif
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Dimension that = (Dimension) o;
		return Objects.equals(dimensionType, that.dimensionType);
	}

	@Override
	public int hashCode()
	{
		return this.dimensionType.hashCode();
	}

	@Override
	public String toString()
	{
		return this.getIdentifier().toString();
	}
}
