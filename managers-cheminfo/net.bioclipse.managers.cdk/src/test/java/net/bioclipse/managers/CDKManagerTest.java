/* Copyright (c) 2008       The Bioclipse Project and others
 *               2020-2021  Egon Willighagen <egon.willighagen@gmail.com>
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
	static UIManager ui;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("cdktestws").toString();
		cdk = new CDKManager(tmpPath);
		ui = new UIManager(tmpPath);
		ui.newProject("/CDKTests/");
		ui.newFile("/testFiles/cs2a.cml",
		      " <cml:molecule xmlns:cml=\"http://www.xml-cml.org/schema/cml2/core\">\n"
			+ " <!--\n"
			+ " <cml:metadataList title=\"generated automatically from Openbabel\">\n"
			+ "  <cml:metadata name=\"dc:creator\" content=\"OpenBabel version 1-100.1\" /> \n"
			+ "  <cml:metadata name=\"dc:description\" content=\"Conversion of legacy filetype to CML\" /> \n"
			+ "  <cml:metadata name=\"dc:identifier\" content=\"Unknown\" /> \n"
			+ "  <cml:metadata name=\"dc:content\" /> \n"
			+ "  <cml:metadata name=\"dc:rights\" content=\"unknown\" /> \n"
			+ "  <cml:metadata name=\"dc:type\" content=\"chemistry\" /> \n"
			+ "  <cml:metadata name=\"dc:contributor\" content=\"unknown\" /> \n"
			+ "  <cml:metadata name=\"dc:creator\" content=\"Openbabel V1-100.1\" /> \n"
			+ "  <cml:metadata name=\"dc:date\" content=\"Sun Aug 03 20:23:51 BST 2003\" /> \n"
			+ "  <cml:metadata name=\"cmlm:structure\" content=\"yes\" /> \n"
			+ "  </cml:metadataList>\n"
			+ "-->  \n"
			+ "  <cml:atomArray atomID=\"a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 a17 a18 a19 a20 a21 a22 a23 a24 a25 a26 a27 a28 a29 a30 a31 a32 a33 a34 a35 a36 a37 a38\" elementType=\"Cr Fe C N C C N C C C C C C C C C C C C O C O C O C O H H H H H H H H H H H H\" formalCharge=\"0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\" x3=\"2.787500 -2.808500 1.471000 0.572300 -0.701900 1.325500 0.360200 -0.925700 -1.835800 -3.225200 -3.992300 -3.083400 -1.753600 -2.071600 -3.418000 -4.255300 -3.432900 -2.087000 3.408100 3.687100 4.147300 5.032900 2.133100 1.743500 3.669700 4.262400 -0.596100 -0.932000 -0.886200 -1.076000 -3.615300 -5.068900 -3.350900 -0.844500 -3.746100 -5.325300 -3.770200 -1.235600\" y3=\"0.134500 -0.057200 -0.999700 -1.575100 -2.105700 1.295300 1.844300 2.352000 -1.800100 -1.798500 -1.782200 -1.755900 -1.763500 1.830700 1.643800 1.452900 1.507200 1.736400 1.723100 2.824300 -0.935800 -1.633400 -0.525300 -1.097200 -0.136800 -0.462600 -3.195200 -1.715300 3.447600 2.087800 -1.886500 -1.813000 -1.755400 -1.801100 1.726700 1.315800 1.407400 1.878200\" z3=\"0.096600 0.115500 -0.661300 -1.173900 -1.589200 -0.504200 -0.901900 -1.318100 -0.620800 -0.978800 0.226800 1.328500 0.809600 -0.466600 -0.931500 0.211100 1.377700 0.964300 0.647200 0.963000 0.631900 0.980400 1.629400 2.588200 -1.447900 -2.414900 -1.682800 -2.588400 -1.253300 -2.372000 -1.981400 0.290500 2.373900 1.390600 -1.956400 0.193300 2.397800 1.612300\" /> \n"
			+ "  <cml:bondArray atomRefs1=\"a1 a1 a1 a1 a1 a1 a2 a2 a2 a2 a2 a2 a2 a2 a2 a2 a3 a4 a5 a5 a5 a6 a7 a8 a8 a8 a9 a9 a10 a10 a11 a11 a12 a12 a13 a14 a14 a15 a15 a16 a16 a17 a17 a18 a19 a21 a23 a25\" atomRefs2=\"a3 a6 a19 a21 a23 a25 a9 a10 a11 a12 a13 a14 a15 a16 a17 a18 a4 a5 a9 a27 a28 a7 a8 a14 a29 a30 a10 a13 a11 a31 a12 a32 a13 a33 a34 a15 a18 a16 a35 a17 a36 a18 a37 a38 a20 a22 a24 a26\" order=\"1 1 2 2 2 2 1 1 1 1 1 1 1 1 1 1 2 1 1 1 1 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 3 3 3 3\" /> \n"
			+ "  </cml:molecule>\n"
			+ "");
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
	public void testloadMolecule_Format() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.loadMolecule(
			new ByteArrayInputStream("CCC".getBytes()),
			(IChemFormat)SMILESFormat.getInstance()
		);
		assertNotNull(mol);
		assertSame(3, mol.getAtomContainer().getAtomCount());
	}

	@Test
	public void testloadMolecule_FakeFormat() throws BioclipseException, IOException {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			cdk.loadMolecule(
				new ByteArrayInputStream("CCC".getBytes()),
				new IChemFormat() {
					public boolean isXMLBased() {return false;}
					public String getPreferredNameExtension() {return null;}
					public String[] getNameExtensions() {return null;}
					public String getMIMEType() {return null;}
					public String getFormatName() {return null;}
					public String getWriterClassName() {return null;}
					public int getSupportedDataFeatures() {return 0;}
					public int getRequiredDataFeatures() {return 0;}
					public String getReaderClassName() {return null;}
				}
			);
		});
		assertTrue(exception.getMessage().contains("Could not create reader in CDK"));
	}

	@Test
	public void testloadMolecule_NullFormat() throws BioclipseException, IOException {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			cdk.loadMolecule(
				new ByteArrayInputStream("CCC".getBytes()), null
			);
		});
		assertTrue(exception.getMessage().contains("Unsupported file format in CDK"));
	}

	@Test
	public void testloadMolecule() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.loadMolecule("/testFiles/cs2a.cml");
		assertNotNull(mol);
		assertSame(38, mol.getAtomContainer().getAtomCount());
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
	public void testCalculateSMILES() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		assertNotNull(mol);
		String smiles = cdk.calculateSMILES(mol);
		assertTrue("CCC".equals(smiles));
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
	public void testDetermineFormat_String() throws BioclipseException, IOException {
		IChemFormat format = cdk.determineIChemFormatOfString("<molecule/>");
		assertNotNull(format);
		assertSame("Chemical Markup Language", format.getFormatName());
	}

	@Test
	public void testDetermineFormat() throws BioclipseException, IOException {
		IChemFormat format = cdk.determineIChemFormat("/testFiles/cs2a.cml");
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

    @Test
    public void testGetFormats() {
        String formats = cdk.getFormats();
        assertTrue(formats.contains("Mol2Format"));
        assertTrue(formats.contains("CMLFormat"));
        assertTrue(formats.contains("MDLV2000Format"));
        assertTrue(formats.contains("SDFFormat"));
    }

    @Test
    public void testCalculateMass() throws Exception {
    	ICDKMolecule mol = cdk.fromSMILES("NC(=O)NO");
        double mass = cdk.calculateMass(mol);
        assertEquals(76.05474, mass, 0.001, "Unexpected mass");
    }

    @Test
    public void testCalculateMajorIsotopeMass() throws Exception {
    	ICDKMolecule mol = cdk.fromSMILES("NC(=O)NO");
        double mass = cdk.calculateMajorIsotopeMass(mol);
        assertEquals(76.02728, mass, 0.0001, "Unexpected mass");
    }

    @Test
    public void testTotalFormalCharge() throws Exception {
        ICDKMolecule mol = cdk.fromSMILES("O=C(CC)[O-].[Na+]");
        assertEquals(0, cdk.totalFormalCharge(mol));

        mol = cdk.fromSMILES("O=C(CC)[O-]");
        assertEquals(-1, cdk.totalFormalCharge(mol));

        mol = cdk.fromSMILES("O=C(CC(=O)[O-])[O-]");
        assertEquals(-2, cdk.totalFormalCharge(mol));

        SMILESMolecule smilesMol = new SMILESMolecule("CC[O-]");
        assertEquals(-1, cdk.totalFormalCharge(smilesMol));
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
