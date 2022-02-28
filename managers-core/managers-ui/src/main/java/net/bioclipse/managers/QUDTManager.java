/* Copyright (c) 2022  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.managers;

import java.util.Collections;
import java.util.List;

import com.github.jqudt.Quantity;
import com.github.jqudt.Unit;
import com.github.jqudt.onto.UnitFactory;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;

/**
 * Bioclipse manager providing unit conversion functionality. 
 */
public class QUDTManager implements IBactingManager {

	private String workspaceRoot;
	private UnitFactory factory = null;

    /**
     * Creates a new {@link QUDTManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
	public QUDTManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
		this.factory = UnitFactory.getInstance();
	}

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "qudt";
    }

    /**
     * Creates a new jQUDT {@link Quantity} for the given input.
     *
     * @param quantity the quantity
     * @param unit     the {@link Unit}
     * @return a {@link Quantity} for the given input
     */
    public Quantity newQuantity(double quantity, Unit unit) {
    	return new Quantity(quantity, unit);
    }

    /**
     * Returns a list of zero or more units matching the given unit.
     *
     * @param symbol the symbol to find the units for
     * @return a {@link List} of {@link Unit}s matching the given symbol
     */
    public List<Unit> findUnits(String symbol) {
    	return factory.findUnits(symbol);
    }

    /**
     * Converts a quantity from one unit to another.
     *
     * @param quantity the quantity to convert
     * @param symbol   the symbol to find the units for
     * @return a {@link List} of {@link Unit}s matching the given symbol
     * @throws BioclipseException when zero or more than one unit matches this symbol
     */
    public Quantity convertTo(Quantity quantity, String symbol) throws BioclipseException {
    	List<Unit> units = factory.findUnits(symbol);
    	if (units.size() == 0) throw new BioclipseException("Cannot find unit with symbol: " + symbol);
    	if (units.size() > 1) throw new BioclipseException("More than one unit with symbol: " + symbol);
    	return convertTo(quantity, units.get(0));
    }

    /**
     * Converts a quantity from one unit to another.
     *
     * @param quantity the quantity to convert
     * @param unit     the {@link Unit} to convert to
     * @return a {@link List} of {@link Unit}s matching the given symbol
     * @throws BioclipseException when zero or more than one unit matches this symbol
     */
    public Quantity convertTo(Quantity quantity, Unit unit) throws BioclipseException {
    	try {
			return quantity.convertTo(unit);
		} catch (Exception exception) {
			throw new BioclipseException("Error while converting the quantity: " + exception.getMessage(), exception);
		}
    }

    /**
     * Creates a new jQUDT {@link Quantity} for the given input.
     *
     * @param quantity the quantity
     * @param unit     the {@link Unit}
     * @return a {@link Quantity} for the given input
     * @throws BioclipseException when zero or more than one unit matches this symbol
     */
    public Quantity newQuantity(double quantity, String unit) throws BioclipseException {
    	List<Unit> units = factory.findUnits(unit);
    	if (units.size() == 0) throw new BioclipseException("Cannot find unit with symbol: " + unit);
    	if (units.size() > 1) throw new BioclipseException("More than one unit with symbol: " + unit);
    	return new Quantity(quantity, units.get(0));
    }

	@Override
	public List<String> doi() {
		return Collections.emptyList();
	}

}
