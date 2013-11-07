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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
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
    
    public static final Pattern REGEX_EXPERIMENT = Pattern.compile("^Project#(\\d{4})-(\\d{4})_Experiment#(\\d{4}).*");
    
    public static final String TEMPLATE_PROJECT = "Project#%04d-%04d";
    
    public static final String TEMPLATE_EXPERIMENT = "Project#%04d-%04d_Experiment#%04d";
    
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
            "CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } WHERE { ?object a ?type . OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } } VALUES (?type) { ( %s ) }";
    
    public static final String TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS =
            "CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } WHERE { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . FILTER(STRSTARTS(?label, \"%s\")) } VALUES (?type) { ( %s ) }";
    
    public HrppcPoddClient()
    {
        super();
    }
    
    public HrppcPoddClient(final String serverUrl)
    {
        super(serverUrl);
    }
    
    /**
     * Parses the given TrayScan project/experiment/tray/pot list and inserts the items into PODD
     * where they do not exist.
     * 
     * TODO: Should this process create new projects where they do not already exist? Ideally they
     * should be created and roles assigned before this process, but could be fine to do that in
     * here
     */
    public void uploadTrayScanList(final InputStream in) throws IOException, PoddClientException, OpenRDFException
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
        // TODO: This could be converted to not be prefilled in future, but currently it contains
        // all experiments in all unpublished projects in PODD that are accessible to the current
        // user
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap = new ConcurrentHashMap<>();
        
        // Cache for tray name mappings, starting at tray barcodes and ending with a mapping from
        // the URI of the tray to the URI of the experiment that contains the tray
        // NOTE: This is not prefilled, as it is populated on demand during processing of lines to
        // only contain the necessary elements
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap = new ConcurrentHashMap<>();
        
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
                    this.processTrayScanLine(headers, Arrays.asList(nextLine), projectUriMap, experimentUriMap,
                            trayUriMap, uploadQueue);
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
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap) throws PoddClientException
    {
        for(final String nextProjectName : projectUriMap.keySet())
        {
            final ConcurrentMap<URI, InferredOWLOntologyID> nextProjectNameMapping = projectUriMap.get(nextProjectName);
            for(final URI projectUri : nextProjectNameMapping.keySet())
            {
                final InferredOWLOntologyID artifactId = nextProjectNameMapping.get(projectUri);
                final Model nextSparqlResults =
                        this.doSPARQL(
                                String.format(HrppcPoddClient.TEMPLATE_SPARQL_BY_TYPE,
                                        RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_INVESTIGATION)),
                                artifactId);
                
                if(nextSparqlResults.isEmpty())
                {
                    this.log.info("Could not find any experiments for project: {} {}", nextProjectName, projectUri);
                }
                
                for(final Resource nextExperiment : nextSparqlResults.filter(null, RDF.TYPE,
                        PoddRdfConstants.PODD_SCIENCE_INVESTIGATION).subjects())
                {
                    if(!(nextExperiment instanceof URI))
                    {
                        this.log.error("Found experiment that was not assigned a URI: {} artifact={}", nextExperiment,
                                artifactId);
                    }
                    else
                    {
                        final Model label = nextSparqlResults.filter(nextExperiment, RDFS.LABEL, null);
                        
                        DebugUtils.printContents(label);
                        
                        if(label.isEmpty())
                        {
                            this.log.error("Experiment did not have a label: {} {}", artifactId, nextExperiment);
                        }
                        else
                        {
                            for(final Value nextLabel : label.objects())
                            {
                                if(!(nextLabel instanceof Literal))
                                {
                                    this.log.error("Project had a non-literal label: {} {} {}", artifactId,
                                            nextExperiment, nextLabel);
                                }
                                else
                                {
                                    String nextLabelString = nextLabel.stringValue();
                                    
                                    // take off any descriptions and leave the
                                    // project number behind
                                    nextLabelString = nextLabelString.split(" ")[0];
                                    
                                    final Matcher matcher = HrppcPoddClient.REGEX_EXPERIMENT.matcher(nextLabelString);
                                    
                                    if(!matcher.matches())
                                    {
                                        this.log.error(
                                                "Found experiment label that did not start with expected format: {}",
                                                nextLabel);
                                    }
                                    else
                                    {
                                        this.log.info(
                                                "Found experiment label with the expected format: '{}' original=<{}>",
                                                nextLabelString, nextLabel);
                                        
                                        final int nextProjectYear = Integer.parseInt(matcher.group(1));
                                        final int nextProjectNumber = Integer.parseInt(matcher.group(2));
                                        final int nextExperimentNumber = Integer.parseInt(matcher.group(3));
                                        
                                        nextLabelString =
                                                String.format(HrppcPoddClient.TEMPLATE_EXPERIMENT, nextProjectYear,
                                                        nextProjectNumber, nextExperimentNumber);
                                        
                                        this.log.info("Reformatted experiment label to: '{}' original=<{}>",
                                                nextLabelString, nextLabel);
                                        
                                        ConcurrentMap<URI, URI> labelMap = new ConcurrentHashMap<>();
                                        final ConcurrentMap<URI, URI> putIfAbsent =
                                                experimentUriMap.putIfAbsent(nextLabelString, labelMap);
                                        if(putIfAbsent != null)
                                        {
                                            this.log.error(
                                                    "Found duplicate experiment name, inconsistent results may follow: {} {} {}",
                                                    artifactId, nextExperiment, nextLabel);
                                            // Overwrite our reference with the one that already
                                            // existed
                                            labelMap = putIfAbsent;
                                        }
                                        final URI existingProject =
                                                labelMap.putIfAbsent((URI)nextExperiment, projectUri);
                                        // Check for the case where project name maps to different
                                        // artifacts
                                        if(existingProject != null && !existingProject.equals(projectUri))
                                        {
                                            this.log.error(
                                                    "Found duplicate experiment name across different projects, inconsistent results may follow: {} {} {} {}",
                                                    artifactId, existingProject, projectUri, nextLabel);
                                        }
                                    }
                                }
                            }
                        }
                        
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
                                
                                final Matcher matcher = HrppcPoddClient.REGEX_PROJECT.matcher(nextLabelString);
                                
                                if(!matcher.matches())
                                {
                                    this.log.error("Found project label that did not start with expected format: {}",
                                            nextLabel);
                                }
                                else
                                {
                                    this.log.info("Found project label with the expected format: '{}' original=<{}>",
                                            nextLabelString, nextLabel);
                                    
                                    final int nextProjectYear = Integer.parseInt(matcher.group(1));
                                    final int nextProjectNumber = Integer.parseInt(matcher.group(2));
                                    
                                    nextLabelString =
                                            String.format(HrppcPoddClient.TEMPLATE_PROJECT, nextProjectYear,
                                                    nextProjectNumber);
                                    
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
     * @param headers
     *            The list of headers
     * @param nextLine
     *            A list of values which can be matched against the list of headers
     * @param projectUriMap
     *            A map from the normalised project names to their URIs and the overall artifact
     *            identifiers.
     * @param experimentUriMap
     *            A map from normalised experiment names to their URIs and the projects that they
     *            are located in.
     * @param trayUriMap
     *            A map from normalised tray names (barcodes) to their URIs and the experiments that
     *            they are located in.
     * @param uploadQueue
     *            A map from artifact identifiers to Model objects containing all of the necessary
     *            changes to the artifact.
     * 
     * @throws PoddClientException
     *             If there was a problem communicating with PODD or the line was not valid.
     */
    private void processTrayScanLine(final List<String> headers, final List<String> nextLine,
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap,
            ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue) throws PoddClientException, OpenRDFException
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
            this.log.error("Position did not match expected format: {} {}", position, nextLine);
            // TODO: These may not be populated so do not throw an exception if it fails.
            // throw new
            // PoddClientException(MessageFormat.format("Position did not match expected format: {0}",
            // position));
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
        
        this.generateTrayScanRDF(projectUriMap, experimentUriMap, trayUriMap, uploadQueue, projectYear, projectNumber,
                experimentNumber, trayId, trayNotes, trayTypeName, plantId, genus, species);
    }
    
    /**
     * Generates the RDF triples necessary for the given TrayScan parameters and adds the details to
     * the relevant model in the upload queue.
     * 
     * @param projectUriMap
     *            A map of relevant project URIs and their artifact identifiers using their
     *            standardised labels.
     * @param experimentUriMap
     *            A map of relevant experiment URIs and their project URIs using their standardised
     *            labels.
     * @param trayUriMap
     *            A map of relevant tray URIs and their experiment URIs using their standardised
     *            labels.
     * @param uploadQueue
     *            The upload queue containing all of the models to be uploaded.
     * @param projectYear
     *            The TrayScan parameter detailing the project year for the next tray.
     * @param projectNumber
     *            The TrayScan parameter detailing the project number for the next tray.
     * @param experimentNumber
     *            The TrayScan parameter detailing the experiment number for the next tray.
     * @param species
     *            The species for the current line.
     * @param genus
     *            The genus for the current line.
     * @throws PoddClientException
     *             If there is a PODD Client exception.
     * @throws GraphUtilException
     *             If there was an illformed graph.
     */
    private void generateTrayScanRDF(
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap,
            ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue, final int projectYear,
            final int projectNumber, final int experimentNumber, final String trayId, final String trayNotes,
            final String trayTypeName, final String plantId, String genus, String species) throws PoddClientException,
        GraphUtilException
    {
        // Reconstruct Project#0001-0002 structure to get a normalised string
        final String baseProjectName = String.format(HrppcPoddClient.TEMPLATE_PROJECT, projectYear, projectNumber);
        URI nextProjectUri = null;
        URI nextExperimentUri = null;
        InferredOWLOntologyID nextProjectID = null;
        
        if(!projectUriMap.containsKey(baseProjectName))
        {
            this.log.error("Did not find an existing project for a line in the CSV file: {}", baseProjectName);
            
            // TODO: Create a new project?
            return;
        }
        
        final Map<URI, InferredOWLOntologyID> projectDetails = projectUriMap.get(baseProjectName);
        
        if(projectDetails.isEmpty())
        {
            this.log.error("Project mapping seemed to exist but it was empty: {}", baseProjectName);
            
            // TODO: Create a new project?
            return;
        }
        else if(projectDetails.size() > 1)
        {
            this.log.error(
                    "Found multiple PODD Project name mappings (not able to select between them automatically) : {}\n\n {}",
                    baseProjectName, projectDetails.keySet());
            
            // TODO: Throw exception?
            return;
        }
        else
        {
            this.log.info("Found unique PODD Project name to URI mapping: {} {}", baseProjectName, projectDetails);
            
            nextProjectUri = projectDetails.keySet().iterator().next();
            nextProjectID = projectDetails.get(nextProjectUri);
        }
        // Reconstruct Project#0001-0002_Experiment#0001 structure to get a normalised
        // string
        final String baseExperimentName =
                String.format(HrppcPoddClient.TEMPLATE_EXPERIMENT, projectYear, projectNumber, experimentNumber);
        
        if(!experimentUriMap.containsKey(baseExperimentName))
        {
            this.log.error("Did not find an existing experiment for a line in the CSV file: {}", baseExperimentName);
            
            // TODO: Create a new experiment?
            return;
        }
        
        final Map<URI, URI> experimentDetails = experimentUriMap.get(baseExperimentName);
        
        if(experimentDetails.isEmpty())
        {
            this.log.error("Experiment mapping seemed to exist but it was empty: {}", baseExperimentName);
            
            // TODO: Create a new experiment?
            return;
        }
        else if(experimentDetails.size() > 1)
        {
            this.log.error(
                    "Found multiple PODD Experiment name mappings (not able to select between them automatically) : {} {}",
                    baseExperimentName, experimentDetails);
            
            // TODO: Throw exception?
            return;
        }
        else
        {
            nextExperimentUri = experimentDetails.keySet().iterator().next();
            final URI checkProjectUri = experimentDetails.get(nextExperimentUri);
            if(!checkProjectUri.equals(nextProjectUri))
            {
                this.log.error(
                        "Experiment mapping was against a different project: {} experimentURI={} nextProjectUri={} checkProjectUri={}",
                        baseExperimentName, nextExperimentUri, nextProjectUri, checkProjectUri);
            }
        }
        
        // Create or find an existing model for the necessary modifications to this
        // project/artifact
        Model nextResult = new LinkedHashModel();
        final Model putIfAbsent = uploadQueue.putIfAbsent(nextProjectID, nextResult);
        if(putIfAbsent != null)
        {
            nextResult = putIfAbsent;
        }
        
        // Check whether trayId already has an assigned URI
        URI nextTrayURI;
        if(trayUriMap.containsKey(trayId))
        {
            nextTrayURI = trayUriMap.get(trayId).keySet().iterator().next();
        }
        else
        {
            final Model trayIdSparqlResults =
                    this.doSPARQL(
                            String.format(HrppcPoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS,
                                    RenderUtils.escape(trayId),
                                    RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_CONTAINER)),
                            nextProjectID);
            
            if(trayIdSparqlResults.isEmpty())
            {
                this.log.info(
                        "Could not find an existing container for tray barcode, assigning a temporary URI: {} {}",
                        trayId, nextProjectID);
                
                nextTrayURI = this.vf.createURI("urn:temp:uuid:tray:" + UUID.randomUUID().toString());
            }
            else
            {
                nextTrayURI =
                        GraphUtil.getUniqueSubjectURI(trayIdSparqlResults, RDF.TYPE,
                                PoddRdfConstants.PODD_SCIENCE_CONTAINER);
            }
            
            ConcurrentMap<URI, URI> nextTrayUriMap = new ConcurrentHashMap<>();
            ConcurrentMap<URI, URI> putIfAbsent2 = trayUriMap.putIfAbsent(trayId, nextTrayUriMap);
            if(putIfAbsent2 != null)
            {
                nextTrayUriMap = putIfAbsent2;
            }
            nextTrayUriMap.put(nextTrayURI, nextExperimentUri);
            
            trayUriMap.putIfAbsent(trayId, nextTrayUriMap);
        }
        
        // Check whether plantId already has an assigned URI
        URI nextPotURI;
        final Model plantIdSparqlResults =
                this.doSPARQL(
                        String.format(HrppcPoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS,
                                RenderUtils.escape(plantId),
                                RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_CONTAINER)),
                        nextProjectID);
        
        if(plantIdSparqlResults.isEmpty())
        {
            this.log.info("Could not find an existing container for pot barcode, assigning a temporary URI: {} {}",
                    plantId, nextProjectID);
            
            nextPotURI = this.vf.createURI("urn:temp:uuid:pot:" + UUID.randomUUID().toString());
        }
        else
        {
            nextPotURI =
                    GraphUtil.getUniqueSubjectURI(plantIdSparqlResults, RDF.TYPE,
                            PoddRdfConstants.PODD_SCIENCE_CONTAINER);
        }
        
        // TODO
        // Add new poddScience:Container for tray
        nextResult.add(nextTrayURI, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_CONTAINER);
        // Link tray to experiment
        nextResult.add(nextExperimentUri, PoddRdfConstants.PODD_SCIENCE_HAS_CONTAINER, nextTrayURI);
        // TrayID => Add poddScience:hasBarcode to tray
        nextResult.add(nextTrayURI, PoddRdfConstants.PODD_SCIENCE_HAS_BARCODE, this.vf.createLiteral(trayId));
        // TrayNotes => Add rdfs:label to tray
        nextResult.add(nextTrayURI, RDFS.LABEL, this.vf.createLiteral(trayNotes));
        // TrayTypeName => Add poddScience:hasContainerType to tray
        nextResult.add(nextTrayURI, PoddRdfConstants.PODD_SCIENCE_HAS_CONTAINER_TYPE,
                this.vf.createLiteral(trayTypeName));
        
        // Position => TODO: Need to use randomisation data to populate the position
        
        // Add new poddScience:Container for pot
        nextResult.add(nextPotURI, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_CONTAINER);
        // Link pot to tray
        nextResult.add(nextTrayURI, PoddRdfConstants.PODD_SCIENCE_HAS_CONTAINER, nextPotURI);
        // PlantID => Add poddScience:hasBarcode to pot
        nextResult.add(nextPotURI, PoddRdfConstants.PODD_SCIENCE_HAS_BARCODE, this.vf.createLiteral(plantId));
        // PlantName => TODO: Link using poddScience:hasLine
        // TODO: Also link genus and species using poddScience:hasGenusSpecies
        // PlantNotes => Add rdfs:comment to pot
        
        // TODO Using d110cc.csv
        // Add poddScience:hasReplicate for tray to link it to the rep #
        // Add poddScience:hasReplicate for pot to link it to the rep # (??to make queries easier??)
        
        DebugUtils.printContents(nextResult);
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