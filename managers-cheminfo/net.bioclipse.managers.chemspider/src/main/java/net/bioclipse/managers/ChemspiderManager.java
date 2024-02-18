/* Copyright (c) 2006-2020  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

/**
 * Bioclipse manager that provides functionality to interact with the
 * ChemSpider database.
 */
public class ChemspiderManager implements IBactingManager {

    private String workspaceRoot;

	private CDKManager cdk;

    /**
     * Creates a new {@link ChemspiderManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public ChemspiderManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(workspaceRoot);
	}

    /**
     * Looks up ChemSpider identifiers for the given InChIKey.
     * 
     * @param inchiKey  InChIKey to resolve
     * @return          a Java {@link List} with ChemSpider identifiers
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException
     */
	public List<Integer> resolve(String inchiKey)
	throws IOException, BioclipseException, CoreException {

		Set<Integer> results = new HashSet<Integer>();

		URL url = new URL("http://www.chemspider.com/InChIKey/" + inchiKey);
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				url.openConnection().getInputStream()
			)
		);
		String line = reader.readLine();
		Pattern pattern = Pattern.compile("Chemical-Structure.(\\d*).html");
		String csid = "";
		while (line != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				csid = matcher.group(1);
				results.add(Integer.valueOf(csid));
			}
			line = reader.readLine();
		}

		List<Integer> uniqueResults = new ArrayList<Integer>();
		uniqueResults.addAll(results);
		return uniqueResults;
	}

	/**
	 * Downloads the molecular for the given ChemSpider identifier as an
	 * SD file.
	 *
	 * @param csid  the ChemSpider identifiers
	 * @return      the molecule as {@link String}
	 * @throws IOException
	 * @throws BioclipseException
	 * @throws CoreException
	 */
	public String downloadAsString(Integer csid)
	throws IOException, BioclipseException, CoreException {
		StringBuffer fileContent = new StringBuffer(); 
		try {                
			URL url = new URL("http://www.chemspider.com/mol/" + csid);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							url.openConnection().getInputStream()
					)
			);
			String line = reader.readLine();
			while (line != null) {
				fileContent.append(line).append('\n');
				line = reader.readLine();
			}
			reader.close();
		} catch (PatternSyntaxException exception) {
			exception.printStackTrace();
			throw new BioclipseException("Invalid Pattern.", exception);
		} catch (MalformedURLException exception) {
			exception.printStackTrace();
			throw new BioclipseException("Invalid URL.", exception);
		}
		return fileContent.toString();
	}

	/**
	 * Downloads the molecular for the given ChemSpider identifier as
	 * a {@link IMolecule} object.
	 *
	 * @param csid  the ChemSpider identifiers
	 * @return      the molecule as {@link IMolecule}
	 * @throws IOException
	 * @throws BioclipseException
	 * @throws CoreException
	 */
	public IMolecule download(Integer csid)
	throws IOException, BioclipseException, CoreException {
		String molstring = downloadAsString(csid);

		ICDKMolecule molecule = cdk.fromString(molstring);
		return molecule;
	}

	/**
	 * Loads the ChemSpider MDL molfile with the given ChemSpider ID (csid) to the given path.
	 *
	 * @param csid    the ChemSpider ID
	 * @param target  filename of MDL molfile from ChemSpider to write to
	 */
	public String loadCompound(int csid, String target)
	throws IOException, BioclipseException, CoreException {
		URL url = new URL("https://www.chemspider.com/mol/" + csid);
		Files.copy(
			url.openConnection().getInputStream(),
			Paths.get(workspaceRoot + target),
			StandardCopyOption.REPLACE_EXISTING
		);
		return target;
	}

	@Override
	public String getManagerName() {
		return "chemspider";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}
}
