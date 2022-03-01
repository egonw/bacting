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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

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
        ui.append(newFile, " and more content");
	}

	@Test
	public void testAppendToNotExists() throws IOException {
		Assertions.assertThrows(Exception.class, () -> {
			String newFile = "/DoesNotExist/append.txt";
	        ui.append(newFile, "test append content");
		});
	}

	@Test
	public void testAppendInputStream() throws IOException {
		String newFile = "/NewFiles/append_bytes.txt";
        ui.append(newFile, new ByteArrayInputStream("test append content".getBytes()));
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		ui.append(newFile, new ByteArrayInputStream(" and more content".getBytes()));
	}

	@Test
	public void testAppendInputStreamToNotExists() throws IOException {
		Assertions.assertThrows(Exception.class, () -> {
			String newFile = "/DoesNotExist/append2.txt";
	        ui.append(newFile, new ByteArrayInputStream("test append content".getBytes()));
			assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		});
	}

	@Test
	public void testOpen() throws IOException {
		ui.open("It's not supported and nothing will happen.");;
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

	@Test
	public void readFile() throws Exception {
		String newFile = ui.newFile("/NewFiles/toRead.txt");
		ui.append(newFile, "test append content\nfoo");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		String content = ui.readFile("/NewFiles/toRead.txt");
		assertTrue(content.contains("test append"));
	}

	@Test
	public void readFileIntoArray() throws Exception {
		String newFile = ui.newFile("/NewFiles/toReadArray.txt");
		ui.append(newFile, "test append content\nfoo");
		assertTrue(Files.exists(Paths.get(workspaceRoot + newFile)));
		String[] content = ui.readFileIntoArray("/NewFiles/toReadArray.txt");
		assertEquals(2, content.length);
		assertTrue(content[0].contains("test append"));
		assertTrue(content[1].equals("foo"));
	}

	@Test
	public void readFile_Nonexisting() {
		BioclipseException exception = Assertions.assertThrows(BioclipseException.class, () -> {
			ui.readFile("DoesNotExist/append2.txt");
		});
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("does not exit"));
	}

	@Test
	public void readFileIntoArray_readFile_Nonexisting() {
		BioclipseException exception = Assertions.assertThrows(BioclipseException.class, () -> {
			ui.readFileIntoArray("DoesNotExist/append2.txt");
		});
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("does not exit"));
	}
}
