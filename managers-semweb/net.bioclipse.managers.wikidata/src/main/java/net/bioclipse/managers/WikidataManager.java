/* Copyright (c) 2016-2023  Egon Willighagen <egonw@users.sf.net>
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

import com.google.common.collect.Lists;

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

    /**
     * Return a work with the given DOI, or throws an BioclipseException
     * when it does not exist.
     *
     * @param  doi DOI of the work to check if it already exists in Wikidata
     * @return a String with the Q-id
     */
    public String getEntityID(String doi) throws BioclipseException {
    	if (doi == null) throw new BioclipseException("You must give a DOI.");
    	String hasWorkByDOI =
            "PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
            + "SELECT ?work WHERE {"
            + "  ?work wdt:P356  \"" + doi + "\" ."
            + "}";
        byte[] resultRaw = bioclipse.sparqlRemote(
          	"https://query.wikidata.org/sparql", hasWorkByDOI
        );
        IStringMatrix results = rdf.processSPARQLXML(resultRaw, hasWorkByDOI);
        if (results.getRowCount() == 0)
        	throw new BioclipseException("No work in Wikidata with the DOI: " + doi);
        if (results.getRowCount() > 1)
        	throw new BioclipseException("Too many works in Wikidata with the DOI: " + doi);
        return results.get(1, "work");
    }

    /**
     * Returns the Wikidata entity IDs for the works with the given DOIs.
     *
     * @param dois List of DOIs of the works to check if they already exists in Wikidata
     */
    public Map<String,String> getEntityIDsForDOIs(List<String> dois) throws BioclipseException {
    	if (dois == null) throw new BioclipseException("You must give a list of DOIs.");
		Map<String,String> mappings = new HashMap<>();
    	if (dois.size() > 5000) {
    		for (List<String> subset : Lists.partition(dois, 5000)) {
    			mappings.putAll(getEntityIDsForDOIs(subset));
    		}
    	} else {
    		String values = "  VALUES ?doi { ";
        	Map<String,String> doiMappings = new HashMap<>();
    		for (String doi : dois) {
    			values += "\"" + doi.toUpperCase() + "\"\n";
    			doiMappings.put(doi.toUpperCase(), doi);
    		}
    		values += " }\n";
    		String query =
    				"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
    						+ "SELECT ?doi ?work WHERE {"
    						+ values
    						+ "  ?work wdt:P356 ?doi ."
    						+ "}";
    		byte[] resultRaw = bioclipse.sparqlRemote(
    				"https://query.wikidata.org/sparql", query
    				);
    		IStringMatrix results = rdf.processSPARQLXML(resultRaw, query);
    		for (int i=1; i<=results.getRowCount(); i++) {
    			mappings.put(doiMappings.get(results.get(i, "doi")), results.get(i, "work"));
    		}
    	}
        return mappings;
    }

    /**
     * Returns the Wikidata entity IDs for entities instance of a certain type (P31).
     *
     * @param type identifier of a Wikidata item
     */
    public List<String> getEntityIDsForType(String type) throws BioclipseException {
    	if (!isValidQIdentifier(type)) throw new BioclipseException("You must give a valid Wikidata identifier, but got " + type + ".");
    	String query =
        	"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
        	+ "SELECT DISTINCT ?entity WHERE {"
        	+ "  ?entity wdt:P31 wd:" + type + " ."
        	+ "}";
    	byte[] resultRaw = bioclipse.sparqlRemote(
       		"https://query.wikidata.org/sparql", query
       	);
       	IStringMatrix results = rdf.processSPARQLXML(resultRaw, query);
       	return results.getColumn("entity");
    }

    /**
     * Determines if an identifier is a valid Wikidata entity identifier, like Q5. 
     *
     * @param  identifier to test
     * @return true, if the identifier is valid
     */
	public boolean isValidQIdentifier(String identifier) {
		if (identifier == null) return false;
    	if (identifier.length() < 2) return false;
    	if (!identifier.startsWith("Q")) return false;
    	try { Integer.valueOf(identifier.substring(1)); } catch (Exception exception) { return false; } 
		return true;
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
