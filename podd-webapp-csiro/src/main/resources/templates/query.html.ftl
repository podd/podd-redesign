<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="queryString" type="java.lang.String" -->
<#-- @ftlvariable name="queryResult" type="org.json.JSONObject" -->
<#-- @ftlvariable name="defaultQuery" type="java.util.List<podd.resources.SPARQLQueryResource.StringPair>" -->
<#-- @ftlvariable name="limit_number" type="java.lang.String" -->
<#-- @ftlvariable name="query_area_error" type="java.lang.String" -->
<#-- @ftlvariable name="query_limit_error" type="java.lang.String" -->

<div id="title_pane">
    <h3>SPARQL Query</h3>
</div>

<div id="content_pane">
    <#include "search_aux.html.ftl"/>

    <div id="main">
        <form enctype="multipart/form-data" method="post" action="/podd/query">
            <#if errorMessage?? && errorMessage != "">
                <h4 class="errorMsg">${errorMessage!""}</h4>
            </#if>

            <h3 class="underlined_heading">SPARQL Query
                <a href="javascript:animatedcollapse.toggle('query_pane')" icon="toggle" title="SPARQL Query"></a>
            </h3>

            <div id="query_pane">
                <div id="queryTerms" class="fieldset">
                    <ol>
                        <li>
                            <label for="query_area" class="bold">Query String</label>
                            <textarea class="gigantic" id="query_area" name="query_area"></textarea>
                            <h6 class="errorMsg">${query_area_error!""}</h6>
                        </li>

                        <li>
                            <label for="query_limit" class="bold">Limit: </label>
                            <input class="wide" id="query_limit" type="text" maxlength="3" value="${limit_number}">
                            <h6 class="errorMsg">${query_limit_error!""}</h6>
                        </li>
                    </ol>

                    <!-- submit buttons -->
                    <div id="buttonwrapper">
                        <button type="submit">Query</button>
                        <button type="button" onclick='resetSPARQL();'>Reset</button>
                    </div>
                    <!-- if this is not here the buttons fall out of the fieldset?? -->
                    <p><br></p>
                </div>
            </div>
        </form>

        <#if (queryResult)??>
            <br><#--<br>-->
            <h3 class="underlined_heading">Query Results
                <a href="javascript:animatedcollapse.toggle('query_result_pane')" icon="toggle" title="Query Results"></a>
            </h3>

            <div id="query_result_pane">
                <div id="query_result_area" class="fieldset">
                    <table id='table' class="tablesorter" cellspacing="0">
                        <thead>
                        <tr>
                        <@populateTable headerObject=queryResult.get("head")/>
                        </tr>
                        </thead>
                        <tbody>
                        <@populateResultValues valueObject=queryResult.get("results") headerObject=queryResult.get("head").get("vars")/>
                        </tbody>
                    </table>
                </div>
            </div>
        </#if>
    </div>  <!-- main -->
</div>  <!-- content pane -->


<#macro populateTable headerObject>
    <#list 0..headerObject.get("vars").length() - 1 as idx>
        <th>${headerObject.get("vars").get(idx)}</th>
    </#list>
</#macro>

<#macro populateResultValues valueObject headerObject>
    <#if (valueObject.get("bindings").length() > 0)>
        <#list 0..valueObject.get("bindings").length() - 1 as idx>
            <tr>
                <#local valueRow = valueObject.get("bindings").get(idx)>
                <#list 0..headerObject.length() - 1 as i>
                    <#local headerIdx = headerObject.get(i)>
                    <#if !valueRow.isNull(headerIdx)>
                        <#local singleValue = valueRow.get(headerIdx)>
                        <#if singleValue??>
                            <td>${singleValue.get("value")}</td>
                        <#else>
                            <td>&nbsp;</td>
                        </#if>
                    <#else>
                        <td>&nbsp;</td>
                    </#if>
                </#list>
            </tr>
        </#list>
    </#if>
</#macro>

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>
<script type="text/javascript">
	animatedcollapse.addDiv('query_result_pane', 'fade=1,hide=0')
    animatedcollapse.addDiv('query_pane', 'fade=1,hide=0')

	animatedcollapse.ontoggle = function($, divobj, state) {
        //fires each time a DIV is expanded/contracted
        //$: Access to jQuery
        //divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
        //state: "block" or "none", depending on state
    };
	animatedcollapse.init();
</script>

<script type="text/javascript">
    var prefixes;

    $(document).ready(function() {
        prefixes = "${defaultQuery}";

        <#if queryString??>
            $("#query_area").val("${queryString}");
        <#else>
            $("#query_area").val(prefixes);
        </#if>
    });

    function resetSPARQL() {
        $("#query_area").val(prefixes);
    }
</script>