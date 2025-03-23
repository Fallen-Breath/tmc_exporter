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

package me.fallenbreath.tmcexporter.config;

import com.google.common.collect.Sets;
import me.fallenbreath.tmcexporter.TmcExporterMod;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Just like uvicorn's $FORWARDED_ALLOW_IPS
 */
public class AddressWhitelistChecker implements Predicate<SocketAddress>
{
	private final boolean alwaysTrust;
	private final Set<InetAddress> trustedHosts = Sets.newHashSet();
	private final Set<SubnetInfo> trustedNetworks = Sets.newHashSet();

	public AddressWhitelistChecker(String[] trustedHosts)
	{
		this.alwaysTrust = trustedHosts != null && Arrays.asList(trustedHosts).contains("*");
		if (alwaysTrust || trustedHosts == null)
		{
			return;
		}

		for (String host : trustedHosts)
		{
			if (host == null || host.trim().isEmpty())
			{
				continue;
			}

			String trimmedHost = host.trim();
			if (trimmedHost.contains("/"))
			{
				try
				{
					SubnetInfo subnet = SubnetInfo.fromCIDR(trimmedHost);
					this.trustedNetworks.add(subnet);
				}
				catch (IllegalArgumentException e)
				{
					TmcExporterMod.LOGGER.warn("Invalid subnet: {}", trimmedHost);
				}
			}
			else
			{
				try
				{
					InetAddress addr = InetAddress.getByName(trimmedHost);
					this.trustedHosts.add(addr);
				}
				catch (UnknownHostException e)
				{
					TmcExporterMod.LOGGER.warn("Invalid address: {}", trimmedHost);
				}
			}
		}
	}

	@SuppressWarnings("PatternVariableCanBeUsed")
	@Override
	public boolean test(SocketAddress socketAddress)
	{
		if (this.alwaysTrust)
		{
			return true;
		}
		if (!(socketAddress instanceof InetSocketAddress))
		{
			return false;
		}

		InetAddress addr = ((InetSocketAddress)socketAddress).getAddress();
		if (addr == null)
		{
			return false;
		}

		if (this.trustedHosts.contains(addr))
		{
			return true;
		}

		for (SubnetInfo network : this.trustedNetworks)
		{
			if (network.contains(addr))
			{
				return true;
			}
		}

		return false;
	}

	private static class SubnetInfo
	{
		private final InetAddress networkAddress;
		private final byte[] mask;
		private final int addressLength;

		private SubnetInfo(InetAddress networkAddress, int maskBits)
		{
			this.networkAddress = networkAddress;
			this.addressLength = networkAddress.getAddress().length;
			this.mask = createMask(addressLength, maskBits);
		}

		public static SubnetInfo fromCIDR(String cidr)
		{
			String[] parts = cidr.split("/");
			if (parts.length != 2)
			{
				throw new IllegalArgumentException("BAD CIDR format " + cidr);
			}

			String netAddrStr = parts[0];
			int maskBits;
			try
			{
				maskBits = Integer.parseInt(parts[1]);
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Bad mask " + parts[1]);
			}

			InetAddress netAddr;
			try
			{
				netAddr = InetAddress.getByName(netAddrStr);
			}
			catch (UnknownHostException e)
			{
				throw new IllegalArgumentException("Invalid address: " + netAddrStr);
			}

			int maxBits = netAddr.getAddress().length * 8;
			if (maskBits < 0 || maskBits > maxBits)
			{
				throw new IllegalArgumentException("Bad mask bits, should be <= " + maxBits);
			}

			return new SubnetInfo(netAddr, maskBits);
		}

		public boolean contains(InetAddress addr)
		{
			if (addr.getAddress().length != this.addressLength)
			{
				return false;
			}

			byte[] clientBytes = addr.getAddress();
			byte[] netBytes = this.networkAddress.getAddress();

			for (int i = 0; i < this.addressLength; i++)
			{
				if ((clientBytes[i] & mask[i]) != netBytes[i])
				{
					return false;
				}
			}
			return true;
		}

		private static byte[] createMask(int addrLen, int maskBits)
		{
			byte[] mask = new byte[addrLen];
			int fullBytes = maskBits / 8;
			int remainingBits = maskBits % 8;

			for (int i = 0; i < fullBytes; i++)
			{
				mask[i] = (byte)0xFF;
			}
			if (remainingBits > 0)
			{
				mask[fullBytes] = (byte)(0xFF << (8 - remainingBits));
			}
			return mask;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SubnetInfo that = (SubnetInfo)o;
			return this.networkAddress.equals(that.networkAddress) && Arrays.equals(this.mask, that.mask);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(this.networkAddress, Arrays.hashCode(this.mask));
		}
	}
}