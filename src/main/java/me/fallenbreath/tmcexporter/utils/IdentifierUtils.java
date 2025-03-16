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

package me.fallenbreath.tmcexporter.utils;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class IdentifierUtils
{
	public static String IdToString(Identifier identifier)
	{
		return identifier.getNamespace().equals("minecraft") ? identifier.getPath() : identifier.toString();
	}

	public static Identifier of(Block block)
	{
		return Registries.BLOCK.getId(block);
	}

	public static Identifier of(Fluid fluid)
	{
		return Registries.FLUID.getId(fluid);
	}

	public static Identifier of(EntityType<?> entityType)
	{
		return Registries.ENTITY_TYPE.getId(entityType);
	}

	public static Identifier of(BlockEntityType<?> blockEntityType)
	{
		return Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType);
	}
}
