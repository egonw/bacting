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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	static BioclipseManager bioclipse;

	private String workspaceRoot;

	/**
     * Creates a new {@link WikidataManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public WikidataManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		rdf = new RDFManager(workspaceRoot);
		bioclipse = new BioclipseManager(workspaceRoot);
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
    	byte[] resultRaw = bioclipse.sparqlRemote(
    		"https://query.wikidata.org/sparql", hasMoleculeByInChI
    	);
    	IStringMatrix results = rdf.processSPARQLXML(resultRaw, hasMoleculeByInChI);
    	return (results.getRowCount() > 0);
    }

    /**
     * Returns the Wikidata entity ID for the molecule with the given InChI.
     *
     * @param inchi InChI of the molecule to return Wikidata ID for
     */
    public String getEntityID(InChI inchi) throws BioclipseException {
    	if (inchi == null) throw new BioclipseException("You must give an InChI.");
    	String hasMoleculeByInChI =
        	"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
        	+ "SELECT ?compound WHERE {"
        	+ "  ?compound wdt:P235  \"" + inchi.getKey() + "\" ."
        	+ "}";
    	byte[] resultRaw = bioclipse.sparqlRemote(
       		"https://query.wikidata.org/sparql", hasMoleculeByInChI
       	);
       	IStringMatrix results = rdf.processSPARQLXML(resultRaw, hasMoleculeByInChI);
    	if (results.getRowCount() == 0)
    		throw new BioclipseException("No molecule in Wikidata with the InChI: " + inchi);
    	if (results.getRowCount() > 1)
    		throw new BioclipseException("Too many molecules in Wikidata with the InChI: " + inchi);
        return results.get(1, "compound");
    }

    /**
     * Returns the Wikidata entity IDs for the molecules with the given InChIs.
     *
     * @param inchis List of InChIs of the molecules to check if they already exists in Wikidata
     */
    public Map<String,String> getEntityIDs(List<InChI> inchis) throws BioclipseException {
    	if (inchis == null) throw new BioclipseException("You must give a list of InChIs.");
    	String values = "  VALUES ?inchikey { ";
    	for (InChI inchi : inchis) {
    		values += "\"" + inchi.getKey() + "\"\n";
    	}
    	values += " }\n";
    	String query =
        	"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
        	+ "SELECT ?inchikey ?compound WHERE {"
        	+ values
        	+ "  ?compound wdt:P235 ?inchikey ."
        	+ "}";
    	System.out.println(query);
    	byte[] resultRaw = bioclipse.sparqlRemote(
       		"https://query.wikidata.org/sparql", query
       	);
       	IStringMatrix results = rdf.processSPARQLXML(resultRaw, query);
    	Map<String,String> mappings = new HashMap<>();
    	for (int i=0; i<results.getRowCount(); i++) {
    		mappings.put(results.get(i, "inchikey"), results.get(i, "compound"));
    	}
        return mappings;
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
