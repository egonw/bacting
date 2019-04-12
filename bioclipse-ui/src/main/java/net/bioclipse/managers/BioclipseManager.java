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

import java.net.URL;
import java.net.URLConnection;

import net.bioclipse.core.business.BioclipseException;

public class BioclipseManager {

	private String workspaceRoot;

	public BioclipseManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    public boolean isOnline() {
    	// if both fail, we do not have internet
    	String[] sites = new String[]{
    		"http://google.com/",
    		"http://slashdot.org/"
    	};
    	for (String site : sites) {
    		try {
    		    URL url = new URL(site);
    		    URLConnection conn = url.openConnection();
    		    conn.connect();
    		    return true;
    		} catch (Exception exception) {}
    	}
    	return false;
    }

    public void assumeOnline() throws BioclipseException {
    	if (!isOnline())
    		throw new BioclipseException(
    			"Bioclipse does not have internet access."
    		);
    }

    public String fullPath( String file ) {
    	return workspaceRoot + file;
    }

}
