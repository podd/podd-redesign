<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="generalErrorList" type="java.util.ArrayList<java.lang.String>" -->

<div id="title_pane">
    <h3>Advanced Search</h3>
</div>

<div id="content_pane">

    <#if errorMessage?? && errorMessage != "">
    <h4 class="errorMsg">${errorMessage!""}</h4>
    </#if>

    <#-- add general error messages -->
    <#if generalErrorList?? && generalErrorList?has_content>
    <ol>
        <#list generalErrorList as errorMsg>
        <li class="errorMsg">${errorMsg}</li>
        </#list>
    </ol>
    </#if>

    <#include "search_aux.html.ftl"/>

    <div id="main">
        <#include "searchForm.html.ftl"/>
        <br>
        <#include "searchResults.html.ftl"/>
    </div> <!-- main -->
</div>  <!-- content pane -->


<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>
<script type="text/javascript">
	animatedcollapse.addDiv('searchContent', 'fade=1,hide=0')
    animatedcollapse.addDiv('searchResults', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
</script>

<script type="text/javascript">
	$(document).ready(function() {
		var searchResultsId = 'startSearchResults';
		if (document.getElementById(searchResultsId) != null) {
			scrollTo('#' + searchResultsId);
		}
	});

	function scrollTo(selector) {
	    var targetOffset = $(selector).offset().top;
	    $('html,body').animate({scrollTop: targetOffset}, 500);
	}

    function resetSearchQuery() {
        $("input[searchText='true']").each(function() {
            this.value = '';
        });

        $("input[searchSelect='true']").each(function() {
            this.selectedIndex = 0;
        });

        $("input[searchCheckbox='true']").each(function() {
            this.checked = true;
        });
	}
</script>