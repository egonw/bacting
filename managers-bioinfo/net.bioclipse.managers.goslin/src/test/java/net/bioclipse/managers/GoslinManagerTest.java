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

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GoslinManagerTest {

	static GoslinManager goslin;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("goslintestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		goslin = new GoslinManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = goslin.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("goslin", goslin.getManagerName());
	}

}
