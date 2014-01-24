/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OntologyImportsComparator implements Comparator<URI>
{
    private static final Logger log = LoggerFactory.getLogger(OntologyImportsComparator.class);
    
    private static final int BEFORE = -1;
    private static final int EQUALS = 0;
    private static final int AFTER = 1;
    
    private final ConcurrentMap<URI, Set<URI>> importsMap;
    
    OntologyImportsComparator(final ConcurrentMap<URI, Set<URI>> importsMap)
    {
        this.importsMap = importsMap;
    }
    
    @Override
    public int compare(final URI o1, final URI o2)
    {
        if(o1.equals(o2))
        {
            return OntologyImportsComparator.EQUALS;
        }
        
        Set<URI> set1 = this.importsMap.get(o1);
        Set<URI> set2 = this.importsMap.get(o2);
        
        if(set1 == null)
        {
            set1 = Collections.emptySet();
        }
        
        if(set2 == null)
        {
            set2 = Collections.emptySet();
        }
        
        if(set1.contains(o2) && set2.contains(o1))
        {
            OntologyImportsComparator.log.error("Ontologies have mutual imports: {} {}", o1, o2);
            throw new RuntimeException("Ontologies have mutual imports: " + o1.stringValue() + " " + o2.stringValue());
        }
        else if(set1.equals(set2))
        {
            return OntologyImportsComparator.EQUALS;
        }
        else if(set1.contains(o2))
        {
            return OntologyImportsComparator.AFTER;
        }
        else if(set2.contains(o1))
        {
            return OntologyImportsComparator.BEFORE;
        }
        else
        {
            Set<URI> tempSet1;
            if(set1.isEmpty())
            {
                tempSet1 = Collections.emptySet();
            }
            else
            {
                tempSet1 = new HashSet<>(set1);
            }
            Set<URI> tempSet2;
            if(set2.isEmpty())
            {
                tempSet2 = Collections.emptySet();
            }
            else
            {
                tempSet2 = new HashSet<>(set2);
            }
            if(!set2.isEmpty())
            {
                for(final URI nextImport1 : set1)
                {
                    if(set2.contains(nextImport1))
                    {
                        tempSet1.remove(nextImport1);
                    }
                }
            }
            if(!set1.isEmpty())
            {
                for(final URI nextImport2 : set2)
                {
                    if(set1.contains(nextImport2))
                    {
                        tempSet2.remove(nextImport2);
                    }
                }
            }
            
            if(tempSet1.size() > tempSet2.size())
            {
                return OntologyImportsComparator.AFTER;
            }
            else if(tempSet2.size() > tempSet1.size())
            {
                return OntologyImportsComparator.BEFORE;
            }
            
            for(final URI nextImport1 : tempSet1)
            {
                final int compare = this.compare(nextImport1, o2);
                if(compare != OntologyImportsComparator.EQUALS)
                {
                    return compare;
                }
            }
            
            for(final URI nextImport2 : tempSet2)
            {
                final int compare = this.compare(o1, nextImport2);
                if(compare != OntologyImportsComparator.EQUALS)
                {
                    return compare;
                }
            }
            
            // Default to lexical mapping, as there is no direct semantic link between
            // them
            // at this point
            // FIXME: This should not be part of the comparison as the imports map
            // should always contain one of the URIs
            // return o1.stringValue().compareTo(o2.stringValue());
            throw new RuntimeException("Could not determine comparison result for ontology imports: " + o1 + " " + o2);
        }
    }
}