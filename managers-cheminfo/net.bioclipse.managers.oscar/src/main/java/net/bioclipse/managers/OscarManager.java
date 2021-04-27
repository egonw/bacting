/* Copyright (c) 2011,2021  Egon Willighagen <egon.willighagen@gmail.com>
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

/**
 * Bioclipse manager that adds text mining functionality.
 */
public class OscarManager implements IBactingManager {

	private Oscar oscar = new Oscar();
	private CDKManager cdk;

	/**
     * Creates a new {@link OscarManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public OscarManager(String workspaceRoot) {
		this.cdk = new CDKManager(workspaceRoot);
	}

	/**
	 * Extracts named entities from a text.
	 *
	 * @param text Text with named entities (chemical names).
	 * @return List of named entities. 
	 */
    public List<String> findNamedEntities(String text) {
    	List<String> mols = new ArrayList<String>();
    	List<NamedEntity> entities = oscar.findNamedEntities(text);
    	for (NamedEntity entity : entities) {
    		mols.add(entity.getSurface());
    	}
    	return mols;
    }

	/**
	 * Extracts molecules from a text for which the corresponding named entities are recognized as chemicals.
	 *
	 * @param text Text with chemical names.
	 * @return List of {@link IMolecule}s. 
	 */
    public List<IMolecule> findResolvedNamedEntities(String text) throws BioclipseException {
    	List<IMolecule> mols = new ArrayList<IMolecule>();
    	List<ResolvedNamedEntity> entities = oscar.findAndResolveNamedEntities(text);
    	for (ResolvedNamedEntity entity : entities) {
    		ChemicalStructure structure = 
    			entity.getFirstChemicalStructure(FormatType.CML);
    		if (structure != null) {
    			IMolecule mol;
				try {
					mol = cdk.loadMolecule(
						new ByteArrayInputStream(
							structure.getValue().getBytes()
						), (IChemFormat)CMLFormat.getInstance()
					);
	    		    mols.add(mol);
				} catch (BioclipseException e) {
					throw new BioclipseException(
						"Error while creating an IMolecule for an " +
						"extracted compound: " + e.getMessage(), e);
				} catch (IOException e) {
					throw new BioclipseException(
						"Error while creating an IMolecule for an " +
						"extracted compound: " + e.getMessage(), e);
				}
    		}
    	}
    	return mols;
    }

	@Override
	public String getManagerName() {
		return "oscar";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		dois.add("10.1186/1758-2946-3-41");
		return dois;
	}
}
