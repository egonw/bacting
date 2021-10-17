/* Copyright (c) 2012  Ola Spjuth <ola.spjuth@gmail.com>
 *               2021  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.io.ConverterException;
import org.pathvisio.model.PathwayModel;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager that provides identifier mapping functionality
 * using the PathVisio framework.
 */
public class PathvisioManager implements IBactingManager {

	public static final String WIKIPATHWAYS_BASE_URL = 
		"https://www.wikipathways.org//wpi/wpi.php?action=downloadFile&type=gpml&pwTitle=Pathway:";

	private String workspaceRoot;

	static BioclipseManager bioclipse;
	static UIManager ui;

    /**
     * Creates a new {@link PathvisioManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public PathvisioManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		bioclipse = new BioclipseManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
	}

	public String getGPML(String pathwayID) 
			throws BioclipseException{
		
		//Extract the WP ID
		if (!pathwayID.startsWith("WP")) pathwayID = "WP" + pathwayID;
		if (pathwayID.contains("_")) {
			String pattern = ".*\\/(WP\\d+)_.*";
			pathwayID = pathwayID.replaceAll(pattern, "$1");
		}
		
		try {
			ui.newProject("/Virtual");
		} catch (IOException exception) {
			throw new BioclipseException("Exception whil creating the Virtual project: " + exception.getMessage(), exception);
		}
		String res = bioclipse.downloadAsFile(WIKIPATHWAYS_BASE_URL+pathwayID, "/Virtual/"+pathwayID+".gpml");
		return res;
	}

	public PathwayModel loadGPML(String file) throws Exception {
		PathwayModel pathwayModel = new PathwayModel();
		pathwayModel.readFromXml(
			new FileReader(Paths.get(workspaceRoot + file).toFile()), true
		);
		return pathwayModel;
	}

	@Override
	public String getManagerName() {
		return "pathvisio";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1371/JOURNAL.PCBI.1004085");
		return dois;
	}
}
