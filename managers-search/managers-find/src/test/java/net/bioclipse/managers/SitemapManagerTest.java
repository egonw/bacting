/* Copyright (c) 2023  Egon Willighagen <egon.willighagen@gmail.com>
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.bioclipse.core.business.BioclipseException;

public class SitemapManagerTest {

	static SitemapManager sitemap;
	static String workspaceRoot;

	@BeforeAll
	static void setupManager() throws Exception {
		sitemap = new SitemapManager(workspaceRoot);
	}

	@Test
	public void testParse() throws BioclipseException {
		List<String> urls = sitemap.parse("https://ammar257ammar.github.io/Nanosafety-data-reusability-34-datasets/sitemap.xml");
		assertNotNull(urls);
		assertNotSame(0, urls.size());
		for (String url : urls) {
			if (url.equals("https://ammar257ammar.github.io/Nanosafety-data-reusability-34-datasets/overview/9e1d426c90.html")) return;
		}
		assertTrue(false, "The URL https://ammar257ammar.github.io/Nanosafety-data-reusability-34-datasets/overview/9e1d426c90.html was excepted but not found");
	}

	@Test
	public void testDOIs() {
		List<String> dois = sitemap.doi();
		assertNotNull(dois);
		assertSame(0, dois.size());
	}

	@Test
	public void testManagerName() {
		assertSame("sitemap", sitemap.getManagerName());
	}

}
