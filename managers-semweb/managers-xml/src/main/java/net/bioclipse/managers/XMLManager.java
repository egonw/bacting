/* Copyright (c) 2009-2020  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import io.github.egonw.bacting.IBactingManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.xml.business.DummyErrorHandler;
import net.bioclipse.xml.business.NamespaceAggregator;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * Bioclipse manager that provides functionality around the eXtensible
 * Markup Language standard.
 */
public class XMLManager implements IBactingManager {

    private String workspaceRoot;

    /**
     * Creates a new {@link XMLManager}.
     *
     * @param workspaceRoot location of the workspace, e.g. "."
     */
    public XMLManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public String getManagerName() {
		return "xml";
	}

	@Override
	public List<String> doi() {
		return new ArrayList<String>();
	}

	public boolean isWellFormed(String file)
			throws BioclipseException, CoreException {
		File xmlFile = new File(workspaceRoot + file);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);

			SAXParser parser = factory.newSAXParser();

			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(new DummyErrorHandler());

			Builder builder = new Builder(reader);
			builder.build(new FileInputStream(xmlFile));
		} catch (ValidityException exception) {
			return false;
		} catch (ParsingException exception) {
			return false;
		} catch (SAXException exception) {
			return false;
		} catch (IOException exception) {
			throw new BioclipseException(
				"Error while opening file",
				exception
			);
		} catch (ParserConfigurationException exception) {
			throw new BioclipseException(
				"Error while setting up validatin engine",
				exception
			);
		}
		return true;
	}

    public boolean isValid(String file)
    throws BioclipseException, CoreException {
		File xmlFile = new File(workspaceRoot + file);
		try {
            XMLReader xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser"); 
            xerces.setFeature("http://apache.org/xml/features/validation/schema", true);                         

            Builder builder = new Builder(xerces, true);
            builder.build(new FileInputStream(xmlFile));
        } catch (ValidityException exception) {
            return false;
        } catch (ParsingException exception) {
            return false;
        } catch (IOException exception) {
            throw new BioclipseException(
                "Error while opening file",
                exception
            );
        } catch (SAXException exception) {
            throw new BioclipseException(
                "Error creating Xerces parser",
                exception
            );
        }
        return true;
    }

    public List<String> listNamespaces(String file) throws BioclipseException {
    	File xmlFile = new File(workspaceRoot + file);
        try {
            javax.xml.parsers.SAXParserFactory spf =
                javax.xml.parsers.SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            javax.xml.parsers.SAXParser saxParser = spf.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            NamespaceAggregator nsAggr = new NamespaceAggregator();
            parser.setContentHandler(nsAggr);
            parser.parse(new InputSource(new FileInputStream(xmlFile)));
            return nsAggr.getNamespaces();
        } catch (Exception exc) {
            throw new BioclipseException(
                "Could not instantiate JAXP/SAX XML reader: ", exc
            );
        }
    }
}
