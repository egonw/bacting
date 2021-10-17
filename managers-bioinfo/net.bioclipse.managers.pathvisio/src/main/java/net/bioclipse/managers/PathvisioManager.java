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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pathvisio.io.GPML2013aWriter;
import org.pathvisio.io.GPML2021Writer;
import org.pathvisio.model.PathwayModel;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IStringMatrix;

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
	static RDFManager rdf;

    /**
     * Creates a new {@link PathvisioManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public PathvisioManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		bioclipse = new BioclipseManager(workspaceRoot);
		ui = new UIManager(workspaceRoot);
		rdf = new RDFManager(workspaceRoot);
	}

	public Set<String> queryWikipathways(String label) throws BioclipseException{
		String endpoint = "https://sparql.wikipathways.org/sparql";
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
			+ "PREFIX wp: <http://vocabularies.wikipathways.org/wp#>"
			+ "PREFIX dcterms: <http://purl.org/dc/terms/>"
			+ "SELECT DISTINCT ?pathway ?label WHERE {"
			+ " ?geneProduct a wp:GeneProduct . "
			+ " ?geneProduct rdfs:label ?label . "
			+ " ?geneProduct dcterms:isPartOf ?pathway ."
			+ " FILTER regex(str(?label), \"" + label + "\"). "
			+ " FILTER regex(str(?pathway), \"^http\")."
			+ "}";
		IStringMatrix results = rdf.sparqlRemote(endpoint, query);
		if (results.getRowCount() == 0) return Collections.emptySet();

		List<String> col = results.getColumn("pathway");
		Set<String> res = new HashSet<String>();
		for (String str : col){
			res.add(str);
		}
		return res;
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

	public String writeGPML2013a(String file, PathwayModel pathwayModel) throws Exception {
		return writeGPML(file, pathwayModel, "GPML2013a");
	}

	public String writeGPML2021(String file, PathwayModel pathwayModel) throws Exception {
		return writeGPML(file, pathwayModel, "GPML2021");
	}

	public String writeGPML(String file, PathwayModel pathwayModel, String format) throws Exception {
		File tmp = Paths.get(workspaceRoot + file).toFile();
		if ("GPML2013a".equals(format)) {
			GPML2013aWriter.GPML2013aWRITER.writeToXml(pathwayModel, tmp, true);
		} else if ("GPML2021".equals(format)) {
			GPML2021Writer.GPML2021WRITER.writeToXml(pathwayModel, tmp, true);
		} else {
			throw new BioclipseException("No support for GPML version: " + format);
		}
		return workspaceRoot + file;
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
