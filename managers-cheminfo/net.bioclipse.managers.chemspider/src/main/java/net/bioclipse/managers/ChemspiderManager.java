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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;

import net.bioclipse.core.business.BioclipseException;

public class ChemspiderManager {

    private String workspaceRoot;

	public ChemspiderManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
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
}
