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

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class OpsinManagerTest {

	static OpsinManager opsin;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("opsintestws").toString();
		opsin = new OpsinManager(workspaceRoot);
	}

	@Test
	public void testParseIUPACName() throws Exception {
		IMolecule molecule = opsin.parseIUPACName("methane");
		assertNotNull(molecule);
	}

	@Test
	public void testParseIUPACNameAsCML() throws Exception {
		String cmlMolecule = opsin.parseIUPACNameAsCML("methane");
		assertNotNull(cmlMolecule);
		assertTrue(cmlMolecule.contains("cml"));
	}

	@Test
	public void testParseIUPACNameAsCML_Bad() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				opsin.parseIUPACNameAsCML("brexit");
			}
		);
		assertTrue(exception.getMessage().contains("Could not parse"));
	}

	@Test
	public void testParseIUPACName_Bad() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				opsin.parseIUPACName("brexit");
			}
		);
		assertTrue(exception.getMessage().contains("Could not parse"));
	}

	@Test
	public void testParseIUPACNameAsTokens_Bad() throws Exception {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				opsin.parseIUPACNameAsTokens("10,10-bis(4-pyridinylmethyl)-9(10H)-anthracenone dihydrochloride");
			}
		);
		assertTrue(exception.getMessage().contains("Could not interpret"));
	}

	@Test
	public void testParseIUPACNameAsTokens() throws Exception {
		List<String> tokens = opsin.parseIUPACNameAsTokens("2-methylmethane");
		assertNotSame(0, tokens.size());
	}

	@Test
	public void testCreateVariations() throws Exception {
		List<List<String>> variations = new ArrayList<>();
		List<String> alkanes = new ArrayList<>();
		alkanes.add("meth");
		alkanes.add("eth");
		alkanes.add("prop");
		variations.add(alkanes);

		List<String> newNames = opsin.createVariations("2-methylpropane", variations, false);
		assertSame(9, newNames.size());
	}

	@Test
	public void testParseIUPACNameAsTokens_VeryLongInput() throws Exception {
		List<List<String>> variations = new ArrayList<>();
		variations.add( Arrays.asList("meth", "eth", "prop", "but", "pent") );
		variations.add( Arrays.asList("(R,S)-", "(S,R)-", "(R,R)-", "(S,S)-") );
		variations.add( Arrays.asList("ane", "ene") );
		variations.add( Arrays.asList("iodide", "bromide", "fluoride", "chloride") );
		variations.add( Arrays.asList("hydroxy", "methoxy", "ethoxy") );

		String input = "1-[(2R)-2-[(3aR,5R,6S,6aR)-2,2-Dimethyl-6-(prop-2-en-1-yloxy)-tetrahydro-2H-furo[2,3-d][1,3]dioxol-5-yl]-2-(methoxymethoxy)ethyl]-4-({[(3aR,5R,6S,6aR)-5-[(1R)-2-[(tert-butyldimethylsilyl)oxy]-1-(prop-2-en-1-yloxy)ethyl]-2,2-dimethyl-tetrahydro-2H-furo[2,3-d][1,3]dioxol-6-yl]oxy}methyl)-1H-1,2,3-triazole"; 
		List<String> names = opsin.createVariations(input, variations, false);

		assertTrue(names.size() > 5000); // 5000 is set as the max; but this also tests it completes
		assertEquals( // make sure the full token list is used
			names.get(1003).substring(names.get(1003).length() - 7),
			input.substring(input.length() - 7)
		);
	}
	
	@Test
	public void testCreateVariations2() throws Exception {
		List<List<String>> variations = new ArrayList<>();
		List<String> alkanes = new ArrayList<>();
		alkanes.add("meth");
		alkanes.add("eth");
		alkanes.add("prop");
		variations.add(alkanes);

		List<String> newNames = opsin.createVariations("2-propanol", variations, true);
		assertSame(2, newNames.size());
	}

	@Test
	public void testCountPotentialVariations() throws Exception {
		List<List<String>> variations = new ArrayList<>();
		List<String> alkanes = new ArrayList<>();
		alkanes.add("meth");
		alkanes.add("eth");
		alkanes.add("prop");
		variations.add(alkanes);

		int count = opsin.countPotentialVariations("2-methylpropane", variations);
		assertEquals(9, count);
	}

	@Test
	public void testCountPotentialVariations2() throws Exception {
		List<List<String>> variations = new ArrayList<>();
		variations.add( Arrays.asList("meth", "eth", "prop", "but", "pent") );
		variations.add( Arrays.asList("(R,S)-", "(S,R)-", "(R,R)-", "(S,S)-") );
		variations.add( Arrays.asList("ane", "ene") );
		variations.add( Arrays.asList("iodide", "bromide", "fluoride", "chloride") );
		variations.add( Arrays.asList("hydroxy", "methoxy", "ethoxy") );

		String input = "1-[(2R)-2-[(3aR,5R,6S,6aR)-2,2-Dimethyl-6-(prop-2-en-1-yloxy)-tetrahydro-2H-furo[2,3-d][1,3]dioxol-5-yl]-2-(methoxymethoxy)ethyl]-4-({[(3aR,5R,6S,6aR)-5-[(1R)-2-[(tert-butyldimethylsilyl)oxy]-1-(prop-2-en-1-yloxy)ethyl]-2,2-dimethyl-tetrahydro-2H-furo[2,3-d][1,3]dioxol-6-yl]oxy}methyl)-1H-1,2,3-triazole";

		int count = opsin.countPotentialVariations(input, variations);
		assertEquals(1953125, count);
	}

	@Test
	public void testDOIs() {
		List<String> dois = opsin.doi();
		assertNotNull(dois);
		assertSame(1, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("opsin", opsin.getManagerName());
	}

}
