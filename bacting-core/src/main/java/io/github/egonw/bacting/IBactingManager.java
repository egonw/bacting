/* Copyright (c) 2019  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.github.egonw.bacting;

import java.util.List;

import net.bioclipse.managers.business.IBioclipseManager;

/**
 * Interface for Bacting managers extension the {@link IBioclipseManager} with a method
 * to return DOIs relevant to this manager.
 */
public interface IBactingManager extends IBioclipseManager {

    /**
     * Lists the DOIs of the articles associated to this manager.
     *
     * @return a {@link List} of String with DOIs
     */
    public List<String> doi();

}
