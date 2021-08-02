/* Copyright (c) 2006-2020  Egon Willighagen <egonw@users.sf.net>
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.runtime.CoreException;
import org.openscience.cdk.io.formats.IChemFormat;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.rdf.business.IRDFStore;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * Bioclipse manager that provides functionality to interact with the
 * PubChem database.
 */
public class PubChemManager implements IBactingManager {

    private final static String EUTILS_URL_BASE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils";
    private final static String PUBCHEM_URL_BASE = "https://pubchem.ncbi.nlm.nih.gov/";
    private final static String PUBCHEMRDF_URL_BASE = "https://rdf.ncbi.nlm.nih.gov/pubchem/compound/";

    private final static String TOOL = "bioclipse.net";

    private String workspaceRoot;
	private CDKManager cdk;
	private RDFManager rdf;

	/**
     * Creates a new {@link PubChemManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public PubChemManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(this.workspaceRoot);
		this.rdf = new RDFManager(this.workspaceRoot);
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

    /**
     * Searches PubChem for compounds using the given query.
     *
     * @param query  the string to search in PubChem
     * @return       a Java {@link List} of PubChem compound identifiers
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
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

	@Override
	public String getManagerName() {
		return "pubchem";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

    public IMolecule download(Integer cid)
        throws IOException, BioclipseException, CoreException {
    	String molstring = downloadAsString(cid);
        if ( molstring == null || molstring.isEmpty() ) {
            throw new BioclipseException( "Could not read molecule from" + cid );
        }
        IChemFormat format = cdk.getFormat( "PubChemCompoundXMLFormat" );
        ICDKMolecule molecule = cdk.loadMolecule(
        	new ByteArrayInputStream( molstring.getBytes() ), format
        );
        return molecule;
    }

    public String downloadAsString(Integer cid)
        throws IOException, BioclipseException, CoreException {
        return downloadAsString(cid, "DisplayXML");
    }

    private String downloadAsString(Integer cid, String type)
            throws IOException, BioclipseException, CoreException {
        String efetch = PUBCHEM_URL_BASE + "summary/summary.cgi?cid=" +
            cid + "&disopt=" + type;
        return downloadAsString(efetch, null);
    }

    private String downloadAsString(String URL, String accepts)
            throws IOException, BioclipseException, CoreException {
        HttpClient client = HttpClientBuilder.create().build();
        String fileContent = "";
        try {
            HttpGet method = new HttpGet(URL);
			if (accepts != null) {
				method.addHeader("Accept", accepts);
				method.addHeader("Content-Type", accepts);
			}
			HttpResponse response = client.execute(method);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200) throw new BioclipseException(
					"Expected HTTP 200, but got a " + statusCode + ": " + statusLine.getReasonPhrase()
					);

			HttpEntity responseEntity = response.getEntity();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			responseEntity.writeTo(buffer);
			buffer.flush();
			fileContent = new String(buffer.toByteArray());
        } catch (PatternSyntaxException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid Pattern.", exception);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            throw new BioclipseException("Invalid URL.", exception);
        }
        return fileContent;
    }

    public IRDFStore downloadRDF(Integer cid, IRDFStore store)
        throws IOException, BioclipseException, CoreException {
        String downloadURI = PUBCHEMRDF_URL_BASE + "CID" + cid;
        String rdfContent = downloadAsString(downloadURI, "application/rdf+xml");
        rdf.importFromString(store, rdfContent, "RDF/XML");
        return store;
    }

    public IMolecule download3d(Integer cid)
        throws IOException, BioclipseException, CoreException{
    	String molstring = download3dAsString(cid);

    	// convert the returned SD file into a MDL molfile by stripping the
    	// $$$$ and beyond
    	molstring = molstring.substring(0, molstring.indexOf("$$$$"));

    	ICDKMolecule molecule = cdk.fromString(molstring);
    	return molecule;
    }

    public String download3dAsString(Integer cid)
        throws IOException, BioclipseException, CoreException{
        return downloadAsString(cid, "3DDisplaySDF");
    }

    public List<IMolecule> download(List<Integer> cids)
    				throws IOException, BioclipseException, CoreException {
    	List<IMolecule> results = new ArrayList<IMolecule>();
    	for (Integer cid : cids) {
    		results.add(download(cid));
    	}
    	return results;
    }

    public List<IMolecule> download3d(List<Integer> cids)
    	throws IOException, BioclipseException, CoreException {
    	List<IMolecule> results = new ArrayList<IMolecule>();
    	for (Integer cid : cids) {
    		results.add(download3d(cid));
    	}
    	return results;
    }

}
