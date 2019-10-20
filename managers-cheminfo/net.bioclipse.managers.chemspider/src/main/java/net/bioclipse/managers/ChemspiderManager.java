/* Copyright (c) 2006-2019  Egon Willighagen <egonw@users.sf.net>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

public class ChemspiderManager implements IBactingManager {

    private String workspaceRoot;

	private CDKManager cdk;

	public ChemspiderManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.cdk = new CDKManager(workspaceRoot);
	}

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

	public IMolecule download(Integer csid)
	throws IOException, BioclipseException, CoreException {
		String molstring = downloadAsString(csid);

		ICDKMolecule molecule = cdk.fromString(molstring);
		return molecule;
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
