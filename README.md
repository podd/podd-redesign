This project is being used for the extensive redesign of the PODD system in response to the failure of the original system to support very small workloads.

# Prototype module

The prototype module contains some simple code in the form of JUnit tests to highlight the major operations that are required.

# Dependencies

The following dependencies are fetched by maven from the Sonatype Snapshots repository for snapshots, or from Maven Central for releases.

* OWLAPI: https://github.com/ansell/owlapi/tree/ansellpatches This is patched to provide direct interaction with OpenRDF Sesame.
* OpenRDF Sesame: https://github.com/ansell/openrdf-sesame/tree/master
* Pellet: https://github.com/ansell/pellet/tree/develop This is patched to use the OWLAPI Sesame patches.
* Restlet Utils: https://github.com/ansell/restlet-utils/tree/master
* Property Util: https://github.com/ansell/property-util/tree/master
* Abstract Service Loader: https://github.com/ansell/abstract-service-loader/tree/master
