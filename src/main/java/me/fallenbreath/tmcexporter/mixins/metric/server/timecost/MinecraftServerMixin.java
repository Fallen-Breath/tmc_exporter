/*
 * This file is part of the TMC Exporter project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
 *
 * TechMetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TechMetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TechMetrics.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tmcexporter.mixins.metric.server.timecost;

import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
	@Inject(
			method = "tick", at = {
					@At("HEAD"),
					@At("TAIL")
			}
	)
	private void timeCost_clearPhase(CallbackInfo ci)
	{
		// ensure the recorder state is correct, in case something bad happened
		MetricCollector.getServerStats().ifPresent(s -> s.phaseCosts.reset());
	}

	@Inject(
			//#if MC >= 1.21.3
			method = "runAutosave",
			//#else
			//$$ method = "tick",
			//#endif
			at = @At(
					value = "CONSTANT",
					args = "stringValue=Autosave started"
			)
	)
	private void timeCost_autoSaveStart(CallbackInfo ci)
	{
		MetricCollector.getServerStats().ifPresent(s -> s.phaseCosts.push(GamePhase.AUTO_SAVE));
	}

	@Inject(
			//#if MC >= 1.21.3
			method = "runAutosave",
			//#else
			//$$ method = "tick",
			//#endif
			at = @At(
					value = "CONSTANT",
					args = "stringValue=Autosave finished"
			)
	)
	private void timeCost_autoSaveEnd(CallbackInfo ci)
	{
		MetricCollector.getServerStats().ifPresent(s -> s.phaseCosts.pop(GamePhase.AUTO_SAVE));
	}

	@Inject(
			//#if MC >= 11700
			method = "runTasksTillTickEnd",
			//#else
			//$$ method = "method_16208",
			//#endif
			at = @At("HEAD")
	)
	private void timeCost_asyncTaskStart(CallbackInfo ci)
	{
		MetricCollector.getServerStats().ifPresent(s -> s.phaseCosts.push(GamePhase.ASYNC_TASK));
	}

	@Inject(
			//#if MC >= 11700
			method = "runTasksTillTickEnd",
			//#else
			//$$ method = "method_16208",
			//#endif
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;runTasks()V",
					shift = At.Shift.AFTER
			)
	)
	private void timeCost_asyncTaskEnd(CallbackInfo ci)
	{
		MetricCollector.getServerStats().ifPresent(s -> s.phaseCosts.pop(GamePhase.ASYNC_TASK));
	}
}
