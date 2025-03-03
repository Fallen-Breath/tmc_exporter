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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.*;
import me.fallenbreath.tmcexporter.TmcExporterMod;
import me.fallenbreath.tmcexporter.http.HttpRequestHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TmcExporterPacketHandler extends ByteToMessageDecoder
{
	private final VanillaHandlerRestorer vanillaHandlerRestorer;
	private final HttpRequestHandler httpRequestHandler;

	public TmcExporterPacketHandler(VanillaHandlerRestorer vanillaHandlerRestorer)
	{
		this.vanillaHandlerRestorer = vanillaHandlerRestorer;
		this.httpRequestHandler = new HttpRequestHandler();
	}

	private static boolean startsWith(byte[] source, byte[] prefix)
	{
		if (prefix.length > source.length)
		{
			return false;
		}
		byte[] sourcePrefix = Arrays.copyOfRange(source, 0, prefix.length);
		return Arrays.equals(sourcePrefix, prefix);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception
	{
		byteBuf.markReaderIndex();
		byte[] expectedPrefix = "GET /metrics".getBytes();
		byte[] actualRead = new byte[Math.min(expectedPrefix.length, byteBuf.readableBytes())];
		byteBuf.readBytes(actualRead);
		byteBuf.resetReaderIndex();

		if (!startsWith(expectedPrefix, actualRead))
		{
			// Case 1: Not the HTTP request
			this.vanillaHandlerRestorer.restore(ctx);
			ctx.pipeline().fireChannelRead(byteBuf.retain());
			return;
		}
		if (expectedPrefix.length != actualRead.length)
		{
			// Case 2: Not enough data
			return;
		}

		// Case 3: It's what we what
		this.setupForHttp(ctx, byteBuf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		TmcExporterMod.LOGGER.error("handler error: {}", cause.toString());
		ctx.channel().close();
	}

	private void setupForHttp(ChannelHandlerContext ctx, ByteBuf byteBuf)
	{
		ctx.pipeline().remove(this);

		ctx.pipeline().addLast("tmce_http_codec", new HttpServerCodec());
		ctx.pipeline().addLast("tmce_http_aggregator", new HttpObjectAggregator(8192));
		ctx.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>()
		{
			@Override
			protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
			{
				FullHttpResponse response = httpRequestHandler.process(request);

				response.headers().set(HttpHeaderNames.SERVER, "TMC Exporter");
				response.headers().set(HttpHeaderNames.DATE, new Date());
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			}
		});

		ctx.pipeline().fireChannelRead(byteBuf.retain());
	}
}
