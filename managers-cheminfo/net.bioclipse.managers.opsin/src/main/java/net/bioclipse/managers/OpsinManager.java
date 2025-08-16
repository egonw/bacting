/* Copyright (c) 2010,2020,2024-2025  Egon Willighagen <egon.willighagen@gmail.com>
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;
import uk.ac.cam.ch.wwmm.opsin.ParseRulesResults;
import uk.ac.cam.ch.wwmm.opsin.ParseTokens;
import uk.ac.cam.ch.wwmm.opsin.ParsingException;

/**
 * Bioclipse manager that wraps OPSIN functionality for processing
 * IUPAC names.
 */
public class OpsinManager implements IBactingManager {

	private CDKManager cdk;
	private NameToStructure nameToStructure;

	/**
     * Creates a new {@link OpsinManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public OpsinManager(String workspaceRoot) {
		this.cdk = new CDKManager(workspaceRoot);
	}

	/**
	 * Parses a IUPAC name into a molecule.
	 *
	 * @param iupacName the IUPAC name
	 * @return          the molecule as {@link ICDKMolecule}
	 * @throws BioclipseException
	 */
	public ICDKMolecule parseIUPACName(String iupacName) 
	                    throws BioclipseException {
		return cdk.fromSMILES(parseIUPACNameAsSMILES(iupacName));
	}

