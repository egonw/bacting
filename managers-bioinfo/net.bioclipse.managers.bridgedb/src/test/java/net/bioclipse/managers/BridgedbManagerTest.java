/* Copyright (c) 2020,2021  Egon Willighagen <egon.willighagen@gmail.com>
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class BridgedbManagerTest {

	static BridgedbManager bridgedb;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("bridgedbtestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		bridgedb = new BridgedbManager(tmpPath);
		bridgedb.registerDataSource("U", "UniGene");
		bridgedb.registerDataSource("UUU", "Unknown data source"); // something in the old Ensembl ID mapping database that BridgeDb 3.0.10 doesn't like
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
	public void testGetSourceByPrefix() throws BioclipseException {
		DataSource source = bridgedb.getSourceByPrefix("wikidata");
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
		assertTrue("L:1234:T".equals(xref.toString()));
	}

	@Test
	public void testXref() throws BioclipseException {
		Xref xref = bridgedb.xref("L:1234");
		assertTrue("L:1234:T".equals(xref.toString()));
	}

	@Test
	public void testCompactIdentifier() throws BioclipseException {
		Xref xref = bridgedb.compactIdentifier("ncbigene:1234");
		assertTrue("L:1234:T".equals(xref.toString()));
	}

	@Test
	public void testXref_bad() throws BioclipseException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				bridgedb.xref("1234");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Unexpected format"));
	}

	@Test
	public void testMapREST() throws BioclipseException {
		List<String> map = bridgedb.map(
			"https://webservice.bridgedb.org/Human",
			"1234", "L"
		);
		assertNotNull(map);
		assertNotEquals(0, map.size());
		// and check full provider style link
        map = bridgedb.map(
            "idmapper-bridgerest:https://webservice.bridgedb.org/Human", "1234", "L"
        );
        assertNotNull(map);
        assertNotEquals(0, map.size());
	}

	@Test
	public void testRegisterDataSource() {
		DataSource source = bridgedb.registerDataSource("Unp", "UniGene No Problem");
		assertNotNull(source);
	}

	@Test
	public void testMapREST_NonExistingService() throws BioclipseException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				bridgedb.map(
					"https://bridgedb.elixir-europe.org/Human",
					"1234", "L"
				);
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Could not connect to the REST service"));
	}

	@Test
	public void testSearchMapREST() throws BioclipseException {
		List<String> map = bridgedb.search(
			"https://webservice.bridgedb.org/Human", "NFKB", 3
		);
		assertNotNull(map);
		assertNotEquals(0, map.size());
		// and check full provider style link
        map = bridgedb.search(
            "idmapper-bridgerest:https://webservice.bridgedb.org/Human", "NFKB", 3
        );
        assertNotNull(map);
        assertNotEquals(0, map.size());
	}

	@Test
	public void testSearchREST_NonExistingService() throws BioclipseException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				bridgedb.search(
					"https://bridgedb.elixir-europe.org/Human",
					"NFKB", 3
				);
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Could not connect to the REST service"));
	}

	@Test
	public void guessIdentifierType() throws BioclipseException {
		List<DataSource> types = bridgedb.guessIdentifierType("ENSG00000099250");
		assertNotNull(types);
		assertNotEquals(0, types.size());
	}

	@Test
	public void loadRelationalDatabase() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				bridgedb.loadRelationalDatabase("noExist.bridge");
			}
		);
		assertNotNull(exception);
		System.out.println(exception.getMessage());
		assertTrue(exception.getMessage().contains("the database at this location"));
	}

	@Test
    public void listIDMapperProviders() {
		List<String> providers = bridgedb.listIDMapperProviders();
		assertNotNull(providers);
    }

	@Test
    public void getIDMapper() throws BioclipseException {
		IDMapper mapper = bridgedb.getIDMapper("Gene ID Mapping Database (Homo sapiens)");
		assertNull(mapper);
    }

}
