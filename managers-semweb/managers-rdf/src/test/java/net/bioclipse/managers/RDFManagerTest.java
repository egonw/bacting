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

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.rdf.business.IRDFStore;

public class RDFManagerTest {

	static RDFManager rdf;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("rdftestws").toString();
		rdf = new RDFManager(workspaceRoot);
	}

	@Test
	public void testCreateInMemoryStore() {
		IRDFStore store = rdf.createInMemoryStore();
		assertNotNull(store);
	}

}
