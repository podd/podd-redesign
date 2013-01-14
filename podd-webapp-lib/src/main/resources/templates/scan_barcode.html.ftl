<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="barcode" type="java.lang.String" -->
<#-- @ftlvariable name="barcodeError" type="java.lang.String" -->

<div id="title_pane">
    <h3>Scan Barcode</h3>
</div>

<div id="content_pane">

    <#if errorMessage?? && errorMessage != "">
    <h4 class="errorMsg">${errorMessage!""}</h4>
    </#if>

    <#include "search_aux.html.ftl"/>

    <div id="main">

        <div id="buttonwrapper_right">
            <form id="removeFilterForm" method="POST" action="/podd/removeprojectsfilter?redirect=scanBarcode" style="display:none">
            </form>
            <#if hasFilter?? && hasFilter>
            <a href="javascript:void(0);" onclick="removeFilter()">Remove Filter</a>
            </#if>
        </div>
        <p><br></p>

        <form enctype="multipart/form-data" method="post" action="/podd/scanBarcode">
            <h3 class="underlined_heading">Barcode
                <a href="javascript:animatedcollapse.toggle('searchContent')" icon="toggle" title="View Search Query"></a>
            </h3>

            <#escape x as x?html>
            <div id="searchContent">
                <div id="searchBarcode" class="fieldset_without_border">
                    <li>
                        <label for="barcode" class="bold">Enter Barcode:</label>
                        <input id="barcode" name="barcode" type="text" value="${barcode!""}" searchBarcode='true'>
                        <h6 class="errorMsg">${barcodeError!""}</h6>
                    </li>

                    <!-- we just want the underline -->
                    <h3 class="underlined_heading"></h3>

                    <!-- submit buttons -->
                    <div id="buttonwrapper">
                        <button type="submit">Search</button>
                        <button type="button" onclick='resetSearchQuery();'>Reset</button>
                    </div>
                    <p><br></p>
                </div>
            </div>
            </#escape>
        </form>
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
        $("input[searchBarcode='true']").each(function() {
            this.value = '';
        });
	}
</script>