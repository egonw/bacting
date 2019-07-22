/* Copyright (c) 2010,2019  Egon Willighagen <egon.willighagen@gmail.com>
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.business.IRDFStore;

public class OpentoxManager {

    private final static String QUERY_ALGORITHMS =
            "SELECT ?algo WHERE {" +
            "  ?algo a <http://www.opentox.org/api/1.1#Algorithm>." +
            "}";

    private final static String QUERY_MODELS =
            "SELECT ?model WHERE {" +
            "  ?model a <http://www.opentox.org/api/1.1#Model>." +
            "}";

    private final static String SPARQL_DESCRIPTORS =
            "SELECT ?algo ?desc WHERE {" +
       	    "  ?algo a <http://www.opentox.org/api/1.1#Algorithm> ;" +
       	    "        a <http://www.opentox.org/algorithmTypes.owl#DescriptorCalculation> ;" +
    	    "     <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#instanceOf> ?desc ." +
            "  ?desc a <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#MolecularDescriptor> ." +
            "}";

    private final static String QUERY_DATASETS =
            "SELECT ?set WHERE {" +
            "  ?set a <http://www.opentox.org/api/1.1#Dataset> ." +
            "}";

    private final static String QUERY_FEATURES =
            "SELECT ?feature WHERE {" +
            "  ?feature a <http://www.opentox.org/api/1.1#Feature> ." +
            "}";

	private RDFManager rdf;

	public OpentoxManager(String workspaceRoot) {
		this.rdf = new RDFManager(workspaceRoot);
	}

    public List<String> listAlgorithms(String ontologyServer)
    throws BioclipseException {
        IStringMatrix results = new StringMatrix();

        try {
            // download the list of data sets as RDF
        	results = rdf.sparqlRemote(ontologyServer, QUERY_ALGORITHMS);
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        return results.getRowCount() > 0 ? results.getColumn("algo") : new ArrayList<String>();
    }

    /**
     * Keep only rows whose column field contains the given substring.
     * 
     * @return
     */
    private IStringMatrix regex(IStringMatrix matrix, String column, String substring) {
        StringMatrix table = new StringMatrix();
        int rowCount = matrix.getRowCount();
    	int colCount = matrix.getColumnCount();
    	int hitCount = 0;
		for (int col=1; col<=colCount; col++) {
			table.setColumnName(col, matrix.getColumnName(col));
		}
		// do the filtering
		for (int row=1; row<=rowCount; row++) {
			String algo = matrix.get(row, column);
    		if (algo.contains(substring)) {
    			// CDK descriptor, copy row
    			hitCount++;
    			for (int col=1; col<=colCount; col++) {
    				table.set(hitCount, col, matrix.get(row, col));
    			}
    		}
    	}
        return table;
    }
    
    public IStringMatrix listDescriptors(String ontologyServer)
    throws BioclipseException {
        IStringMatrix results = new StringMatrix();

        try {
            // download the list of data sets as RDF
        	results = regex(
        		rdf.sparqlRemote(ontologyServer, SPARQL_DESCRIPTORS),
        		"algo", "org.openscience.cdk"
        	);
        } catch (Exception exception) {
            throw new BioclipseException(
            	"Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        return results;
    }

    public List<String> listModels(String ontologyServer)
    throws BioclipseException {
        IStringMatrix results = new StringMatrix();

        try {
            // download the list of data sets as RDF
        	results = rdf.sparqlRemote(ontologyServer, QUERY_MODELS);
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        return results.getRowCount() > 0 ? results.getColumn("model") : new ArrayList<String>();
    }

    public List<String> listDataSets(String service)
    throws BioclipseException {
    	IRDFStore store = rdf.createInMemoryStore();
    	List<String> dataSets = Collections.emptyList();
    	Map<String, String> extraHeaders = new HashMap<String, String>();

    	try {
    		// download the list of data sets as RDF
    		rdf.importURL(store, service + "dataset", extraHeaders);
    		String dump = rdf.asRDFN3(store);
    		System.out.println("RDF: " + dump);

    		// query the downloaded RDF
    		IStringMatrix results = rdf.sparql(store, QUERY_DATASETS);

    		if (results.getRowCount() > 0) {
    			dataSets = results.getColumn("set");
    		}
    	} catch (BioclipseException exception) {
    		throw exception;
    	} catch (Exception exception) {
    		throw new BioclipseException(
    			"Error while accessing RDF API of service: " + exception.getMessage(),
    			exception
    		);
    	}

    	return dataSets;
    }

    public List<String> listFeatures(String service)
    throws BioclipseException {
    	IRDFStore store = rdf.createInMemoryStore();
    	List<String> dataSets = Collections.emptyList();
    	Map<String, String> extraHeaders = new HashMap<String, String>();

    	try {
    		// download the list of data sets as RDF
    		rdf.importURL(store, service + "feature", extraHeaders);
    		String dump = rdf.asRDFN3(store);
    		System.out.println("RDF: " + dump);

    		// query the downloaded RDF
    		IStringMatrix results = rdf.sparql(store, QUERY_FEATURES);

    		if (results.getRowCount() > 0) {
    			dataSets = results.getColumn("feature");
    		}
    	} catch (BioclipseException exception) {
    		throw exception;
    	} catch (Exception exception) {
    		throw new BioclipseException(
    			"Error while accessing RDF API of service: " + exception.getMessage(),
    			exception
    		);
    	}

    	return dataSets;
    }
}
