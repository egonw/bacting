/* Copyright (c) 2009-2022  Egon Willighagen <egonw@users.sf.net>
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.NoReaderForLangException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.SyntaxError;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.shex.ShapeMap;
import org.apache.jena.shex.ShapeMap.Builder;
import org.apache.jena.shex.Shex;
import org.apache.jena.shex.ShexReport;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.ShexValidator;
import org.apache.jena.shex.sys.ShexLib;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.core.runtime.CoreException;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.StringMatrixHelper;
import net.bioclipse.rdf.business.IJenaDatasetStore;
import net.bioclipse.rdf.business.IJenaStore;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.JenaModel;
import net.bioclipse.rdf.business.TDBModel;

/**
 * Bioclipse manager that provides functionality around the Resource
 * Description Framework standard. It allows creating in memory and
 * on disk triple stores, creating on content, and IO functionality.
 */
public class RDFManager {

    public static final Integer CONNECT_TIME_OUT = 5000; 
    public static final Integer READ_TIME_OUT = 30000; 

	private String workspaceRoot;

	/**
     * Creates a new {@link RDFManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public RDFManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    /**
     * Creates a triple store that is fully stored in memory.
     *
     * @return a triple store as {@link IRDFStore} object
     */
    public IRDFStore createInMemoryStore() {
    	return new JenaModel();
    }

    /**
     * Creates a triple store that is fully stored on disk.
     *
     * @return a triple store as {@link IRDFStore} object
     */
    public IRDFStore createStore(String tripleStoreDirectoryPath) {
    	return new TDBModel(tripleStoreDirectoryPath);
    }

    /**
     * Creates an ontology triple store that is fully stored in memory.
     *
     * @return a triple store as {@link IRDFStore} object
     */
    public IRDFStore createInMemoryStore(boolean ontologyModel) {
    	return new JenaModel(ontologyModel);
    }

