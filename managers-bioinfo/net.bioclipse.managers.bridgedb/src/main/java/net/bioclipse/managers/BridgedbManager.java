/* Copyright (c) 2011,2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.DataSourcePatterns;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.bio.Organism;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager that provides identifier mapping functionality
 * using the BridgeDb framework.
 */
public class BridgedbManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link BridgedbManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public BridgedbManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		DataSourceTxt.init();
	}

	/**
	 * Looks up the {@link DataSource} for the given Bioregistry.io prefix.
	 *
	 * @param source  the system code
	 * @return        the matching {@link DataSource}
	 * @throws BioclipseException
	 */
    public DataSource getSourceByPrefix(String source) throws BioclipseException {
        return DataSource.getExistingByBioregistryPrefix(source);
    }

	/**
	 * Looks up the {@link DataSource} for the given system code.
	 *
	 * @param source  the system code
	 * @return        the matching {@link DataSource}
	 * @throws BioclipseException
	 */
    public DataSource getSource(String source) throws BioclipseException {
        return DataSource.getExistingBySystemCode(source);
    }

	/**
	 * Looks up the {@link DataSource} for the given full name.
	 *
	 * @param name    the full name
	 * @return        the matching {@link DataSource}
	 * @throws BioclipseException
	 */
    public DataSource getSourceFromName(String name) throws BioclipseException {
        return DataSource.getExistingByFullName(name);
    }

    /**
     * Returns all known sources as a Java {@link List}.
     *
     * @return a Java {@link List}
     */
    public List<String> listAllSources() {
    	List<String> sourceCodes = new ArrayList<String>();
    	for (DataSource source : DataSource.getDataSources()) {
    		String code = source.getSystemCode();
    		if (code != null && code.length() > 0) sourceCodes.add(code);
    	}
    	return sourceCodes;
    }

    /**
     * Returns all known organisms as a Java {@link List}.
     *
     * @return a Java {@link List}
     */
    public List<Organism> listAllOrganisms() {
    	List<Organism> organisms = new ArrayList<Organism>();
    	for (Organism organism : Organism.values()) organisms.add(organism);
    	return organisms;
    }

    /**
     * Searches in the given database for hits, with a given maximum.
     * 
     * @param database  the BridgeDb {@link IDMapper} to search in
     * @param query     the content to search for
     * @param limit     the maximum number of hits
     * @return          the search results as a Java {@link List}
     * @throws BioclipseException
     */
    public List<String> search(IDMapper database, String query, int limit) throws BioclipseException {
		try {
			return extractIdentifierStrings(database.freeSearch(query, limit));
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not search in the IDMapper: " + exception, exception);
		}
    }

    /**
     * Based on the given identifier, tries to guess what database that identifier comes from.
     * 
     * @param identifier the identifier to guess 
     * @return a Java {@link List} is possible data sources
     * @throws BioclipseException
     */
    public List<DataSource> guessIdentifierType(String identifier) throws BioclipseException {
        Map<DataSource, Pattern> patterns = DataSourcePatterns.getPatterns();

    	List<DataSource> sources = new ArrayList<DataSource>();
    	for (DataSource source : patterns.keySet()) {
    	        Matcher matcher = patterns.get(source).matcher(identifier);
    	        if (matcher.matches()) sources.add(source);
    	}
    	return sources;
    }

    /**
     * Using the given connection string, it returns mappings for the given identifier.
     *
     * @param restService the connection string
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public List<String> map(String restService, String identifier, String source) throws BioclipseException {
    	try {
			Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
		} catch (ClassNotFoundException e) {
			throw new BioclipseException("Could not load the BridgeDb REST client: " + e.getMessage(), e);
		}
    	return map(restService, identifier, source, null);
    }

    /**
     * Using the given connection string, it returns mappings for the given identifier.
     *
     * @param restService the connection string
     * @param identifier  the identifier to return mappings for
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public Set<Xref> map(String restService, Xref identifier) throws BioclipseException {
        try {
			Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
		} catch (ClassNotFoundException e) {
			throw new BioclipseException("Could not load the BridgeDb REST client: " + e.getMessage(), e);
		}
        return map(restService, identifier, null);
    }

    /**
     * Using the given {@link IDMapper} string, it returns mappings for the given identifier.
     *
     * @param database    the {@link IDMapper} to get the mappings from
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public List<String> map(IDMapper database, String identifier, String source) throws BioclipseException {
    	return map(database, identifier, source, null);
    }

    /**
     * Using the given connection string, it returns mappings for the given identifier, but only for the
     * given target data source.
     *
     * @param restService the connection string
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @param target      the data source for which to return mappings
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public List<String> map(String restService, String identifier, String source, String target) throws BioclipseException {
    	// now we connect to the driver and create a IDMapper instance.
		try {
			IDMapper mapper;
			if (restService.startsWith("idmapper-bridgerest:")) {
				mapper = BridgeDb.connect(restService);
			} else {
				mapper = BridgeDb.connect("idmapper-bridgerest:" + restService);
			}
			return map(mapper, identifier, source, target);
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not connect to the REST service at: " + restService, exception);
		}		
    }

    /**
     * Using the given connection string, it returns mappings for the given identifier, but only for the
     * given target data source.
     *
     * @param restService the connection string
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @param target      the data source for which to return mappings
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public Set<Xref> map(String restService, Xref identifier, String target) throws BioclipseException {
        // now we connect to the driver and create a IDMapper instance.
		try {
			IDMapper mapper;
			if (restService.startsWith("idmapper-bridgerest:")) {
				mapper = BridgeDb.connect(restService);
			} else {
				mapper = BridgeDb.connect("idmapper-bridgerest:" + restService);
			}
			return map(mapper, identifier, target);
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not connect to the REST service at: " + restService, exception);
		}
    }

    /**
     * Using the given connection string, it returns mappings for the given identifier, but only for the
     * given target data source.
     *
     * @param restService the REST server, or the full connection string
     * @param query       what to search for
     * @param limit       maximal number of identifiers to be found
     * @return            a Java {@link List} of found identifiers
     * @throws BioclipseException
     */
    public List<String> search(String restService, String query, int limit) throws BioclipseException {
        try {
            Class.forName ("org.bridgedb.webservice.bridgerest.BridgeRest");
        } catch (ClassNotFoundException e) {
            throw new BioclipseException("Could not load the BridgeDb REST client: " + e.getMessage(), e);
        }
    	// now we connect to the driver and create a IDMapper instance.
    	IDMapper mapper;
		try {
			if (restService.startsWith("idmapper-bridgerest:")) {
				mapper = BridgeDb.connect(restService);
			} else {
				mapper = BridgeDb.connect("idmapper-bridgerest:" + restService);
			}
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not connect to the REST service at: " + restService);
		}

		return search(mapper, query, limit);
    }

    /**
     * Using the given {@link IDMapper} string, it returns mappings for the given identifier, but only for the
     * given target data source.
     *
     * @param database    the {@link IDMapper} to get the mappings from
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @param target      the data source for which to return mappings
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public List<String> map(IDMapper database, String identifier, String source, String target) throws BioclipseException {
    	// We create an Xref instance for the identifier that we want to look up.
    	DataSource sourceObj = getSource(source);
    	Xref src = new Xref(identifier, sourceObj);
    	Set<Xref> dests = map(database, src, target);

    	// and create a list of found, mapped URNs
    	return extractIdentifierStrings(dests);
    }

    /**
     * Using the given {@link IDMapper} string, it returns mappings for the given identifier.
     *
     * @param database    the {@link IDMapper} to get the mappings from
     * @param source      the identifier to return mappings for
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public Set<Xref> map(IDMapper database, Xref source) throws BioclipseException {
    	return map(database, source, null);
    }

    /**
     * Using the given {@link IDMapper} string, it returns mappings for the given identifier, but only for the
     * given target data source.
     *
     * @param database    the {@link IDMapper} to get the mappings from
     * @param source      the identifier to return mappings for
     * @param target      the data source for which to return mappings
     * @return            a Java {@link List} of mapped identifiers
     * @throws BioclipseException
     */
    public Set<Xref> map(IDMapper database, Xref source, String target) throws BioclipseException {
    	Set<Xref> dests;

    	// let's see if there are cross-references in the target database
    	if (target != null) {
        	DataSource targetObj = getSource(target);
    		try {
    			dests = database.mapID(source, targetObj);
    		} catch (IDMapperException exception) {
    			throw new BioclipseException(
    				"Error while mapping the identifier: " + exception.getMessage()
    			);
    		}
    	} else {
    		try {
    			dests = database.mapID(source);
    		} catch (IDMapperException exception) {
    			throw new BioclipseException(
    				"Error while mapping the identifier: " + exception.getMessage()
    			);
    		}
    	}

    	// and create a list of found, mapped URNs
    	return Collections.unmodifiableSet(dests);
    }

    private List<String> extractIdentifierStrings(Set<Xref> dests) {
		List<String> results = new ArrayList<String>();
    	for (Xref dest : dests)
    	    results.add(dest.getMiriamURN());
		return results;
	}

    /**
     * Creates a {@link Xref} object for the given Bioregistry.io compact identifier.
     *
     * @param sourcedIdentifier  the identifier
     * @return                   an {@link Xref} object
     * @throws BioclipseException
     */
	public Xref compactIdentifier(String compactIdentifier) throws BioclipseException {
		int index = compactIdentifier.indexOf(':');
		if (index < 0) throw new BioclipseException("Unexpected format. Use something like \"ncbigene:12345\".");

		String identifier = compactIdentifier.substring(index + 1);
		String source = compactIdentifier.substring(0, index);
		return new Xref(identifier, getSourceByPrefix(source));
	}

    /**
     * Creates a {@link Xref} object for the given identifier.
     *
     * @param sourcedIdentifier  the identifier
     * @return                   an {@link Xref} object
     * @throws BioclipseException
     */
	public Xref xref(String sourcedIdentifier) throws BioclipseException {
		int index = sourcedIdentifier.indexOf(':'); 
		if (index < 0) throw new BioclipseException("Unexpected format. Use something like \"Wi:Aspirin\".");

		String identifier = sourcedIdentifier.substring(index + 1);
		String source = sourcedIdentifier.substring(0, index);
		return new Xref(identifier, getSource(source));
	}

    /**
     * Creates a {@link Xref} object for the given identifier.
     * 
     * @param identifier  the identifier to return mappings for
     * @param source      the data source of the identifier to return mappings for
     * @return            an {@link Xref} object
     * @throws BioclipseException
     */
	public Xref xref(String identifier, String source) throws BioclipseException {
		return new Xref(identifier, getSource(source));
	}

	/**
	 * Creates a BridgeDb {@link IDMapper} for the given Derby database location.
	 *
	 * @param location  the location of the Derby file
	 * @return          the {@link IDMapper} object
	 * @throws BioclipseException
	 */
	public IDMapper loadRelationalDatabase(String location) throws BioclipseException {
		try {
			Class.forName ("org.bridgedb.rdb.IDMapperRdb");
		} catch (ClassNotFoundException exception) {
			throw new BioclipseException("Could not load the IDMapperRdb driver.", exception);
		}
		try {
			return BridgeDb.connect("idmapper-pgdb:" + location);
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not the database at this location: " + location, exception);
		}
	}

	/**
	 * Creates a new {@link DataSource} based on the system code and full name.
	 *
	 * @param code      the system code for the new data source
	 * @param name      the full name for the new data source
	 * @return          the new {@link DataSource} object
	 */
	public DataSource registerDataSource(String code, String name) {
		return DataSource.register(code, name).asDataSource();
	}

	/**
	 * Returns IDMappers supported by Bacting.
	 *
	 * @return a {@link List} with {@link IDMapper}s
	 */
    public List<String> listIDMapperProviders() {
        return Collections.emptyList();
        // example: "Gene ID Mapping Database (Homo sapiens)"
    }

    /**
     * Returns the {@link IDMapper} for the given provider.
     *
     * @param provider name of the provider, for example "Gene ID Mapping Database (Homo sapiens)"
     * @return the IDMapper for the given provider
     * 
     * @throws BioclipseException when the mapping database could not be properly loaded
     */
    public IDMapper getIDMapper(String provider) throws BioclipseException {
    	return null;
    }

	@Override
	public String getManagerName() {
		return "bridgedb";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1186/1471-2105-11-5");
		return dois;
	}
}
