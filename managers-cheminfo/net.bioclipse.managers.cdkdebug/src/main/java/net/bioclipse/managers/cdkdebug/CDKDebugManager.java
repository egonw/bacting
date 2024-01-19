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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.atomtype.mapper.AtomTypeMapper;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.WriterFactory;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.setting.IOSetting;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.diff.AtomContainerDiff;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

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

	public static String NEWLINE = System.getProperty("line.separator");

	private String workspaceRoot;

	private CDKManager cdk;
	private static AtomTypeFactory factory;
    private static final WriterFactory writerFactory = new WriterFactory();
    private static final ReaderFactory readerFactory = new ReaderFactory();

    static {
    	try {
    	URL owlURL = CDKDebugManager.class.getResource("/org/openscience/cdk/dict/data/sybyl-atom-types.owl");
        InputStream iStream = owlURL.openStream();
//            = org.openscience.cdk.atomtype.Activator.class.getResourceAsStream(
//                "/org/openscience/cdk/dict/data/sybyl-atom-types.owl");
        factory = AtomTypeFactory.getInstance( iStream, "owl",
                                               SilentChemObjectBuilder.getInstance()
        );
    	} catch (IOException e) {
    		// logger.error("Could not get sybyl-atom-types.owl file",e);
    	} 
    }

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
	 * Returns a string of Sybyl (mol2) atom types as recognized by the CDK.
	 *
	 * @param mol  molecule for which the atoms are typed
	 * @return     a {@link String} with the results
	 * @throws InvocationTargetException
	 */
	public String perceiveSybylAtomTypes(IMolecule mol)
			throws InvocationTargetException {

		ICDKMolecule cdkmol;

		try {
			cdkmol = cdk.asCDKMolecule(mol);
		} 
		catch (BioclipseException e) {
			System.out.println("Error converting cdk10 to cdk");
			throw new InvocationTargetException(e);
		}

		IAtomContainer ac = cdkmol.getAtomContainer();
		CDKAtomTypeMatcher cdkMatcher 
		= CDKAtomTypeMatcher.getInstance(ac.getBuilder());
		AtomTypeMapper mapper 
		= AtomTypeMapper.getInstance(
				"org/openscience/cdk/dict/data/cdk-sybyl-mappings.owl" );

		IAtomType[] sybylTypes = new IAtomType[ac.getAtomCount()];

		int atomCounter = 0;
		for (IAtom atom : ac.atoms()) {
			IAtomType type;
			try {
				type = cdkMatcher.findMatchingAtomType(ac, atom);
			} 
			catch (CDKException e) {
				type = null;
			}
			if (type==null) {
				//    logger.debug("AT null for atom: " + atom);
				type = atom.getBuilder().newInstance(
						IAtomType.class, atom.getSymbol()
						);
				type.setAtomTypeName("X");
			}
			AtomTypeManipulator.configure(atom, type);
		}
		try {
			Aromaticity arom = new Aromaticity(ElectronDonation.cdk(),Cycles.cdkAromaticSet());
			arom.apply( ac );
		} 
		catch (CDKException e) {
			// logger.debug("Failed to perceive aromaticity: " + e.getMessage());
		}
		for (IAtom atom : ac.atoms()) {
			String mappedType = mapper.mapAtomType(atom.getAtomTypeName());
			if ("C.2".equals(mappedType)
					&& atom.getFlag(CDKConstants.ISAROMATIC)) {
				mappedType = "C.ar";
			} 
			else if ("N.pl3".equals(mappedType)
					&& atom.getFlag(CDKConstants.ISAROMATIC)) {
				mappedType = "N.ar";
			}
			try {
				sybylTypes[atomCounter] = factory.getAtomType(mappedType);
			} 
			catch (NoSuchAtomTypeException e) {
				// yes, setting null's here is important
				sybylTypes[atomCounter] = null; 
			}
			atomCounter++;
		}
		StringBuffer result = new StringBuffer();
		// now that full perception is finished, we can set atom type names:
		for (int i = 0; i < sybylTypes.length; i++) {
			if (sybylTypes[i] != null) {
				ac.getAtom(i).setAtomTypeName(sybylTypes[i].getAtomTypeName());
			} 
			else {
				ac.getAtom(i).setAtomTypeName("X");
			}

			result.append(i).append(':').append(ac.getAtom(i).getAtomTypeName()).append('\n');
		}
		return result.toString();
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

	/**
	 * Returns a string representation of a {@link ICDKMolecule}.
	 *
	 * @param mol an {@link ICDKMolecule}
	 * @return    a string
	 */
    public String debug(ICDKMolecule mol) {
        return mol.getAtomContainer().toString();
    }

    /**
     * Returns a writer options for the CDK writer for the given {@link IChemFormat}.
     *
     * @param format the {@link IChemFormat} for which to return the write options
     * @return a string describing the options
     */
    public String listWriterOptions(IChemFormat format) {
        if (format.getWriterClassName() == null)
            return "No writer avaiable for this format";

        IChemObjectWriter writer = writerFactory.createWriter(format);
        if (writer == null)
            return "Cannot instantiate writer: " + format.getWriterClassName();

        IOSetting[] settings = writer.getIOSettings();
        if (settings == null || settings.length == 0)
            return "The writer does not have options";
        
        StringBuffer overview = new StringBuffer();
        for (String string : new String[] {
            format.getFormatName(), NEWLINE })
            overview.append(string);
        for (IOSetting setting : settings) {
            for (String string : new String[] {
                "[", setting.getName(), "]",             NEWLINE,
                setting.getQuestion(),                   NEWLINE,
                "Default value: ", 
                setting.getDefaultSetting(),             NEWLINE,
                "Value        : ", setting.getSetting(), NEWLINE})
                overview.append(string);
        }
        if (overview.length() == 0) // there are no options
            return "The reader does not have options";

        return overview.toString();
    }

    /**
     * Returns a reader options for the CDK reader for the given {@link IChemFormat}.
     *
     * @param format the {@link IChemFormat} for which to return the read options
     * @return a string describing the options
     */
    public String listReaderOptions(IChemFormat format) {
        if (format.getReaderClassName() == null)
            return "No reader avaiable for this format";

        IChemObjectReader reader = readerFactory.createReader(format);
        if (reader == null)
            return "Cannot instantiate writer: " + format.getReaderClassName();

        IOSetting[] settings = reader.getIOSettings();
        if (settings == null || settings.length == 0)
            return "The reader does not have options";

        StringBuffer overview = new StringBuffer();
        for (String string : new String[] {
            format.getFormatName(), NEWLINE })
            overview.append(string);
        for (IOSetting setting : settings) {
            for (String string : new String[] {
                "[", setting.getName(), "]",             NEWLINE,
                setting.getQuestion(),                   NEWLINE,
                "Default value: ", 
                setting.getDefaultSetting(),             NEWLINE,
                "Value        : ", setting.getSetting(), NEWLINE})
                overview.append(string);
        }

        return overview.toString();
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
