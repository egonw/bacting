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

import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class DOIManagerTest {

	static DOIManager doi;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		doi = new DOIManager(workspaceRoot);
	}

	@Test
	public void testFetch() throws BioclipseException {
		JSONObject json = doi.fetchInfo("10.1371/journal.pcbi.1004085");
		assertNotNull(json);
		assertEquals("Crossref", json.getString("source"));
		assertEquals("PathVisio 3: An Extendable Pathway Analysis Toolbox", json.getString("title"));
		assertEquals(2015, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(0)); // year
		assertEquals(2, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(1));    // month
		assertEquals(23, json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0).getInt(2));   // day
	}
	
	@Test
	public void testDOIs() {
		List<String> dois = doi.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("doi", doi.getManagerName());
	}

}
