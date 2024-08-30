/* Copyright (c) 2011,2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.pathvisio.libgpml.io.ConverterException;
import org.pathvisio.libgpml.model.GPML2013aWriter;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayModel;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager that provides BioJava functionality.
 */
public class LibGPMLManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link LibGPMLManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public LibGPMLManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	public PathwayModel loadModel(String file) throws BioclipseException {
		File fileObj = new File(workspaceRoot + file);
		PathwayModel pathwayModel = new PathwayModel();
		try {
			pathwayModel.readFromXml(fileObj, true);
		} catch (ConverterException exception) {
			throw new BioclipseException("Error while reading GPML file: " + exception.getMessage(), exception);
		}
		return pathwayModel;
	}

	/**
	 * Save a GPML model in GPML2013a file format.
	 *
	 * @param file                the file to save too
	 * @param pathwayModel        the GPML model to save
	 * @throws BioclipseException
	 */
	public void saveModelAsGPML2013a(String file, PathwayModel pathwayModel) throws BioclipseException {
		File fileObj = new File(workspaceRoot + file);
    	try {
			GPML2013aWriter.GPML2013aWRITER.writeToXml(pathwayModel, fileObj, true);
		} catch (ConverterException exception) {
			throw new BioclipseException("Error while reading GPML file: " + exception.getMessage(), exception);
		}
	}
	
	@Override
	public String getManagerName() {
		return "gpml";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

}
