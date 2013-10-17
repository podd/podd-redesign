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

import com.github.podd.client.impl.restlet.RestletPoddClientImpl;

/**
 * Provides operations specific to HRPPC in relation to putting projects into PODD.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class HrppcPoddClient extends RestletPoddClientImpl 
{
	public HrppcPoddClient()
	{
		super();
	}
	
	public HrppcPoddClient(final String serverUrl)
	{
		super(serverUrl);
	}
    
	/**
	 * Parses the given project list and inserts the items into PODD where they do not exist.
	 */
	public void parseProjectList(InputStream in) throws IOException
	{
		List<String> headers = null;
                CSVReader reader = new CSVReader(new InputStreamReader(in, Charset.forName("UTF-8")));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) 
                {
                        if (headers == null) 
                        {
                                // header line is mandatory in PODD CSV
                                headers = Arrays.asList(nextLine);
                                verifyProjectListHeaders(headers);
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
        }
}