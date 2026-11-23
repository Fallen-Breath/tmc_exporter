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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

public class IdentifierUtils
{
	public static String IdToString(ResourceLocation identifier)
	{
		return identifier.getNamespace().equals("minecraft") ? identifier.getPath() : identifier.toString();
	}

	public static ResourceLocation of(Block block)
	{
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	public static ResourceLocation of(Fluid fluid)
	{
		return BuiltInRegistries.FLUID.getKey(fluid);
	}

	public static ResourceLocation of(EntityType<?> entityType)
	{
		return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
	}

	public static ResourceLocation of(BlockEntityType<?> blockEntityType)
	{
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
	}
}
