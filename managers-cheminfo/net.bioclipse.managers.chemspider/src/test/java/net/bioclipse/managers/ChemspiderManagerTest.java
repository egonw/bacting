package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class ChemspiderManagerTest {

	static ChemspiderManager chemspider;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("chemspidertestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		chemspider = new ChemspiderManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = chemspider.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("chemspider", chemspider.getManagerName());
	}

	@Test
	public void testResolve() throws IOException, BioclipseException, CoreException {
		List<Integer> hits = chemspider.resolve("QTBSBXVTEAMEQO-UHFFFAOYSA-N");
		assertNotNull(hits);
		assertNotEquals(0, hits.size());
	}

	@Test
	public void testDownloadAsString() throws IOException, BioclipseException, CoreException {
		String mol = chemspider.downloadAsString(171);
		assertTrue(mol.contains("<StdInChI>"));
	}

	@Test
	public void testDownload() throws IOException, BioclipseException, CoreException {
		IMolecule mol = chemspider.download(171);
		assertNotNull(mol);
	}
}
