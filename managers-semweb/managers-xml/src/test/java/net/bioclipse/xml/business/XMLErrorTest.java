/* Copyright (c) 2024  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */package net.bioclipse.xml.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class XMLErrorTest {

	@Test
	public void testConstructorToString() {
		XMLError error = new XMLError("some message");
		assertNotNull(error.toString());
		assertTrue(error.toString().contains("some message"));
	}

	@Test
	public void testConstructorToStringWithLocation() {
		XMLError error = new XMLError("some message", 3, 7);
		assertNotNull(error.toString());
		assertTrue(error.toString().contains("some message"));
		assertTrue(error.toString().contains("3"));
		assertTrue(error.toString().contains("7"));
	}

	@Test
	public void testConstructorToStringWithWrongLocations() {
		XMLError error = new XMLError("some message", 3, -1);
		assertNotNull(error.toString());
		assertTrue(error.toString().contains("some message"));
		assertFalse(error.toString().contains("3"));
		assertFalse(error.toString().contains("7"));

		error = new XMLError("some message", -1, 7);
		assertNotNull(error.toString());
		assertTrue(error.toString().contains("some message"));
		assertFalse(error.toString().contains("3"));
		assertFalse(error.toString().contains("7"));
	}
}
