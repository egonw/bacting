/* Copyright (c) 2012  Egon Willighagen <egonw@users.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.managers.domain;

import io.github.egonw.nanojava.data.MaterialType;
import net.bioclipse.core.domain.BioObject;

public class Material extends BioObject implements IMaterial {

	private io.github.egonw.nanojava.data.Material internalModel;

	public Material() {
		this.internalModel = new io.github.egonw.nanojava.data.Material();
	}

	public Material(MaterialType type) {
		this.internalModel = new io.github.egonw.nanojava.data.Material(type);
	}

	public Material(io.github.egonw.nanojava.data.Material nmaterial) {
		this.internalModel = nmaterial;
	}

	public io.github.egonw.nanojava.data.Material getInternalModel() {
		return internalModel;
	}

	@Override
	public MaterialType getType() {
		return this.internalModel.getType();
	}

}
