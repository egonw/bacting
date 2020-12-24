/* Copyright (c) 2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.report.data.IReport;

public class ReportManagerTest {

	static ReportManager report;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("reporttestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		report = new ReportManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = report.doi();
		assertNotNull(dois);
		assertSame(0, dois.size()); // unpublished
	}

	@Test
	public void testManagerName() {
		assertSame("report", report.getManagerName());
	}

	@Test
	public void testCreateReport() {
		IReport ireport = report.createReport();
		assertNotNull(ireport);
	}

	@Test
	public void testAsHTML() {
		String html = report.asHTML(report.createReport());
		assertNotNull(html);
		assertTrue(html.contains("<html>"));
	}

	@Test
	public void testAsMarkdown() {
		IReport someReport = report.createReport();
		someReport.createTitle("Title");
		String markdown = report.asMarkdown(someReport);
		assertNotNull(markdown);
		assertTrue(markdown.contains("**Title**"));
	}

	@Test
	public void testMarkdown_Sections() {
		IReport someReport = report.createReport();
		someReport.startSection("Level 1");
		someReport.startSubSection("Level 2");
		someReport.startSubSubSection("Level 3");
		String markdown = report.asMarkdown(someReport);
		assertNotNull(markdown);
		assertTrue(markdown.contains("# "));
		assertTrue(markdown.contains("## "));
		assertTrue(markdown.contains("### "));
	}

	@Test
	public void testMarkdown_Hyperlink() {
		IReport someReport = report.createReport();
		someReport.addLink("https://github.com/egonw/bacting", "Bacting repository");
		String markdown = report.asMarkdown(someReport);
		assertNotNull(markdown);
		assertTrue(markdown.contains("]("));
	}


	@Test
	public void testMarkdown_Various() {
		IReport someReport = report.createReport();
		someReport.forceNewLine();
		someReport.startParagraph();
		someReport.endParagraph();
		someReport.startIndent();
		someReport.endIndent();
		String markdown = report.asMarkdown(someReport);
		assertNotNull(markdown);
		assertTrue(markdown.contains("<br"));
		assertTrue(markdown.contains("<p"));
		assertTrue(markdown.contains("</p"));
		assertTrue(markdown.contains("<ul"));
		assertTrue(markdown.contains("</ul"));
	}
}
