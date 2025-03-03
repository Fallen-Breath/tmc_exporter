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

package me.fallenbreath.tmcexporter.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import me.fallenbreath.tmcexporter.mixins.network.ServerNetworkIoChannelInitializerAccessor;

import java.util.concurrent.TimeUnit;

public class TmcExporterChannelInitializer extends ChannelInitializer<Channel>
{
	private final ServerNetworkIoChannelInitializerAccessor vanillaInitializer;

	public TmcExporterChannelInitializer(ChannelInitializer<Channel> vanillaInitializer)
	{
		this.vanillaInitializer = (ServerNetworkIoChannelInitializerAccessor)vanillaInitializer;
	}

	@Override
	protected void initChannel(Channel channel)
	{
		VanillaHandlerRestorer restorer = VanillaHandlerRestorer.once(ctx -> {
			for (String name : new String[]{"tmcexporter_timeout", "tmcexporter_handler"})
			{
				if (channel.pipeline().get(name) != null)
				{
					channel.pipeline().remove(name);
				}
			}

			this.vanillaInit(channel);
			// vanilla handlers need this to init something
			ctx.pipeline().fireChannelActive();
		});
		TmcExporterPacketHandler packetHandler = new TmcExporterPacketHandler(restorer);

		channel.pipeline().addLast("tmcexporter_timeout", new HttpSnifferTimeoutHandler(restorer));
		channel.pipeline().addLast("tmcexporter_handler", packetHandler);
	}

	private void vanillaInit(Channel channel)
	{
		this.vanillaInitializer.invokeInitChannel(channel);
	}

	private static class HttpSnifferTimeoutHandler extends IdleStateHandler
	{
		private final VanillaHandlerRestorer restorer;

		public HttpSnifferTimeoutHandler(VanillaHandlerRestorer restorer)
		{
			// 10s read timeout
			super(10, 0, 0, TimeUnit.SECONDS);
			this.restorer = restorer;
		}

		@Override
		protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
		{
			this.restorer.restore(ctx);
		}
	}
}
