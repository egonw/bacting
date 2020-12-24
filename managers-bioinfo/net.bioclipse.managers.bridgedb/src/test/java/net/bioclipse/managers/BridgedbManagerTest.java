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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class BridgedbManagerTest {

	static BridgedbManager bridgedb;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("bridgedbtestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		bridgedb = new BridgedbManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = bridgedb.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("bridgedb", bridgedb.getManagerName());
	}

	@Test
	public void testGetSource() throws BioclipseException {
		DataSource source = bridgedb.getSource("L");
		assertNotNull(source);
	}

	@Test
	public void testGetSourceFromName() throws BioclipseException {
		DataSource source = bridgedb.getSourceFromName("Ensembl");
		assertNotNull(source);
	}

	@Test
	public void testListAllSources() throws BioclipseException {
		List<String> sourceCodes = bridgedb.listAllSources();
		assertNotNull(sourceCodes);
	}

	@Test
	public void testListOrganisms() throws BioclipseException {
		List<Organism> organisms = bridgedb.listAllOrganisms();
		assertNotNull(organisms);
	}

	@Test
	public void testXref2() throws BioclipseException {
		Xref xref = bridgedb.xref("1234", "L");
		assertTrue("L:1234".equals(xref.toString()));
	}

	@Test
	public void testXref() throws BioclipseException {
		Xref xref = bridgedb.xref("L:1234");
		assertTrue("L:1234".equals(xref.toString()));
	}

	@Test
	public void testMapREST() throws BioclipseException {
		List<String> map = bridgedb.map(
			"http://webservice.bridgedb.org/Human",
			"1234", "L"
		);
		assertNotNull(map);
		assertNotEquals(0, map.size());
	}
}
