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
    
    public HrppcPoddClient()
    {
        super();
    }
    
    public HrppcPoddClient(final String serverUrl)
    {
        super(serverUrl);
    }
    
    /**
     * @param baseExperimentName
     * @param experimentDetails
     * @throws PoddClientException
     */
    private void checkExperimentDetails(final String baseExperimentName, final Map<URI, URI> experimentDetails)
        throws PoddClientException
    {
        if(experimentDetails.isEmpty())
        {
            this.log.error("Experiment mapping seemed to exist but it was empty: {}", baseExperimentName);
            
            // TODO: Create a new experiment?
            // return;
            throw new PoddClientException("Did not find an existing experiment for a line in the CSV file: "
                    + baseExperimentName);
            
        }
        else if(experimentDetails.size() > 1)
        {
            this.log.error(
                    "Found multiple PODD Experiment name mappings (not able to select between them automatically) : {} {}",
                    baseExperimentName, experimentDetails);
            
            // TODO: Throw exception?
            // return;
            throw new PoddClientException("Found multiple experiments for a line in the CSV file: "
                    + baseExperimentName);
            
        }
    }
    
    /**
     * @param baseProjectName
     * @param projectDetails
     * @throws PoddClientException
     */
    private void checkProjectDetails(final String baseProjectName, final Map<URI, InferredOWLOntologyID> projectDetails)
        throws PoddClientException
    {
        if(projectDetails.isEmpty())
        {
            this.log.error("Project mapping seemed to exist but it was empty: {}", baseProjectName);
            
            // TODO: Create a new project?
            // return;
            throw new PoddClientException("Did not find an existing project for a line in the CSV file: "
                    + baseProjectName);
        }
        else if(projectDetails.size() > 1)
        {
            this.log.error(
                    "Found multiple PODD Project name mappings (not able to select between them automatically) : {}\n\n {}",
                    baseProjectName, projectDetails.keySet());
            
            // TODO: Throw exception?
            // return;
            throw new PoddClientException("Found multiple projects for a line in the CSV file: " + baseProjectName);
        }
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
     * @param potUriMap
     *            A map of relevant pot URIs and their tray URIs using their standardised labels.
     * @param uploadQueue
     *            The upload queue containing all of the models to be uploaded.
     * @param projectYear
     *            The TrayScan parameter detailing the project year for the next tray.
     * @param projectNumber
     *            The TrayScan parameter detailing the project number for the next tray.
     * @param experimentNumber
     *            The TrayScan parameter detailing the experiment number for the next tray.
     * @param plantName
     *            The name of this plant
     * @param plantNotes
     *            Specific notes about this plant
     * @param species
     *            The species for the current line.
     * @param genus
     *            The genus for the current line.
     * @throws PoddClientException
     *             If there is a PODD Client exception.
     * @throws GraphUtilException
     *             If there was an illformed graph.
     */
    private void generateRandomisationRDF(
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap,
            final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap,
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue, final int projectYear,
            final int projectNumber, final int experimentNumber, final String trayId, final String trayNotes,
            final String trayTypeName, final String plantId, final String plantName, final String plantNotes,
            final String genus, final String species) throws PoddClientException, GraphUtilException
    {
        // Reconstruct Project#0001-0002 structure to get a normalised string
        final String baseProjectName =
                String.format(ClientSpreadsheetConstants.TEMPLATE_PROJECT, projectYear, projectNumber);
        URI nextExperimentUri = null;
        
        final Map<URI, InferredOWLOntologyID> projectDetails = this.getProjectDetails(projectUriMap, baseProjectName);
        this.checkProjectDetails(baseProjectName, projectDetails);
        
        final URI nextProjectUri = projectDetails.keySet().iterator().next();
        final InferredOWLOntologyID nextProjectID = projectDetails.get(nextProjectUri);
        
        this.log.debug("Found unique PODD Project name to URI mapping: {} {}", baseProjectName, projectDetails);
        
        // Reconstruct Project#0001-0002_Experiment#0001 structure to get a normalised
        // string
        final String baseExperimentName =
                String.format(ClientSpreadsheetConstants.TEMPLATE_EXPERIMENT, projectYear, projectNumber,
                        experimentNumber);
        
        final Map<URI, URI> experimentDetails = this.getExperimentDetails(experimentUriMap, baseExperimentName);
        this.checkExperimentDetails(baseExperimentName, experimentDetails);
        
        nextExperimentUri = experimentDetails.keySet().iterator().next();
        final URI checkProjectUri = experimentDetails.get(nextExperimentUri);
        if(!checkProjectUri.equals(nextProjectUri))
        {
            this.log.error(
                    "Experiment mapping was against a different project: {} experimentURI={} nextProjectUri={} checkProjectUri={}",
                    baseExperimentName, nextExperimentUri, nextProjectUri, checkProjectUri);
        }
        
        // Create or find an existing model for the necessary modifications to this
        // project/artifact
        Model nextResult = new LinkedHashModel();
        final Model putIfAbsent = uploadQueue.putIfAbsent(nextProjectID, nextResult);
        if(putIfAbsent != null)
        {
            nextResult = putIfAbsent;
        }
        
        final URI nextTrayURI = this.getTrayUri(trayUriMap, trayId, nextProjectID, nextExperimentUri);
        
        // Check whether plantId already has an assigned URI
        final URI nextPotURI = this.getPotUri(potUriMap, plantId, nextProjectID, nextTrayURI);
        
        // Check whether genus/specieis/plantName already has an assigned URI (and automatically
        // assign a temporary URI if it does not)
        final URI nextGenotypeURI =
                this.getGenotypeUri(genotypeUriMap, genus, species, plantName, nextProjectID, nextProjectUri);
        
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
        
        // Add new poddScience:Container for pot
        nextResult.add(nextPotURI, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_CONTAINER);
        // Link pot to tray
        nextResult.add(nextTrayURI, PoddRdfConstants.PODD_SCIENCE_HAS_CONTAINER, nextPotURI);
        // PlantID => Add poddScience:hasBarcode to pot
        nextResult.add(nextPotURI, PoddRdfConstants.PODD_SCIENCE_HAS_BARCODE, this.vf.createLiteral(plantId));
        // Link the genus/species/plantName combination to a genotype
        nextResult.add(nextPotURI, PoddRdfConstants.PODD_SCIENCE_REFERS_TO_GENOTYPE, nextGenotypeURI);
        if(nextGenotypeURI.stringValue().startsWith(this.TEMP_UUID_PREFIX))
        {
            // Add all of the statements for the genotype to the update to make sure that temporary
            // descriptions are added
            nextResult.addAll(genotypeUriMap.get(nextProjectUri).get(nextGenotypeURI));
        }
        // PlantNotes => Add rdfs:label to pot
        nextResult.add(nextPotURI, RDFS.LABEL, this.vf.createLiteral(plantNotes));
        
        // Position => TODO: Need to use randomisation data to populate the position
        // TODO Using d110cc.csv
        // Add poddScience:hasReplicate for tray to link it to the rep #
        // Add poddScience:hasReplicate for pot to link it to the rep # (??to make queries easier??)
        
        // DebugUtils.printContents(nextResult);
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
     * @param potUriMap
     *            A map of relevant pot URIs and their tray URIs using their standardised labels.
     * @param uploadQueue
     *            The upload queue containing all of the models to be uploaded.
     * @param projectYear
     *            The TrayScan parameter detailing the project year for the next tray.
     * @param projectNumber
     *            The TrayScan parameter detailing the project number for the next tray.
     * @param experimentNumber
     *            The TrayScan parameter detailing the experiment number for the next tray.
     * @param plantName
     *            The name of this plant
     * @param plantNotes
     *            Specific notes about this plant
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
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap,
            final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap,
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue, final int projectYear,
            final int projectNumber, final int experimentNumber, final String trayId, final String trayNotes,
            final String trayTypeName, final String plantId, final String plantName, final String plantNotes,
            final String genus, final String species) throws PoddClientException, GraphUtilException
    {
        // Reconstruct Project#0001-0002 structure to get a normalised string
        final String baseProjectName =
                String.format(ClientSpreadsheetConstants.TEMPLATE_PROJECT, projectYear, projectNumber);
        URI nextExperimentUri = null;
        
        final Map<URI, InferredOWLOntologyID> projectDetails = this.getProjectDetails(projectUriMap, baseProjectName);
        this.checkProjectDetails(baseProjectName, projectDetails);
        
        final URI nextProjectUri = projectDetails.keySet().iterator().next();
        final InferredOWLOntologyID nextProjectID = projectDetails.get(nextProjectUri);
        
        this.log.debug("Found unique PODD Project name to URI mapping: {} {}", baseProjectName, projectDetails);
        
        // Reconstruct Project#0001-0002_Experiment#0001 structure to get a normalised
        // string
        final String baseExperimentName =
                String.format(ClientSpreadsheetConstants.TEMPLATE_EXPERIMENT, projectYear, projectNumber,
                        experimentNumber);
        
        final Map<URI, URI> experimentDetails = this.getExperimentDetails(experimentUriMap, baseExperimentName);
        this.checkExperimentDetails(baseExperimentName, experimentDetails);
        
        nextExperimentUri = experimentDetails.keySet().iterator().next();
        final URI checkProjectUri = experimentDetails.get(nextExperimentUri);
        if(!checkProjectUri.equals(nextProjectUri))
        {
            this.log.error(
                    "Experiment mapping was against a different project: {} experimentURI={} nextProjectUri={} checkProjectUri={}",
                    baseExperimentName, nextExperimentUri, nextProjectUri, checkProjectUri);
        }
        
        // Create or find an existing model for the necessary modifications to this
        // project/artifact
        Model nextResult = new LinkedHashModel();
        final Model putIfAbsent = uploadQueue.putIfAbsent(nextProjectID, nextResult);
        if(putIfAbsent != null)
        {
            nextResult = putIfAbsent;
        }
        
        final URI nextTrayURI = this.getTrayUri(trayUriMap, trayId, nextProjectID, nextExperimentUri);
        
        // Check whether plantId already has an assigned URI
        final URI nextPotURI = this.getPotUri(potUriMap, plantId, nextProjectID, nextTrayURI);
        
        // Check whether genus/specieis/plantName already has an assigned URI (and automatically
        // assign a temporary URI if it does not)
        final URI nextGenotypeURI =
                this.getGenotypeUri(genotypeUriMap, genus, species, plantName, nextProjectID, nextProjectUri);
        
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
        
        // Add new poddScience:Container for pot
        nextResult.add(nextPotURI, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_CONTAINER);
        // Link pot to tray
        nextResult.add(nextTrayURI, PoddRdfConstants.PODD_SCIENCE_HAS_CONTAINER, nextPotURI);
        // PlantID => Add poddScience:hasBarcode to pot
        nextResult.add(nextPotURI, PoddRdfConstants.PODD_SCIENCE_HAS_BARCODE, this.vf.createLiteral(plantId));
        // Link the genus/species/plantName combination to a genotype
        nextResult.add(nextPotURI, PoddRdfConstants.PODD_SCIENCE_REFERS_TO_GENOTYPE, nextGenotypeURI);
        if(nextGenotypeURI.stringValue().startsWith(this.TEMP_UUID_PREFIX))
        {
            // Add all of the statements for the genotype to the update to make sure that temporary
            // descriptions are added
            nextResult.addAll(genotypeUriMap.get(nextProjectUri).get(nextGenotypeURI));
        }
        // PlantNotes => Add rdfs:label to pot
        nextResult.add(nextPotURI, RDFS.LABEL, this.vf.createLiteral(plantNotes));
        
        // Position => TODO: Need to use randomisation data to populate the position
        // TODO Using d110cc.csv
        // Add poddScience:hasReplicate for tray to link it to the rep #
        // Add poddScience:hasReplicate for pot to link it to the rep # (??to make queries easier??)
        
        // DebugUtils.printContents(nextResult);
    }
    
    /**
     * @param experimentUriMap
     * @param baseExperimentName
     * @return
     * @throws PoddClientException
     */
    private Map<URI, URI> getExperimentDetails(final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap,
            final String baseExperimentName) throws PoddClientException
    {
        if(!experimentUriMap.containsKey(baseExperimentName))
        {
            this.log.error("Did not find an existing experiment for a line in the CSV file: {}", baseExperimentName);
            
            // TODO: Create a new experiment?
            // return;
            throw new PoddClientException("Did not find an existing experiment for a line in the CSV file: "
                    + baseExperimentName);
        }
        
        return experimentUriMap.get(baseExperimentName);
    }
    
    /**
     * Gets a genotype URI matching the given genus, species, and plantName (line) from the given
     * cache, creating a new entry if necessary and giving it a temporary URI.
     * 
     * @param genotypeUriMap
     * @param genus
     * @param species
     * @param plantName
     * @param nextProjectID
     * @param nextProjectUri
     * @return
     */
    private URI getGenotypeUri(final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap, final String genus,
            final String species, final String plantName, final InferredOWLOntologyID nextProjectID,
            final URI nextProjectUri)
    {
        URI nextGenotypeURI = null;
        if(genotypeUriMap.containsKey(nextProjectUri))
        {
            final ConcurrentMap<URI, Model> nextProjectGenotypeMap = genotypeUriMap.get(nextProjectUri);
            
            for(final URI existingGenotypeURI : nextProjectGenotypeMap.keySet())
            {
                final Model nextModel = nextProjectGenotypeMap.get(existingGenotypeURI);
                
                if(nextModel.contains(existingGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_GENUS,
                        this.vf.createLiteral(genus)))
                {
                    if(nextModel.contains(existingGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_SPECIES,
                            this.vf.createLiteral(species)))
                    {
                        if(nextModel.contains(existingGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_LINE,
                                this.vf.createLiteral(plantName)))
                        {
                            nextGenotypeURI = existingGenotypeURI;
                            break;
                        }
                        else
                        {
                            this.log.debug(
                                    "Did not find any genotypes with the given genus and species and line in this project: {} {} {} {}",
                                    nextProjectUri, genus, species, plantName);
                        }
                    }
                    else
                    {
                        this.log.debug(
                                "Did not find any genotypes with the given genus and species in this project: {} {} {}",
                                nextProjectUri, genus, species);
                    }
                }
                else
                {
                    this.log.debug("Did not find any genotypes with the given genus in this project: {} {}",
                            nextProjectUri, genus);
                }
            }
        }
        
        // If no genotype was found, then create a new description and assign it a temporary URI
        if(nextGenotypeURI == null)
        {
            this.log.debug(
                    "Could not find an existing genotype for description provided, assigning a temporary URI: {} {} {} {}",
                    nextProjectID, genus, species, plantName);
            
            nextGenotypeURI = this.vf.createURI(this.TEMP_UUID_PREFIX + "genotype:" + UUID.randomUUID().toString());
            
            final Model newModel = new LinkedHashModel();
            newModel.add(nextProjectUri, PoddRdfConstants.PODD_SCIENCE_HAS_GENOTYPE, nextGenotypeURI);
            newModel.add(nextGenotypeURI, RDF.TYPE, PoddRdfConstants.PODD_SCIENCE_GENOTYPE);
            newModel.add(nextGenotypeURI, RDFS.LABEL,
                    this.vf.createLiteral(genus + " " + species + " (" + plantName + ")"));
            newModel.add(nextGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_GENUS, this.vf.createLiteral(genus));
            newModel.add(nextGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_SPECIES, this.vf.createLiteral(species));
            newModel.add(nextGenotypeURI, PoddRdfConstants.PODD_SCIENCE_HAS_LINE, this.vf.createLiteral(plantName));
            
            ConcurrentMap<URI, Model> nextGenotypeUriMap = new ConcurrentHashMap<>();
            final ConcurrentMap<URI, Model> putIfAbsent =
                    genotypeUriMap.putIfAbsent(nextProjectUri, nextGenotypeUriMap);
            if(putIfAbsent != null)
            {
                nextGenotypeUriMap = putIfAbsent;
            }
            final Model putIfAbsent2 = nextGenotypeUriMap.putIfAbsent(nextGenotypeURI, newModel);
            if(putIfAbsent2 != null)
            {
                this.log.error("ERROR: Generated two temporary Genotype URIs that were identical! : {} {}",
                        nextProjectUri, nextGenotypeURI);
            }
        }
        return nextGenotypeURI;
        
    }
    
    /**
     * @param potUriMap
     * @param plantId
     * @param nextProjectID
     * @param nextTrayURI
     * @return
     * @throws PoddClientException
     * @throws GraphUtilException
     */
    private URI getPotUri(final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap, final String plantId,
            final InferredOWLOntologyID nextProjectID, final URI nextTrayURI) throws PoddClientException,
        GraphUtilException
    {
        URI nextPotURI;
        if(potUriMap.containsKey(plantId))
        {
            nextPotURI = potUriMap.get(plantId).keySet().iterator().next();
        }
        else
        {
            final Model plantIdSparqlResults =
                    this.doSPARQL(String.format(ClientSpreadsheetConstants.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS,
                            RenderUtils.escape(plantId),
                            RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_CONTAINER)), nextProjectID);
            
            if(plantIdSparqlResults.isEmpty())
            {
                this.log.debug(
                        "Could not find an existing container for pot barcode, assigning a temporary URI: {} {}",
                        plantId, nextProjectID);
                
                nextPotURI = this.vf.createURI(this.TEMP_UUID_PREFIX + "pot:" + UUID.randomUUID().toString());
            }
            else
            {
                nextPotURI =
                        GraphUtil.getUniqueSubjectURI(plantIdSparqlResults, RDF.TYPE,
                                PoddRdfConstants.PODD_SCIENCE_CONTAINER);
            }
            
            ConcurrentMap<URI, URI> nextPotUriMap = new ConcurrentHashMap<>();
            final ConcurrentMap<URI, URI> putIfAbsent2 = potUriMap.putIfAbsent(plantId, nextPotUriMap);
            if(putIfAbsent2 != null)
            {
                nextPotUriMap = putIfAbsent2;
            }
            nextPotUriMap.put(nextPotURI, nextTrayURI);
        }
        return nextPotURI;
    }
    
    /**
     * @param projectUriMap
     * @param baseProjectName
     * @return
     * @throws PoddClientException
     */
    private Map<URI, InferredOWLOntologyID> getProjectDetails(
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final String baseProjectName) throws PoddClientException
    {
        if(!projectUriMap.containsKey(baseProjectName))
        {
            this.log.error("Did not find an existing project for a line in the CSV file: {}", baseProjectName);
            
            // TODO: Create a new project?
            // return;
            
            throw new PoddClientException("Did not find an existing project for a line in the CSV file: "
                    + baseProjectName);
        }
        
        return projectUriMap.get(baseProjectName);
    }
    
    private Model getTopObject(final InferredOWLOntologyID nextArtifact, final Model artifactDetails)
        throws PoddClientException
    {
        return artifactDetails.filter(
                artifactDetails.filter(nextArtifact.getOntologyIRI().toOpenRDFURI(),
                        PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, null).objectURI(), null, null);
    }
    
    /**
     * @param trayUriMap
     * @param trayId
     * @param nextProjectID
     * @param nextExperimentUri
     * @return
     * @throws PoddClientException
     * @throws GraphUtilException
     */
    private URI getTrayUri(final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap, final String trayId,
            final InferredOWLOntologyID nextProjectID, final URI nextExperimentUri) throws PoddClientException,
        GraphUtilException
    {
        // Check whether trayId already has an assigned URI
        URI nextTrayURI;
        if(trayUriMap.containsKey(trayId))
        {
            nextTrayURI = trayUriMap.get(trayId).keySet().iterator().next();
        }
        else
        {
            final Model trayIdSparqlResults =
                    this.doSPARQL(String.format(ClientSpreadsheetConstants.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS,
                            RenderUtils.escape(trayId),
                            RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_CONTAINER)), nextProjectID);
            
            if(trayIdSparqlResults.isEmpty())
            {
                this.log.debug(
                        "Could not find an existing container for tray barcode, assigning a temporary URI: {} {}",
                        trayId, nextProjectID);
                
                nextTrayURI = this.vf.createURI(this.TEMP_UUID_PREFIX + "tray:" + UUID.randomUUID().toString());
            }
            else
            {
                nextTrayURI =
                        GraphUtil.getUniqueSubjectURI(trayIdSparqlResults, RDF.TYPE,
                                PoddRdfConstants.PODD_SCIENCE_CONTAINER);
            }
            
            ConcurrentMap<URI, URI> nextTrayUriMap = new ConcurrentHashMap<>();
            final ConcurrentMap<URI, URI> putIfAbsent2 = trayUriMap.putIfAbsent(trayId, nextTrayUriMap);
            if(putIfAbsent2 != null)
            {
                nextTrayUriMap = putIfAbsent2;
            }
            nextTrayUriMap.put(nextTrayURI, nextExperimentUri);
        }
        return nextTrayURI;
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
                                String.format(ClientSpreadsheetConstants.TEMPLATE_SPARQL_BY_TYPE,
                                        RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_INVESTIGATION)),
                                artifactId);
                
                if(nextSparqlResults.isEmpty())
                {
                    this.log.info("Could not find any existing experiments for project: {} {}", nextProjectName,
                            projectUri);
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
                        
                        // DebugUtils.printContents(label);
                        
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
                                    
                                    final Matcher matcher =
                                            ClientSpreadsheetConstants.REGEX_EXPERIMENT.matcher(nextLabelString);
                                    
                                    if(!matcher.matches())
                                    {
                                        this.log.error(
                                                "Found experiment label that did not start with expected format: {}",
                                                nextLabel);
                                    }
                                    else
                                    {
                                        this.log.debug(
                                                "Found experiment label with the expected format: '{}' original=<{}>",
                                                nextLabelString, nextLabel);
                                        
                                        final int nextProjectYear = Integer.parseInt(matcher.group(1));
                                        final int nextProjectNumber = Integer.parseInt(matcher.group(2));
                                        final int nextExperimentNumber = Integer.parseInt(matcher.group(3));
                                        
                                        nextLabelString =
                                                String.format(ClientSpreadsheetConstants.TEMPLATE_EXPERIMENT,
                                                        nextProjectYear, nextProjectNumber, nextExperimentNumber);
                                        
                                        this.log.debug("Reformatted experiment label to: '{}' original=<{}>",
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
    
    private void populateGenotypeUriMap(
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap) throws PoddClientException
    {
        for(final String nextProjectName : projectUriMap.keySet())
        {
            final ConcurrentMap<URI, InferredOWLOntologyID> nextProjectNameMapping = projectUriMap.get(nextProjectName);
            for(final URI projectUri : nextProjectNameMapping.keySet())
            {
                final InferredOWLOntologyID artifactId = nextProjectNameMapping.get(projectUri);
                final Model nextSparqlResults =
                        this.doSPARQL(String.format(ClientSpreadsheetConstants.TEMPLATE_SPARQL_BY_TYPE_ALL_PROPERTIES,
                                RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_GENOTYPE)), artifactId);
                if(nextSparqlResults.isEmpty())
                {
                    this.log.debug("Could not find any existing genotypes for project: {} {}", nextProjectName,
                            projectUri);
                }
                
                for(final Resource nextGenotype : nextSparqlResults.filter(null, RDF.TYPE,
                        PoddRdfConstants.PODD_SCIENCE_GENOTYPE).subjects())
                {
                    if(!(nextGenotype instanceof URI))
                    {
                        this.log.error("Found genotype that was not assigned a URI: {} artifact={}", nextGenotype,
                                artifactId);
                    }
                    else
                    {
                        ConcurrentMap<URI, Model> nextGenotypeMap = new ConcurrentHashMap<>();
                        final ConcurrentMap<URI, Model> putIfAbsent = genotypeUriMap.put(projectUri, nextGenotypeMap);
                        if(putIfAbsent != null)
                        {
                            nextGenotypeMap = putIfAbsent;
                        }
                        final Model putIfAbsent2 = nextGenotypeMap.putIfAbsent((URI)nextGenotype, nextSparqlResults);
                        if(putIfAbsent2 != null)
                        {
                            this.log.info("Found existing description for genotype URI within the same project: {} {}",
                                    projectUri, nextGenotype);
                        }
                    }
                }
            }
        }
    }
    
    private void populateProjectUriMap(final Model currentUnpublishedArtifacts,
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap)
        throws PoddClientException
    {
        for(final InferredOWLOntologyID nextArtifact : OntologyUtils.modelToOntologyIDs(currentUnpublishedArtifacts,
                true, false))
        {
            final Model nextTopObject = this.getTopObject(nextArtifact, currentUnpublishedArtifacts);
            
            // DebugUtils.printContents(nextTopObject);
            
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
                    
                    // DebugUtils.printContents(label);
                    
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
                                
                                final Matcher matcher =
                                        ClientSpreadsheetConstants.REGEX_PROJECT.matcher(nextLabelString);
                                
                                if(!matcher.matches())
                                {
                                    this.log.error("Found project label that did not start with expected format: {}",
                                            nextLabel);
                                }
                                else
                                {
                                    this.log.debug("Found project label with the expected format: '{}' original=<{}>",
                                            nextLabelString, nextLabel);
                                    
                                    final int nextProjectYear = Integer.parseInt(matcher.group(1));
                                    final int nextProjectNumber = Integer.parseInt(matcher.group(2));
                                    
                                    nextLabelString =
                                            String.format(ClientSpreadsheetConstants.TEMPLATE_PROJECT, nextProjectYear,
                                                    nextProjectNumber);
                                    
                                    this.log.debug("Reformatted project label to: '{}' original=<{}>", nextLabelString,
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
     * @param potUriMap
     *            A map from normalised pot names (barcodes) to their URIs and the experiments that
     *            they are located in.
     * @param uploadQueue
     *            A map from artifact identifiers to Model objects containing all of the necessary
     *            changes to the artifact.
     * 
     * @throws PoddClientException
     *             If there was a problem communicating with PODD or the line was not valid.
     */
    private void processRandomisationLine(final List<String> headers, final List<String> nextLine,
            final ConcurrentMap<String, ConcurrentMap<URI, InferredOWLOntologyID>> projectUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> experimentUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap,
            final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap,
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
            
            if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_ID))
            {
                trayId = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_NOTES))
            {
                trayNotes = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_TYPE_NAME))
            {
                trayTypeName = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_POSITION))
            {
                position = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_ID))
            {
                plantId = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_NAME))
            {
                plantName = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_NOTES))
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
        
        final Matcher trayMatcher = ClientSpreadsheetConstants.REGEX_TRAY.matcher(trayId);
        
        if(!trayMatcher.matches())
        {
            this.log.error("Tray ID did not match expected format: {}", trayId);
        }
        else
        {
            if(trayMatcher.groupCount() != ClientSpreadsheetConstants.CLIENT_TRAY_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Tray ID: {} {}",
                        trayMatcher.groupCount(), ClientSpreadsheetConstants.CLIENT_TRAY_ID_SIZE);
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
        
        final Matcher plantMatcher = ClientSpreadsheetConstants.REGEX_PLANT.matcher(plantId);
        
        if(!plantMatcher.matches())
        {
            this.log.error("Plant ID did not match expected format: {}", plantId);
        }
        else
        {
            if(plantMatcher.groupCount() != ClientSpreadsheetConstants.CLIENT_PLANT_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Plant ID: {} {}",
                        plantMatcher.groupCount(), ClientSpreadsheetConstants.CLIENT_PLANT_ID_SIZE);
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
        
        final Matcher positionMatcher = ClientSpreadsheetConstants.REGEX_POSITION.matcher(position);
        
        if(!positionMatcher.matches())
        {
            this.log.error("Position did not match expected format: {} {}", position, nextLine);
            // TODO: These may not be populated so do not throw an exception if it fails.
            // throw new
            // PoddClientException(MessageFormat.format("Position did not match expected format: {0}",
            // position));
        }
        else if(positionMatcher.groupCount() != ClientSpreadsheetConstants.POSITION_SIZE)
        {
            this.log.error("Did not find the expected number of regex matches for Position: {} {}",
                    positionMatcher.groupCount(), ClientSpreadsheetConstants.POSITION_SIZE);
            throw new PoddClientException(MessageFormat.format(
                    "Did not find the expected number of regex matches for Position: {0}", position));
        }
        else
        {
            columnLetter = positionMatcher.group(1).trim();
            rowNumber = positionMatcher.group(2).trim();
        }
        
        this.generateRandomisationRDF(projectUriMap, experimentUriMap, trayUriMap, potUriMap, genotypeUriMap,
                uploadQueue, projectYear, projectNumber, experimentNumber, trayId, trayNotes, trayTypeName, plantId,
                plantName, plantNotes, genus, species);
    }
    
    /**
     * Parses the given TrayScan pot/tray randomisation list. All items must already exist in PODD,
     * and should be populated using the {@link #processTrayScanList(InputStream)} method and
     * uploaded using the {@link #uploadToPodd(ConcurrentMap)} method.
     * 
     * TODO: Should this process create new projects where they do not already exist? Ideally they
     * should be created and roles assigned before this process, but could be fine to do that in
     * here
     */
    public ConcurrentMap<InferredOWLOntologyID, Model> processRandomisationList(final InputStream in)
        throws IOException, PoddClientException, OpenRDFException
    {
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
        
        // Genotype mappings, starting at the URI of the project and mapping to the URI of the
        // genotype and the RDF Model containing the statements describing this genotype
        final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap = new ConcurrentHashMap<>();
        
        // Cache for tray name mappings, starting at tray barcodes and ending with a mapping from
        // the URI of the tray to the URI of the experiment that contains the tray
        // NOTE: This is not prefilled, as it is populated on demand during processing of lines to
        // only contain the necessary elements
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap = new ConcurrentHashMap<>();
        
        // Cache for pot name mappings, starting at pot barcodes and ending with a mapping from
        // the URI of the pot to the URI of the tray that contains the pot
        // NOTE: This is not prefilled, as it is populated on demand during processing of lines to
        // only contain the necessary elements
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap = new ConcurrentHashMap<>();
        
        // -----------------------------------------------------------------------------------------
        // Now cache URIs for projects, experiments, and genotypes for all unpublished projects that
        // the current user can access
        // -----------------------------------------------------------------------------------------
        
        // Only select the unpublished artifacts, as we cannot edit published artifacts
        final Model currentUnpublishedArtifacts = this.listArtifacts(false, true);
        
        // Map known project names to their URIs, as the URIs are needed to
        // create statements internally
        this.populateProjectUriMap(currentUnpublishedArtifacts, projectUriMap);
        
        this.populateExperimentUriMap(projectUriMap, experimentUriMap);
        
        this.populateGenotypeUriMap(projectUriMap, genotypeUriMap);
        
        // -----------------------------------------------------------------------------------------
        // Now process the CSV file line by line using the caches to reduce multiple queries to the
        // server where possible
        // -----------------------------------------------------------------------------------------
        
        List<String> headers = null;
        // Supressing try-with-resources warning generated erroneously by Eclipse:
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
                        this.verifyRandomisationListHeaders(headers);
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
                    this.processRandomisationLine(headers, Arrays.asList(nextLine), projectUriMap, experimentUriMap,
                            trayUriMap, potUriMap, genotypeUriMap, uploadQueue);
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
        
        return uploadQueue;
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
     * @param potUriMap
     *            A map from normalised pot names (barcodes) to their URIs and the experiments that
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
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap,
            final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap,
            final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap,
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
            
            if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_ID))
            {
                trayId = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_NOTES))
            {
                trayNotes = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_TRAY_TYPE_NAME))
            {
                trayTypeName = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_POSITION))
            {
                position = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_ID))
            {
                plantId = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_NAME))
            {
                plantName = nextField;
            }
            else if(nextHeader.trim().equals(ClientSpreadsheetConstants.CLIENT_PLANT_NOTES))
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
        
        final Matcher trayMatcher = ClientSpreadsheetConstants.REGEX_TRAY.matcher(trayId);
        
        if(!trayMatcher.matches())
        {
            this.log.error("Tray ID did not match expected format: {}", trayId);
        }
        else
        {
            if(trayMatcher.groupCount() != ClientSpreadsheetConstants.CLIENT_TRAY_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Tray ID: {} {}",
                        trayMatcher.groupCount(), ClientSpreadsheetConstants.CLIENT_TRAY_ID_SIZE);
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
        
        final Matcher plantMatcher = ClientSpreadsheetConstants.REGEX_PLANT.matcher(plantId);
        
        if(!plantMatcher.matches())
        {
            this.log.error("Plant ID did not match expected format: {}", plantId);
        }
        else
        {
            if(plantMatcher.groupCount() != ClientSpreadsheetConstants.CLIENT_PLANT_ID_SIZE)
            {
                this.log.error("Did not find the expected number of regex matches for Plant ID: {} {}",
                        plantMatcher.groupCount(), ClientSpreadsheetConstants.CLIENT_PLANT_ID_SIZE);
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
        
        final Matcher positionMatcher = ClientSpreadsheetConstants.REGEX_POSITION.matcher(position);
        
        if(!positionMatcher.matches())
        {
            this.log.error("Position did not match expected format: {} {}", position, nextLine);
            // TODO: These may not be populated so do not throw an exception if it fails.
            // throw new
            // PoddClientException(MessageFormat.format("Position did not match expected format: {0}",
            // position));
        }
        else if(positionMatcher.groupCount() != ClientSpreadsheetConstants.POSITION_SIZE)
        {
            this.log.error("Did not find the expected number of regex matches for Position: {} {}",
                    positionMatcher.groupCount(), ClientSpreadsheetConstants.POSITION_SIZE);
            throw new PoddClientException(MessageFormat.format(
                    "Did not find the expected number of regex matches for Position: {0}", position));
        }
        else
        {
            columnLetter = positionMatcher.group(1).trim();
            rowNumber = positionMatcher.group(2).trim();
        }
        
        this.generateTrayScanRDF(projectUriMap, experimentUriMap, trayUriMap, potUriMap, genotypeUriMap, uploadQueue,
                projectYear, projectNumber, experimentNumber, trayId, trayNotes, trayTypeName, plantId, plantName,
                plantNotes, genus, species);
    }
    
    /**
     * Parses the mapping of line numbers to the line names used to identify lines in the
     * randomisation process.
     * 
     * @param in
     *            An {@link InputStream} containing the CSV file with the mapping of line numbers to
     *            line names
     * @return A map from line numbers to line names.
     * @throws IOException
     *             If there is an {@link IOException}.
     * @throws PoddClientException
     *             If there is a problem communicating with the PODD server.
     */
    public ConcurrentMap<String, String> processRandomisationLineNameMappingList(final InputStream in)
        throws IOException, PoddClientException
    {
        // -----------------------------------------------------------------------------------------
        // Now process the CSV file line by line using the caches to reduce multiple queries to the
        // server where possible
        // -----------------------------------------------------------------------------------------
        
        List<String> headers = null;
        final ConcurrentMap<String, String> result = new ConcurrentHashMap<>();
        // Supressing try-with-resources warning generated erroneously by Eclipse:
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
                        if(headers.size() != 2)
                        {
                            throw new IllegalArgumentException("Did not find required number of headers");
                        }
                        
                        if(!headers.get(0).equals(RandomisationConstants.RAND_LINE_NUMBER))
                        {
                            throw new IllegalArgumentException("Missing " + RandomisationConstants.RAND_LINE_NUMBER
                                    + " header");
                        }
                        
                        if(!headers.get(1).equals(RandomisationConstants.RAND_CLIENT_LINE_NAME))
                        {
                            throw new IllegalArgumentException("Missing "
                                    + RandomisationConstants.RAND_CLIENT_LINE_NAME + " header");
                        }
                    }
                    catch(final IllegalArgumentException e)
                    {
                        this.log.error("Could not verify headers for line name mappings file: {}", e.getMessage());
                        throw new PoddClientException("Could not verify headers for line name mappings file", e);
                    }
                }
                else
                {
                    if(nextLine.length != headers.size())
                    {
                        this.log.error("Line and header sizes were different: {} {}", headers, nextLine);
                    }
                    
                    final String putIfAbsent = result.putIfAbsent(nextLine[0], nextLine[1]);
                    if(putIfAbsent != null)
                    {
                        this.log.error(
                                "Found multiple mappings for line name and number: linenumber={} duplicate={} original={}",
                                nextLine[0], nextLine[1], putIfAbsent);
                    }
                }
            }
        }
        
        if(headers == null)
        {
            this.log.error("Document did not contain a valid header line");
        }
        
        if(result.isEmpty())
        {
            this.log.error("Document did not contain any valid rows");
        }
        
        return result;
    }
    
    /**
     * Parses the given TrayScan project/experiment/tray/pot list and inserts the items into PODD
     * where they do not exist.
     * 
     * TODO: Should this process create new projects where they do not already exist? Ideally they
     * should be created and roles assigned before this process, but could be fine to do that in
     * here
     */
    public ConcurrentMap<InferredOWLOntologyID, Model> processTrayScanList(final InputStream in) throws IOException,
        PoddClientException, OpenRDFException
    {
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
        
        // Genotype mappings, starting at the URI of the project and mapping to the URI of the
        // genotype and the RDF Model containing the statements describing this genotype
        final ConcurrentMap<URI, ConcurrentMap<URI, Model>> genotypeUriMap = new ConcurrentHashMap<>();
        
        // Cache for tray name mappings, starting at tray barcodes and ending with a mapping from
        // the URI of the tray to the URI of the experiment that contains the tray
        // NOTE: This is not prefilled, as it is populated on demand during processing of lines to
        // only contain the necessary elements
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> trayUriMap = new ConcurrentHashMap<>();
        
        // Cache for pot name mappings, starting at pot barcodes and ending with a mapping from
        // the URI of the pot to the URI of the tray that contains the pot
        // NOTE: This is not prefilled, as it is populated on demand during processing of lines to
        // only contain the necessary elements
        final ConcurrentMap<String, ConcurrentMap<URI, URI>> potUriMap = new ConcurrentHashMap<>();
        
        // -----------------------------------------------------------------------------------------
        // Now cache URIs for projects, experiments, and genotypes for all unpublished projects that
        // the current user can access
        // -----------------------------------------------------------------------------------------
        
        // Only select the unpublished artifacts, as we cannot edit published artifacts
        final Model currentUnpublishedArtifacts = this.listArtifacts(false, true);
        
        // Map known project names to their URIs, as the URIs are needed to
        // create statements internally
        this.populateProjectUriMap(currentUnpublishedArtifacts, projectUriMap);
        
        this.populateExperimentUriMap(projectUriMap, experimentUriMap);
        
        this.populateGenotypeUriMap(projectUriMap, genotypeUriMap);
        
        // -----------------------------------------------------------------------------------------
        // Now process the CSV file line by line using the caches to reduce multiple queries to the
        // server where possible
        // -----------------------------------------------------------------------------------------
        
        List<String> headers = null;
        // Supressing try-with-resources warning generated erroneously by Eclipse:
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
                        this.verifyTrayScanListHeaders(headers);
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
                            trayUriMap, potUriMap, genotypeUriMap, uploadQueue);
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
        
        return uploadQueue;
    }
    
    public ConcurrentMap<InferredOWLOntologyID, InferredOWLOntologyID> uploadToPodd(
            final ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue) throws PoddClientException
    {
        final ConcurrentMap<InferredOWLOntologyID, InferredOWLOntologyID> resultMap = new ConcurrentHashMap<>();
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
                else if(nextUpload.equals(newID))
                {
                    this.log.error("Result from append artifact was not changed, as expected. {} {}", nextUpload, newID);
                }
                else
                {
                    resultMap.putIfAbsent(nextUpload, newID);
                }
            }
            catch(final RDFHandlerException e)
            {
                this.log.error("Found exception generating upload body: ", e);
            }
        }
        return resultMap;
    }
    
    /**
     * Verifies the list of randomisation tray/pot combinations, throwing an
     * IllegalArgumentException if there are unrecognised headers or if any mandatory headers are
     * missing.
     * 
     * @throws IllegalArgumentException
     *             If the headers are not verified correctly.
     */
    private void verifyRandomisationListHeaders(final List<String> headers) throws IllegalArgumentException
    {
        if(headers == null || headers.size() < RandomisationConstants.MIN_RANDOMISATION_HEADERS_SIZE)
        {
            this.log.error("Did not find valid headers: {}", headers);
            throw new IllegalArgumentException("Did not find valid headers");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_SHELF))
        {
            throw new IllegalArgumentException("Did not find shelf number header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_SHELF_SIDE))
        {
            throw new IllegalArgumentException("Did not find shelf side header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_REPLICATE))
        {
            throw new IllegalArgumentException("Did not find replicate header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_TRAY))
        {
            throw new IllegalArgumentException("Did not find tray header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_ROW))
        {
            throw new IllegalArgumentException("Did not find row header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_COLUMN))
        {
            throw new IllegalArgumentException("Did not find column header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_LAYOUT))
        {
            throw new IllegalArgumentException("Did not find layout header");
        }
        
        if(!headers.contains(RandomisationConstants.RAND_VAR))
        {
            throw new IllegalArgumentException("Did not find var header");
        }
    }
    
    /**
     * Verifies the list of projects, throwing an IllegalArgumentException if there are unrecognised
     * headers or if any mandatory headers are missing.
     * 
     * @throws IllegalArgumentException
     *             If the headers are not verified correctly.
     */
    private void verifyTrayScanListHeaders(final List<String> headers) throws IllegalArgumentException
    {
        if(headers == null || headers.size() < ClientSpreadsheetConstants.MIN_PLANTSCAN_HEADERS_SIZE)
        {
            this.log.error("Did not find valid headers: {}", headers);
            throw new IllegalArgumentException("Did not find valid headers");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_TRAY_ID))
        {
            throw new IllegalArgumentException("Did not find tray id header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_TRAY_NOTES))
        {
            throw new IllegalArgumentException("Did not find tray notes header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_TRAY_TYPE_NAME))
        {
            throw new IllegalArgumentException("Did not find tray type name header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_POSITION))
        {
            throw new IllegalArgumentException("Did not find position header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_PLANT_ID))
        {
            throw new IllegalArgumentException("Did not find plant id header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_PLANT_NAME))
        {
            throw new IllegalArgumentException("Did not find plant name header");
        }
        
        if(!headers.contains(ClientSpreadsheetConstants.CLIENT_PLANT_NOTES))
        {
            throw new IllegalArgumentException("Did not find plant notes header");
        }
    }
}