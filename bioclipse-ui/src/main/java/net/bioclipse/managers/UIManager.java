/* Copyright (c) 2008-2009 The Bioclipse Project and others.
 *                    2018 Egon Willighagen <egonw@users.sf.net>
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

public class UIManager {

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
    
}
