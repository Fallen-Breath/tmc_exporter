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

package me.fallenbreath.tmcexporter.tests;

import junit.framework.TestCase;
import me.fallenbreath.tmcexporter.config.AddressWhitelistChecker;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AddressWhitelistCheckerTest extends TestCase
{
	static
	{
		AddressWhitelistChecker.IS_UNITTEST = true;
	}

	public void testWildcard()
	{
		String[] whitelist = {"*"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		SocketAddress addr = new InetSocketAddress("192.168.1.1", 80);
		assertTrue(checker.test(addr));
		SocketAddress addr2 = new InetSocketAddress("2001:db8::1", 80);
		assertTrue(checker.test(addr2));
	}

	public void testIndividualIPs()
	{
		String[] whitelist = {"192.168.1.1", "10.0.0.1", "2001:db8::1"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("10.0.0.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::1", 80)));
		assertFalse(checker.test(new InetSocketAddress("192.168.1.2", 80)));
		assertFalse(checker.test(new InetSocketAddress("10.0.0.2", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db8::2", 80)));
	}

	public void testSubnets()
	{
		String[] whitelist = {"192.168.1.0/24", "10.0.0.0/8", "2001:db8::/32"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.100", 80)));
		assertTrue(checker.test(new InetSocketAddress("10.255.255.255", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::abcd", 80)));
		assertFalse(checker.test(new InetSocketAddress("192.168.2.1", 80)));
		assertFalse(checker.test(new InetSocketAddress("11.0.0.1", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db9::1", 80)));
	}

	public void testMixedWhitelist()
	{
		String[] whitelist = {"192.168.1.1", "10.0.0.0/8", "2001:db8::1", "2001:db8:1::/48", "example.com"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::1", 80)));
		assertTrue(checker.test(new InetSocketAddress("10.0.0.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8:1::abcd", 80)));
		assertFalse(checker.test(new InetSocketAddress("192.168.1.2", 80)));
		assertFalse(checker.test(new InetSocketAddress("11.0.0.1", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db9::1", 80)));
	}

	public void testInvalidEntries()
	{
		String[] whitelist = {"invalid_ip", "256.256.256.256", "2001:db8::/129"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertFalse(checker.test(new InetSocketAddress("192.168.1.1", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db8::1", 80)));
	}

	public void testNullWhitelist()
	{
		AddressWhitelistChecker checker = new AddressWhitelistChecker(null);
		assertFalse(checker.test(new InetSocketAddress("192.168.1.1", 80)));
	}

	public void testEmptyWhitelist()
	{
		String[] whitelist = {};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertFalse(checker.test(new InetSocketAddress("192.168.1.1", 80)));
	}

	public void testFullSubnets()
	{
		String[] whitelist = {"0.0.0.0/0", "::/0"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("10.0.0.1", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::1", 80)));
	}

	public void testHostSubnets()
	{
		String[] whitelist = {"192.168.1.1/32", "2001:db8::1/128"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.1", 80)));
		assertFalse(checker.test(new InetSocketAddress("192.168.1.2", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::1", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db8::2", 80)));
	}

	public void testMixedIPVersions()
	{
		String[] whitelist = {"192.168.1.0/24", "2001:db8::/32"};
		AddressWhitelistChecker checker = new AddressWhitelistChecker(whitelist);
		assertTrue(checker.test(new InetSocketAddress("192.168.1.100", 80)));
		assertFalse(checker.test(new InetSocketAddress("192.168.2.100", 80)));
		assertTrue(checker.test(new InetSocketAddress("2001:db8::100", 80)));
		assertFalse(checker.test(new InetSocketAddress("2001:db9::100", 80)));
	}

	public void testIPVersionMismatch()
	{
		String[] whitelistIPv4 = {"192.168.1.0/24"};
		AddressWhitelistChecker checkerIPv4 = new AddressWhitelistChecker(whitelistIPv4);
		assertFalse(checkerIPv4.test(new InetSocketAddress("2001:db8::1", 80)));
		String[] whitelistIPv6 = {"2001:db8::/32"};
		AddressWhitelistChecker checkerIPv6 = new AddressWhitelistChecker(whitelistIPv6);
		assertFalse(checkerIPv6.test(new InetSocketAddress("192.168.1.1", 80)));
	}
}