/* Copyright (c) 2022  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.managers;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.jqudt.Quantity;
import com.github.jqudt.Unit;
import com.github.jqudt.onto.units.TemperatureUnit;

import net.bioclipse.core.business.BioclipseException;

public class QUDTManagerTest {

	static QUDTManager qudt;

	@BeforeAll
	static void setupManager() throws IOException {
		String tmpPath = Files.createTempDirectory("qudttestws").toString();
		System.out.println("tmpPath: " + tmpPath);
		qudt = new QUDTManager(tmpPath);
	}

	@Test
	public void testDOIs() {
		List<String> dois = qudt.doi();
		assertNotNull(dois);
		assertSame(0, dois.size()); // unpublished
	}

	@Test
	public void newQuantity() {
		Quantity quantity = qudt.newQuantity(1.0, TemperatureUnit.KELVIN);
		assertNotNull(quantity);
	}

	@Test
	public void newQuantity_Symbol() throws Exception {
		Quantity quantity = qudt.newQuantity(1.0, "m");
		assertNotNull(quantity);
	}

	@Test
	public void newQuantity_Fake() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				qudt.newQuantity(1.0, "Lavoisier");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Cannot find unit with symbol"));
	}

	@Test
	public void convertTo_Fake() {
		Exception exception = assertThrows(
			BioclipseException.class, () ->
			{
				qudt.convertTo(qudt.newQuantity(1.0, "m"), "Lavoisier");
			}
		);
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Cannot find unit with symbol"));
	}

	@Test
	public void convertTo() throws BioclipseException {
		Quantity quantity = qudt.convertTo(qudt.newQuantity(1.0, "m"), "nm");
		assertNotNull(quantity);
	}

	@Test
	public void findUnits() throws Exception {
		List<Unit> units = qudt.findUnits("m");
		assertNotNull(units);
		assertNotEquals(0, units.size());
	}

	@Test
	public void testManagerName() {
		assertSame("qudt", qudt.getManagerName());
	}

}
