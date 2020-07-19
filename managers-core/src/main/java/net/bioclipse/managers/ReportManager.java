/* Copyright (c) 2015,2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import net.bioclipse.report.data.Report;

import java.util.Collections;
import java.util.List;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.report.data.IReport;
import net.bioclipse.report.serializer.HTMLSerializer;

/**
 * Bioclipse manager providing reporting functionality. Reports are
 * created as {@link IReport} which can be serialized as HTML. 
 */
public class ReportManager implements IBactingManager {

	private String workspaceRoot;

	public ReportManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "report";
    }

    public IReport createReport() {
    	return new Report();
    }

    public String asHTML(IReport report) {
    	return new HTMLSerializer().serialize(report);
    }

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

}
