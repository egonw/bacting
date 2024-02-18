/* Copyright (c) 2020-2022  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.shex.ShexReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.business.IRDFStore;

public class RDFManagerTest {

	static BioclipseManager bioclipse;
	static UIManager ui;
	static RDFManager rdf;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("rdftestws").toString();
		rdf = new RDFManager(workspaceRoot);
		bioclipse = new BioclipseManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		ui.newProject("/RDFTests/");
		ui.newFile("/RDFTests/exampleContent.xml",
			"@prefix ex:    <https://example.org/> .\n" +
			"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"ex:subject  a ex:type ; ex:predicate  \"Object\"@en .\n");
		ui.newFile("/RDFTests/exampleContent2.xml",
				"@prefix ex:    <https://example.org/> .\n" +
				"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
				"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
				"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
				"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
				"\n" +
				"ex:subject  a ex:type ; ex:predicate  \"Object\"@en .\n" +
				"ex:subject2 a ex:type ; ex:predicate  \"Object\"@en .\n");
		ui.newFile("/RDFTests/exampleContent.shex",
            "PREFIX ex:    <https://example.org/>\n" +
            "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
            "ex:shape {\n" +
            "  ex:predicate [ @en~ ] ; \n" +
            "}\n");
		ui.newFile("/RDFTests/exampleContent2.shex",
	            "PREFIX ex:    <https://example.org/>\n" +
	            "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
	            "ex:shape {\n" +
	            "  ex:predicate [ @nl~ ] ; \n" +
	            "}\n");
		ui.newFile("/RDFTests/classes.ttl",
	            "PREFIX ex:    <https://example.org/>\n" +
	            "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
	            "ex:shape a ex:ClassOne ; a ex:ClassTwo . \n");
	}

	// mock class, aimed to be not supported
	private class Store implements IRDFStore {}

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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.asTurtle(new Store());
		});
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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.size(new Store());
		});
	}

	@Test
	public void testImportFromString() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long initialSize = rdf.size(store);
		String content = 			"@prefix ex:    <https://example.org/> .\n" +
				"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
				"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
				"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
				"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
				"\n" +
				"ex:subject  ex:predicate  \"Object\"@en .\n";
		store = rdf.importFromString(store, content, "TURTLE");
		assertNotSame(initialSize, rdf.size(store));
		Assertions.assertThrows(Exception.class, () -> {
			rdf.importFromString(new Store(), content, "TURTLE");
		});
	}

	@Test
	public void testImportURL() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long initialSize = rdf.size(store);
		store = rdf.importURL(
			store, "https://raw.githubusercontent.com/NanoCommons/ontologies/master/enanomapper.owl"
		);
		assertNotSame(initialSize, rdf.size(store));
	}

	@Test
	public void testImportURL_UnknownHost() throws Exception {
		Exception exception = assertThrows(
			UnknownHostException.class, () ->
			{
				IRDFStore store = rdf.createInMemoryStore(true);
				rdf.importURL(
					store, "https://xxx.example.org/file.ttl"
				);
			}
		);
		assertNotNull(exception);
		System.out.println(exception.getMessage());
		assertTrue(exception.getMessage().contains("xxx.example.org"));
	}

	@Test
	public void testImportURLWithRedirect() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long initialSize = rdf.size(store);
		store = rdf.importURL(
			store, "http://www.wikidata.org/entity/Q5"
		);
		assertNotSame(initialSize, rdf.size(store));
	}

	@Test
	public void testImportURLWithHeaders() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		long initialSize = rdf.size(store);
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("SourceCode", "https://github.com/egonw/bacting/");
		store = rdf.importURL(
			store, "http://www.wikidata.org/entity/Q5",
			extraHeaders
		);
		assertNotSame(initialSize, rdf.size(store));
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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.addObjectProperty(new Store(),
				"https://example.org/subject",
				"https://example.org/predicate",
				"https://example.org/object"
			);
		});
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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.addObjectProperty(new Store(),
				"https://example.org/subject",
				"https://example.org/predicate",
				"https://example.org/object"
			);
		});
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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.addDataProperty(new Store(),
				"https://example.org/subject",
				"https://example.org/predicate",
				"Object"
			);
		});
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
		Assertions.assertThrows(Exception.class, () -> {
			rdf.addTypedDataProperty(new Store(),
				"https://example.org/subject",
				"https://example.org/predicate",
				"Object", "xsd:string"
			);
		});
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
		assertTrue(turtle.contains("prefix ex:"));
		assertTrue(turtle.contains("\"Object\""));
		assertFalse(turtle.contains("ex:Object"));
		assertTrue(turtle.contains("@en"));
		Assertions.assertThrows(Exception.class, () -> {
			rdf.addPropertyInLanguage(new Store(),
				"https://example.org/subject",
				"https://example.org/predicate",
				"Object", "en"
			);
		});
	}

	@Test
	public void testSPARQLLocal() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		rdf.addPropertyInLanguage(store,
			"https://example.org/subject",
			"https://example.org/predicate",
			"Object", "en"
		);
		StringMatrix results = rdf.sparql(store,
			"SELECT ?s WHERE { ?s <https://example.org/predicate> [] }"
		);
		assertNotNull(store);
		assertSame(1, results.getRowCount());
	}

	@Test
	public void testSPARQLRemote() throws Exception {
		StringMatrix results = rdf.sparqlRemote(
			"https://sparql.wikipathways.org/sparql",
			"SELECT ?node WHERE { ?node a <http://vocabularies.wikipathways.org/wp#DataNode> } LIMIT 1"
		);
		assertNotNull(results);
		assertSame(1, results.getRowCount());
	}

	@Test
	public void testSPARQLRemoteNoJena() throws Exception {
		String query = "SELECT ?node WHERE { ?node a <http://vocabularies.wikipathways.org/wp#DataNode> } LIMIT 1";
		byte[] queryResults = bioclipse.sparqlRemote(
			"https://sparql.wikipathways.org/sparql", query
		);
		IStringMatrix results = rdf.processSPARQLXML(queryResults, query);
		assertNotNull(results);
		assertSame(1, results.getRowCount());
	}

	@Test
	public void getForPredicate() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		String content =
			"@prefix ex:    <https://example.org/> .\n" +
			"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"ex:subject  ex:predicate  \"Object\"@en .\n";
		store = rdf.importFromString(store, content, "TURTLE");
		List<String> resources = rdf.getForPredicate(store, "https://example.org/subject", "https://example.org/predicate");
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	@Test
	public void allWolSameAs() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		String content =
			"@prefix ex:    <https://example.org/> .\n" +
			"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"ex:subject  owl:sameAs  ex:subject2 .\n";
		store = rdf.importFromString(store, content, "TURTLE");
		List<String> resources = rdf.allOwlSameAs(store, "https://example.org/subject");
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	@Test
	public void allOwlEquivalentClass() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		String content =
			"@prefix ex:    <https://example.org/> .\n" +
			"@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"ex:subject  owl:equivalentClass  ex:subject2 .\n";
		store = rdf.importFromString(store, content, "TURTLE");
		List<String> resources = rdf.allOwlEquivalentClass(store, "https://example.org/subject");
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	@Test
	public void validateShEx() throws Exception {
		IRDFStore store = rdf.createInMemoryStore(true);
		assertNotNull(store);
		store = rdf.importFile(store, "/RDFTests/exampleContent2.xml", "TURTLE");
		List<String> resources = new ArrayList<>();
		resources.add("https://example.org/subject");
		resources.add("https://example.org/bar");
		ShexReport report = rdf.validateAllOfType(store, "/RDFTests/exampleContent.shex", "https://example.org/shape", "https://example.org/type");
		assertNotNull(report);
		assertTrue(report.conforms());
		String reportStr = rdf.asString(report);
		assertTrue(reportStr.contains("Status = conformant"));
		report = rdf.validateAllOfType(store, "/RDFTests/exampleContent2.shex", "https://example.org/shape", "https://example.org/type");
		assertNotNull(report);
		assertFalse(report.conforms());
		reportStr = rdf.asString(report);
		assertTrue(reportStr.contains("Status = nonconformant"));
		Assertions.assertThrows(Exception.class, () -> {
			rdf.validateAllOfType(new Store(), "/RDFTests/exampleContent.shex", "https://example.org/shape", "https://example.org/type");
		});
	}

    @Test
    public void allClasses() throws Exception {
        IRDFStore store = rdf.createInMemoryStore(true);
        store = rdf.importFile(store, "/RDFTests/classes.ttl", "TURTLE");
        List<String> classes = rdf.allClasses(store);
        System.out.println(classes);
        assertNotNull(classes);
        assertNotSame(0, classes.size());
        for (String clazz : classes) {
            if (clazz.equals("https://example.org/ClassOne")) return;
        }
    }
}
