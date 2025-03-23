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

package me.fallenbreath.tmcexporter.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import me.fallenbreath.tmcexporter.TmcExporterMod;
import me.fallenbreath.tmcexporter.config.AddressWhitelistChecker;
import me.fallenbreath.tmcexporter.metric.registry.MetricRegistry;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Comparator;
import java.util.Objects;

public class HttpRequestHandler
{
	public FullHttpResponse process(SocketAddress clientAddress, FullHttpRequest request)
	{
		TmcExporterMod.LOGGER.debug("Received request: {} {} {}", request.method(), request.uri(), request.headers());

		if (!this.allowForClient(clientAddress))
		{
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
		}

		if (!Objects.equals(request.uri(), "/metrics") && !Objects.equals(request.uri(), "/metrics/"))
		{
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		}

		ByteBuf metricsBuffer = this.createMetrics();

		DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, metricsBuffer);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, PrometheusTextFormatWriter.CONTENT_TYPE);

		return response;
	}

	private boolean allowForClient(SocketAddress clientAddress)
	{
		AddressWhitelistChecker checker = new AddressWhitelistChecker(new String[]{"*"});
		return checker.test(clientAddress);
	}

	private ByteBuf createMetrics()
	{
		MetricSnapshots.Builder builder = MetricSnapshots.builder();
		MetricRegistry.REGISTRY.scrape().stream().
				sorted(Comparator.comparing(ms -> ms.getMetadata().getPrometheusName())).
				forEach(builder::metricSnapshot);
		MetricSnapshots metricSnapshots = builder.build();

		ByteBuf byteBuf = Unpooled.buffer();
		try
		{
			ByteBufOutputStream bufOut = new ByteBufOutputStream(byteBuf);
			new PrometheusTextFormatWriter(false).write(bufOut, metricSnapshots);
		}
		catch (IOException e)
		{
			// should not happen
			throw new AssertionError(e);
		}

		return byteBuf;
	}
}
