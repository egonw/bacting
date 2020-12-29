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

	static UIManager ui;
	static RDFManager rdf;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("rdftestws").toString();
		rdf = new RDFManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		ui.newProject("/RDFTests/");
		ui.newFile("/RDFTests/exampleContent.xml",
			"@prefix ex:    <https://example.org/> .\n" +
			"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"ex:subject  ex:predicate  \"Object\"@en .\n");
	}

	@Test
	public void testCreateStore() {
		IRDFStore store = rdf.createStore("/tmp/");
		assertNotNull(store);
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
	public void testImportFile() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long initialSize = rdf.size(store);
		store = rdf.importFile(store, "/RDFTests/exampleContent.xml", "TURTLE");
		assertNotSame(initialSize, rdf.size(store));
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

	@Test
	public void testAddTypedDataProperty() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addTypedDataProperty(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"Object", "xsd:string"
		);
		rdf.addPrefix(store, "ex", "https://example.org/");
		String turtle = rdf.asTurtle(store);
		assertTrue(turtle.contains("prefix ex:"));
		assertTrue(turtle.contains("\"Object\""));
		assertFalse(turtle.contains("ex:Object"));
		assertTrue(turtle.contains("^^<xsd:string>"));
	}

	@Test
	public void testAddPropertyInLanguage() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addPropertyInLanguage(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"Object", "en"
		);
		rdf.addPrefix(store, "ex", "https://example.org/");
		String turtle = rdf.asTurtle(store);
		System.out.println(turtle);
		assertTrue(turtle.contains("prefix ex:"));
		assertTrue(turtle.contains("\"Object\""));
		assertFalse(turtle.contains("ex:Object"));
		assertTrue(turtle.contains("@en"));
	}
}
