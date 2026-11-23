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

package me.fallenbreath.tmcexporter.mixins.metric.dimension.tiletick;

import me.fallenbreath.tmcexporter.metric.collect.fake.ServerWorldAssociated;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin
{
	@Shadow @Final private LevelTicks<Block> blockTicks;
	@Shadow @Final private LevelTicks<Fluid> fluidTicks;

	@Inject(method = "<init>", 	at = @At("TAIL"))
	private void associateSelfToTickSchedulers(CallbackInfo ci)
	{
		ServerLevel self = (ServerLevel)(Object)this;
		((ServerWorldAssociated)this.blockTicks).setServerWorld$TMCE(self);
		((ServerWorldAssociated)this.fluidTicks).setServerWorld$TMCE(self);
	}
}
