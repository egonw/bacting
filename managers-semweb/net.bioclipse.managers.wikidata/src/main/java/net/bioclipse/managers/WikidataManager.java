/* Copyright (c) 2016-2021  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.util.ArrayList;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.inchi.InChI;
import net.bioclipse.wikidata.domain.WikidataMolecule;

/**
 * Bioclipse manager that provides functionality around the Resource
 * Description Framework standard. It allows creating in memory and
 * on disk triple stores, creating on content, and IO functionality.
 */
public class WikidataManager implements IBactingManager {

	static RDFManager rdf;

	private String workspaceRoot;

	/**
     * Creates a new {@link WikidataManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public WikidataManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		rdf = new RDFManager(workspaceRoot);
	}

    /**
     * Return true if Wikidata contains a molecule with the given InChI.
     *
     * @param inchi InChI of the molecule to check if it already exists in Wikidata
     */
    public boolean hasMolecule(InChI inchi) throws BioclipseException {
    	if (inchi == null) throw new BioclipseException("You must give an InChI.");
    	String hasMoleculeByInChI =
    		"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
    	   	+ "SELECT ?compound WHERE {"
    	    + "  ?compound wdt:P235 \"" + inchi.getKey() + "\" ."
    	    + "}";
    	IStringMatrix results = rdf.sparqlRemote(
    		"https://query.wikidata.org/sparql", hasMoleculeByInChI
    	);
    	return (results.getRowCount() > 0);
    }

    /**
     * Return true the Wikidata entity ID for the molecule with the given InChI.
     *
     * @param inchi InChI of the molecule to check if it already exists in Wikidata
     */
    public String getEntityID(InChI inchi) throws BioclipseException {
    	if (inchi == null) throw new BioclipseException("You must give an InChI.");
    	String hasMoleculeByInChI =
        	"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
        	+ "SELECT ?compound WHERE {"
        	+ "  ?compound wdt:P235  \"" + inchi.getKey() + "\" ."
        	+ "}";
    	IStringMatrix results = rdf.sparqlRemote(
    		"https://query.wikidata.org/sparql", hasMoleculeByInChI
        );
    	if (results.getRowCount() == 0)
    		throw new BioclipseException("No molecule in Wikidata with the InChI: " + inchi);
    	if (results.getRowCount() > 1)
    		throw new BioclipseException("Too many molecules in Wikidata with the InChI: " + inchi);
    	String entityID = results.get(1, "compound");
    	if (entityID == null || entityID.length() == 0)
    		throw new BioclipseException("No Wikidata entity found for the molecule with the InChI: " + inchi);
    	return entityID;
    }

    /**
     * Return a molecule with the given InChI, or throws an BioclipseException
     * when it does not exist.
     *
     * @param inchi InChI of the molecule to check if it already exists in Wikidata
     */
    public IMolecule getMolecule(InChI inchi) throws BioclipseException {
    	if (inchi == null) throw new BioclipseException("You must give an InChI.");
    	return new WikidataMolecule(getEntityID(inchi));
    }

    @Override
	public String getManagerName() {
		return "wikidata";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.7554/eLife.52614");
		return dois;
	}
}
