/* Copyright (c) 2009-2020  Egon Willighagen <egonw@users.sf.net>
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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.StringMatrix;

/**
 * Bioclipse manager providing core functionality. It is similar to the
 * {@link UIManager} with similar functionality but this manager
 * is less related to the Bioclipse workspace and user interface interaction.
 */
public class BioclipseManager implements IBactingManager {

	private String workspaceRoot;

	/**
     * Creates a new {@link BioclipseManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public BioclipseManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	/**
	 * Returns the location of the current logfile.
	 */
	public String logfileLocation() {
		// no logging functionality at this moment. Not clear how this can be shared among all managers
		return "";
	}

	/**
	 * Determines if online websites can be reached, reflecting access to
	 * the internet.
	 *
	 * @return  true, if the machine has an active internet connection
	 */
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

    /**
     * Tests if there is an active internet connection and throws an
     * {@link BioclipseException} if not.
     *
     * @throws BioclipseException when Bioclipse does not have internet access
     */
    public void assumeOnline() throws BioclipseException {
    	if (!isOnline())
    		throw new BioclipseException(
    			"Bioclipse does not have internet access."
    		);
    }

    /**
     * Converts a Bioclipse workspace path for the given file to an
     * operating system level absolute path. This method is needed if
     * you want to have access to the file using regular Java, Groovy,
     * etc programming languages.
     *
     * @param file  Bioclipse file path to convert
     * @return      an absolute file path on the local machine
     */
    public String fullPath( String file ) {
    	return workspaceRoot + file;
    }

