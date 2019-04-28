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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.silent.ChemFile;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.stereo.Stereocenters;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.common.collect.Lists;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class CDKManager {

	private String workspaceRoot;

	// ReaderFactory used to instantiate IChemObjectReaders
    private static ReaderFactory readerFactory = new ReaderFactory();

	public CDKManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	public ICDKMolecule loadMolecule( InputStream instream,
			IChemFormat format)
					throws BioclipseException, IOException {
		ISimpleChemObjectReader reader = readerFactory.createReader(format);
		if (reader == null) {
			String message = "Could not create reader in CDK.";
			if ( format == null ) {
				message = "Unsupported file format in CDK";
			}
			throw new BioclipseException( message );
		}

		try {
			reader.setReader(instream);
		}
		catch ( CDKException e1 ) {
			throw new RuntimeException(
					"Failed to set the reader's inputstream", e1
					);
		}

		List<IAtomContainer> atomContainersList =
				new ArrayList<IAtomContainer>();

		// Read file
		try {
			IChemObjectBuilder scob = 
					SilentChemObjectBuilder.getInstance(); 
			if (reader.accepts(ChemFile.class)) {

				IChemFile chemFile =
						(IChemFile) reader.read(scob.newInstance( IChemFile.class ));
				atomContainersList =
						ChemFileManipulator.getAllAtomContainers(chemFile);
			} else if ( reader.accepts( IAtomContainer.class ) ) {
				atomContainersList.add( reader.read( scob
						.newInstance( IAtomContainer.class ) ) );
			} else {
				throw new RuntimeException("Failed to read file.");
			}
		}
		catch (CDKException e) {
			throw new RuntimeException("Failed to read file", e);
		}

		// Store the chemFormat used for the reader
		IResourceFormat chemFormat = reader.getFormat();

		int nuMols = atomContainersList.size();
		if (atomContainersList.size() == 0)
			throw new RuntimeException("File did not contain any molecule");

		IAtomContainer containerToReturn = atomContainersList.get(0);
		// sanatize the input for certain file formats
		CDKMolecule retmol = new CDKMolecule(containerToReturn);
		String molName = (String)containerToReturn
				.getProperty(CDKConstants.TITLE);
		if (molName != null && !(molName.length() > 0)) {
			retmol.setName(molName);
		}

		return retmol;
	}

    public ICDKMolecule fromCml(String molstring)
            throws BioclipseException, IOException {

    	if (molstring == null)
    		throw new IllegalArgumentException("Input cannot be null");

    	ByteArrayInputStream bais
    	= new ByteArrayInputStream( molstring.getBytes() );

    	return loadMolecule( (InputStream)bais,
    			(IChemFormat)CMLFormat.getInstance());
    }

    public ICDKMolecule asCDKMolecule(IMolecule imol) throws BioclipseException {

        if (imol instanceof ICDKMolecule) {
            return (ICDKMolecule) imol;
        }

        // First try to create from CML
        try {
            String cmlString = imol.toCML();
            if (cmlString != null) {
                return fromCml(cmlString);
            }
        }
        catch (IOException e) {
            // logger.debug("Could not create mol from CML");
        }
        catch (UnsupportedOperationException e) {
        	// logger.debug("Could not create mol from CML");
        }

        // Secondly, try to create from SMILES
        return fromSMILES( imol.toSMILES() );
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

    public List<IAtom> getAtomsWithUndefinedStereo(IMolecule molecule) throws BioclipseException {
    	List<IAtom> atoms = new ArrayList<IAtom>();
    	ICDKMolecule cdkMol = asCDKMolecule(molecule);
    	IAtomContainer container = cdkMol.getAtomContainer();
    	Stereocenters centers =  Stereocenters.of(container);
    	List<IStereoElement> stereoInfo = Lists.newArrayList(getDefinedStereoCenters(cdkMol));
    	for (int i = 0; i < container.getAtomCount(); i++)  {
    		if (centers.isStereocenter(i)) {
    			IAtom atom = container.getAtom(i);
    			boolean atomIsDefined = false;
    			for (IStereoElement elem : stereoInfo) {
    				IChemObject focus = elem.getFocus();
    				if (focus instanceof IAtom && focus == atom) {
    					atomIsDefined = true;
    				} else if (focus instanceof IBond && ((IBond)focus).contains(atom)) {
    					atomIsDefined = true;
    				}
    			}
    			if (!atomIsDefined) atoms.add(container.getAtom(i));
    		}
    	}
        return atoms;
    }

    public Iterable<IStereoElement> getDefinedStereoCenters(IMolecule molecule) throws BioclipseException {
        return asCDKMolecule(molecule).getAtomContainer().stereoElements();
    }
}
