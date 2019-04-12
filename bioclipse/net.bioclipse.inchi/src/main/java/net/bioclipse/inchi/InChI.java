/* Copyright (c) 2009-2019  Egon Willighagen <egonw@users.sf.net>
 *               2009       Jonathan Alvarsson <jonalv@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.inchi;

/**
 * Helper class for InChI calculation. As the InChIFactory calculates
 * InChI and InChIKey simultaneously, but the InChIManager is supposed
 * to be stateless, this class can be used to store and return both.
 * 
 * Also notice that if InChI calculation has failed the instance 
 * <code>FAILED_TO_CALCULATE</code> can be used.
 * 
 * @author egonw
 */
public class InChI {

    private String value;
    private String key;

    public InChI() {}
    
    public static final InChI FAILED_TO_CALCULATE 
        = new InChI("Failed to calculate", "Failed to calculate") {
    	// do not allow overwriting the empty fields
    	public void setValue(String value) {}
        public void setKey(String key) {}    	
    };
    
    /**
     * @param key a InChi key
     * @param value an InChi value
     */
    public InChI(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    /** Returns the InChI. */
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    /** Returns the InChIKey. */
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return value;
    }
}
