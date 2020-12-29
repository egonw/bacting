/* Copyright (c) 2011,2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.util.Collections;
import java.util.List;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.IllegalSymbolException;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.biojava.domain.BiojavaDNA;
import net.bioclipse.biojava.domain.BiojavaProtein;
import net.bioclipse.core.domain.IDNA;
import net.bioclipse.core.domain.IProtein;

/**
 * Bioclipse manager that provides BioJava functionality.
 */
public class BiojavaManager implements IBactingManager {

	private String workspaceRoot;

    /**
     * Creates a new {@link BiojavaManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public BiojavaManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public String getManagerName() {
		return "biojava";
	}

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

	public IDNA DNAfromPlainSequence(String dnaString) {
		return DNAfromPlainSequence(dnaString,
                "seq" + System.currentTimeMillis());
	}

	public IDNA DNAfromPlainSequence(String dnaString, String name) {
        try {
            return new BiojavaDNA(DNATools.createDNASequence(
                dnaString, name
            ));
        } catch (IllegalSymbolException e) {
            throw new IllegalArgumentException(e);
        }
    }

	public IProtein proteinFromPlainSequence(String proteinString) {
		return proteinFromPlainSequence(proteinString,
                "seq" + System.currentTimeMillis());
	}

	public IProtein proteinFromPlainSequence(String proteinString, String name) {
		try {
            return new BiojavaProtein(ProteinTools.createProteinSequence(
                proteinString,
                name
            ));
        } catch (IllegalSymbolException e) {
            throw new IllegalArgumentException(e);
        }
	}

}
