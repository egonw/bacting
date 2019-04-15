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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public String download(String url)
    		throws BioclipseException {
    	return download(url, null);
    }

    public String download(String url, String mimeType) throws BioclipseException {
    	StringBuffer content = new StringBuffer();
    	URLConnection rawConn;
    	try {
    		rawConn = createURL(url).openConnection();
    		if (mimeType != null)
    			rawConn.addRequestProperty("Accept", mimeType);
    		BufferedReader reader = new BufferedReader(
    			new InputStreamReader(rawConn.getInputStream())
    		);
    		String line = reader.readLine();
    		while (line != null) {
    			content.append(line).append('\n');
    			line = reader.readLine();
    		}
    	} catch (IOException exception) {
    		throw new BioclipseException(
    			"Error while downloading from URL.", exception
    		);
    	}
    	return content.toString();
    }

    public String downloadAsFile(String url, String target) throws BioclipseException {
    	return downloadAsFile(url, null, target);
    }

    public String downloadAsFile(String url, String mimeType, String target)
    throws BioclipseException {
    	return downloadAsFile(url, mimeType, target, null);
    }

    public String downloadAsFile(String url, String mimeType, String target,
    		Map<String,String> extraHeaders)
    				throws BioclipseException {
    	URLConnection rawConn;
    	try {
    		rawConn = createURL(url).openConnection();
    		if (extraHeaders != null) {
    			for (String header : extraHeaders.keySet()) {
    				rawConn.addRequestProperty(header, extraHeaders.get(header));
    			}
    		}
    		if (mimeType != null)
    			rawConn.addRequestProperty("Accept", mimeType);
    	    Files.copy(rawConn.getInputStream(), Paths.get(workspaceRoot + target), StandardCopyOption.REPLACE_EXISTING);
    	} catch (IOException exception) {
    		if (exception.getMessage().contains("403"))
    			throw new BioclipseException(
    				"No access.", exception
    			);
    		throw new BioclipseException(
    			"Error while downloading from URL.", exception
    		);
    	}
    	return target;
    }

    private URL createURL(String url) throws BioclipseException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new BioclipseException("Error while opening browser: " +
                e.getMessage(), e);
        }
    }
}
