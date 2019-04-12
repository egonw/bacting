/* Copyright (c) 2009-2019  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.StringMatrixHelper;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.JenaModel;

public class RDFManager {

    public static final Integer CONNECT_TIME_OUT = 5000; 
    public static final Integer READ_TIME_OUT = 30000; 

	private String workspaceRoot;

	public RDFManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    public IRDFStore createInMemoryStore() {
    	return new JenaModel();
    }

    public IRDFStore createInMemoryStore(boolean ontologyModel) {
    	return new JenaModel(ontologyModel);
    }

    public StringMatrix sparqlRemote(
            String serviceURL,
            String sparqlQueryString) {
         Query query = QueryFactory.create(sparqlQueryString);
         QueryEngineHTTP qexec = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(serviceURL, query);
         qexec.addParam("timeout", "" + CONNECT_TIME_OUT);
         PrefixMapping prefixMap = query.getPrefixMapping();

         StringMatrix table = null;
         try {
             ResultSet results = qexec.execSelect();
             table = StringMatrixHelper.convertIntoTable(prefixMap, results);
         } finally {
             qexec.close();
         }
         return table;
     }
}
