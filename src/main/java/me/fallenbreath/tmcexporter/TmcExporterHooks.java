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

package me.fallenbreath.tmcexporter;

import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import net.minecraft.server.MinecraftServer;

public class TmcExporterHooks
{
	public static void onInitialize()
	{
	}

	public static void onServerInitDone(MinecraftServer server)
	{
		MetricCollector.getInstance().init(server);
	}

	public static void onServerTickStart(MinecraftServer server)
	{
		MetricCollector.getInstance().tickStart();
	}

	public static void onServerTickEnd(MinecraftServer server)
	{
		MetricCollector.getInstance().tickEnd();
	}
}
