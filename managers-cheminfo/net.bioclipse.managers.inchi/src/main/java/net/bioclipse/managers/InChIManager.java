/* Copyright (c) 2007-2009  Jonathan Alvarsson
 *               2008-2020  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import io.github.dan2097.jnainchi.InchiCheckStatus;
import io.github.dan2097.jnainchi.InchiFlag;
import io.github.dan2097.jnainchi.InchiKeyCheckStatus;
import io.github.dan2097.jnainchi.InchiStatus;
import io.github.dan2097.jnainchi.JnaInchi;
import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.inchi.InChI;

/**
 * Bioclipse manager that provides functionality to create and
 * validate InChI and InChIKeys.
 */
public class InChIManager implements IBactingManager {

	private String workspaceRoot;

	/**
     * Creates a new {@link InChIManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public InChIManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	private static final String LOADING_SUCCESS =
		"InChI library is loaded.";

	protected InChIGeneratorFactory factory;
	private boolean loadingFailed = false;
	private boolean isLoaded = false;

	/**
	 * Loads the InChI library.
	 *
	 * @return a {@link String} that reflects the success of loading
	 */
    public String load() {
        if (factory == null) {
            try {
				factory = InChIGeneratorFactory.getInstance();
			} catch (Exception exception) {
				loadingFailed = true;
				isLoaded = false;
				return "Loading of the InChI library failed: " +
				       exception.getMessage();
			}
        }
        loadingFailed = false;
        isLoaded = true;
        return LOADING_SUCCESS;
    }

    public List<String> options() {
    	List<String> options = new ArrayList<String>();
    	for (InchiFlag option : InchiFlag.values()) {
    		options.add("" + option);
    	}
    	return options;
    }

    /**
     * Generates an InChI for the given {@link IMolecule}, using the given options.
     * This options String consists of one or more, space-delimited options, such as FixedH.
     *
     * @param molecule the {@link IMolecule} to create the InChI for
     * @return         an {@link InChI} object
     * @throws Exception
     */
	public InChI generate(IMolecule molecule, String options) throws Exception {
		if (!isAvailable()) {
    		return InChI.FAILED_TO_CALCULATE;
    	}

		Object adapted = molecule.getAdapter(IAtomContainer.class);
        if (adapted != null) {
            IAtomContainer container = (IAtomContainer)adapted;
            IAtomContainer clone = (IAtomContainer)container.clone();
            // remove aromaticity flags
            for (IAtom atom : clone.atoms())
                atom.setFlag(CDKConstants.ISAROMATIC, false);
            for (IBond bond : clone.bonds())
                bond.setFlag(CDKConstants.ISAROMATIC, false);
            InChIGenerator gen = factory.getInChIGenerator(clone, options);
            InchiStatus status = gen.getStatus();
            if (status == InchiStatus.SUCCESS) {
            	InChI inchi = new InChI();
            	inchi.setValue(gen.getInchi());
            	inchi.setKey(gen.getInchiKey());
            	return inchi;
            } else {
            	throw new InvalidParameterException(
            			"Error while generating InChI (" + status + "): " +
            			gen.getMessage()
            	);
            }
        } else {
            throw new InvalidParameterException(
                "Given molecule must be a CDKMolecule"
            );
        }
	}

    /**
     * Generates an InChI for the given {@link IMolecule}.
     *
     * @param molecule the {@link IMolecule} to create the InChI for
     * @return         an {@link InChI} object
     * @throws Exception
     */
	public InChI generate(IMolecule molecule) throws Exception {
		if (!isAvailable()) {
    		return InChI.FAILED_TO_CALCULATE;
    	}

		Object adapted = molecule.getAdapter(IAtomContainer.class);
        if (adapted != null) {
            IAtomContainer container = (IAtomContainer)adapted;
            IAtomContainer clone = (IAtomContainer)container.clone();
            // remove aromaticity flags
            for (IAtom atom : clone.atoms())
                atom.setFlag(CDKConstants.ISAROMATIC, false);
            for (IBond bond : clone.bonds())
                bond.setFlag(CDKConstants.ISAROMATIC, false);
            InChIGenerator gen = factory.getInChIGenerator(clone);
            InchiStatus status = gen.getStatus();
            if (status == InchiStatus.SUCCESS) {
            	InChI inchi = new InChI();
            	inchi.setValue(gen.getInchi());
            	inchi.setKey(gen.getInchiKey());
            	return inchi;
            } else {
            	throw new InvalidParameterException(
            			"Error while generating InChI (" + status + "): " +
            			gen.getMessage()
            	);
            }
        } else {
            throw new InvalidParameterException(
                "Given molecule must be a CDKMolecule"
            );
        }
	}

	/**
	 * Returns true if the InChI library was properly loaded.
	 *
	 * @return true if the InChI library was properly loaded.
	 */
    public boolean isLoaded() {
    	return isLoaded;
    }

    /**
     * Checks the validity of the InChIkey.
     *
     * @param inchikey  the InChIKey to test
     * @return          true if the key is valid
     * @throws BioclipseException
     */
    public boolean checkKey(String inchikey) throws BioclipseException {
    	InchiKeyCheckStatus status = JnaInchi.checkInchiKey(inchikey);
    	if (status == InchiKeyCheckStatus.VALID_STANDARD || status == InchiKeyCheckStatus.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    /**
     * Checks the validity of the InChI.
     *
     * @param inchi     the InChI to test
     * @return          true if the InChI is valid
     * @throws BioclipseException
     */
    public boolean check(String inchi) throws BioclipseException {
    	InchiCheckStatus status = JnaInchi.checkInchi(inchi, false);
    	if (status == InchiCheckStatus.VALID_STANDARD || status == InchiCheckStatus.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    /**
     * Checks the validity of the InChI using more strict rules.
     *
     * @param inchi     the InChI to test
     * @return          true if the InChI is valid
     * @throws BioclipseException
     */
    public boolean checkStrict(String inchi) throws BioclipseException {
    	InchiCheckStatus status = JnaInchi.checkInchi(inchi, true);
    	if (status == InchiCheckStatus.VALID_STANDARD || status == InchiCheckStatus.VALID_NON_STANDARD)
    		return true;
    	// everything else is false
    	return false;
    }

    /**
	 * Returns true if the InChI library can be used.
	 *
	 * @return true if the InChI library can be used.
	 */
    public boolean isAvailable() {
    	if (!isLoaded && loadingFailed) return false;
    	if (!loadingFailed && isLoaded) return true;
    	load();
    	return (factory != null);
    }

	@Override
	public String getManagerName() {
		return "inchi";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
