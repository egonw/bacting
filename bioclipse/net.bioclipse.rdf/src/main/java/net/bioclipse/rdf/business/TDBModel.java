/* Copyright (c) 2009-2019  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.rdf.business;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TDBModel implements IJenaStore {

    private Model model;
    
    public TDBModel(String tripleStoreDirectoryPath) {
    	org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_TTL.class.getName();
    	org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_NT.class.getName();
    	org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_RDFJSON.class.getName();
    	org.apache.jena.riot.adapters.JenaReadersWriters.RDFReaderRIOT_RDFXML.class.getName();
        model = TDBFactory.createModel(tripleStoreDirectoryPath);
    }
    
    public Model getModel() {
        return this.model;
    }
    
    public String toString() {
        return "RDFStore (Jena TDB): " + model.size() + " triples";
    }
    
}
