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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.rdf.business.IRDFStore;

public class RDFManagerTest {

	static RDFManager rdf;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("rdftestws").toString();
		rdf = new RDFManager(workspaceRoot);
	}

	@Test
	public void testCreateInMemoryStore() {
		IRDFStore store = rdf.createInMemoryStore();
		assertNotNull(store);
	}

	@Test
	public void testCreateInMemoryStore_Ontology() {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
	}

	@Test
	public void testAsTurtle() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		String turtle = rdf.asTurtle(store);
		assertTrue(turtle.contains("prefix"));
	}

	@Test
	public void testAsRDFN3() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		String n3 = rdf.asRDFN3(store);
		assertTrue(n3.contains("<"));
	}

	@Test
	public void testSize() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long size = rdf.size(store);
		assertNotSame((long)0, size);
	}

	@Test
	public void testAddObjectProperty() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addObjectProperty(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"https://example.org/object"
		);
		String turtle = rdf.asTurtle(store);
		assertTrue(turtle.contains("prefix"));
		assertTrue(turtle.contains("example.org"));
	}

	@Test
	public void testAddPrefix() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addObjectProperty(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"https://example.org/object"
		);
		rdf.addPrefix(store, "ex", "https://example.org/");
		String turtle = rdf.asTurtle(store);
		assertTrue(turtle.contains("prefix ex:"));
		assertTrue(turtle.contains("ex:subject"));
	}

	@Test
	public void testAddDataProperty() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addDataProperty(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"Object"
		);
		rdf.addPrefix(store, "ex", "https://example.org/");
		String turtle = rdf.asTurtle(store);
		assertTrue(turtle.contains("prefix ex:"));
		assertTrue(turtle.contains("\"Object\""));
		assertFalse(turtle.contains("ex:Object"));
	}
}
