/* Copyright (c) 2022-2026  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import io.github.egonw.nanojava.data.MaterialType;
import net.bioclipse.core.business.BioclipseException;

public class MaterialTest {

	@Test
	public void testNewMaterial() throws BioclipseException {
		IMaterial material = new Material();
		assertNotNull(material);
		assertNull(material.getType());
	}

	@Test
	public void testNewMaterialWithType() throws BioclipseException {
		IMaterial material = new Material(MaterialType.METALOXIDE);
		assertNotNull(material);
		assertSame(MaterialType.METALOXIDE, material.getType());
	}

	@Test
	public void testNewMaterialWithMaterial() throws BioclipseException {
		IMaterial material = new Material(new Material(MaterialType.METALOXIDE).getInternalModel());
		assertNotNull(material);
		assertSame(MaterialType.METALOXIDE, material.getType());
	}

}
