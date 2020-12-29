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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.domain.IDNA;
import net.bioclipse.core.domain.IProtein;

public class BiojavaManagerTest {

	static BiojavaManager biojava;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("bridgedbtestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		biojava = new BiojavaManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = biojava.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testDNAfromPlainSequence() {
		IDNA dna = biojava.DNAfromPlainSequence("CAT");
		assertNotNull(dna);
		assertNotNull(dna.toString());
		assertNotNull(dna.getPlainSequence());
		dna = biojava.DNAfromPlainSequence("CAT", "foo");
		assertSame("foo", dna.getName());
	}

	@Test
	public void testDNAfromPlainSequence_Bad() {
		Exception exception = assertThrows(
			IllegalArgumentException.class, () ->
			{
				biojava.DNAfromPlainSequence("NOT");
			}
		);
		assertNotNull(exception);
	}

	@Test
	public void testProteinfromPlainSequence() {
		IProtein protein = biojava.proteinFromPlainSequence("PLINT");
		assertNotNull(protein);
		assertNotNull(protein.toString());
		assertNotNull(protein.getPlainSequence());
		assertNotNull(protein.getName());
	}

}
