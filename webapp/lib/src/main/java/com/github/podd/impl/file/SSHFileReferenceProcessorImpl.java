package com.github.podd.impl.file;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.SSHFileReferenceProcessor;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Processor for File References of type <i>http://purl.org/podd/ns/poddBase#SSHFileReference</i>.
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorImpl implements SSHFileReferenceProcessor
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
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
                
                if(log.isDebugEnabled())
                {
                    DebugUtils.printContents(model);
                }
                
                final SSHFileReference fileReference = new SSHFileReferenceImpl();
                
                // note: artifact ID is not available to us in here and must be added externally
                
                if(fileRef instanceof URI)
                {
                    fileReference.setObjectIri(IRI.create((URI)fileRef));
                }
                
                final Set<Value> label = model.filter(fileRef, RDFS.LABEL, null).objects();
                if(!label.isEmpty())
                {
                    fileReference.setLabel(label.iterator().next().stringValue());
                }
                
                final Set<Value> filename =
                        model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_FILENAME, null).objects();
                if(!filename.isEmpty())
                {
                    fileReference.setFilename(filename.iterator().next().stringValue());
                }
                
                final Set<Value> path = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_FILE_PATH, null).objects();
                if(!path.isEmpty())
                {
                    fileReference.setPath(path.iterator().next().stringValue());
                }
                
                final Set<Value> alias = model.filter(fileRef, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).objects();
                if(!alias.isEmpty())
                {
                    fileReference.setRepositoryAlias(alias.iterator().next().stringValue());
                }
                
                Model linksToFileReference = rdfStatements.filter(null, null, fileRef);
                
                // TODO: Need to use a SPARQL query to verify that the property is a sub-property of
                // PODD Contains
                if(!linksToFileReference.isEmpty())
                {
                    for(Resource nextResource : linksToFileReference.subjects())
                    {
                        if(nextResource instanceof URI)
                        {
                            fileReference.setParentIri(IRI.create((URI)nextResource));
                            break;
                        }
                    }
                    fileReference
                            .setParentPredicateIRI(IRI.create(linksToFileReference.predicates().iterator().next()));
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
