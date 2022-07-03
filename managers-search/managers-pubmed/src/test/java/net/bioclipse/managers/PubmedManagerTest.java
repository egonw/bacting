/* Copyright (c) 2022  Egon Willighagen <egon.willighagen@gmail.com>
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class PubmedManagerTest {

	static PubmedManager pubmed;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		pubmed = new PubmedManager(workspaceRoot);
	}

	@Test
	public void testFetch() throws BioclipseException {
		JSONObject json = pubmed.fetchInfo("25706687");
		assertNotNull(json);
		assertEquals("PubMed", json.getString("source"));
		assertEquals("PathVisio 3: an extendable pathway analysis toolbox", json.getString("title"));
		assertEquals(2015, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(0)); // year
		assertEquals(2, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(1));    // month
		assertEquals(23, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(2));   // day
	}
	
	@Test
	public void testInvalidID() throws BioclipseException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{ pubmed.fetchInfo("a"); }
		);
		assertTrue(exception.getMessage().contains("integer"));
	}

	@Test
	public void testDOIs() {
		List<String> dois = pubmed.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("pubmed", pubmed.getManagerName());
	}

}
