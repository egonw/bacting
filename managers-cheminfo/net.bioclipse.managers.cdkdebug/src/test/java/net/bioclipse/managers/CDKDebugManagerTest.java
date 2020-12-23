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

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.managers.cdkdebug.CDKDebugManager;

public class CDKDebugManagerTest {

	static CDKDebugManager cdkdebug;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("cdkdebugtestws").toString();
		cdkdebug = new CDKDebugManager(workspaceRoot);
	}

	@Test
	public void testDOIs() {
		List<String> dois = cdkdebug.doi();
		assertNotNull(dois);
		assertSame(3, dois.size());
	}
}
