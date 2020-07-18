/* Copyright (c) 2010,2019  Egon Willighagen <egon.willighagen@gmail.com>
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
import java.util.List;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;

public class OpsinManager implements IBactingManager {

	private CDKManager cdk;

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
		NameToStructure nameToStructure;
		try {
			nameToStructure = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			throw new BioclipseException(
				"Error while loading OPSIN: " + e.getMessage(),
				e
			);
		}
        OpsinResult result = nameToStructure.parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
        	return result.getCml();
        }
        throw new BioclipseException(
        	"Could not parse the IUPAC name (" + iupacName + "), because: " +
        	result.getMessage()
        );
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
    	NameToStructure nameToStructure;
		try {
			nameToStructure = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			throw new BioclipseException(
				"Error while loading OPSIN: " + e.getMessage(),
				e
			);
		}
        OpsinResult result = nameToStructure.parseChemicalName(iupacName);
        if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
        	return result.getSmiles();
        }
        throw new BioclipseException(
        	"Could not parse the IUPAC name (" + iupacName + "), because: " +
        	result.getMessage()
        );
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
