/* *****************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.xml.business;

/**
 * Error that happens during validation of an XML file.
 */
public class XMLError {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private String message;

    /**
     * Creates a new XML validation error with the given message.
     *
     * @param message description of the validation error
     */
    public XMLError(String message) {
        this.message = message;
    }

    /**
     * Creates a new XML validation error with the given message.
     *
     * @param message description of the validation error
     * @param lineNumber    line at what the validation error is location
     * @param columnNumber  position on the line where the validation error is location
     */
    public XMLError(String message, int lineNumber, int columnNumber) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * Generates a representation of the full validation error information.
     * It includes the message and, if given, the location in the file.
     */
    public String toString() {
        if (lineNumber != -1 && columnNumber != -1)
            return "l" + lineNumber + ",c" + columnNumber 
                   + ": " + message;
        return message;
    }

}
