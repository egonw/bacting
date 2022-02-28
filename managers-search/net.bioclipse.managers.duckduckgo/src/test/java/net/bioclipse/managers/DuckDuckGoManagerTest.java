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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class DuckDuckGoManagerTest {

	static DuckDuckGoManager duckduckgo;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		duckduckgo = new DuckDuckGoManager(workspaceRoot);
	}

	@Test
	public void testSearch() throws BioclipseException {
		List<String> foundPages = duckduckgo.search("\"ERM00000001\"");
		assertNotNull(foundPages);
	}

	@Test
	public void testDOIs() {
		List<String> dois = duckduckgo.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("duckduckgo", duckduckgo.getManagerName());
	}

}
