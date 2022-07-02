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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

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
	 * @throws IOException when the file could not be created
	 */
	public String newFile(String path) throws IOException {
    	return newFile(path, "");
    }

	/**
	 * Creates a new empty file in the Bioclipse workspace or empties the
	 * existing file.
	 *
	 * @param file the location of the file in the Bioclipse workspace
	 * @return     the location of the file in the Bioclipse workspace
	 * @throws IOException when the file could not be created
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
	 * @throws IOException when the file could not be created
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
	 */
    public void append(String path, String toWrite) {
        this.append(path, toWrite.getBytes());
    }

	/**
	 * Appends the new content to write to a new or existing file in the Bioclipse workspace.
	 *
	 * @param path    the location of the file in the Bioclipse workspace
	 * @param toWrite the content to append to the new file
	 */
    public void append(String path, byte[] toWrite) {
    	try {
    		if (fileExists(path)) {
                Files.write(Paths.get(workspaceRoot + path), toWrite, StandardOpenOption.APPEND);
    		} else {
                Files.write(Paths.get(workspaceRoot + path), toWrite, StandardOpenOption.CREATE);
    		}
    	} catch (Exception exception) {
    		throw new RuntimeException(
    			"Error while appending to File", exception
    		);
    	}
    }

	/**
	 * Appends the new content to write to a new or existing file in the Bioclipse workspace.
	 *
	 * @param path    the location of the file in the Bioclipse workspace
	 * @param toWrite the content to append to the new file
	 */
    public void append(String path, InputStream toWrite) {
    	try {
    		byte[] buffer = new byte[toWrite.available()];
    		toWrite.read(buffer);
    		if (fileExists(path)) {
    			Files.write(Paths.get(workspaceRoot + path), buffer, StandardOpenOption.APPEND);
    		} else {
    			Files.write(Paths.get(workspaceRoot + path), buffer, StandardOpenOption.CREATE);
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
     * @param object the object to be opened in Bioclipse
     */
    public void open(final Object object) {
    	System.out.println("Cannot open file on the command line");
    }

	/**
	 * Creates a new project in the Bioclipse workspace.
	 *
	 * @param name the location of the project in the Bioclipse workspace
	 * @return     the location of the project in the Bioclipse workspace
	 * @throws IOException when the project could not be created
	 */
    public String newProject(String name) throws IOException {
        if (fileExists(name)) return name;
        Files.createDirectory(Paths.get(workspaceRoot + name));
        return name;
    }

    /**
     * Read a file content line by line into memory.
     *
     * @param path IFile to read from
     * @return     String with contents
     * @throws BioclipseException when the file does not exist or could not be opened or read
     */
    public String readFile(String path) throws BioclipseException {
        if (!fileExists(path)) throw new BioclipseException("File '"
                                         + path + "' does not exit.");

        try {
        	BufferedReader reader = new BufferedReader(
        		new InputStreamReader(new FileInputStream(workspaceRoot + path)));
            StringBuffer buffer=new StringBuffer();
            String line=reader.readLine();
            while ( line  != null ) {
                buffer.append( line + "\n");
                line=reader.readLine();
            }
            reader.close();
            return buffer.toString();
        } catch ( Exception e ) {
            throw new BioclipseException("Error opening/reading file: "
                                         + path, e);
        }
    }

    /**
     * Read a file line by line into memory.
     *
     * @param path IFile to read from
     * @return     String[] with one entry per line
     * @throws BioclipseException when the file does not exist or could not be opened or read
     */
    public String[] readFileIntoArray(String path) throws BioclipseException{
        if (!fileExists(path)) throw new BioclipseException("File '"
                                         + path + "' does not exit.");

        List<String> lines=new ArrayList<String>();
        try {
        	BufferedReader reader = new BufferedReader(
            		new InputStreamReader(new FileInputStream(workspaceRoot + path)));
            String line=reader.readLine();
            while ( line  != null ) {
                lines.add(line);
                line=reader.readLine();
            }
            reader.close();
            return lines.toArray(new String[0]);
        } catch ( Exception e ) {
            throw new BioclipseException("Error opening/reading file: " + path, e);
        }
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
