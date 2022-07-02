/* Copyright (c) 2012,2022  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import io.github.egonw.bacting.IBactingManager;
import io.github.egonw.nanojava.data.MaterialType;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.domain.IMaterial;
import net.bioclipse.managers.domain.Material;

/**
 * Bioclipse manager that wraps the CDK-based nanojava library.
 */
public class NanoManager implements IBactingManager {

	/**
     * Creates a new {@link NanoManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public NanoManager(String workspaceRoot) {
	}

    @SuppressWarnings("serial")
	private static final Map<String,MaterialType> materialTypes = new HashMap<String,MaterialType>() {{
    	put(MaterialType.CARBONNANOTUBE.getLabel(), MaterialType.CARBONNANOTUBE);
    	put(MaterialType.GRAPHENE.getLabel(), MaterialType.GRAPHENE);
    	put(MaterialType.METAL.getLabel(), MaterialType.METAL);
    	put(MaterialType.METALOXIDE.getLabel(), MaterialType.METALOXIDE);
    }};
    
    public IMaterial newMaterial(String type) throws BioclipseException {
    	MaterialType material = materialTypes.get(type);
    	if (material == null)
    		throw new BioclipseException(
    			"Unknown material type '" + type + "'. Use listMaterialTypes() for "
    			+ "a full list of accepted values."
    		);
    	return new Material(material);
    }

    public IMaterial newMaterial(String type, String formula) throws BioclipseException {
    	Material material = (Material)newMaterial(type);
    	io.github.egonw.nanojava.data.Material nm = material.getInternalModel();
    	nm.addAtomContainer(MolecularFormulaManipulator.getAtomContainer(
    		MolecularFormulaManipulator.getMolecularFormula(formula, SilentChemObjectBuilder.getInstance())
    	));
    	return material;
    }

    public Set<String> listMaterialTypes()
    		throws BioclipseException, UnsupportedEncodingException, CoreException {
    	return Collections.unmodifiableSet(materialTypes.keySet());
    }

	@Override
	public String getManagerName() {
		return "nm";
	}

	@Override
	public List<String> doi() {
		List<String> dois = new ArrayList<String>();
		return dois;
	}
}
