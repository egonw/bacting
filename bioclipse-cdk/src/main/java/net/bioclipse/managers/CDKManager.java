/* Copyright (c) 2008-2009  Ola Spjuth
 *               2008-2012  Jonathan Alvarsson
 *               2008-2009  Stefan Kuhn
 *               2008-2018  Egon Willighagen <egonw@users.sf.net>
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

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;

public class CDKManager {

	private String workspaceRoot;

	public CDKManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	
	public List<ICDKMolecule> createMoleculeList() {
		return new ArrayList<ICDKMolecule>();
	}

    public ICDKMolecule fromSMILES(String smilesDescription)
            throws BioclipseException {
        SmilesParser parser = new SmilesParser( SilentChemObjectBuilder.getInstance() );
        IAtomContainer molecule;
        try {
            molecule = parser.parseSmiles( smilesDescription.trim() );
        } catch (InvalidSmilesException e) {
            String message = "SMILES string is invalid. Error message said: ";
            throw new BioclipseException( message + e.getMessage(), e );
        }
        return new CDKMolecule(molecule);
    }

}
