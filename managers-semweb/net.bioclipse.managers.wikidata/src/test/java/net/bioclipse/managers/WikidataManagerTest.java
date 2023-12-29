/* Copyright (c) 2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;
import net.bioclipse.wikidata.domain.WikidataMolecule;

public class WikidataManagerTest {

	static WikidataManager wikidata;
	static CDKManager cdk;
	static InChIManager inchi;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		wikidata = new WikidataManager(workspaceRoot);
		cdk = new CDKManager(workspaceRoot);
		inchi = new InChIManager(workspaceRoot);
	}

	@Test
	public void testHasMethane() throws Exception {
		IMolecule methane = cdk.fromSMILES("C");
		InChI inchiObj = inchi.generate(methane);
		assertTrue(wikidata.hasMolecule(inchiObj));
	}

	@Test
	public void testNull() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.hasMolecule(null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give an InChI"));
		exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityID((InChI)null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give an InChI"));
		exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getMolecule(null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give an InChI"));
	}

	@Test
	public void testGetEntityID() throws Exception {
		IMolecule methane = cdk.fromSMILES("C");
		InChI inchiObj = inchi.generate(methane);
		assertEquals("http://www.wikidata.org/entity/Q37129", wikidata.getEntityID(inchiObj));
	}

	@Test
	public void testGetEntityIDs() throws Exception {
		List<InChI> inchis = new ArrayList<>();
		inchis.add(inchi.generate(cdk.fromSMILES("C")));
		inchis.add(inchi.generate(cdk.fromSMILES("CCO")));
		Map<String,String> mappings = wikidata.getEntityIDs(inchis);
		assertNotNull(mappings);
		assertEquals(2, mappings.size());
	}

	@Test
	public void testGetEntityIDs_NotInWikidata() throws Exception {
		List<InChI> inchis = new ArrayList<>();
		inchis.add(inchi.generate(cdk.fromSMILES("CCCC[O-]")));
		Map<String,String> mappings = wikidata.getEntityIDs(inchis);
		assertNotNull(mappings);
		assertEquals(0, mappings.size());
	}

	@Test
	public void testGetEntityIDs_Null() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityIDs(null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give a list of InChIs"));
	}

	@Test
	public void testGetMolecule() throws Exception {
		IMolecule methane = cdk.fromSMILES("C");
		InChI inchiObj = inchi.generate(methane);
		IMolecule mol = wikidata.getMolecule(inchiObj);
		assertNotNull(mol);
		assertTrue(mol instanceof WikidataMolecule);
		assertEquals(0, cdk.totalFormalCharge(mol));
		ICDKMolecule cdkMol = ((WikidataMolecule)mol).asCDKMolecule();
		assertEquals("C", cdkMol.toSMILES());
		String cml = mol.toCML();
		assertNotNull(cml);
		assertTrue(cml.contains("\"C\""));
	}

	@Test
	public void testGetEntityIDsForType_Null() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityIDsForType(null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give a valid Wikidata identifier"));
	}

	@Test
	public void testGetEntityIDsForDOIs() throws Exception {
		Map<String,String> entityIDs = wikidata.getEntityIDsForDOIs(Arrays.asList(new String[] { "10.1186/S13321-023-00780-2" }));
		assertNotNull(entityIDs);
		assertEquals(1, entityIDs.size());
	}

	@Test
	public void testGetEntityIDsForDOIs_Null() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityIDsForDOIs(null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give a list of DOIs"));
	}

	@Test
	public void testGetEntityID_DOI() throws Exception {
		String entityID = wikidata.getEntityID("10.1186/S13321-023-00780-2");
		assertNotNull(entityID);
		assertTrue(entityID.startsWith("http://www.wikidata.org/entity/Q"));
	}

	@Test
	public void testGetEntityID_NonExistingDOI() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityID("99.1186/S13321-023-00780-2");
			}
		);
		assertTrue(exception.getMessage().contains("No work in Wikidata with the DOI"));
	}

	@Test
	public void testGetEntityIDs_DOI_Null() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityID((String)null);
			}
		);
		assertTrue(exception.getMessage().contains("You must give a DOI"));
	}

	@Test
	public void testNotInWikidata() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				IMolecule methane = cdk.fromSMILES("CCCC[O-]");
				InChI inchiObj = inchi.generate(methane);
				wikidata.getMolecule(inchiObj);
			}
		);
		assertTrue(exception.getMessage().contains("No molecule in Wikidata with the InChI"));
	}

	@Test
	public void testGetEntityIDsForType() throws Exception {
		List<String> entityIDs = wikidata.getEntityIDsForType("Q7316896");
		assertNotNull(entityIDs);
		assertTrue(entityIDs.size() > 0);
	}

	@Test
	public void testGetEntityIDsForType_InvalidType() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				wikidata.getEntityIDsForType("P31");
			}
		);
		assertTrue(exception.getMessage().contains("You must give a valid Wikidata identifier"));
	}

	@Test
	public void testIsValidQIdentifier() {
		assertTrue(wikidata.isValidQIdentifier("Q5"));
		assertTrue(wikidata.isValidQIdentifier("Q566666666"));
		assertFalse(wikidata.isValidQIdentifier("Q5x"));
		assertFalse(wikidata.isValidQIdentifier("Q"));
		assertFalse(wikidata.isValidQIdentifier("P5"));
		assertFalse(wikidata.isValidQIdentifier("Px"));
		assertFalse(wikidata.isValidQIdentifier("P"));
		assertFalse(wikidata.isValidQIdentifier(null));
	}

	@Test
	public void testDOIs() {
		List<String> dois = wikidata.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("wikidata", wikidata.getManagerName());
	}

}
