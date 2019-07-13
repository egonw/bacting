/* Copyright (c) 2006-2019  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import net.bioclipse.core.business.BioclipseException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class PubChemManager {

    private final static String EUTILS_URL_BASE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils";
    private final static String PUBCHEM_URL_BASE = "https://pubchem.ncbi.nlm.nih.gov/";
    private final static String PUBCHEMRDF_URL_BASE = "https://rdf.ncbi.nlm.nih.gov/pubchem/compound/";

    private final static String TOOL = "bioclipse.net";

    private String workspaceRoot;

	public PubChemManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    private String replaceSpaces(String molecule2) {
        StringBuffer result = new StringBuffer();
        for (int i=0; i<molecule2.length(); i++) {
            if (Character.isWhitespace(molecule2.charAt(i))) {
                result.append("+");
            } else {
                result.append(molecule2.charAt(i));
            }
        }
        return result.toString();
    }

    public List<Integer> search(String query)
            throws IOException, BioclipseException, CoreException {
        int max = 50;

        List<Integer> results = new ArrayList<Integer>();

        String db = "pccompound";
        query = replaceSpaces(query);

        String esearch = EUTILS_URL_BASE + "/esearch.fcgi?" +
            "db=" + db + "&retmax=" + max + "&usehistory=y&tool=" + TOOL + "&term=" + query;

        URL queryURL = new URL(esearch);
        URLConnection connection = queryURL.openConnection();

        Builder parser = new Builder();
        Document doc;
        try {
            doc = parser.build(connection.getInputStream());
            Nodes countNodes = doc.query("/eSearchResult/Count");
            if (countNodes.size() > 0) {
                // System.out.println(countNodes.get(0).toString());
            } else {
                return results;
            }

            Nodes cidNodes = doc.query("/eSearchResult/IdList/Id");

            for (int cidCount=0; cidCount<cidNodes.size(); cidCount++) {
                String cidStr = cidNodes.get(cidCount).getValue();
                int cid = Integer.parseInt(cidStr);
                results.add(cid);
            }
        } catch (ValidityException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        return results;
    }
}
