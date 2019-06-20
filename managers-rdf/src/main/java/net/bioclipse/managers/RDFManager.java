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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.StringMatrixHelper;
import net.bioclipse.rdf.business.IJenaStore;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.JenaModel;
import net.bioclipse.rdf.business.TDBModel;

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

    public IRDFStore createStore(String tripleStoreDirectoryPath) {
    	return new TDBModel(tripleStoreDirectoryPath);
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

    public IStringMatrix processSPARQLXML(byte[] queryResults, String originalQuery)
            throws BioclipseException {
    	PrefixMapping prefixMap = null;
        if (originalQuery != null) {
       	 try {
                Query query = QueryFactory.create(originalQuery);
                prefixMap = query.getPrefixMapping();
       	 } catch (Exception exception) {
       		 // could not parse the query for namespaces
       		 prefixMap = new PrefixMappingImpl();
       	 }
        }

        // now the Jena part
        ResultSet results = ResultSetFactory.fromXML(new ByteArrayInputStream(queryResults));
        StringMatrix table = StringMatrixHelper.convertIntoTable(prefixMap, results);

        return table;
    }

    public StringMatrix sparql(IRDFStore store, String queryString) throws IOException, BioclipseException,
    CoreException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );

        StringMatrix table = null;
        Model model = ((IJenaStore)store).getModel();
        Query query = QueryFactory.create(queryString);
        PrefixMapping prefixMap = query.getPrefixMapping();
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            table = StringMatrixHelper.convertIntoTable(prefixMap, results);
        } finally {
            qexec.close();
        }
        return table;
    }

    public long size(IRDFStore store) throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        Model model = ((IJenaStore)store).getModel();
        return model.size();
    }

}
