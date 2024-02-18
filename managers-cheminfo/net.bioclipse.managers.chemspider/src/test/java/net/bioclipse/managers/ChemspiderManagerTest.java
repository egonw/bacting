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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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

	@BeforeEach
	public void slowDown() throws InterruptedException {
		// keep ChemSpider happy
		Thread.sleep(150);
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
	@Tag("chemspider")
	public void testResolve() throws IOException, BioclipseException, CoreException {
		List<Integer> hits = chemspider.resolve("QTBSBXVTEAMEQO-UHFFFAOYSA-N");
		assertNotNull(hits);
		assertNotEquals(0, hits.size());
	}

	@Test
	@Tag("chemspider")
	public void testDownloadAsString() throws IOException, BioclipseException, CoreException {
		String mol = chemspider.downloadAsString(171);
		assertTrue(mol.contains("<StdInChI>"));
	}

	@Test
	@Tag("chemspider")
	public void testDownload() throws IOException, BioclipseException, CoreException {
		IMolecule mol = chemspider.download(171);
		assertNotNull(mol);
	}

	@Test
	@Tag("chemspider")
	public void testLoadCompound() throws IOException, BioclipseException, CoreException {
		String results = chemspider.loadCompound(
			171,
			"/Download/cs171.mol"
		);
		assertTrue(results.equals("/Download/cs171.mol"));
		// should also test the file content
	}

}
