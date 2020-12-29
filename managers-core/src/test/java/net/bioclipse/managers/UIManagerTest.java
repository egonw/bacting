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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UIManagerTest {

	static UIManager ui;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws IOException {
		workspaceRoot = Files.createTempDirectory("uitestws").toString();
		System.out.println("tmpPath: " + workspaceRoot);
		ui = new UIManager(workspaceRoot);
		ui.newProject("/NewFiles/");
	}

	@Test
	public void testDOIs() {
		List<String> dois = ui.doi();
		assertNotNull(dois);
		assertNotEquals(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("ui", ui.getManagerName());
	}

	@Test
	public void testNewProject() throws IOException {
		String name = "/Download/";
		String path = ui.newProject(name);
		assertTrue(Files.isDirectory(Paths.get(workspaceRoot + path)));
	}

	@Test
	public void testNewFile() throws IOException {
		String newFile = ui.newFile("/NewFiles/test1.txt");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
	}

	@Test
	public void testRenewFile() throws IOException {
		// test it creates a file if not exists
		String newFile = ui.renewFile("/NewFiles/test2.txt");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		// and now the renew functionality
		newFile = ui.renewFile("/NewFiles/test2.txt");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
	}

	@Test
	public void testNewFile2() throws IOException {
        String newFile = ui.newFile(
		    "/NewFiles/test3.txt", "test content"
        );
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
	}

	@Test
	public void testAppend() throws IOException {
		String newFile = "/NewFiles/append.txt";
        ui.append(newFile, "test append content");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
	}

	@Test
	public void testRemove() throws IOException {
		// test it creates a file if not exists
		String newFile = ui.newFile("/NewFiles/toDelete.txt");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		// and now the remove() functionality
		ui.remove("/NewFiles/toDelete.txt");
		assertFalse(Files.exists(Paths.get(workspaceRoot + newFile)));
	}

}
