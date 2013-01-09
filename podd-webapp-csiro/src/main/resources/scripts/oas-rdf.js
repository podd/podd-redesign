/**
 * Ontology Annotation Services :
 * Uses jquery-RDF to analyse the current document with respect to the RDFa information it contains and populate the rdfadebug element with the resulting information
 */
$.debugRdfaBody = function()
{
$('body')
.rdf()
.where("?subject ?property ?object")
.each(function (i, data, triples) {
	$('#rdfadebug').rdfa(triples);
  });
};

$(document).ready(function() {
	$.debugRdfaBody();
});
