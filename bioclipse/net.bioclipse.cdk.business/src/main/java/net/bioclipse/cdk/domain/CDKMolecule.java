/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *
 ******************************************************************************/

package net.bioclipse.cdk.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Properties;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.libio.cml.ICMLCustomizer;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cml.element.CMLAtomType;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.sf.jniinchi.INCHI_RET;
import nu.xom.Element;

/**
 * The CKMolecule wraps an IAtomContainer and is able to cache SMILES
 * @author ola
 *
 */
public class CDKMolecule extends BioObject implements ICDKMolecule {

    private String name;
    private IAtomContainer atomContainer;
	private Object icon;

    // cached properties
    public static final String FINGERPRINT_KEY = "net.bioclipse.fingerprint";
    public static final String INCHI_OBJECT = "net.bioclipse.InChI";
    
    /*
     * Needed by Spring
     */
    CDKMolecule() {
        super();
    }
    
    public CDKMolecule(IAtomContainer atomContainer) {
        this();
        this.atomContainer=atomContainer;
    }
    
    /**
     * Used for creating a CDKMolecule if all values already are calculated
     * 
     * @param name
     * @param atomContainer
     * @param smiles
     * @param fingerprint
     */
    public CDKMolecule( String name, 
                        IAtomContainer atomContainer, 
                        BitSet fingerprint ) {
        this(atomContainer);
        this.name = name;
        setProperty( FINGERPRINT_KEY, fingerprint );
    }


    /**
     * Returns the SMILES for this {@link IAtomContainer}. It will throw
     * a {@link BioclipseException} when one or more atoms cannot be typed.
     */
    public String toSMILES() throws BioclipseException {
        if (getAtomContainer() == null) return "";
        try {
            return SmilesGenerator.absolute().create( getAtomContainer() );
        } catch ( CDKException e ) {
            throw new BioclipseException( e.getMessage(), e );
        }
    }

	private String ensureFullAtomTyping(IAtomContainer hydrogenlessClone) {
		// Do atom typing, and if atom typing did not work for all atoms,
        // throw a BioclipseException.
        // First, reset atom types
        for (IAtom atom : hydrogenlessClone.atoms())
        	atom.setAtomTypeName(null);
        CDKAtomTypeMatcher matcher =
        	CDKAtomTypeMatcher.getInstance(hydrogenlessClone.getBuilder());
        IAtomType[] types;
		try {
            types = matcher.findMatchingAtomTypes( hydrogenlessClone );
		} catch (CDKException exception) {
			return "Cannot calculate SMILES: " + exception.getMessage();
		}
        int i = 0;
        for (IAtomType type : types) {
        	i++;
        	if (type == null || "X".equals(type.getAtomTypeName()))
        		return "Cannot calculate SMILES; Missing " +
        			"atom type for atom " + i + ": " +
        			hydrogenlessClone.getAtom(i-1);
        }
        return "";
	}

    public IAtomContainer getAtomContainer() {
        return atomContainer;
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
    }

