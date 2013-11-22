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

import java.util.regex.Pattern;

/**
 * Constants used in the Client spreadsheet, used to determine the trays and pots for each
 * experiment.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class ClientSpreadsheetConstants
{
    public static final String CLIENT_YEAR = "Year";
    public static final String CLIENT_PROJECT_NUMBER = "ProjectNumber";
    public static final String CLIENT_EXPERIMENT_NUMBER = "ExperimentNumber";
    public static final String CLIENT_GENUS = "Genus";
    public static final String CLIENT_SPECIES = "Species";
    public static final String CLIENT_POT_NUMBER = "PotNumber";
    public static final String CLIENT_TRAY_NUMBER = "TrayNumber";
    public static final String CLIENT_POT_NUMBER_TRAY = "PotNumberTray";
    public static final String CLIENT_COLUMN_NUMBER_TRAY = "ColumnNumberTray";
    public static final String CLIENT_COLUMN_LETTER = "ColumnLetter";
    public static final String CLIENT_ROW_NUMBER_TRAY = "RowNumberTray";
    public static final String CLIENT_ROW_NUMBER_REP = "RowNumberRep";
    public static final String CLIENT_COLUMN_NUMBER_REP = "ColumnNumberRep";
    public static final String CLIENT_COLUMN_NUMBER = "ColumnNumber";
    /**
     * Client spreadsheet header for the barcode for the tray.
     */
    public static final String CLIENT_TRAY_ID = "TrayID";
    /**
     * Client spreadsheet header for the notes for the tray.
     */
    public static final String CLIENT_TRAY_NOTES = "TrayNotes";
    public static final String CLIENT_TRAY_ROW_NUMBER = "TrayRowNumber";
    /**
     * Client spreadsheet header for the type of the tray.
     */
    public static final String CLIENT_TRAY_TYPE_NAME = "TrayTypeName";
    /**
     * Client spreadsheet header for the position.
     */
    public static final String CLIENT_POSITION = "Position";
    public static final String CLIENT_PLANT_ID = "PlantID";
    public static final String CLIENT_PLANT_LINE_NUMBER = "PlantLineNumber";
    public static final String CLIENT_PLANT_LINE_NAME = "PlantLineName";
    public static final String CLIENT_PLANT_NOTE = "PlantNote";
    public static final String CLIENT_CONTROL = "Control";
    public static final String CLIENT_REPLICATE_NUMBER = "ReplicateNumber";
    public static final String CLIENT_POT_REPLICATE_NUMBER = "PotReplicateNumber";
    
    /**
     * Number of groups matching in the plant id regex.
     */
    public static final int CLIENT_PLANT_ID_SIZE = 7;
    public static final String CLIENT_PLANT_NAME = "PlantName";
    public static final String CLIENT_PLANT_NOTES = "PlantNotes";
    /**
     * Number of groups matching in the tray id regex.
     */
    public static final int CLIENT_TRAY_ID_SIZE = 6;
    public static final int MIN_TRAYSCAN_HEADERS_SIZE = 7;
    /**
     * Number of groups matching in the position regex.
     */
    public static final int POSITION_SIZE = 2;
    public static final Pattern REGEX_EXPERIMENT = Pattern.compile("^Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4}).*");
    // PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN_POT#NNNNN
    public static final Pattern REGEX_PLANT = Pattern
            .compile("Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4})_(\\w+)\\.(\\w+)_Tray#(\\d{4,5})_Pot#(\\d{4,5})");
    public static final Pattern REGEX_POSITION = Pattern.compile("([a-zA-Z]+)([0-9]+)");
    public static final Pattern REGEX_PROJECT = Pattern.compile("^Project#(\\d{4})-(\\d{4}).*");
    // PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN
    public static final Pattern REGEX_TRAY = Pattern
            .compile("Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4})_(\\w+)\\.(\\w+)_Tray#(\\d{4,5})");
    public static final String TEMPLATE_EXPERIMENT = "Project#%04d-%04d_Experiment#%04d";
    public static final String TEMPLATE_PROJECT = "Project#%04d-%04d";
    public static final String TEMPLATE_SPARQL_BY_TYPE =
            "CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } WHERE { ?object a ?type . OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } } VALUES (?type) { ( %s ) }";
    public static final String TEMPLATE_SPARQL_BY_TYPE_ALL_PROPERTIES =
            "CONSTRUCT { ?object a ?type . ?object ?predicate ?value . } WHERE { ?object a ?type . ?object ?predicate ?value . } VALUES (?type) { ( %s ) }";
    public static final String TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS =
            "CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } WHERE { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . FILTER(STRSTARTS(?label, \"%s\")) } VALUES (?type) { ( %s ) }";
    public static final String LABEL_POT = "Pot #";
    
}
