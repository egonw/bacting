/* Copyright (c) 2020-2021  Egon Willighagen <egon.willighagen@gmail.com>
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

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.StringMatrix;

public class ExcelManagerTest {

	static ExcelManager excel;
	static UIManager ui;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		workspaceRoot = Files.createTempDirectory("exceltestws").toString();
		excel = new ExcelManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		InputStream stream = ExcelManagerTest.class.getClassLoader().getResourceAsStream("testSpreadsheet.xlsx");
		ui.newProject("/NewFiles/");
		String newFile = ui.newFile("/NewFiles/testSpreadsheet.xlsx");
		ui.append(newFile, stream);
	}

	@Test
	public void testDOIs() {
		List<String> dois = excel.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testGetSheet() throws BioclipseException {
		StringMatrix sheet = excel.getSheet("/NewFiles/testSpreadsheet.xlsx", 0);
		assertNotNull(sheet);
	}

	@Test
	public void testGetSheet_NotFound() throws BioclipseException {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{ excel.getSheet("/NewFiles/doesnotexist.xlsx", 0); }
		);
		assertTrue(exception.getMessage().contains("Could not open file"));
	}

	@Test
	public void testManagerName() {
		assertSame("excel", excel.getManagerName());
	}

}
