/* Copyright (c) 2024  Egon Willighagen <egon.willighagen@gmail.com>
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

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

import org.lifstools.jgoslin.domain.*;
import org.lifstools.jgoslin.parser.*;

/**
 * Bioclipse manager that provides BioJava functionality.
 */
public class GoslinManager implements IBactingManager {

	private String workspaceRoot;
	private SwissLipidsParser slParser = new SwissLipidsParser();
	private SwissLipidsParserEventHandler slHandler = slParser.newEventHandler();
	private LipidMapsParser lmParser = new LipidMapsParser();
	private LipidMapsParserEventHandler lmHandler = lmParser.newEventHandler();
	private ShorthandParser shorthandParser = new ShorthandParser();
	private ShorthandParserEventHandler shorthandHandler = shorthandParser.newEventHandler();


    /**
     * Creates a new {@link GoslinManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public GoslinManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	/**
	 * Parses a SwissLipids notation to the internal Goslin data model.
	 *
	 * @param swissLipidsNotation SwissLipids notation, e.g. <code>Cer(d18:1/20:2)</code>.
	 * @return Returns a Goslin {@link LipidAdduct} class reflecting the parsed string
	 * @throws BioclipseException when a parsing error occurred
	 */
	public LipidAdduct parseSwissLipids(String swissLipidsNotation) throws BioclipseException {
		try {
			return slParser.parse(swissLipidsNotation, slHandler);
		} catch (LipidParsingException exception) {
			throw new BioclipseException("Parsing error: " + exception.getLocalizedMessage(), exception);
		}
	}

	/**
	 * Parses a LIPID MAPS notation to the internal Goslin data model.
	 *
	 * @param lipidMapsNotation LIPID MAPS notation, e.g. <code>FA14:2</code>.
	 * @return Returns a Goslin {@link LipidAdduct} class reflecting the parsed string
	 * @throws BioclipseException when a parsing error occurred
	 */
	public LipidAdduct parseLipidMaps(String lipidMapsNotation) throws BioclipseException {
		try {
			return lmParser.parse(lipidMapsNotation, lmHandler);
		} catch (LipidParsingException exception) {
			throw new BioclipseException("Parsing error: " + exception.getLocalizedMessage(), exception);
		}
	}

	/**
	 * Parses a shorthand (2020) notation to the internal Goslin data model.
	 *
	 * @param shorthandNotation shorthand (2020) notation, e.g. <code>Cer 18:1;O2/20:2</code>.
	 * @return Returns a Goslin {@link LipidAdduct} class reflecting the parsed string
	 * @throws BioclipseException when a parsing error occurred
	 */
	public LipidAdduct parseShorthand(String shorthandNotation) throws BioclipseException {
		try {
			return shorthandParser.parse(shorthandNotation, shorthandHandler);
		} catch (LipidParsingException exception) {
			throw new BioclipseException("Parsing error: " + exception.getLocalizedMessage(), exception);
		}
	}

	@Override
	public String getManagerName() {
		return "goslin";
	}

	@Override
	@SuppressWarnings("serial")
	public List<String> doi() {
		return new ArrayList<String>() {{
		  add("10.1021/acs.analchem.0c01690");
		  add("10.1021/acs.analchem.1c05430");
		}};
	}

}
