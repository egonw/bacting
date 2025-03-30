/* Copyright (c) 2024  Egon Willighagen <egon.willighagen@gmail.com>
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class MwmblManagerTest {

	static MwmblManager mwmbl;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		mwmbl = new MwmblManager(workspaceRoot);
	}

	@Test
	@Tag("mwmbl")
	public void testSearch() throws BioclipseException {
		List<String> foundPages = mwmbl.search("acetic acid");
		assertNotNull(foundPages);
	}

	@Test
	@Tag("mwmbl")
	public void testDOIs() {
		List<String> dois = mwmbl.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	@Tag("mwmbl")
	public void testManagerName() {
		assertSame("mwmbl", mwmbl.getManagerName());
	}

}