    /**
     * Queries a remote SPARQL end point without Apache Jena.
     *
     * @param serviceURL        the URL of the SPARQL end point
     * @param sparqlQueryString the SPARQL query
     * @return                  an {@link StringMatrix} object with results
     * @throws BioclipseException
     */
    public byte[] sparqlRemote(String serviceURL, String sparqlQueryString)
    throws BioclipseException {

         // use Apache for doing the SPARQL query
         HttpClient httpclient = HttpClientBuilder.create()
             .useSystemProperties()
             .disableAutomaticRetries()
             .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
             .build();

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

    /**
     * Downloads the content of the page located by the given URL string as
     * a Java {@link String}.
     *
     * @param url {@link String} version of the URL of the document to download
     * @return    a {@link String} with the content of the webpage
     * @throws BioclipseException when there was a downloading problem
     */
    public String download(String url)
    		throws BioclipseException {
    	return download(url, null);
    }

    /**
     * Downloads the content of the page located by the given URL string as
     * a Java {@link String} in the given mimetype (if provided by the webserver).
     *
     * @param url      {@link String} version of the URL of the document to download
     * @param mimeType the mimetype in which the content should be returned, e.g. text/n3
     * @return         a {@link String} with the content of the webpage
     * @throws BioclipseException when there was a downloading problem
     */
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

    /**
     * Downloads the content of the page located by the given URL string as
     * a file in the Bioclipse workspace and return the path as {@link String}.
     *
     * @param url    {@link String} version of the URL of the document to download
     * @param target path in the Bioclipse workspace where the content should be stored
     * @return       a {@link String} with the content of the webpage
     * @throws BioclipseException when there was a downloading problem
     */
    public String downloadAsFile(String url, String target) throws BioclipseException {
    	return downloadAsFile(url, null, target);
    }

    /**
     * Downloads the content of the page located by the given URL string as
     * a file in the given mimetype (if provided by the webserver)
     * in the Bioclipse workspace and return the path as {@link String}.
     *
     * @param url      {@link String} version of the URL of the document to download
     * @param mimeType the mimetype in which the content should be returned, e.g. text/n3
     * @param target   path in the Bioclipse workspace where the content should be stored
     * @return         a {@link String} with the content of the webpage
     * @throws BioclipseException when there was a downloading problem
     */
    public String downloadAsFile(String url, String mimeType, String target)
    throws BioclipseException {
    	return downloadAsFile(url, mimeType, target, null);
    }

    /**
     * Downloads the content of the page located by the given URL string as
     * a file in the given mimetype (if provided by the webserver)
     * in the Bioclipse workspace and return the path as {@link String}.
     * This version allows setting additional HTTP headers.
     *
     * @param url          {@link String} version of the URL of the document to download
     * @param mimeType     the mimetype in which the content should be returned, e.g. text/n3
     * @param target       path in the Bioclipse workspace where the content should be stored
     * @param extraHeaders additional HTTP headers, e.g. useful if authentication is needed
     * @return             a {@link String} with the content of the webpage
     * @throws BioclipseException when there was a downloading problem
     */
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

    /**
     * Creates an {@link URL} object for the given url.
     *
     * @param url  {@link String} representation of the URL to return
     * @return     a {@link URL} object
     * @throws BioclipseException
     */
    private URL createURL(String url) throws BioclipseException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new BioclipseException("Error while opening browser: " +
                e.getMessage(), e);
        }
    }

    /**
     * Returns the version of the current Bioclipse libraries.
     *
     * @return a String with the version
     */
    public String version() {
        return "2.8.0"; // need to get this from the bioclipse-core pom.xml or so
    }

    public static class VersionNumberComparator implements Comparator<String> {

        private static Pattern p
            = Pattern.compile( "(\\d+)\\.(\\d+)(?:\\.(\\d+)(?:\\.(\\S+))?)?" );

        private static final int QUALIFER_POSITION = 4;

        private VersionNumberComparator() {
        }

        public static final VersionNumberComparator INSTANCE
            = new VersionNumberComparator();

        @Override
        public int compare( String o1, String o2 ) {
            Matcher m1 = p.matcher( o1 );
            Matcher m2 = p.matcher( o2 );
            if ( !m1.matches() || !m2.matches() ) {
                // Build error message
                String s = null;
                if ( !m1.matches() ) {
                    s = o1;
                }
                else if ( !m2.matches() ) {
                    s = o2;
                }
                throw new IllegalArgumentException(
                    "Could not identify the String: \"" + s + "\" as a " +
                    "version number. Version numbers looks like these: " +
                    "\"2.2\", \"2.2.0\", or \"2.2.0.RC1");
            }
            else {
                int groups = Math.max( m1.groupCount(), m2.groupCount() );
                for ( int i = 0 ; i < groups ; i++ ) {

                    if ( i+1 == QUALIFER_POSITION ) {
                        String g1 = m1.group(i+1) != null ? m1.group(i+1)
                                                          : "";
                        String g2 = m2.group(i+1) != null ? m2.group(i+1)
                                                        : "";
                        return g1.compareTo( g2 );
                    }
                    String g1 = m1.group(i+1) != null ? m1.group(i+1)
                                                      : "0";
                    String g2 = m2.group(i+1) != null ? m2.group(i+1)
                                                      : "0";
                    Integer i1 = Integer.parseInt( g1 );
                    Integer i2 = Integer.parseInt( g2 );
                    if ( i1 < i2 ) {
                        return -1;
                    }
                    if ( i1 > i2 ) {
                        return +1;
                    }
                }
                return 0;
            }
        }
    }

    /**
     * Method to check if Bioclipse has the right version that can be used to 
     * ensure it is new enough.
     *
     * @param version the minimum required Bioclipse version
     * @throws BioclipseException
     */
    public void requireVersion( String version ) throws BioclipseException {
        try {
            if (!(VersionNumberComparator.INSTANCE
                                         .compare( version, version() ) <= 0)) {
                throw new BioclipseException(
                              "You are running Bioclipse version " + version()
                              + ", but this script requires at least version " + version + ".");
            }
        } catch(Exception e) {
            throw new BioclipseException(e.getMessage(), e);
        }
    }

	@Override
	public String getManagerName() {
		return "bioclipse";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1186/1471-2105-8-59");
		dois.add("10.1186/1471-2105-10-397");
		return dois;
	}

}
