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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.restlet.representation.Representation;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.internal.Strings;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.xfer.FileSystemFile;


import com.github.ansell.jdefaultdict.JDefaultDict;
import com.github.podd.exception.PoddException;
import com.github.podd.ontologies.PODDBASE;
import com.github.podd.ontologies.PODDSCIENCE;
import com.github.podd.resources.APPFPoddClient;
import com.github.podd.resources.Filter;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Performs queries against PODD.
 *
 * @author Vidya Bala
 */
public class APPFQueryClient
{
    private static final String DEFAULT_REPLICATE_NUMBER = "1";
    
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Australia/Sydney"));
            
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static void main(final String... args) throws Exception
    {
        System.out.println("user.home=" + System.getProperty("user.home"));
        System.out.println("user.dir=" + System.getProperty("user.dir"));
        
        // Scan the loaded properties, which can be overriden using system properties (ie,
        // -Dverbose=... on command line)
        final OptionParser parser = new OptionParser();
        
        final OptionSpec<Void> help = parser.accepts("help").forHelp();
        final OptionSpec<String> sparqlQuery = parser.accepts("sparql-query").withOptionalArg().
                ofType(String.class).describedAs("The input SPARQL query to process.");
                
        final OptionSpec<String> standardQuery =
                parser.accepts("standard-query").withOptionalArg().ofType(String.class).describedAs(
                        "Use standard APPF query options to retrieve desired results: "
                        + "experiment to list and filter all experiments, plant to list and filter all plants, genotype to list and filter all genotypes");
        final OptionSpec<String> keywordSearch =
                parser.accepts("keyword").withOptionalArg().ofType(String.class).describedAs(
                        "The keyword to search against");                
        final OptionSpec<String> genus =
                parser.accepts("genus").withOptionalArg().ofType(String.class).defaultsTo("").describedAs(
                        "The genus to filter by");
        final OptionSpec<String> species =
                parser.accepts("species").withOptionalArg().ofType(String.class).defaultsTo("").describedAs(
                        "The species to filter by");
        final OptionSpec<String> experimentId =
                parser.accepts("experimentId").withOptionalArg().ofType(String.class).defaultsTo("").describedAs(
                        "The experiment Id to filter by");
        
        final OptionSpec<String> treatment =
                parser.accepts("treatment").withOptionalArg().ofType(String.class).defaultsTo("").describedAs(
                        "The treatment to filter by");
        
        final OptionSpec<Integer> limit =
                parser.accepts("limit").withOptionalArg().ofType(Integer.class).defaultsTo(50).describedAs(
                        "The number of results to limit by");
        OptionSet options = null;

        try
        {
        	options = parser.parse(args);

        }
        catch(final OptionException e)
        {
        	System.out.println(e.getMessage());
        	parser.printHelpOn(System.out);
        	throw e;
        }
        if(options.has(help))
        {
        	parser.printHelpOn(System.out);
        	return;
        }
        String query = standardQuery.value(options);
        String gene = genus.value(options);      
        String specie = species.value(options);        
        String keyword = keywordSearch.value(options);
        String treatmen = treatment.value(options);
        String expid = experimentId.value(options);
        String sparql = sparqlQuery.value(options);
        Integer lim = limit.value(options);
        if (query == null && keyword == null && sparql == null) {
        	parser.printHelpOn(System.out);
        	return;
        }
        final APPFPoddClient client = new APPFPoddClient("https://poddtest.plantphenomics.org.au/podd");
        if (limit != null) {
        	client.setLimit(lim);
        }
        if (query != null) {
        	if(query.equals("experiment")) {
        		if (gene.length() > 0 || specie.length() > 0 || expid.length() > 0) {
        			List<Filter> filter = new ArrayList<Filter>();
        			Filter f1 = new Filter("genus", gene);
        			Filter f2 = new Filter("species", specie);
        			Filter f3 = new Filter("barcode", expid);
        			filter.add(f1);
        			filter.add(f2);
        			filter.add(f3);
        			client.filterExperiments(filter);

        		} else {
        			client.listAllExperiments();
        		}
        	} else if (query.equals("plant")) {
        		if (gene.length() > 0 || specie.length() > 0 || treatmen.length() > 0) {
        			List<Filter> filter = new ArrayList<Filter>();
        			Filter f1 = new Filter("genus", gene);
        			Filter f2 = new Filter("species", specie);
        			Filter f3 = new Filter("treatment", treatmen);
        			filter.add(f1);
        			filter.add(f2);
        			filter.add(f3);
        			client.filterAllPlants(filter);
        			
        		} else {
        			client.listAllPlants();

        		}

        	} else if (query.equals("genotype")) {

        		if (query.length() > 0) {
        			List<Filter> filter = new ArrayList<Filter>();
        			Filter f1 = new Filter("genus", gene);
        			Filter f2 = new Filter("species", specie);
        			filter.add(f1);
        			filter.add(f2);
        			client.filterAllGenotypes(filter);
        		} else {
        			client.listAllgenotypes();
        		}
        	} else {
        		parser.printHelpOn(System.out);
        		return;
        	}
        }
        if (sparql != null) {
        	client.doSPARQL2(sparql, null);

        }

        if (keyword != null) {
        	client.keywordSearch(keyword);
        }
        
        
 
    }
    
}
