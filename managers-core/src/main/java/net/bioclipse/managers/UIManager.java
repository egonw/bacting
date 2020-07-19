/* Copyright (c) 2008-2009 The Bioclipse Project and others.
 *               2018-2020 Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contributors:
 *     Jonathan Alvarsson
 *     Carl Masak
 *     Christian Hofbauer
 */
package net.bioclipse.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;

/**
 * Bioclipse manager providing core functionality, focusing on
 * the Bioclipse workspace and user interface interaction.
 */
public class UIManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link UIManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public UIManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	/**
	 * Determines if the given file exists in the Bioclipse workspace.
	 *
	 * @param file the location of the file in the Bioclipse workspace
	 * @return     true, if the file exists
	 */
	public boolean fileExists(String file) {
        return new File(workspaceRoot + file).exists();
    }

	/**
	 * Creates a new file in the Bioclipse workspace.
	 *
	 * @param path the location of the file in the Bioclipse workspace
	 * @return     the location of the file in the Bioclipse workspace
	 * @throws IOException
	 */
	public String newFile(String path) throws IOException {
    	return newFile(path, "");
    }

	/**
	 * Creates a new empty file in the Bioclipse workspace or empties the
	 * existing file.
	 *
	 * @param path the location of the file in the Bioclipse workspace
	 * @return     the location of the file in the Bioclipse workspace
	 * @throws IOException
	 */
	public String renewFile(String file) throws IOException {
		if (fileExists(file)) remove(file);
		newFile(file);
		return file;
	}

	/**
	 * Creates a new file in the Bioclipse workspace with the given content
	 *
	 * @param path    the location of the file in the Bioclipse workspace
	 * @param content the content of the new file
	 * @return        the location of the file in the Bioclipse workspace
	 * @throws IOException
	 */
    public String newFile(String path, String content) throws IOException {
    	File file = new File(workspaceRoot + path);
    	file.getParentFile().mkdirs(); 
    	file.createNewFile();
    	PrintWriter out = new PrintWriter(new FileWriter(file));
    	out.print(content);
    	out.close();
        return file.getPath().replace(workspaceRoot, "");
    }

	/**
	 * Deletes the file in the Bioclipse workspace.
	 *
	 * @param path the location of the file in the Bioclipse workspace
	 * @throws IOException
	 */
    public void remove(String path) {
    	File file = new File(workspaceRoot + path);
    	file.delete();
    }

	/**
	 * Appends the new content to write to a new or existing file in the Bioclipse workspace.
	 *
	 * @param path    the location of the file in the Bioclipse workspace
	 * @param toWrite the content to append to the new file
	 * @throws IOException
	 */
    public void append(String path, String toWrite) {
    	try {
    		if (fileExists(path)) {
    			Files.write(Paths.get(workspaceRoot + path), toWrite.getBytes(), StandardOpenOption.APPEND);
    		} else {
    			Files.write(Paths.get(workspaceRoot + path), toWrite.getBytes(), StandardOpenOption.CREATE);
    		}
    	} catch (Exception exception) {
    		throw new RuntimeException(
    			"Error while appending to File", exception
    		);
    	}
    }

    /**
     * Not currently implemented.
     *
     * @param object
     */
    public void open(final Object object) {
    	System.out.println("Cannot open file on the command line");
    }

	/**
	 * Creates a new project in the Bioclipse workspace.
	 *
	 * @param path the location of the project in the Bioclipse workspace
	 * @return     the location of the project in the Bioclipse workspace
	 * @throws IOException
	 */
    public String newProject(String name) throws IOException {
        if (fileExists(name)) return name;
        Files.createDirectory(Paths.get(workspaceRoot + name));
        return name;
    }

	@Override
	public String getManagerName() {
		return "ui";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1186/1471-2105-8-59");
		dois.add("10.1186/1471-2105-10-397");
		return dois;
	}

}
