/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 *               2009  Egon Willighagen <egonw@user.sf.net>
 *               2009 Arvid Berg <goglepox@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 ******************************************************************************/
package net.bioclipse.cdk.domain;

import java.util.BitSet;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * An interface for CDKMolecule.
 *
 * @author ola
 *
 */
public interface ICDKMolecule extends IMolecule{

    /**
     * Calculate CDK fingerprint and cache the result.
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public BitSet getFingerprint(IMolecule.Property urgency) throws BioclipseException;

    /**
     * Calculate the InChI and cache the result.
     *
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public String getInChI(IMolecule.Property urgency) throws BioclipseException;

    /**
     * Calculate the InChIKey and cache the result.
     *
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public String getInChIKey(IMolecule.Property urgency) throws BioclipseException;

    /**
     * If urgency is Property.USE_CALCULATED calculate this property using
     * a contributed property calculator. If Property.USE_CACHED_OR_CALCULATED
     * try to use cached value if it is not cached calculates it.
     * Property.USE_CACHED indicates that it should only return the cached value
     * or null if no value is cached.
     *
     * @param propertyKey indicating which property to get
     * @param urgency indicates it value should be recalculated or cached value
     * used
     * @return value for property indicated by key or null
     */
    public Object getProperty(String propertyKey,Property urgency);

    public void setProperty(String propertyKey, Object value);
    
    /**
     * AtomContainer is the CDK model for a molecule
     * @return
     */
    public IAtomContainer getAtomContainer();

    String getName();

    void setName( String name );

}
