TODO List

# Prototype

* Customise PoddPrototypeUtils.updateCurrentManagedPoddArtifactOntologyVersion so that it removes all outdated Podd Artifact versions, not just the current version. Also customise the predicates that are used to track short-term PODD Artifacts so they can be distinguished in meaning from those used to track long-term Schema Ontologies.
* Add support for explanations from pellet-explanations to let the user know why their ontology was found to be inconsistent for debugging purposes
* Design a Permanent identifier generator to assign identifiers to Podd Artifacts
* Design a good caching policy for Podd Artifacts. Naive policy is to remove them after every operation and then add them again before the next operation.
* Confirm with the ANU and CSIRO phenomics groups whether the current PODD ontologies fit their current needs, as they were designed during the previous project and may not fit currently