	/**
	 * Parses a IUPAC name into a molecule.
	 *
	 * @param iupacName the IUPAC name
	 * @return          the molecule as CML string
	 * @throws BioclipseException
	 */
	public String parseIUPACNameAsCML(String iupacName) 
	              throws BioclipseException {
        OpsinResult result = getNameToStructureInstance().parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
        	return result.getCml();
        }
        throw new BioclipseException(
        	"Could not parse the IUPAC name (" + iupacName + "), because: " +
        	result.getMessage()
        );
	}

	private NameToStructure getNameToStructureInstance() throws BioclipseException {
		if (this.nameToStructure == null) {
			try {
				this.nameToStructure = NameToStructure.getInstance();
			} catch (NameToStructureException e) {
				throw new BioclipseException(
					"Error while loading OPSIN: " + e.getMessage(),
					e
				);
			}
		}
		return this.nameToStructure;
	}

	/**
	 * Parses a IUPAC name into a molecule.
	 *
	 * @param iupacName the IUPAC name
	 * @return          the molecule as SMILES string
	 * @throws BioclipseException
	 */
    public String parseIUPACNameAsSMILES(String iupacName) 
                  throws BioclipseException {
        OpsinResult result = getNameToStructureInstance().parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
        	return result.getSmiles();
        }
        throw new BioclipseException(
        	"Could not parse the IUPAC name (" + iupacName + "), because: " +
        	result.getMessage()
        );
    }

	/**
	 * Parses a IUPAC name into tokens.
	 *
	 * @param iupacName the IUPAC name
	 * @return          a {@link List} of {@link ParseTokens}
	 * @throws BioclipseException
	 */
    public List<String> parseIUPACNameAsTokens(String iupacName)
    		throws BioclipseException {
    	try {
    		ParseRulesResults results = NameToStructure.getOpsinParser().getParses(iupacName);
    		String uninterpretable = results.getUninterpretableName();
    		if (uninterpretable != null && !uninterpretable.isEmpty())
    			throw new BioclipseException("Could not interpret the IUPAC name: " + uninterpretable);
    		String unparsable = results.getUnparseableName();
    		if (unparsable != null && !unparsable.isEmpty())
    			throw new BioclipseException("Could not parse the IUPAC name: " + unparsable);

    		List<ParseTokens> interpretations = results.getParseTokensList();
			if (interpretations.isEmpty()) throw new BioclipseException("No tokens found");

			List<String> tokens = new ArrayList<>();
			for (String token : interpretations.iterator().next().getTokens()) {
				if (!token.isEmpty()) tokens.add(token);
			}
			return tokens;
		} catch (ParsingException e) {
			throw new BioclipseException(
				"Could not parse the IUPAC name: " + e.getMessage(), e
			);
		}
    }

	/**
	 * Parses a IUPAC name and generated variations based on the given substitution collections.
	 * The variations is a list of lists, where each list of a group of tokens that can be replaced
	 * to yield a possible new IUPAC name (including the original name).
	 * 
	 * For example:
	 * <pre>
     * {@code
	 *   List<List<String>> options = new ArrayList<>()
	 *   options.add( [ "meth", "eth", "prop", "but", "pent" ] )
	 *   options.add( [ "(R,S)-", "(S,R)-", "(R,R)-", "(S,S)-" ] )
	 * }
	 * </pre>
	 * 
	 * <p>By default it limits the number of returns variations to around 5000.
	 * 
	 * @param iupacName  the IUPAC name
	 * @param variations the IUPAC name
	 * @param validate   if true, then use {@link #parseIUPACName(String)} to only return valid names
	 * @return           a {@link List} of new IUPAC names
	 * @throws BioclipseException thrown when the name parsing failed
	 */
    public List<String> createVariations(String iupacName, List<List<String>> variations, boolean validate)
    		throws BioclipseException {
    	return createVariations(iupacName, variations, 5000, validate);
    }

	/**
	 * Parses a IUPAC name and generated variations based on the given substitution collections.
	 * The variations is a list of lists, where each list of a group of tokens that can be replaced
	 * to yield a possible new IUPAC name (including the original name).
	 * 
	 * For example:
	 * <pre>
     * {@code
	 *   List<List<String>> options = new ArrayList<>()
	 *   options.add( [ "meth", "eth", "prop", "but", "pent" ] )
	 *   options.add( [ "(R,S)-", "(S,R)-", "(R,R)-", "(S,S)-" ] )
	 * }
	 * </pre>
	 * 
	 * @param iupacName  the IUPAC name
	 * @param variations the IUPAC name
	 * @param max        the maximal number of names to be returned
	 * @param validate   if true, then use {@link #parseIUPACName(String)} to only return valid names
	 * @return           a {@link List} of new IUPAC names
	 * @throws BioclipseException thrown when the name parsing failed
	 */
    public List<String> createVariations(String iupacName, List<List<String>> variations, int max, boolean validate)
   		throws BioclipseException {
    	Set<String> collectedNames = ConcurrentHashMap.newKeySet();
    	collectedNames.add(iupacName);
//    	System.out.println("name: " + iupacName);
    	List<String> tokens = parseIUPACNameAsTokens(iupacName);
    	//println " -> tokens: $tokens"
    	Set<StringBuffer> newNames = ConcurrentHashMap.newKeySet();
    	newNames.add(new StringBuffer());
    	for (String token : tokens) {
    		List<String> matchingOptions = newNames.size() > max
    			? null // we have enough names, no new variations, but do add remaining tokens
    		    : getOptions(variations, token);
    		if (matchingOptions == null) {
    			for (StringBuffer newName : newNames) {
    				newName.append(token);
//    				System.out.println("new token  : " + newName);
    			}
    		} else {
    			//println "variations found: " + matchingOptions
    			Set<StringBuffer> newNewNames = ConcurrentHashMap.newKeySet();
    			for (StringBuffer newName : newNames) { // iterate of the last names
    				String oldNameStr = newName.toString();
    				for (String tokenOption : matchingOptions) {
    					if (tokenOption.equals(token)) {
    						newName.append(token); // should happen only once, extend the existing name
//    						System.out.println("new token  : " + newName);
    					} else {
    						StringBuffer newNewName = new StringBuffer(oldNameStr).append(tokenOption);
    						newNewNames.add(newNewName);
//    						System.out.println("new token  : " + newNewName);
    					}
    				}
    			}
    			newNames.addAll(newNewNames); // add all new names to the working list
//    			System.out.println("new new name: " + newNewNames.size());
//    			System.out.println("new name count: " + newNames.size());
    		}
    		//println "new name count: " + newNames.size()
    	}
    	for (StringBuffer newName : newNames) {
    		collectedNames.add(newName.toString());
    	}
    	List<String> validNames = new ArrayList<>();
    	for (String collectedName : collectedNames) {
    	    try {
    	        if (validate) parseIUPACNameAsSMILES(collectedName);
    	        validNames.add(collectedName);
    	    } catch (Exception e) {
    	        // ignore, not valid
    	    }
    	}
    	return validNames;
    }

	/**
	 * Parses a IUPAC name and count potential variations based on the given substitution collections.
	 * Potential variations are not validated, so the true number of variations generated by
	 * {@link createVariations(String, List<List<String>>, boolean)}.
	 *
	 * @param iupacName  the IUPAC name
	 * @param variations the IUPAC name
	 * @return           a count of potentially valid variations
	 * @throws BioclipseException thrown when the name parsing failed
	 */
    public int countPotentialVariations(String iupacName, List<List<String>> variations)
   		throws BioclipseException {
    	int count = 1;
    	List<String> tokens = parseIUPACNameAsTokens(iupacName);
    	for (String token : tokens) {
    		List<String> matchingOptions = getOptions(variations, token);
    		if (matchingOptions != null) {
    			count *= matchingOptions.size();
    		}
    	}
    	return count;
    }

    private List<String> getOptions(List<List<String>> options, String token) {
    	for (List<String> option : options) {
    		if (option.contains(token)) return option;
    	}
    	return null;
    }
    	
	@Override
	public String getManagerName() {
		return "opsin";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1021/ci100384d");
		return dois;
	}
}
