/* Copyright (c) 2008-2020 The Bioclipse Project and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org/epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Egon Willighagen <egonw@user.sf.net>
 *     Jonathan Alvarsson <jonalv@user.sf.net> 2009-01-15 Corrected Whitespaces 
 *                                             tabs and scripts seemed to have
 *                                             wrecked havoc...
 */
package net.bioclipse.managers.cdkdebug;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.tools.diff.AtomContainerDiff;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.CDKManager;

/**
 * Bioclipse manager that provides functionality by the Chemistry
 * Development Kit that give access to less used functionality
 * and exposes more details of the underlying data model.
 */
public class CDKDebugManager implements IBactingManager {

	private String workspaceRoot;

	private CDKManager cdk;

	/**
     * Creates a new {@link CDKDebugManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public CDKDebugManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(workspaceRoot);
	}

	/**
	 * Returns a string of atom types as recognized by the CDK.
	 *
	 * @param mol  molecule for which the atoms are typed
	 * @return     a {@link String} with the results
	 * @throws InvocationTargetException
	 */
	public String perceiveCDKAtomTypes(IMolecule mol)
			throws InvocationTargetException {

		ICDKMolecule cdkmol;

		try {
			cdkmol = cdk.asCDKMolecule(mol);
		} 
		catch ( BioclipseException e ) {
			e.printStackTrace();
			throw new InvocationTargetException(
					e, "Error while creating a ICDKMolecule" );
		}

		IAtomContainer ac = cdkmol.getAtomContainer();
		CDKAtomTypeMatcher cdkMatcher 
		= CDKAtomTypeMatcher.getInstance(ac.getBuilder());

		StringBuffer result = new StringBuffer();
		int i = 1;
		for (IAtom atom : ac.atoms()) {
			IAtomType type = null;
			try {
				type = cdkMatcher.findMatchingAtomType(ac, atom);
			} 
			catch ( CDKException e ) {}
			result.append(i).append(':').append(
					type != null ? type.getAtomTypeName() : "null"
					).append('\n'); // FIXME: should use NEWLINE here
			i++;
		}
		return result.toString();
	}

	/**
	 * Returns the differences between the two molecules.
	 *
	 * @param mol  One of the two {@link ICDKMolecule}s to compare
	 * @param mol2 One of the two {@link ICDKMolecule}s to compare
	 * @return
	 */
	public String diff(ICDKMolecule mol, ICDKMolecule mol2) {
		return AtomContainerDiff.diff(
	         mol.getAtomContainer(), mol2.getAtomContainer()
	    );
	}

	@Override
	public String getManagerName() {
		return "cdx";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1021/ci025584y");
		dois.add("10.2174/138161206777585274");
		dois.add("10.1186/s13321-017-0220-4");
		return dois;
	}

}
