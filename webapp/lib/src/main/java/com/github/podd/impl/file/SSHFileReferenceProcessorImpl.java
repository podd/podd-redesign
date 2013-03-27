package com.github.podd.impl.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.github.podd.api.file.FileReferenceConstants;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.file.PoddSSHFileReferenceProcessor;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Processor for File References of type <i>http://purl.org/podd/ns/poddBase#SSHFileReference</i>.
 *   
 * @author kutila
 */
public class SSHFileReferenceProcessorImpl implements PoddSSHFileReferenceProcessor
{
    
    private final URI FILE_TYPE = PoddRdfConstants.PODDBASE_FILE_REFERENCE_TYPE_SSH;
    
    @Override
    public boolean canHandle(Model rdfStatements)
    {
        if (rdfStatements == null || rdfStatements.isEmpty())
        {
            return false;
        }
        
        Model matchingModels = rdfStatements.filter((Resource)null, null, this.FILE_TYPE);
        if (!matchingModels.isEmpty())
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Collection<PoddSSHFileReference> createReferences(Model rdfStatements)
    {
        if (rdfStatements == null || rdfStatements.isEmpty())
        {
            return null;
        }
        
        Set<PoddSSHFileReference> results = new HashSet<PoddSSHFileReference>();

        Set<Resource> fileRefUris = rdfStatements.filter(null, RDF.TYPE, FILE_TYPE).subjects();
        
        for(Iterator<Resource> iterator = fileRefUris.iterator(); iterator.hasNext();)
        {
            //check when casting
            URI fileRef = (URI)iterator.next();
            
            Model model = rdfStatements.filter(fileRef, null, null);
            
            PoddSSHFileReference fileReference = new SimplePoddSSHFileReference();
            fileReference.setArtifactID(null);
            fileReference.setObjectIri(null);
            //FIXME: artifactID, parent Uri, this object Uri
            
            String label = model.filter(fileRef, RDFS.LABEL, null).objectString();
            if (label != null)
            {
                fileReference.setLabel(label);
            }
            
            String filename = model.filter(fileRef, PoddRdfConstants.PODD_BASE_FILENAME, null).objectString();
            if (filename != null)
            {
                fileReference.setFilename(filename);
            }
            
            String path = model.filter(fileRef, PoddRdfConstants.PODD_BASE_FILE_PATH, null).objectString();
            if (path != null)
            {
                fileReference.setPath(path);
            }

            String alias = model.filter(fileRef, PoddRdfConstants.PODD_BASE_ALIAS, null).objectString();
            if (alias != null)
            {
                fileReference.setRepositoryAlias(alias);
            }            
            results.add(fileReference);
        }
        
        return results;
    }
    
    @Override
    public Set<URI> getTypes()
    {
        return Collections.singleton(this.FILE_TYPE);
    }
    
}
