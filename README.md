This project is being used for the extensive redesign of the PODD system in response to the failure of the original system to support very small workloads.

# Prototype module

The prototype module contains some simple code in the form of JUnit tests to highlight the major operations that are required.

# Dependencies

This project uses three libraries that do not have the required versions available in maven repositories. These dependencies can be compiled by running "mvn clean install" on the following three repositories in the order they are given here:

* SesameTools: https://github.com/ansell/sesametools/tree/develop
* OWLAPI: https://github.com/ansell/owlapi/tree/ansellpatches
* Pellet: https://github.com/ansell/pellet/tree/develop

