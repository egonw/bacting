package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
	public void testloadMolecule() throws BioclipseException, IOException {
		assertNotNull(cdk);
		ICDKMolecule mol = cdk.loadMolecule(
			new ByteArrayInputStream("CCC".getBytes()),
			(IChemFormat)SMILESFormat.getInstance()
		);
		assertNotNull(mol);
		assertSame(3, mol.getAtomContainer().getAtomCount());
	}

}
