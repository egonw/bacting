/* Copyright (c) 2020-2022  Egon Willighagen <egon.willighagen@gmail.com>
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
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.egonw.nanojava.data.MaterialType;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.domain.IMaterial;

public class NanoManagerTest {

	static NanoManager nm;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("nmtestws").toString();
		nm = new NanoManager(workspaceRoot);
	}

	@Test
	public void testNewMaterial() throws BioclipseException {
		IMaterial material = nm.newMaterial();
		Assert.assertNotNull(material);
		assertNull(material.getType());
	}

	@Test
	public void testNewMaterialWithType() throws BioclipseException {
		IMaterial material = nm.newMaterial("metal oxide");
		Assert.assertNotNull(material);
		assertSame(MaterialType.METALOXIDE, material.getType());
	}

	@Test
	public void testNewMaterialWithNullType() throws BioclipseException {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			nm.newMaterial(null);
		});
		assertTrue(exception.getMessage().contains("Unknown material type"));
	}

	@Test
	public void testNewMaterialWithUnknownType() throws BioclipseException {
		Exception exception = assertThrows(BioclipseException.class, () ->
		{
			nm.newMaterial("metal sulfide");
		});
		assertTrue(exception.getMessage().contains("Unknown material type"));
	}

	@Test
	public void testNewMaterialWithComposition() throws BioclipseException {
		IMaterial material = nm.newMaterial("metal oxide", "TiO2");
		Assert.assertNotNull(material);
		assertSame(MaterialType.METALOXIDE, material.getType());
	}
	
	@Test
	public void testListMaterialTypes() throws Exception {
		Set<String> types = nm.listMaterialTypes();
		Assert.assertNotNull(types);
		assertNotSame(0, types.size());
	}
	
	@Test
	public void testDOIs() {
		List<String> dois = nm.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("nm", nm.getManagerName());
	}

}
