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

/**
 * Constants used in the Randomisation spreadsheet.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class RandomisationConstants
{
    /**
     * The minimum number of headers in the randomisation file.
     */
    public static final int MIN_RANDOMISATION_HEADERS_SIZE = 7;
    
    /**
     * Randomisation header for column number.
     */
    public static final String RAND_COLUMN = "column";
    
    /**
     * Randomisation header for the number used by the R script to identify a line.
     */
    public static final String RAND_LAYOUT = "layout";
    
    /**
     * Randomisation header for replicate number.
     */
    public static final String RAND_REPLICATE = "Rep";
    
    /**
     * Randomisation header for row number.
     */
    public static final String RAND_ROW = "row";
    
    /**
     * Randomisation header for shelf number.
     */
    public static final String RAND_SHELF = "Shelf";
    
    /**
     * Randomisation header for shelf side.
     */
    public static final String RAND_SHELF_SIDE = "shelf side";
    
    /**
     * Randomisation header for tray number.
     */
    public static final String RAND_TRAY = "tray";
    
    /**
     * Randomisation header for the name used by the scientist to identify a line.
     */
    public static final String RAND_VAR = "var";
    
    /**
     * Randomisation header for the name used by the randomisation algorithm to identify a line
     * number in the lookup table used to identify a line.
     */
    public static final String RAND_LINE_NUMBER = "Randomisation line number";
    
    /**
     * Randomisation header for the name used by the client to identify a line in the lookup table
     * used to identify a line.
     */
    public static final String RAND_CLIENT_LINE_NAME = "Client line name";
    
}
