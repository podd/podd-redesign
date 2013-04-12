package com.github.podd.impl.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.SSHFileReferenceProcessor;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Processor for File References of type <i>http://purl.org/podd/ns/poddBase#SSHFileReference</i>.
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorImpl implements SSHFileReferenceProcessor
{
    
    @Override
    public boolean canHandle(final Model rdfStatements)
    {
        if(rdfStatements == null || rdfStatements.isEmpty())
        {
            return false;
        }
        
        for(final URI fileType : this.getTypes())
        {
            final Model matchingModels = rdfStatements.filter((Resource)null, null, fileType);
            if(!matchingModels.isEmpty())
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Collection<SSHFileReference> createReferences(final Model rdfStatements)
    {
        if(rdfStatements == null || rdfStatements.isEmpty())
        {
            return null;
        }
        
        final Set<SSHFileReference> results = new HashSet<SSHFileReference>();
        
        for(final URI fileType : this.getTypes())
        {
            final Set<Resource> fileRefUris = rdfStatements.filter(null, RDF.TYPE, fileType).subjects();
            
            for(final Resource fileRef : fileRefUris)
            {
                final Model model = rdfStatements.filter(fileRef, null, null);
                
                final SSHFileReference fileReference = new SSHFileReferenceImpl();
                
                // note: artifact ID and parent URI are not available to us in here
                
                if(fileRef instanceof URI)
                {
                    fileReference.setObjectIri(IRI.create((URI)fileRef));
                }
                
                final String label = model.filter(fileRef, RDFS.LABEL, null).objectString();
                if(label != null)
                {
                    fileReference.setLabel(label);
                }
                
                final String filename = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_FILENAME, null).objectString();
                if(filename != null)
                {
                    fileReference.setFilename(filename);
                }
                
                final String path = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_FILE_PATH, null).objectString();
                if(path != null)
                {
                    fileReference.setPath(path);
                }
                
                final String alias = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).objectString();
                if(alias != null)
                {
                    fileReference.setRepositoryAlias(alias);
                }
                results.add(fileReference);
            }
        }
        return results;
    }
    
    @Override
    public Set<URI> getTypes()
    {
        return Collections.singleton(PoddRdfConstants.PODD_BASE_FILE_REFERENCE_TYPE_SSH);
    }
    
}