    /**
     * Adds a prefix definition to the given triple store.
     *
     * @param store     an {@link IRDFStore} object
     * @param prefix    the prefix
     * @param namespace the RDF namespace
     * @throws BioclipseException
     */
    public void addPrefix(IRDFStore store, String prefix, String namespace)
        throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new BioclipseException("Only supporting IJenaStore.");
        ((IJenaStore)store).getModel().setNsPrefix(prefix, namespace);
    }

    /**
     * Lists all resources or literals for the resource and predicate.
     *
     * @param store        an {@link IRDFStore} object
     * @param resourceURI  subject of the triples
     * @param predicate    predicate of the triples
     * @return             List of objects (resources and literals).
     */
    public List<String> getForPredicate(IRDFStore store, String resourceURI, String predicate) {
        StringMatrix results = sparql(store,
            "SELECT DISTINCT ?object WHERE {" +
            " <" + resourceURI + "> <" + predicate + "> ?object" +
            "}"
        );
        if (results.getRowCount() == 0) return Collections.emptyList();
        return results.getColumn("object");
    }

    /**
     * Lists all resources that are owl:sameAs as the given resource.
     * 
     * @param  store           the {@link IRDFStore} store where the identical classes are looked up
     * @param  resourceURI     the resource to find the links for
     * @return                 All matching resources.
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public List<String> allOwlSameAs(IRDFStore store, String resourceURI)
    throws IOException, BioclipseException, CoreException {
    	Set<String> resources = new HashSet<String>();
    	resources.add(resourceURI);
    	// implements a non-reasoning sameAs reasoner:
    	// keep looking up sameAs relations, until we find no new ones
    	List<String> newLeads = allOwlSameAsOneDown(store, resourceURI);
    	newLeads.removeAll(resources); //
    	while (newLeads.size() > 0) {
    		List<String> newResources = new ArrayList<String>();
        	for (String resource : newLeads) {
        		System.out.println("Trying: " + resource);
        		if (!resources.contains(resource)) {
        			System.out.println("New: " + resource);
        			resources.add(resource);
        			newResources.addAll(
        				allOwlSameAsOneDown(store, resource)
        			);
        		}
        	}
        	newResources.removeAll(resources);
			newLeads = newResources;
    	}
    	List<String> finalList = new ArrayList<String>();
    	finalList.addAll(resources);
    	finalList.remove(resourceURI); // remove the source resource
    	return finalList;
    }

    /**
     * Lists all resources that are owl:equivalentClass as the given resource.
     * 
     * @param  store        the {@link IRDFStore} store where the equivalent classes are looked up
     * @param  resourceURI  the resource to find the links for
     * @return              All matching resources.
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public List<String> allOwlEquivalentClass(IRDFStore store, String resourceURI)
    throws IOException, BioclipseException, CoreException {
    	Set<String> resources = new HashSet<String>();
    	resources.add(resourceURI);
    	// implements a non-reasoning owl:equivalentClass reasoner:
    	// keep looking up equivalentClass relations, until we find no new ones
    	List<String> newLeads = allOwlEquivalentClassOneDown(store, resourceURI);
    	newLeads.removeAll(resources); //
    	while (newLeads.size() > 0) {
    		List<String> newResources = new ArrayList<String>();
        	for (String resource : newLeads) {
        		System.out.println("Trying: " + resource);
        		if (!resources.contains(resource)) {
        			System.out.println("New: " + resource);
        			resources.add(resource);
        			newResources.addAll(
        				allOwlEquivalentClassOneDown(store, resource)
        			);
        		}
        	}
        	newResources.removeAll(resources);
			newLeads = newResources;
    	}
    	List<String> finalList = new ArrayList<String>();
    	finalList.addAll(resources);
    	finalList.remove(resourceURI); // remove the source resource
    	return finalList;
    }
    
    private List<String> allOwlSameAsOneDown(IRDFStore store, String resourceURI)
    throws IOException, BioclipseException, CoreException {
    	// got no reasoner, so need implement inverse relation manually
    	String sparql =
    		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    		"SELECT ?resource WHERE {" +
    		"  <" + resourceURI + "> owl:sameAs ?resource ." +
    		"}";
    	StringMatrix results = sparql(store, sparql);
    	if (results.getRowCount() == 0) return Collections.emptyList();
    	List<String> resources = results.getColumn("resource");
    	sparql =
    		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    		"SELECT ?resource WHERE {" +
    		"  ?resource  owl:sameAs <" + resourceURI + ">." +
    		"}";
    	results = sparql(store, sparql);
    	if (results.getRowCount() == 0) return resources;
    	resources.addAll(results.getColumn("resource"));
    	return resources;
    }

    private List<String> allOwlEquivalentClassOneDown(IRDFStore store, String resourceURI)
    throws IOException, BioclipseException, CoreException {
    	// got no reasoner, so need implement inverse relation manually
    	String sparql =
    		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    		"SELECT ?resource WHERE {" +
    		"  <" + resourceURI + "> owl:equivalentClass ?resource ." +
    		"}";
    	StringMatrix results = sparql(store, sparql);
    	if (results.getRowCount() == 0) return Collections.emptyList();
    	List<String> resources = results.getColumn("resource");
    	sparql =
    		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    		"SELECT ?resource WHERE {" +
    		"  ?resource  owl:equivalentClass <" + resourceURI + ">." +
    		"}";
    	results = sparql(store, sparql);
    	if (results.getRowCount() == 0) return resources;
    	resources.addAll(results.getColumn("resource"));
    	return resources;
    }

    /**
     * Creates a new RDF object triple in the given triple store.
     *
     * @param store    the {@link IRDFStore} store where the triple is added
     * @param subject  the RDF Subject of the triple
     * @param property the RDF Predicate of the triple
     * @param object   the RDF Object of the triple
     * @throws BioclipseException
     */
    public void addObjectProperty(IRDFStore store,
        String subject, String property, String object)
    throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        Model model = ((IJenaStore)store).getModel();
        Resource subjectRes = model.createResource(subject);
        Property propertyRes = model.createProperty(property);
        Resource objectRes = model.createResource(object);
        model.add(subjectRes, propertyRes, objectRes);
    }

    /**
     * Creates a new RDF data triple in the given triple store.
     *
     * @param store    the {@link IRDFStore} store where the triple is added
     * @param subject  the RDF Subject of the triple
     * @param property the RDF Predicate of the triple
     * @param value    the RDF Literal string
     * @throws BioclipseException
     */
    public void addDataProperty(IRDFStore store, String subject,
        String property, String value) throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        Model model = ((IJenaStore)store).getModel();
        Resource subjectRes = model.createResource(subject);
        Property propertyRes = model.createProperty(property);
        model.add(subjectRes, propertyRes, value);
    }

    /**
     * Creates a new RDF data triple in the given triple store.
     *
     * @param store    the {@link IRDFStore} store where the triple is added
     * @param subject  the RDF Subject of the triple
     * @param property the RDF Predicate of the triple
     * @param value    the RDF Literal string
     * @param dataType the data type of the RDF Literal
     * @throws BioclipseException
     */
    public void addTypedDataProperty(IRDFStore store,
        String subject, String property, String value,
        String dataType)
    throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        Model model = ((IJenaStore)store).getModel();
        Resource subjectRes = model.createResource(subject);
        Property propertyRes = model.createProperty(property);
        model.add(subjectRes, propertyRes, model.createTypedLiteral(value, dataType));
    }

    /**
     * Creates a new RDF data triple in the given triple store.
     *
     * @param store    the {@link IRDFStore} store where the triple is added
     * @param subject  the RDF Subject of the triple
     * @param property the RDF Predicate of the triple
     * @param value    the RDF Literal string
     * @param language the language of the RDF Literal
     * @throws BioclipseException
     */
   public void addPropertyInLanguage(IRDFStore store,
        String subject, String property, String value,
        String language)
    throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        Model model = ((IJenaStore)store).getModel();
        Resource subjectRes = model.createResource(subject);
        Property propertyRes = model.createProperty(property);
        model.add(subjectRes, propertyRes, value, language);
    }

   /**
    * Queries a remote SPARQL end point.
    * 
    * @param serviceURL        the URL of the SPARQL end point
    * @param sparqlQueryString the SPARQL query
    * @return                  an {@link StringMatrix} object with results
    */
    public StringMatrix sparqlRemote(
            String serviceURL,
            String sparqlQueryString) {
         Query query = QueryFactory.create(sparqlQueryString);
         QueryExecutionHTTPBuilder qexecBuilder = QueryExecutionHTTPBuilder.service(serviceURL)
             .param("timeout", "" + CONNECT_TIME_OUT).query(query);
         PrefixMapping prefixMap = query.getPrefixMapping();

         StringMatrix table = null;
         try {
        	 QueryExecution qexec = qexecBuilder.build();
             ResultSet results = qexec.execSelect();
             table = StringMatrixHelper.convertIntoTable(prefixMap, results);
         } finally {}
         return table;
     }

    /**
     * Processes XML returned by a remote SPARQL end point.
     *
     * @param queryResults   the search results as a byte array of the returned XML
     * @param originalQuery  the original SPARQL query that gave the results
     * @return a matrix with SPARQL results
     * @throws BioclipseException
     */
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

    /**
     * Queries a local RDF triple store.
     * 
     * @param store        the RDF triples store to query
     * @param queryString  the SPARQL query
     * @return             an {@link StringMatrix} object with results
     */
    public StringMatrix sparql(IRDFStore store, String queryString) {
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

    /**
     * Returns the number of RDF triples in the triple store.
     *  
     * @param store an {@link IRDFStore} object
     * @return      the number of triples
     * @throws BioclipseException
     */
    public long size(IRDFStore store) throws BioclipseException {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );

        long size = 0;
        if (store instanceof IJenaDatasetStore) {
        	Dataset dataset = ((IJenaDatasetStore)store).getDataset();
        	if (dataset.isInTransaction()) dataset.end();
        	dataset.begin(ReadWrite.READ);
        	size = ((IJenaStore)store).getModel().size();
        	dataset.end();
        } else {
        	size = ((IJenaStore)store).getModel().size();
        }
        return size;
    }

    /**
     * Reads a RDF file in the given format from the workspace and stores
     * the triples in the given triple store.
     *
     * @param store   {@link IRDFStore} to put the triples in
     * @param rdfFile location of the RDF file
     * @param format  format of the RDF file (e.g. "RDF/XML", "TURTLE", or "N3")
     * @return an RDF store with the content of the loaded RDF file
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public IRDFStore importFile(IRDFStore store, String rdfFile, String format)
    throws IOException, BioclipseException, CoreException {
    	return importFromStream(store, new FileInputStream(workspaceRoot + rdfFile), format);
    }

    /**
     * Reads RDF triples from an {@link InputStream} in the given format and stores
     * the triples in the given triple store.
     *
     * @param store   {@link IRDFStore} to put the triples in
     * @param stream  the {@link InputStream} from which the triples are read
     * @param format  format of the RDF file (e.g. "RDF/XML", "TURTLE", or "N3")
     * @return an RDF store with the content of the loaded RDF content
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public IRDFStore importFromStream(IRDFStore store, InputStream stream,
            String format)
    throws IOException, BioclipseException, CoreException {
        if (format == null) format = "RDF/XML";

        if (!(store instanceof IJenaStore))
            throw new RuntimeException(
                "Can only handle IJenaStore's for now."
            );
        
        if ("TRIG".equals(format)) {
           if (store instanceof IJenaDatasetStore) {
               IJenaDatasetStore datasetStore = (IJenaDatasetStore)store;
               Dataset dataset = datasetStore.getDataset();
               dataset.begin(ReadWrite.WRITE);
               RDFDataMgr.read(dataset, stream, Lang.TRIG);
               dataset.commit();
           } else {
               throw new BioclipseException(
                   "TRIG files can only be loaded into an IJenaDatasetStore."
               );
           }
        } else {
           Model model = ((IJenaStore)store).getModel();
           try {
               model.read(stream, "", format);
           } catch (SyntaxError error) {
               throw new BioclipseException(
                   "File format is not correct.", error
               );
           } catch (NoReaderForLangException exception) {
               throw new BioclipseException(
                   "Unknown file format. Supported are \"RDF/XML\", " +
                   "\"N-TRIPLE\", \"TURTLE\" and \"N3\".",
                   exception
               );
           }
        }
        return store;
    }

    /**
     * Reads RDF triples from an {@link String} in the given format and stores
     * the triples in the given triple store.
     *
     * @param store      {@link IRDFStore} to put the triples in
     * @param rdfContent the {@link String} from which the triples are read
     * @param format     format of the RDF file (e.g. "RDF/XML", "TURTLE", or "N3")
     * @return an RDF store with the content of the loaded RDF content
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public IRDFStore importFromString(IRDFStore store, String rdfContent,
            String format)
    throws IOException, BioclipseException, CoreException {
    	InputStream input = new ByteArrayInputStream(rdfContent.getBytes());
    	return importFromStream(store, input, format);
    }

    /**
     * Reads RDF triples from a URL and stores
     * the triples in the given triple store.
     *
     * @param store      {@link IRDFStore} to put the triples in
     * @param url        the URL from which the triples are read
     * @return an RDF store with the content of the loaded RDF file
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public IRDFStore importURL(IRDFStore store, String url)
            throws IOException, BioclipseException, CoreException {
    	return importURL(store, url, null);
    }

    /**
     * Reads RDF triples from a URL and stores the triples in the given
     * triple store, with additional HTTP headers.
     *
     * @param store        {@link IRDFStore} to put the triples in
     * @param url          the URL from which the triples are read
     * @param extraHeaders the extra HTTP headers
     * @return an RDF store with the content of the loaded RDF file
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
    public IRDFStore importURL(IRDFStore store, String url,
    		Map<String, String> extraHeaders)
        throws IOException, BioclipseException, CoreException {
       	URL realURL = new URL(url);
       	HttpURLConnection connection = (HttpURLConnection)realURL.openConnection();
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        connection.setRequestProperty("User-Agent", "Bacting (https://joss.theoj.org/papers/10.21105/joss.02558)");
        connection.setRequestProperty(
            "Accept", "application/xml, application/rdf+xml"
        );
        // set the extra headers
        if (extraHeaders != null) {
        	for (String key : extraHeaders.keySet()) {
        		connection.setRequestProperty(key, extraHeaders.get(key));
        	}
        }
        
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||  // 302
            	status == HttpURLConnection.HTTP_MOVED_PERM ||  // 301
                status == HttpURLConnection.HTTP_SEE_OTHER) {   // 303
            	realURL = new URL(connection.getHeaderField("Location"));
            	connection = (HttpURLConnection)realURL.openConnection();
            	connection.setConnectTimeout(CONNECT_TIME_OUT);
            	connection.setReadTimeout(READ_TIME_OUT);
            	connection.setRequestProperty("User-Agent", "Bacting (https://joss.theoj.org/papers/10.21105/joss.02558)");
            	connection.setRequestProperty(
            			"Accept", "application/xml, application/rdf+xml"
            			);
            	// set the extra headers
            	if (extraHeaders != null) {
            		for (String key : extraHeaders.keySet()) {
            			connection.setRequestProperty(key, extraHeaders.get(key));
            		}
            	}
            }
        }
        
        try {
            InputStream stream = connection.getInputStream();
            importFromStream(store, stream, null);
            stream.close();
        } catch (UnknownHostException exception) {
            throw new BioclipseException(
                "Unknown or unresponsive host: " + realURL.getHost(), exception
            );
        }
        return store;
    }

    /**
     * Serializes the triples in the triple store as Notation3.
     *
     * @param store the {@link IRDFStore}
     * @return      a {@link String} with Notation3-formatted triples
     * @throws BioclipseException
     */
    public String asRDFN3(IRDFStore store)
    throws BioclipseException {
    	return asRDF(store, "N3");
    }

    /**
     * Serializes the triples in the triple store as Turtle.
     *
     * @param store the {@link IRDFStore}
     * @return      a {@link String} with Turtle-formatted triples
     * @throws BioclipseException
     */
    public String asTurtle(IRDFStore store)
    throws BioclipseException {
    	return asRDF(store, "TURTLE");
    }

    private String asRDF(IRDFStore store, String type)
    throws BioclipseException {
    	try {
    		ByteArrayOutputStream output = new ByteArrayOutputStream();
    		if (store instanceof IJenaStore) {
    			Model model = ((IJenaStore)store).getModel();
    			model.write(output, type);
    			output.close();
    			String result = new String(output.toByteArray());
    	    	return result;
    		} else {
    			throw new BioclipseException("Only supporting IJenaStore!");
    		}
    	} catch (IOException e) {
    		throw new BioclipseException("Error while writing RDF.", e);
    	}
    }

    /**
     * Validates triples according to some shape expression in ShEx format.
     *
     * @param store     the {@link IRDFStore}
     * @param shexFile  the file with shape expressions
     * @param shapeURI  URI of the shape to test against
     * @param type      URI of the rdf:type of the resources to test
     * @return          the ShEx validation report as {@link ShexReport}
     */
    public ShexReport validateAllOfType(IRDFStore store, String shexFile, String shapeURI, String type) {
        if (!(store instanceof IJenaStore))
            throw new RuntimeException("Can only handle IJenaStore's for now.");

        Graph dataGraph = ((IJenaStore)store).getModel().getGraph();
        ShexSchema shapes = Shex.readSchema(workspaceRoot + shexFile);
        Builder shapeMapBuilder = ShapeMap.newBuilder();
        Triple instance = Triple.create(Shex.FOCUS, RDF.type.asNode(), NodeFactory.createURI(type));
        shapeMapBuilder.add(instance, NodeFactory.createURI(shapeURI));
        ShapeMap shapeMap = shapeMapBuilder.build();

        return ShexValidator.get().validate(dataGraph, shapes, shapeMap);
    }

    /**
     * Converts a {@link ShexReport} into a String.
     *
     * @param report the {@link ShexReport}
     * @return       String serialization of the report
     */
    public String asString(ShexReport report) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ShexLib.printReport(output, report);
        return output.toString();
    }

    /**
     * Lists all existing classes.
     *
     * @param store    the {@link IRDFStore}
     * @return         a {@link List} with all unique classes
     *
     * @throws BioclipseException when the {@link IRDFStore} could not be queried
     */
    public List<String> allClasses(IRDFStore store) throws BioclipseException {
        StringMatrix results = sparql(store,
            "SELECT DISTINCT ?class WHERE {" +
            " [] a ?class" +
            "}"
        );
        return results.getColumn("class");
    }

    public String saveRDF(IRDFStore store, String file, String type)
    	throws BioclipseException {
    	if (type == null && !"RDF/XML-ABBREV".equals(type) &&
    			!"N-TRIPLE".equals(type) &&
    			!"N3".equals(type))
    		throw new BioclipseException("Can only save RDF/XML-ABBREV, N3, " +
    				"and N-TRIPLE.");

    	File outputFile = new File(workspaceRoot + file);
    	if (outputFile.exists()) {
    		throw new BioclipseException("File already exists!");
    	}

    	try {
    		if (store instanceof IJenaStore) {
        		OutputStream output = new FileOutputStream(outputFile);
                if (store instanceof IJenaDatasetStore) {
                    ((IJenaDatasetStore)store).getDataset().begin(ReadWrite.READ);
                }
    			Model model = ((IJenaStore)store).getModel();
    			model.write(output, type);
    			output.close();
    		} else {
    			throw new BioclipseException("Only supporting IJenaStore!");
    		}
    	} catch (IOException e) {
    		throw new BioclipseException("Error while writing RDF.", e);
    	}

    	return file;
    };

    /**
     * Lists all existing predicates.
     *
     * @param store    the {@link IRDFStore}
     * @return         a {@link List} with all unique predicates
     *
     * @throws BioclipseException when the {@link IRDFStore} could not be queried
     */
    public List<String> allPredicates(IRDFStore store) throws BioclipseException {
        StringMatrix results = sparql(store,
            "SELECT DISTINCT ?predicate WHERE {" +
            " [] ?predicate []" +
            "}"
        );
        return results.getColumn("predicate");
    }

}
