<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="podd.model.user.User" -->

<div id="aux">
	<h3>Search</h3>
	<a href="${baseUrl}/search">Advanced Search</a>
	<br/><a href="${baseUrl}/projectsfilter">Filter Projects</a>
	<br/><a href="${baseUrl}/scanBarcode">Scan Barcode</a>
    <#if user?? && user.isAdministrator>
    <br/><a href="${baseUrl}/query">SPARQL Queries</a>
    </#if>
</div>