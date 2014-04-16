<#-- @ftlvariable name="keywords" type="java.lang.String" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="contentTemplate" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="org.restlet.security.User" -->


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML+RDFa 1.0//EN" "http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" 
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcterms="http://purl.org/dc/terms/" 
	xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:cc="http://creativecommons.org/ns#"
	xmlns:ex="http://example.org/"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
	xmlns:sioc="http://rdfs.org/sioc/ns#"
	xmlns:podd="http://purl.org/ns/poddBase#" 
	version="XHTML+RDFa 1.0">
<head>
	<title>${pageTitle}</title>

	<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />

    <meta name="description" content="The Phenomics Ontology Driven Data Management Project (PODD)
     is a National e-Research Architecture Taskforce (NeAT) project co-funded by ANDS and ARCS.
     The aim of the project is to develop data management solutions to meet the needs of
     researchers working at the Australian Plant Phenomics Facility (APPF) and the Australian
     Phenomics Network (APN). Both research communities have the need to gather and annotate
     data from both high and low throughput phenotyping devices."/>

    <meta name="keywords" content="${keywords}" />

    <link rel="icon" href="${baseUrl}/resources/images/podd.ico" type="image/png" />
    <link rel="shortcut icon" href="${baseUrl}/resources/images/podd_ico.png" type="image/png" />
    <link rel="stylesheet" href="${baseUrl}/resources/styles/podd.css" type="text/css" />
    <link rel="stylesheet" href="${baseUrl}/resources/styles/podd-colours.css" type="text/css" />
	<link rel="stylesheet" href="${baseUrl}/resources/styles/jquery-ui-1.10.3.custom.css" type="text/css" />
	<link rel="stylesheet" href="${baseUrl}/resources/styles/jquery.datetimepicker.css" type="text/css" />
	<link rel="stylesheet" href="${baseUrl}/resources/styles/select2.css" type="text/css" />
	<link rel="stylesheet" href="${baseUrl}/resources/styles/style.css" type="text/css" />

    	<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery-1.9.1.js"></script>
    	<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery-ui-1.10.3.custom.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.cookie-1.3.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.datetimepicker.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/select2.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jstree.js"></script>
		
		<!-- Dependencies for rdfquery -->
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.json.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.rdfquery.rules-1.1-SNAPSHOT.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.rdf.turtle.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.effects.bounce.js"></script>
		
		<!-- Dependencies for autocomplete -->
	    
 		<!-- Dependencies for PODD -->
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.metadata.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.inputlimiter.1.2.js"></script>
		<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.tablesorter.js"></script>

		<script type="text/javascript" src="${baseUrl}/resources/scripts/animatedcollapse.js">
		    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
		    /***********************************************
		     * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
		     * This notice MUST stay intact for legal use
		     * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
		     ***********************************************/
		</script>

		<script type="text/javascript">
			// setup the global PODD object
			podd = {};
			podd.baseUrl = "${baseUrl}";
		</script>
    
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/podd_main.js"></script>
		<script type="text/javascript" src="${baseUrl}/resources/scripts/podd_edit.js"></script>
	    
    <script type="text/javascript">
	    // display an alert if the browser is IE7 or older
	    $(document).ready(podd.displayBrowserAlert);
    </script>
    
</head>

<body>

    <noscript>
        <p>
        <strong>
	        PODD is accessed via JavaScript 1.2 enabled web pages. You will require at least either Netscape 4.0 or
	        Microsoft Internet Explorer 5.0. If you have the minimum browser requirement then you may have disabled
	        JavaScript in your browser preferences.
        </strong>
        </p>
    </noscript>

	<!-- page banner -->
	<div id="site_banner">

		<!-- search panel -->
		<div id="search">
	    	<form enctype="multipart/form-data" method="post" action="${baseUrl}/search">
	    	
				<#if (user)??>							
						<input type="checkbox" name="scopeMyProjects" value="true" checked="checked" style="display:none"/> 
						<input type="checkbox" name="scopePublicProjects" value="true" checked="checked" style="display:none" /> 		
				<#else>
					<!-- Search for public records only for anonymous users -->
					<input type="checkbox" name="scopePublicProjects" value="true" checked="checked" style="display:none" /> 						
				</#if>		    	
	    	
	    		<div>
	            <input class="searchTextField" name="allTheseWords" id="search-box" size="22" maxlength="200"/>
	            <input class="searchButton" type="submit" name="search" value="search"/>
	            <a href='${baseUrl}/search' >Advanced Search</a>
	            </div>
	        </form>
	        
	    </div>

	    <span><h3>PODD - Phenomics Ontology Driven Database</h3> 
	    	<p class="errorMsg">Beta Version : Please file errors in our <a href="https://github.com/podd/podd-redesign/issues/new">issue tracker</a>.</p>
	    </span>
	</div>

    <!-- navigation bar-->
    <div id="user_div">
        <ul id="user_list">
            <!-- user details -->
            <#if user??>
                <li class="left_image float_right"><a href="${baseUrl}/logout">Logout</a></li>
                <li class="left_image float_right"><a href="${baseUrl}/help">Help</a></li>
                <li class="no_image float_right"><a href="${baseUrl}/about">About</a></li>
                <!-- <li class="left_image float_right"><a href="${baseUrl}/supportDesk">Support</a></li> -->
                <li class="left_image float_right"><a href="${baseUrl}/user/details?userIdentifier=${user.identifier!"unknown-username"}">Settings</a></li>
                <li class="no_image float_right"><i>${user.firstName!""} ${user.lastName!""}</i></li>
            <#else>
                <li class="left_image float_right"><a href="${baseUrl}/loginpage">Login</a></li>
                <li class="left_image float_right"><a href="${baseUrl}/help">Help</a></li>
                <li class="no_image float_right"><a href="${baseUrl}/about">About</a></li>
            </#if>
        </ul>
    </div>
    <br />
    
    <div id="toolbar_div">
        <ul id="nav_list">
            <!-- main menu items -->
            <li class="no_image"><a href="${baseUrl}/artifacts">Projects</a></li>
<!--            <li class="left_image"><a href="${baseUrl}/browser">Browser</a></li> -->
<!--            <li class="left_image"><a href="${baseUrl}/search">Search</a></li> -->
            <#if user??>
                <#if isAdmin>
                <li class="left_image"><a href="${baseUrl}/admin/user/list">Administrator</a></li>
                </#if>
            </#if>
        </ul>
    </div>

	<#attempt>
		<!-- content pane -->
    	<#include contentTemplate/>
	<#recover>
		<div id="content_pane">Error: Could not generate page "${contentTemplate}"</div>
	</#attempt>
	
    <a class="no_image float_right" href="${baseUrl}/about">About us</a>
</body>
</html>