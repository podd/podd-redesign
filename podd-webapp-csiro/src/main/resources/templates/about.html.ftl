<!--
  ~ Copyright (c) 2009 - 2010. School of Information Technology and Electrical
  ~ Engineering, The University of Queensland.  This software is being developed
  ~ for the "Phenomics Ontoogy Driven Data Management Project (PODD).
  ~ PODD is a National e-Research Architecture Taskforce (NeAT) project
  ~ co-funded by ANDS and ARCS.
  ~
  ~ PODD is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ PODD is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with PODD.  If not, see <http://www.gnu.org/licenses/>.
  -->

<div id="title_pane">
    <h3>About Us</h3>
</div>

<div id="content_pane">

    <#include "support_aux.html.ftl">

    <div id="main" class="allow_space">

        <p>PODD is a framework for the management of Phenomics data and its concomitant metadata.
            PODD will be developed to provide the following services:</p>

        <ul class="stylized">
            <li>A file management based data store which will manage raw data (primary data) and analysis results
                (derived data). This file management system will incorporate archival capability for large data files
                not of primary importance (e.g. original image files). The file based approach will also support the
                maintenance of the data in perpetuity as a valuable research resource.
            </li>
            <li>A metadata repository, using Fedora Commons, to provide the metadata management capability, to
                identify data location, and to provide data and metadata relationships.</li>
            <li>A web services layer that will support the import and export of data, interaction with the Fedora
                Commons repository, interaction with discovery services and interaction with user supplied analysis and
                workflow tools.</li>
            <li>The management of a diverse range of ontologies that will be used to categorise and annotate data and
                metadata managed by the system.</li>
            <li>An RDF triple store based server, such as Mulgara, to provide metadata and ontology discovery
                capability.</li>
            <li>A web browser capable interface layer to support: data import and export; data management by users;
                data discovery; and service discovery.</li>
            <li>The interface layer will also include ontology traversal interfaces for data discovery.</li>
            <li>The provision of security, at the project level, for data that is not declared as publicly
                available.</li>
            <li>An appropriate authentication layer, ultimately using Shibboleth based tools, to support access and
                authentication from Australian and international users and services, in line with the policies of the
                Australian Access Federation.</li>
            <li>Appropriate tools interfacing with the services layer that will support the automatic capture and
                annotation of data and metadata from instrumentation, where possible.</li>
            <li>The provision of metadata classes to describe common classes of data objects as well as metadata
                classes to describe contextual data and provide models of the data structure (e.g. research projects,
                experiments, materials, biological samples, protocols).</li>
            <li>The system will provide the ability to publish data, or make it publicly available, either after a
                project's conclusion or after a pre-determined period.</li>
            <li>The ability for data owners to share protected data.</li>
        </ul>

        <p>An overview of the project is also available in the original <a
                href="http://www.pfc.org.au/pub/Main/NeATdevelop/PODD_NeAT_May_2009.pdf">PODD NeAT proposal</a>.</p>

        <p>Implementation information can be found at the <a href="http://projects.arcs.org.au/trac/podd/">PODD project
            management website</a>.</p>

        <p>If you have any queries regarding PODD please email <a href="mailto:g.kennedy1@uq.edu.au">g.kennedy1@uq.edu.au</a>.
        </p>

        <p>PODD is a data repository project supported by:</p>
        <ul class="stylized">
            <li>The Australian Research Collaboration Service</li>
            <li>The Australian National Data Service</li>
            <li>The Australian Phenomics Network</li>
            <li>The Australian Plant Phenomics Facility</li>
            <li>The Atlas of Living Australia</li>
            <li>CSIRO</li>
            <li>The University of Queensland</li>
            <li>The Australian National University</li>
        </ul>
    </div>
</div>