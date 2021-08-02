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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.formats.CDKSourceCodeFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.SMILESFormat;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class CDKManagerTest {

	static CDKManager cdk;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("cdktestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		cdk = new CDKManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = cdk.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("cdk", cdk.getManagerName());
	}

	@Test
	public void testloadMolecule() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.loadMolecule(
			new ByteArrayInputStream("CCC".getBytes()),
			(IChemFormat)SMILESFormat.getInstance()
		);
		assertNotNull(mol);
		assertSame(3, mol.getAtomContainer().getAtomCount());
	}

	@Test
	public void testloadMolecule_UnsupportedFormat() throws BioclipseException, IOException {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			cdk.loadMolecule(
				new ByteArrayInputStream("CCC".getBytes()),
				(IChemFormat)CDKSourceCodeFormat.getInstance()
			);
		});
		assertTrue(exception.getMessage().contains("Could not create reader in CDK."));
	}

	@Test
	public void testFromCml() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromCml("<molecule/>");
		assertNotNull(mol);
		assertSame(0, mol.getAtomContainer().getAtomCount());
	}

	@Test
	public void testFromCml_Null() {
		Exception exception = assertThrows(
			IllegalArgumentException.class, () ->
			{
				cdk.fromCml(null);
			}
		);
		assertTrue(exception.getMessage().contains("cannot be null"));
	}

	@Test
	public void testAsCDKMolecule() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromCml("<molecule><atomArray><atom elementType=\"C\" /></atomArray></molecule>");
		ICDKMolecule mol2 = cdk.asCDKMolecule(mol); 
		assertNotNull(mol2);
		assertEquals(1, mol2.getAtomContainer().getAtomCount());
	}

	@Test
	public void testAsCDKMolecule_CML() throws BioclipseException, IOException {
		IMolecule mol = new CMLMolecule("<molecule><atomArray><atom elementType=\"C\" /></atomArray></molecule>");
		ICDKMolecule mol2 = cdk.asCDKMolecule(mol);
		assertNotNull(mol2);
		assertEquals(1, mol2.getAtomContainer().getAtomCount());
	}

	@Test
	public void testAsCDKMolecule_Mock() throws BioclipseException, IOException {
		IMolecule smiMol = new SMILESMolecule("CCCO");
		ICDKMolecule mol2 = cdk.asCDKMolecule(smiMol); 
		assertNotNull(mol2);
		assertEquals(4, mol2.getAtomContainer().getAtomCount());
	}

	@Test
	public void testCreateMoleculeList() throws BioclipseException, IOException {
		List<ICDKMolecule> list= cdk.createMoleculeList();
		assertNotNull(list);
	}

	@Test
	public void testFromSMILES() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		assertNotNull(mol);
		assertSame(3, mol.getAtomContainer().getAtomCount());
	}

	@Test
	public void testPartition() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("O=C[O-].[Na+]");
		assertNotNull(mol);
		assertSame(2, cdk.partition(mol).size());
		mol = cdk.fromSMILES("O=C[O-]");
		assertNotNull(mol);
		assertSame(1, cdk.partition(mol).size());
	}

	@Test
	public void testFromSMILES_Bad() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				cdk.fromSMILES("ANY");
			}
		);
		assertTrue(exception.getMessage().contains("invalid"));
	}

	@Test
	public void testDetermineFormat() throws BioclipseException, IOException {
		IChemFormat format = cdk.determineIChemFormatOfString("<molecule/>");
		assertNotNull(format);
		assertSame("Chemical Markup Language", format.getFormatName());
	}

	@Test
	public void testFromString() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromString("<molecule/>");
		assertNotNull(mol);
	}

	@Test
	public void testFromString_Null() throws BioclipseException, IOException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				cdk.fromString(null);
			}
		);
		assertTrue(exception.getMessage().contains("cannot be null"));
	}

	@Test
	public void testFromString_Empty() throws BioclipseException, IOException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				cdk.fromString("");
			}
		);
		assertTrue(exception.getMessage().contains("cannot be empty"));
	}

	@Test
	public void testFromString_UnknownFormat() throws BioclipseException, IOException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				cdk.fromString("element:C coordXYZ:0,0,0");
			}
		);
		assertTrue(exception.getMessage().contains("Could not identify format"));
	}

	@Test
	public void testMolecularFormulaObject() throws BioclipseException, IOException {
		IMolecularFormula mf = cdk.molecularFormulaObject(
			cdk.fromSMILES("COC")
		);
		assertNotNull(mf);
		assertSame(0, mf.getCharge());
	}

	@Test
	public void testMolecularFormula() throws BioclipseException, IOException {
		String mf = cdk.molecularFormula(cdk.fromSMILES("COC"));
		assertTrue("C2H6O".equals(mf));
	}

	@Test
	public void testAppendToSDF() throws BioclipseException, IOException {
		cdk.appendToSDF(
			"/appendTest.sdf",
			cdk.fromSMILES("COC")
		);
		cdk.appendToSDF(
			"/appendTest.sdf",
			cdk.fromSMILES("COCC")
		);
	}

	@Test
	public void testGetAtomsWithUndefinedStereo() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		Set<IAtom> set = cdk.getAtomsWithUndefinedStereo(mol);
		assertNotNull(set);
		assertSame(0, set.size());
		mol = cdk.fromSMILES("ClC(Br)(F)I");
		set = cdk.getAtomsWithUndefinedStereo(mol);
		assertNotNull(set);
		assertSame(1, set.size());
	}

	@Test
	public void testGetAtomsWithDefinedStereo() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		Set<IAtom> set = cdk.getAtomsWithDefinedStereo(mol);
		assertNotNull(set);
		assertSame(0, set.size());
		mol = cdk.fromSMILES("Cl[C@](Br)(F)I");
		set = cdk.getAtomsWithDefinedStereo(mol);
		assertNotNull(set);
		assertSame(1, set.size());
	}

	@Test
	public void testAsSVG() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("C[C@](O)CC");
		String svg = cdk.asSVG(mol);
		String lines[] = svg.split(System.getProperty("line.separator"),3);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>", lines[0]);
        assertEquals("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">", lines[1]);
	}

	@Test
	public void testIsValidCAS() throws BioclipseException, IOException {
		assertTrue(cdk.isValidCAS("50-00-0"));
		assertFalse(cdk.isValidCAS("50-00"));
	}

	@Test
	public void testGetFormat() {
		IChemFormat format = cdk.getFormat( "PubChemCompoundXMLFormat" );
		assertNotNull(format);
		format = cdk.getFormat( "ChemicalFooFormat" );
		assertNull(format);
	}

	class SMILESMolecule implements IMolecule {

		private String smiles;

		SMILESMolecule(String smiles) { this.smiles = smiles; }

		@Override
		public IResource getResource() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public void setResource(IResource resource) {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public String getUID() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public List<IMolecule> getConformers() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public String toSMILES() throws BioclipseException {
			return this.smiles;
		}

		@Override
		public String toCML() throws BioclipseException {
			throw new UnsupportedOperationException("not support");
		}
		
	}
	
	class CMLMolecule implements IMolecule {

		private String cml;

		CMLMolecule(String cml) { this.cml = cml; }

		@Override
		public IResource getResource() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public void setResource(IResource resource) {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public String getUID() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public List<IMolecule> getConformers() {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public String toSMILES() throws BioclipseException {
			throw new UnsupportedOperationException("not support");
		}

		@Override
		public String toCML() throws BioclipseException {
			return this.cml;
		}

	}

}
