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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.queryrender.RenderUtils;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.github.podd.client.api.PoddClientException;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;

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
    public static final String TRAY_TYPE_NAME = "TrayTypeName";
    public static final String POSITION = "Position";
    public static final String PLANT_ID = "PlantID";
    public static final String PLANT_NAME = "PlantName";
    public static final String PLANT_NOTES = "PlantNotes";
    
    public static final Pattern REGEX_PROJECT = Pattern.compile("^Project#(\\d{4})-(\\d{4}).*");
    
    public static final String TEMPLATE_PROJECT = "Project#%04d-%04d";
    
    // PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN
    public static final Pattern REGEX_TRAY = Pattern
            .compile("Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4})_(\\w+)\\.(\\w+)_Tray#(\\d{4,5})");
    
    /**
     * Number of groups matching in the tray id regex.
     */
    public static final int TRAY_ID_SIZE = 6;
    
    // PROJECT#YYYY-NNNN_EXPERIMENT#NNNN_GENUS.SPECIES_TRAY#NNNNN_POT#NNNNN
    public static final Pattern REGEX_PLANT = Pattern
            .compile("Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4})_(\\w+)\\.(\\w+)_Tray#(\\d{4,5})_Pot#(\\d{4,5})");
    
    /**
     * Number of groups matching in the plant id regex.
     */
    public static final int PLANT_ID_SIZE = 7;
    
    public static final Pattern REGEX_POSITION = Pattern.compile("([a-zA-Z]+)([0-9]+)");
    
    /**
     * Number of groups matching in the position regex.
     */
    public static final int POSITION_SIZE = 2;
    
    public static final String TEMPLATE_SPARQL_BY_TYPE =
            "CONSTRUCT { ?object a ?type } WHERE { ?object a ?type } VALUES (?type) { ( %s ) }";
    
    public HrppcPoddClient()
    {
        super();
    }
    
    public HrppcPoddClient(final String serverUrl)
    {
        super(serverUrl);
    }
    
    /**
     * Parses the given PlantScan project/experiment/tray/pot list and inserts the items into PODD
     * where they do not exist.
     * 
     * TODO: Should this process create new projects where they do not already exist? Ideally they
     * should be created and roles assigned before this process, but could be fine to do that in
     * here
     */
    public void uploadPlantScanList(final InputStream in) throws IOException, PoddClientException
    {
        // Only select the unpublished artifacts, as we cannot edit published artifacts
        final Model currentUnpublishedArtifacts = this.listArtifacts(false, true);
        
        // Keep a queue so that we only need to update each project once for
        // this operation to succeed
        final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue = new ConcurrentHashMap<>();
        
        // Map starting at project name strings and ending with both the URI of
        // the project and the artifact
        final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap =
                new ConcurrentHashMap<>();
        
        // Map starting at experiment name strings and ending with a mapping from the URI of
        // the experiment to the URI of the project that contains the experiment
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap = new ConcurrentHashMap<>();
        
        // Map known project names to their URIs, as the URIs are needed to
        // create statements internally
        this.populateProjectUriMap(currentUnpublishedArtifacts, projectUriMap);
        
        this.populateExperimentUriMap(projectUriMap, experimentUriMap);
        
        List<String> headers = null;
        // TODO: Implement getObjectsByType(InferredOWLOntology, URI) so that
        // experiments etc can be found easily
        // and the identifier can be mapped as necessary to the identifier in
        // the header
        // Supressing warning generated erroneously by Eclipse:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=371614
        try (@SuppressWarnings("resource")
        final InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                final CSVReader reader = new CSVReader(inputStreamReader);)
        {
            String[] nextLine;
            while((nextLine = reader.readNext()) != null)
            {
                if(headers == null)
                {
                    // header line is mandatory in PODD CSV
                    headers = Arrays.asList(nextLine);
                    try
                    {
                        this.verifyProjectListHeaders(headers);
                    }
                    catch(final IllegalArgumentException e)
                    {
                        this.log.error("Could not verify headers for project list: {}", e.getMessage());
                        throw new PoddClientException("Could not verify headers for project list", e);
                    }
                }
                else
                {
                    if(nextLine.length != headers.size())
                    {
                        this.log.error("Line and header sizes were different: {} {}", headers, nextLine);
                    }
                    
                    // Process the next line and add it to the upload queue
                    this.processPlantScanLine(headers, Arrays.asList(nextLine), projectUriMap, uploadQueue);
                }
            }
        }
        
        if(headers == null)
        {
            this.log.error("Document did not contain a valid header line");
        }
        
        if(uploadQueue.isEmpty())
        {
            this.log.error("Document did not contain any valid rows");
        }
        
        this.uploadToPodd(uploadQueue);
    }
    
    private void populateExperimentUriMap(
            ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap) throws PoddClientException
    {
        for(String nextProjectName : projectUriMap.keySet())
        {
            ConcurrentMap<URI, InferredOWLOntologyID> nextProjectNameMapping = projectUriMap.get(nextProjectName);
            for(URI projectUri : nextProjectNameMapping.keySet())
            {
                InferredOWLOntologyID artifactId = nextProjectNameMapping.get(projectUri);
                Model nextSparqlResults =
                        this.doSPARQL(
                                String.format(TEMPLATE_SPARQL_BY_TYPE,
                                        RenderUtils.getSPARQLQueryString(projectUri)), artifactId);
                
                if(nextSparqlResults.isEmpty())
                {
                    this.log.info("Could not find any experiments for project: {} {}", nextProjectName, projectUri);
                }
                
                for(Resource nextExperiment : nextSparqlResults.filter(null, RDF.TYPE,
                        PoddRdfConstants.PODD_SCIENCE_EXPERIMENT).subjects())
                {
                    if(!(nextExperiment instanceof URI))
                    {
                        this.log.error("Found experiment that was not assigned a URI: {} artifact={}", nextExperiment,
                                artifactId);
                    }
                    else
                    {
                        String name = nextSparqlResults.filter(nextExperiment, RDFS.LABEL, null).objectString();
                        
                        ConcurrentMap<URI, URI> nextMap = new ConcurrentHashMap<>();
                        nextMap.put((URI)nextExperiment, projectUri);
                        experimentUriMap.put(name, nextMap);
                    }
                }
            }
        }
    }
    
    private void uploadToPodd(final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue) throws PoddClientException
    {
        for(final InferredOWLOntologyID nextUpload : uploadQueue.keySet())
        {
            try
            {
                final StringWriter writer = new StringWriter(4096);
                Rio.write(uploadQueue.get(nextUpload), writer, RDFFormat.RDFJSON);
                final InferredOWLOntologyID newID =
                        this.appendArtifact(nextUpload,
                                new ByteArrayInputStream(writer.toString().getBytes(Charset.forName("UTF-8"))),
                                RDFFormat.RDFJSON);
                
                if(newID == null)
                {
                    this.log.error("Did not find a valid result from append artifact: {}", nextUpload);
                }
                
                if(nextUpload.equals(newID))
                {
                    this.log.error("Result from append artifact was not changed, as expected. {} {}", nextUpload, newID);
                }
            }
            catch(final RDFHandlerException e)
            {
                this.log.error("Found exception generating upload body: ", e);
            }
        }
    }
    
    private void populateProjectUriMap(final Model currentUnpublishedArtifacts,
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap)
        throws PoddClientException
    {
        for(final InferredOWLOntologyID nextArtifact : OntologyUtils.modelToOntologyIDs(currentUnpublishedArtifacts))
        {
            final Model nextTopObject = this.getTopObject(nextArtifact, currentUnpublishedArtifacts);
            
            DebugUtils.printContents(nextTopObject);
            
            final Model types = nextTopObject.filter(null, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_PROJECT);
            if(types.isEmpty())
            {
                // We only map project based artifacts, others are ignored with
                // log messages here
                this.log.info("Found a non-project based artifact, ignoring as it isn't relevant here: {}",
                        nextArtifact);
            }
            else if(types.subjects().size() > 1)
            {
                // We only map single-project based artifacts, others are
                // ignored with log messages here
                this.log.error("Found multiple projects for an artifact: {} {}", nextArtifact, types.subjects());
            }
            else
            {
                final Resource project = types.subjects().iterator().next();
                
                if(!(project instanceof URI))
                {
                    // We only map URI references, as blank nodes which are
                    // allowable, cannot be reserialised to update the artifact,
                    // and should not exist
                    this.log.error("Found non-URI project reference for an artifact: {} {}", nextArtifact,
                            types.subjects());
                }
                else
                {
                    final Model label = nextTopObject.filter(project, RDFS.LABEL, null);
                    
                    DebugUtils.printContents(label);
                    
                    if(label.isEmpty())
                    {
                        this.log.error("Project did not have a label: {} {}", nextArtifact, project);
                    }
                    else
                    {
                        for(final Value nextLabel : label.objects())
                        {
                            if(!(nextLabel instanceof Literal))
                            {
                                this.log.error("Project had a non-literal label: {} {} {}", nextArtifact, project,
                                        nextLabel);
                            }
                            else
                            {
                                String nextLabelString = nextLabel.stringValue();
                                
                                // take off any descriptions and leave the
                                // project number behind
                                nextLabelString = nextLabelString.split(" ")[0];
                                
                                Matcher matcher = HrppcPoddClient.REGEX_PROJECT.matcher(nextLabelString);
                                
                                if(!matcher.matches())
                                {
                                    this.log.error("Found project label that did not start with expected format: {}",
                                            nextLabel);
                                }
                                else
                                {
                                    this.log.info("Found project label with the expected format: '{}' original=<{}>",
                                            nextLabelString, nextLabel);
                                    
                                    int nextProjectYear = Integer.parseInt(matcher.group(1));
                                    int nextProjectNumber = Integer.parseInt(matcher.group(2));
                                    
                                    nextLabelString =
                                            String.format(TEMPLATE_PROJECT, nextProjectYear, nextProjectNumber);
                                    
                                    this.log.info("Reformatted project label to: '{}' original=<{}>", nextLabelString,
                                            nextLabel);
                                    
                                    ConcurrentMap<URI, InferredOWLOntologyID> labelMap = new ConcurrentHashMap<>();
                                    final ConcurrentMap<URI, InferredOWLOntologyID> putIfAbsent =
                                            projectUriMap.putIfAbsent(nextLabelString, labelMap);
                                    if(putIfAbsent != null)
                                    {
                                        this.log.error(
                                                "Found duplicate project name, inconsistent results may follow: {} {} {}",
                                                nextArtifact, project, nextLabel);
                                        // Overwrite our reference with the one that
                                        // already existed
                                        labelMap = putIfAbsent;
                                    }
                                    final InferredOWLOntologyID existingArtifact =
                                            labelMap.putIfAbsent((URI)project, nextArtifact);
                                    // Check for the case where project name maps to
                                    // different artifacts
                                    if(existingArtifact != null && !existingArtifact.equals(nextArtifact))
                                    {
                                        this.log.error(
                                                "Found duplicate project name across different projects, inconsistent results may follow: {} {} {} {}",
                                                nextArtifact, existingArtifact, project, nextLabel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Model getTopObject(final InferredOWLOntologyID nextArtifact, final Model artifactDetails)
        throws PoddClientException
    {
        return artifactDetails.filter(
                artifactDetails.filter(nextArtifact.getOntologyIRI().toOpenRDFURI(),
                        PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, null).objectURI(), null, null);
    }
    
    /**
     * Process a single line from the input file, using the given headers as the definitions for the
     * line.
     * 
     * @throws PoddClientException
     */
    private void processPlantScanLine(final List<String> headers, final List<String> nextLine,
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue) throws PoddClientException
    {
        this.log.info("About to process line: {}", nextLine);
        
        String trayId = null;
        String trayNotes = null;
        String trayTypeName = null;
        String position = null;
        String plantId = null;
        String plantName = null;
        String plantNotes = null;
        
        for(int i = 0; i < headers.size(); i++)
        {
            final String nextHeader = headers.get(i);
            final String nextField = nextLine.get(i);
            
            if(nextHeader.trim().equals(HrppcPoddClient.TRAY_ID))
            {
                trayId = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.TRAY_NOTES))
            {
                trayNotes = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.TRAY_TYPE_NAME))
            {
                trayTypeName = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.POSITION))
            {
                position = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.PLANT_ID))
            {
                plantId = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.PLANT_NAME))
            {
                plantName = nextField;
            }
            else if(nextHeader.trim().equals(HrppcPoddClient.PLANT_NOTES))
            {
                plantNotes = nextField;
            }
            else
            {
                this.log.error("Found unrecognised header: {} {}", nextHeader, nextField);
            }
        }
        
        int projectYear = 0;
        int projectNumber = 0;
        int experimentNumber = 0;
        String genus = null;
        String species = null;
        int trayNumber = 0;
        int potNumber = 0;
        
        final Matcher trayMatcher = HrppcPoddClient.REGEX_TRAY.matcher(trayId);
        
        if(!trayMatcher.matches())
        {
            this.log.error("Tray ID did not match expected format: {}", trayId);
        }
        else
        {
            if(trayMatcher.groupCount() != HrppcPoddClient.TRAY_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Tray ID: {} {}",
                        trayMatcher.groupCount(), HrppcPoddClient.TRAY_ID_SIZE);
            }
            else
            {
                projectYear = Integer.parseInt(trayMatcher.group(1).trim());
                projectNumber = Integer.parseInt(trayMatcher.group(2).trim());
                experimentNumber = Integer.parseInt(trayMatcher.group(3).trim());
                genus = trayMatcher.group(4).trim();
                species = trayMatcher.group(5).trim();
                trayNumber = Integer.parseInt(trayMatcher.group(6).trim());
            }
        }
        
        final Matcher plantMatcher = HrppcPoddClient.REGEX_PLANT.matcher(plantId);
        
        if(!plantMatcher.matches())
        {
            this.log.error("Plant ID did not match expected format: {}", plantId);
        }
        else
        {
            if(plantMatcher.groupCount() != HrppcPoddClient.PLANT_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Plant ID: {} {}",
                        plantMatcher.groupCount(), HrppcPoddClient.PLANT_ID_SIZE);
            }
            else
            {
                // if(projectYear == null)
                // {
                // projectYear = plantMatcher.group(1).trim();
                // }
                // if(projectNumber == null)
                // {
                // projectNumber = plantMatcher.group(2);
                // }
                // if(experimentNumber == null)
                // {
                // experimentNumber = plantMatcher.group(3);
                // }
                if(genus == null)
                {
                    genus = plantMatcher.group(4).trim();
                }
                if(species == null)
                {
                    species = plantMatcher.group(5).trim();
                }
                // if(trayNumber == null)
                // {
                // trayNumber = plantMatcher.group(6);
                // }
                potNumber = Integer.parseInt(plantMatcher.group(7).trim());
            }
        }
        
        String columnLetter = null;
        String rowNumber = null;
        
        final Matcher positionMatcher = HrppcPoddClient.REGEX_POSITION.matcher(position);
        
        if(!positionMatcher.matches())
        {
            this.log.error("Position did not match expected format: {}", position);
            throw new PoddClientException(MessageFormat.format("Position did not match expected format: {0}", position));
        }
        else if(positionMatcher.groupCount() != HrppcPoddClient.POSITION_SIZE)
        {
            this.log.error("Did not find the expected number of regex matches for Position: {} {}",
                    positionMatcher.groupCount(), HrppcPoddClient.POSITION_SIZE);
            throw new PoddClientException(MessageFormat.format(
                    "Did not find the expected number of regex matches for Position: {0}", position));
        }
        else
        {
            columnLetter = positionMatcher.group(1).trim();
            rowNumber = positionMatcher.group(2).trim();
        }
        
        // Reconstruct Project#0001-0002 structure
        String baseProjectName = String.format(HrppcPoddClient.TEMPLATE_PROJECT, projectYear, projectNumber);
        
        if(!projectUriMap.containsKey(baseProjectName))
        {
            this.log.error("Did not find an existing project for a line in the CSV file: {}", baseProjectName);
        }
        else
        {
            Map<URI, InferredOWLOntologyID> projectDetails = projectUriMap.get(baseProjectName);
            
            if(projectDetails.isEmpty())
            {
                this.log.error("Project mapping seemed to exist but it was empty: {}", baseProjectName);
            }
            else if(projectDetails.size() > 1)
            {
                this.log.error("Found multiple PODD Project name mappings : {} {}", baseProjectName, projectDetails);
            }
            else
            {
                this.log.info("Found unique PODD Project name to URI mapping: {} {}", baseProjectName, projectDetails);
                
                Model nextResult = new LinkedHashModel();
                
            }
            
        }
    }
    
    /**
     * Verifies the list of projects, throwing an IllegalArgumentException if there are unrecognised
     * headers or if any mandatory headers are missing.
     * 
     * @throws IllegalArgumentException
     *             If the headers are not verified correctly.
     */
    private void verifyProjectListHeaders(final List<String> headers) throws IllegalArgumentException
    {
        if(headers == null || headers.size() < HrppcPoddClient.MIN_HEADERS_SIZE)
        {
            this.log.error("Did not find valid headers: {}", headers);
            throw new IllegalArgumentException("Did not find valid headers");
        }
        
        if(!headers.contains(HrppcPoddClient.TRAY_ID))
        {
            throw new IllegalArgumentException("Did not find tray id header");
        }
        
        if(!headers.contains(HrppcPoddClient.TRAY_NOTES))
        {
            throw new IllegalArgumentException("Did not find tray notes header");
        }
        
        if(!headers.contains(HrppcPoddClient.TRAY_TYPE_NAME))
        {
            throw new IllegalArgumentException("Did not find tray type name header");
        }
        
        if(!headers.contains(HrppcPoddClient.POSITION))
        {
            throw new IllegalArgumentException("Did not find position header");
        }
        
        if(!headers.contains(HrppcPoddClient.PLANT_ID))
        {
            throw new IllegalArgumentException("Did not find plant id header");
        }
        
        if(!headers.contains(HrppcPoddClient.PLANT_NAME))
        {
            throw new IllegalArgumentException("Did not find plant name header");
        }
        
        if(!headers.contains(HrppcPoddClient.PLANT_NOTES))
        {
            throw new IllegalArgumentException("Did not find plant notes header");
        }
    }
}