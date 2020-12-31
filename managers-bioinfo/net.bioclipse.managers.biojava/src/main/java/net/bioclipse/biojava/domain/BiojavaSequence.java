/* *****************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Jonathan Alvarsson
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.biojava.domain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.biojava.bio.seq.Sequence;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;

import net.bioclipse.core.domain.BioObject;
import net.bioclipse.core.domain.ISequence;

/**
 * @author jonalv, olas
 *
 */
public class BiojavaSequence extends BioObject implements ISequence {

    Sequence sequence;

    /**
     * Create a {@link BiojavaSequence} from a {@link Sequence}.
     *
     * @param sequence sequence to create the object from
     */
    public BiojavaSequence(Sequence sequence) {
        super();
        this.sequence = sequence;
    }
    
    public BiojavaSequence() {
    }

    public String getPlainSequence() {
        return sequence.seqString();
    }

    /**
     * Convert RichSequence to FASTA and return as String
     * @throws IOException 
     */
    public String toFasta() {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Namespace ns = RichObjectFactory.getDefaultNamespace();   
        try {
            RichSequence.IOTools.writeFasta(os,sequence,ns);
            // XXX: Check if we really need the following line.
            os.close();
        } catch (IOException e) {
            throw new IllegalStateException("Illegal BiojavaSequence", e);
        }

        return new String(os.toByteArray());
    }

    /**
     * Returns the RichSequence
     */
    public Object getParsedResource() {
        return sequence;
    }

    public void setRichSequence(RichSequence richSequence) {
        this.sequence = richSequence;
    }

    public String getName() {
        return sequence != null ? sequence.getName()
                                : "";
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return super.getAdapter(adapter);
    }
}
