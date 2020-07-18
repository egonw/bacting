/* Copyright (c) 2011  Egon Willighagen <egon.willighagen@gmail.com>
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
import org.bridgedb.bio.Organism;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

public class BridgedbManager implements IBactingManager {

	private String workspaceRoot;

	public BridgedbManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    public DataSource getSource(String source) throws BioclipseException {
    	return DataSource.getBySystemCode(source);
    }
    
    public DataSource getSourceFromName(String name) throws BioclipseException {
    	return DataSource.getByFullName(name);
    }

    public List<String> listAllSources() {
    	List<String> sourceCodes = new ArrayList<String>();
    	for (DataSource source : DataSource.getDataSources()) {
    		String code = source.getSystemCode();
    		if (code != null && code.length() > 0) sourceCodes.add(code);
    	}
    	return sourceCodes;
    }

    public List<Organism> listAllOrganisms() {
    	List<Organism> organisms = new ArrayList<Organism>();
    	for (Organism organism : Organism.values()) organisms.add(organism);
    	return organisms;
    }

    public List<String> search(IDMapper database, String query, int limit) throws BioclipseException {
		try {
			return extractIdentifierStrings(database.freeSearch(query, limit));
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not search in the IDMapper: " + exception);
		}
    }

    public List<DataSource> guessIdentifierType(String identifier) throws BioclipseException {
    	Map<DataSource, Pattern> patterns = DataSourcePatterns.getPatterns();

    	List<DataSource> sources = new ArrayList<DataSource>();
    	for (DataSource source : patterns.keySet()) {
    	        Matcher matcher = patterns.get(source).matcher(identifier);
    	        if (matcher.matches()) sources.add(source);
    	}
    	return sources;
    }

    public List<String> map(String restService, String identifier, String source) throws BioclipseException {
    	return map(restService, identifier, source, null);
    }

    public List<String> map(IDMapper database, String identifier, String source) throws BioclipseException {
    	return map(database, identifier, source, null);
    }

    public List<String> map(String restService, String identifier, String source, String target) throws BioclipseException {
    	// now we connect to the driver and create a IDMapper instance.
    	IDMapper mapper;
		try {
			mapper = BridgeDb.connect(restService);
		} catch (IDMapperException exception) {
			throw new BioclipseException("Could not connect to the REST service at: " + restService);
		}
		
		return map(mapper, identifier, source, target);
    }

    public List<String> map(IDMapper database, String identifier, String source, String target) throws BioclipseException {
    	// We create an Xref instance for the identifier that we want to look up.
    	DataSource sourceObj = getSource(source);
    	Xref src = new Xref(identifier, sourceObj);
    	Set<Xref> dests = map(database, src, target);

    	// and create a list of found, mapped URNs
    	return extractIdentifierStrings(dests);
    }

    public Set<Xref> map(IDMapper database, Xref source) throws BioclipseException {
    	return map(database, source, null);
    }

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
    	    results.add(dest.getURN());
		return results;
	}

	public Xref xref(String sourcedIdentifier) throws BioclipseException {
		int index = sourcedIdentifier.indexOf(':'); 
		if (index < 0) throw new BioclipseException("Unexpected format. Use something like \"Wi:Aspirin\".");

		String identifier = sourcedIdentifier.substring(index + 1);
		String source = sourcedIdentifier.substring(0, index);
		return new Xref(identifier, getSource(source));
	}

	public Xref xref(String identifier, String source) throws BioclipseException {
		return new Xref(identifier, getSource(source));
	}

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
