/* Copyright (c) 2008-2009 The Bioclipse Project and others.
 *               2018-2019 Egon Willighagen <egonw@users.sf.net>
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

public class UIManager implements IBactingManager {

	private String workspaceRoot;

	public UIManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	public boolean fileExists(String file) {
        return new File(workspaceRoot + file).exists();
    }

	public String newFile(String path) throws IOException {
    	return newFile(path, "");
    }

	public String renewFile(String file) throws IOException {
		if (fileExists(file)) remove(file);
		newFile(file);
		return file;
	}

    public String newFile(String path, String content) throws IOException {
    	File file = new File(workspaceRoot + path);
    	file.getParentFile().mkdirs(); 
    	file.createNewFile();
    	PrintWriter out = new PrintWriter(new FileWriter(file));
    	out.print(content);
    	out.close();
        return file.getPath();
    }

    public void remove(String path) {
    	File file = new File(workspaceRoot + path);
    	file.delete();
    }

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

    public void open(final Object object) {
    	System.out.println("Cannot open file on the command line");
    }

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
