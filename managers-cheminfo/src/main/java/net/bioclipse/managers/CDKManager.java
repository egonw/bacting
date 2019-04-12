/* Copyright (c) 2008-2009  Ola Spjuth
 *               2008-2012  Jonathan Alvarsson
 *               2008-2009  Stefan Kuhn
 *               2008-2019  Egon Willighagen <egonw@users.sf.net>
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

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

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

    public IMolecularFormula molecularFormulaObject(ICDKMolecule m) {
        IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(
            m.getAtomContainer()
        );

        int missingHCount = 0;
        for (IAtom atom : m.getAtomContainer().atoms()) {
            missingHCount += calculateMissingHydrogens( m.getAtomContainer(),
                                                        atom );
        }
        
        if (missingHCount > 0) {
            mf.addIsotope( m.getAtomContainer().getBuilder()
                           .newInstance(IIsotope.class, Elements.HYDROGEN),
                           missingHCount
            );
        }
        return mf;
    }

    private int calculateMissingHydrogens( IAtomContainer container,
    		IAtom atom ) {
    	CDKAtomTypeMatcher matcher
    	= CDKAtomTypeMatcher.getInstance(container.getBuilder());
    	IAtomType type;
    	try {
    		type = matcher.findMatchingAtomType(container, atom);
    		if (type == null || type.getAtomTypeName() == null)
    			return 0;

    		if ("X".equals(atom.getAtomTypeName())) {
    			return 0;
    		}

    		if (type.getFormalNeighbourCount() == CDKConstants.UNSET)
    			return 0;

    		Integer at = atom.getImplicitHydrogenCount();
    		at = at != null ? at : 0;
    		// very simply counting:
    		// each missing explicit neighbor is a missing hydrogen
    		return type.getFormalNeighbourCount() - at
    				- container.getConnectedAtomsCount(atom);
    	}
    	catch ( CDKException e ) {
    		return 0;
    	}
    }

    public String molecularFormula( ICDKMolecule m ) {
        return MolecularFormulaManipulator.getString(molecularFormulaObject(m));
    }
}
