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
package au.org.plantphenomics.podd;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.github.podd.client.api.PoddClientException;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
import com.github.podd.utils.InferredOWLOntologyID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides operations specific to HRPPC in relation to putting projects into PODD.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class HrppcPoddClient extends RestletPoddClientImpl 
{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    	public static final int MIN_HEADERS_SIZE = 7;
	
	public static final String TRAY_ID = "TrayID";
	public static final String TRAY_NOTES = "TrayNotes";
	public static final String TRAY_TYPE_NAME = "TrayTypeName"
	public static final String POSITION = "Position"
	public static final String PLANT_ID = "PlantID"
	public static final String PLANT_NAME = "PlantName";
	public static final String PLANT_NOTES = "PlantNotes";
	
	public HrppcPoddClient()
	{
		super();
	}
	
	public HrppcPoddClient(final String serverUrl)
	{
		super(serverUrl);
	}
    
	/**
	 * Parses the given PlantScan project/experiment/tray/pot list and inserts the 
	 * items into PODD where they do not exist.
	 */
	public void uploadPlantScanList(InputStream in) throws IOException, PoddClientException
	{
		List<InferredOWLOntologyID> currentUnpublishedArtifacts = this.listUnpublishedArtifacts();
		
		// TODO: Implement getTopObject(InferredOWLOntologyID) so that the top object for each can be 
		// scanned easily to determine its name which is required, by convention, here
		
		// TODO: Implement getObjectsByType(InferredOWLOntology, URI) so that experiments etc can be found easily 
		// and the identifier can be mapped as necessary to the identifier in the header
		
		List<String> headers = null;
                CSVReader reader = new CSVReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) 
                {
                        if (headers == null) 
                        {
                                // header line is mandatory in PODD CSV
                                headers = Arrays.asList(nextLine);
                                try
                                {
                                	verifyProjectListHeaders(headers);
                                }
                                catch(IllegalArgumentException e)
                                {
                                	this.log.error("Could not verify headers for project list: {}", e.getMessage());
                                	throw new PoddClientException("Could not verify headers for project list", e);
                                }
                        }
                        else 
                        {
                        	
                        }
                }
        }
        
        /**
         * Verifies the list of projects, throwing an IllegalArgumentException if there are 
         * unrecognised headers or if any mandatory headers are missing.
         * 
         * @throws IllegalArgumentException If the headers are not verified correctly.
         */
        public void verifyProjectListHeaders(List<String> headers) throws IllegalArgumentException
        {
        	if(headers == null || headers.size() < MIN_HEADERS_SIZE)
        	{
        		throw new IllegalArgumentException("Did not find valid headers");
        	}
        	
        	if(!headers.contains(TRAY_ID))
        	{
        		throw new IllegalArgumentException("Did not find tray id header");
        	}

        	if(!headers.contains(TRAY_NOTES))
        	{
        		throw new IllegalArgumentException("Did not find tray notes header");
        	}

        	if(!headers.contains(TRAY_TYPE_NAME))
        	{
        		throw new IllegalArgumentException("Did not find tray type name header");
        	}

        	if(!headers.contains(POSITION))
        	{
        		throw new IllegalArgumentException("Did not find position header");
        	}
        	
        	if(!headers.contains(PLANT_ID))
        	{
        		throw new IllegalArgumentException("Did not find plant id header");
        	}
        	
        	if(!headers.contains(PLANT_NAME))
        	{
        		throw new IllegalArgumentException("Did not find plant name header");
        	}
        	
        	if(!headers.contains(PLANT_NOTES))
        	{
        		throw new IllegalArgumentException("Did not find plant notes header");
        	}
        }
}