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

package me.fallenbreath.tmcexporter.mixins.metric.mod.fabriccarpet;

import carpet.utils.SpawnReporter;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.fallenbreath.tmcexporter.metric.common.Dimension;
import me.fallenbreath.tmcexporter.metric.exporter.FabricCarpetExporter;
import me.fallenbreath.tmcexporter.utils.ModIds;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Restriction(require = @Condition(ModIds.carpet))
@Mixin(SpawnReporter.class)
public abstract class SpawnReporterMixin
{
	@Shadow(remap = false) @Final @Mutable public static Object2LongOpenHashMap<Pair<RegistryKey<World>, SpawnGroup>> spawn_ticks_full;
	@Shadow(remap = false) @Final @Mutable public static Object2LongOpenHashMap<Pair<RegistryKey<World>, SpawnGroup>> spawn_ticks_fail;
	@Shadow(remap = false) @Final @Mutable public static Object2LongOpenHashMap<Pair<RegistryKey<World>, SpawnGroup>> spawn_ticks_succ;
	@Shadow(remap = false) @Final @Mutable public static Object2LongOpenHashMap<Pair<RegistryKey<World>, SpawnGroup>> spawn_ticks_spawns;

	@Unique
	private static <K> Object2LongOpenHashMap<K> hookObject2LongOpenHashMapAddTo(BiFunction<K, Long, Void> callback)
	{
		return new Object2LongOpenHashMap<>()
		{
			@Override
			public long addTo(K key, long incr)
			{
				callback.apply(key, incr);
				return super.addTo(key, incr);
			}
		};
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void hackForRecordAdd(CallbackInfo ci)
	{
		spawn_ticks_full = hookObject2LongOpenHashMapAddTo((key, incr) -> {
			FabricCarpetExporter.recordSpawnRecorderSpawnTicksFullAdd(Dimension.of(key.getLeft()), key.getRight(), incr);
			return null;
		});
		spawn_ticks_fail = hookObject2LongOpenHashMapAddTo((key, incr) -> {
			FabricCarpetExporter.recordSpawnRecorderSpawnTicksFailAdd(Dimension.of(key.getLeft()), key.getRight(), incr);
			return null;
		});
		spawn_ticks_succ = hookObject2LongOpenHashMapAddTo((key, incr) -> {
			FabricCarpetExporter.recordSpawnRecorderSpawnTicksSuccAdd(Dimension.of(key.getLeft()), key.getRight(), incr);
			return null;
		});
		spawn_ticks_spawns = hookObject2LongOpenHashMapAddTo((key, incr) -> {
			FabricCarpetExporter.recordSpawnRecorderSpawnTicksSpawnsAdd(Dimension.of(key.getLeft()), key.getRight(), incr);
			return null;
		});
	}
}
