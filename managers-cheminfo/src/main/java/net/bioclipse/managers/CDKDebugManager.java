/* Copyright (c) 2008 The Bioclipse Project and others.
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
package net.bioclipse.managers;

import java.lang.reflect.InvocationTargetException;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class CDKDebugManager {

	private String workspaceRoot;

	private CDKManager cdk;

	public CDKDebugManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(workspaceRoot);
	}

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
}
