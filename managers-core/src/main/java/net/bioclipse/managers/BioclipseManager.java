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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

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

    public byte[] sparqlRemote(String serviceURL, String sparqlQueryString)
    throws BioclipseException {

         // use Apache for doing the SPARQL query
         HttpClient httpclient = HttpClientBuilder.create().build();

         // Set credentials on the client
         List<NameValuePair> formparams = new ArrayList<NameValuePair>();
         formparams.add(new BasicNameValuePair("query", sparqlQueryString));
         try {
        	 UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        	 HttpPost httppost = new HttpPost(serviceURL);
        	 httppost.setEntity(entity);
        	 HttpResponse response = httpclient.execute(httppost);
        	 StatusLine statusLine = response.getStatusLine();
        	 int statusCode = statusLine.getStatusCode();
        	 if (statusCode != 200) throw new BioclipseException(
        		 "Expected HTTP 200, but got a " + statusCode + ": " + statusLine.getReasonPhrase()
        	 );

         	 HttpEntity responseEntity = response.getEntity();
         	 ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        	 responseEntity.writeTo(buffer);
        	 buffer.flush();
        	 return buffer.toByteArray();
         } catch (UnsupportedEncodingException exception) {
        	 throw new BioclipseException(
                 "Error while creating the SPARQL query: " + exception.getMessage(), exception
             );
         } catch (IOException exception) {
        	 throw new BioclipseException(
                 "Error while processing the SPARQL endpoint feedback: " + exception.getMessage(), exception
             );
         }
    }
}
