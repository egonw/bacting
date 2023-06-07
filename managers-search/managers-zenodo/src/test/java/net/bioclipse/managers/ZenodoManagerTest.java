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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class ZenodoManagerTest {

	static ZenodoManager zenodo;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		zenodo = new ZenodoManager(workspaceRoot);
	}

	@Test
	public void testGetOAIPMHData() throws Exception {
		String oaipmhXML = zenodo.getOAIPMHData("10.5281/zenodo.7990214");
		assertNotNull(oaipmhXML);
	}

	@Test
	public void testGetOAIPMHData_InvalidDOI() throws Exception {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			zenodo.getOAIPMHData("10.0000/something.7990214");
		});
		assertTrue(exception.getMessage().contains("10.5281/zenodo"));
		assertTrue(exception.getMessage().contains("Zenodo DOI and does not start with"));
	}

	@Test
	public void testDOIs() {
		List<String> dois = zenodo.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("zenodo", zenodo.getManagerName());
	}

}
