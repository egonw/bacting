/* Copyright (c) 2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.biojava.domain;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import net.bioclipse.core.domain.IDNA;
import net.bioclipse.core.domain.IProtein;

public class DomainObjectsTest {

	@Test
	public void testConstructors() {
		IDNA dna = new BiojavaDNA();
		assertSame("", dna.getName());
		IProtein protein = new BiojavaProtein();
		assertSame("", protein.getName());
	}
}