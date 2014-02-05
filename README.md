# Introduction

PODD is a database for tracking and managing scientific experiments, including scientific protocols, research data, analysed results, and publications. It is initially implemented for plant phenomics experiments.


# Dependencies

The following dependencies are fetched by maven from the Sonatype Snapshots repository for snapshots, or from Maven Central for releases.

* OWLAPI: https://github.com/ansell/owlapi/tree/ansellpatches This is patched to provide direct interaction with OpenRDF Sesame.
* OpenRDF Sesame: https://github.com/ansell/openrdf-sesame/tree/master
* Pellet: https://github.com/ansell/pellet/tree/develop This is patched to use the OWLAPI Sesame patches.
* Restlet Utils: https://github.com/ansell/restlet-utils/tree/master
* Property Util: https://github.com/ansell/property-util/tree/master
* Abstract Service Loader: https://github.com/ansell/abstract-service-loader/tree/master

# License

PODD is released under the GNU Affero General Public License 3.0
