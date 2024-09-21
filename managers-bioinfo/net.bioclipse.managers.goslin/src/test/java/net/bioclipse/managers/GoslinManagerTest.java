/* Copyright (c) 2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lifstools.jgoslin.domain.LipidAdduct;

import net.bioclipse.core.business.BioclipseException;

public class GoslinManagerTest {

	static GoslinManager goslin;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("goslintestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		goslin = new GoslinManager(tmpPath);
	}

	@Test
	public void parseSwissLipids() throws BioclipseException {
		LipidAdduct la = goslin.parseSwissLipids("Cer(d18:1/20:2)");
		assertNotNull(la);
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				goslin.parseSwissLipids("FALSE");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Parsing error:"));
	}

	@Test
	public void parseLipidMaps() throws BioclipseException {
		LipidAdduct la = goslin.parseLipidMaps("FA14:0");
		assertNotNull(la);
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				goslin.parseLipidMaps("FALSE");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Parsing error:"));
	}

	@Test
	public void parseShorthand() throws BioclipseException {
		LipidAdduct la = goslin.parseShorthand("Cer 18:1;O2/20:2");
		assertNotNull(la);
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				goslin.parseShorthand("FALSE");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Parsing error:"));
	}

	@Test
	public void testDOIs() {
		List<String> dois = goslin.doi();
		assertNotNull(dois);
		assertSame(2, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("goslin", goslin.getManagerName());
	}

}