    public String getName() {
        String returnValue = name;
        if ( returnValue == null ) {
            returnValue = (String) 
                          atomContainer.getProperty(CDKConstants.TITLE);
        }
        if ( returnValue == null ) {
            returnValue = "FIXME";
        }
        return returnValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    private final static Properties cmlPrefs = new Properties();
    static {
        cmlPrefs.put("XMLDeclaration", "false");
    }

    public String toCML() throws BioclipseException {

        if (getAtomContainer()==null) 
            throw new BioclipseException("No molecule to get CML from!");

            ByteArrayOutputStream bo=new ByteArrayOutputStream();

            CMLWriter writer=new CMLWriter(bo);
            writer.registerCustomizer(new ICMLCustomizer() {
            	public void customize( IAtom atom, Object nodeToAdd )
            	throws Exception {
            		if (atom.getAtomTypeName() != null) {
            			if (nodeToAdd instanceof Element) {
            				Element element = (Element)nodeToAdd;
            				CMLAtomType atomType = new CMLAtomType();
            				atomType.setConvention("bioclipse:atomType");
            				atomType.appendChild(atom.getAtomTypeName());
            				element.appendChild(atomType);
            			}
            		}
            	}
            	// don't customize the rest
            	public void customize( IBond bond, Object nodeToAdd )
            	throws Exception {}
            	public void customize( IAtomContainer molecule,
            			Object nodeToAdd ) throws Exception {}
            });
            writer.addChemObjectIOListener(new PropertiesListener(cmlPrefs));
            try {
                writer.write(getAtomContainer());
                writer.close();
            } catch (CDKException e) {
                throw new BioclipseException(
                              "Could not convert molecule to CML: "
                              + e.getMessage() );
            } catch (IOException e) {
                throw new BioclipseException(
                              "Could not write molecule to CML: "
                              + e.getMessage());
            }
            return bo.toString();
    }

    /**
     * Calculate CDK fingerprint and cache the result.
     * @param force if true, do not use cache but force calculation
     * @return
     * @throws BioclipseException
     */
    public BitSet getFingerprint(IMolecule.Property urgency) 
                  throws BioclipseException {
        Fingerprinter fp=new Fingerprinter();
        try {
            BitSet fingerprint = fp.getBitFingerprint( getAtomContainer() ).asBitSet();
            setProperty( FINGERPRINT_KEY, fingerprint );
            return fingerprint;
        } catch (Exception e) {
            throw new BioclipseException("Could not create fingerprint: "
                    + e.getMessage());
        }
    }

    public boolean has3dCoords() throws BioclipseException {
        if (getAtomContainer()==null) 
            throw new BioclipseException("Atomcontainer is null!");
        return GeometryUtil.has3DCoordinates(getAtomContainer());
    }
    
    @Override
    public Object getAdapter( Class adapter ) {

        if (adapter == IMolecule.class){
            return this;
        }
        
        if (adapter.isAssignableFrom(IAtomContainer.class)) {
            return this.getAtomContainer();
        }
        
        // TODO Auto-generated method stub
        return super.getAdapter( adapter );
    }

    public List<IMolecule> getConformers() {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String toString() {
        if ( getName() != null )
            return getClass().getSimpleName() + ":" + getName();
        if (this.getAtomContainer().getAtomCount() == 0) {
            return getClass().getSimpleName() + ": no atoms";
        }
        return getClass().getSimpleName() + ":" + hashCode();
    }

    public String getInChI(IMolecule.Property urgency) 
                  throws BioclipseException {
    	try {
    		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    		InChIGenerator generator = factory.getInChIGenerator(this.getAtomContainer());
    		if (generator.getReturnStatus() == INCHI_RET.OKAY) {
    			return generator.getInchi();
    		} else {
    			throw new BioclipseException("Could not create InChI: " + generator.getMessage() );
    		}
    	} catch (CDKException e) {
            throw new BioclipseException("Could not create InChIKey: "
                    + e.getMessage(), e);
        }
    }

    public String getInChIKey(IMolecule.Property urgency) 
                  throws BioclipseException {
    	try {
    		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    		InChIGenerator generator = factory.getInChIGenerator(this.getAtomContainer());
    		if (generator.getReturnStatus() == INCHI_RET.OKAY) {
    			return generator.getInchiKey();
    		} else {
    			throw new BioclipseException("Could not create InChI: " + generator.getMessage() );
    		}
    	} catch (CDKException e) {
            throw new BioclipseException("Could not create InChIKey: "
                    + e.getMessage(), e);
        }
    }

    public Object getProperty(String propertyKey, IMolecule.Property urgency) {
        return atomContainer.getProperty( propertyKey );
    }

    public void setProperty(String propertyKey, Object value) {
        if(value == null)
            atomContainer.removeProperty( propertyKey );
        else
            atomContainer.setProperty( propertyKey, value );
    }

    void clearProperty( String key ) {
        atomContainer.getProperties().remove( key );
    }
}
