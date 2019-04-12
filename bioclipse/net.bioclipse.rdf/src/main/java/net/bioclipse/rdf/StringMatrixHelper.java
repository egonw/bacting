/* Copyright (c) 2009-2011  Egon Willighagen <egonw@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.rdf;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

import net.bioclipse.core.domain.StringMatrix;

/**
 * Helper class to convert RDF/Jena stuff into {@link StringMatrix}s.
 * 
 * @author egonw
 */
public class StringMatrixHelper {

	/**
	 * Converts SPARQL results in a {@link ResultSet} into a string matrix.
	 * 
	 * @param prefixMap
	 * @param results
	 * @return
	 */
    public static StringMatrix convertIntoTable(
            PrefixMapping prefixMap, ResultSet results) {
    	StringMatrix table = new StringMatrix();
    	int rowCount = 0;
    	List<String> resultVarNames = results.getResultVars();
        while (results.hasNext()) {
        	rowCount++;
            QuerySolution soln = results.nextSolution();
            Iterator<String> varNames = resultVarNames.iterator();
            while (varNames.hasNext()) {
            	String varName = varNames.next();
            	int colCount = -1;
            	if (table.hasColumn(varName)) {
            		colCount = table.getColumnNumber(varName);
            	} else {
            		colCount = table.getColumnCount() + 1;
            		table.setColumnName(colCount, varName);
            	}
                RDFNode node = soln.get(varName);
                if (node != null) {
                    String nodeStr = node.toString();
                    if (node.isAnon()) {
                    	Resource resource = (Resource)node;
                    	table.set(rowCount, colCount, resource.getId().getLabelString());
                    } else if (node.isResource()) {
                        Resource resource = (Resource)node;
                        // the resource.getLocalName() is not accurate, so I
                        // use some custom code
                        String[] uriLocalSplit = split(prefixMap, resource);
                        if (uriLocalSplit[0] == null) {
                        	if (resource.getURI() != null) {
                        		table.set(rowCount, colCount, resource.getURI());
                        	} else {
                        		// anonymous node
                        		table.set(rowCount, colCount, "" + resource.hashCode());
                        	}
                        } else {
                        	table.set(rowCount, colCount,
                                uriLocalSplit[0] + ":" + uriLocalSplit[1]
                            );
                        }
                    } else if (node.isLiteral()) {
                    	// properly get rid of language and typing info
                    	Literal litNode = node.asLiteral();
                    	nodeStr = litNode.getString();
                    	table.set(rowCount, colCount, nodeStr);
                    } else {
                    	table.set(rowCount, colCount, nodeStr);
                    }
                }
            }
        }
        return table;
    }

    /**
     * Helper method that splits up a URI into a namespace and a local part.
     * It uses the prefixMap to recognize namespaces, and replaces the
     * namespace part by a prefix.
     */
    public static String[] split(PrefixMapping prefixMap, Resource resource) {
        String uri = resource.getURI();
        if (uri == null) {
            return new String[] {null, null};
        }
        Map<String,String> prefixMapMap = prefixMap.getNsPrefixMap();
        Set<String> prefixes = prefixMapMap.keySet();
        String[] split = { null, null };
        for (String key : prefixes){
            String ns = prefixMapMap.get(key);
            if (uri.startsWith(ns)) {
                split[0] = key;
                split[1] = uri.substring(ns.length());
                return split;
            }
        }
        split[1] = uri;
        return split;
    }

}
