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

import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.JenaModel;

public class RDFManager {

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

}
