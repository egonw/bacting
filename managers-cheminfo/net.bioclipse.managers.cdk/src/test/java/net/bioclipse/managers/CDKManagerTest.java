package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.SMILESFormat;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

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
	public void testFromCml() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromCml("<molecule/>");
		assertNotNull(mol);
		assertSame(0, mol.getAtomContainer().getAtomCount());
	}

	@Test
	public void testAsCDKMolecule() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromCml("<molecule/>");
		ICDKMolecule mol2 = cdk.asCDKMolecule(mol); 
		assertNotNull(mol2);
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
	}

	@Test
	public void testGetAtomsWithUndefinedStereo() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		Set<IAtom> set = cdk.getAtomsWithUndefinedStereo(mol);
		assertNotNull(set);
		assertSame(0, set.size());
	}

	@Test
	public void testGetAtomsWithDefinedStereo() throws BioclipseException, IOException {
		ICDKMolecule mol = cdk.fromSMILES("CCC");
		Set<IAtom> set = cdk.getAtomsWithDefinedStereo(mol);
		assertNotNull(set);
		assertSame(0, set.size());
	}

}
