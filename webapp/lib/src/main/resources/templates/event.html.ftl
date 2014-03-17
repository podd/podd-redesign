<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="parentObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="relationshipList" type="java.util.ArrayList<podd.resources.AddChildResource.ObjectRelationshipHelper>" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Add Event</h3>
</div>

<div id="content_pane">

<#if errorMessage?? && errorMessage != "">
<p>
<h4 class="errorMsg" id='errorMsg'>${errorMessage!""}</h4>
</p>
<#else>
<br>
</#if>

<form name="addChild">

<#include "parent_details.html.ftl"/>

<br>
<h3 class="underlined_heading">Relationship & Type
    <a href="javascript:animatedcollapse.toggle('relationshipDetails')" icon="toggle" title="View Relationship Details"></a>
</h3>
<br>
<div id="relationshipDetails">  <!-- Collapsible div -->
    <div id="relationshipInfo">

        <label for="relationship" class="bold">Relationship:
            <span icon="required"></span>
        </label>
        <select id="relationship" name="relationship"
            onchange="changeChildList(document.getElementById('relationship').value); changeLink();">
            <option value="no_selection" selected>Please Select</option>
            <#list relationshipList as relationship>
            <option title="${relationship.getTooltip()}" value="${relationship.getEncodedURI()}">${relationship.getLabel()}</option>
            </#list>
        </select>

        <label for="type" class="bold">Child Object:
            <span icon="required"></span>
        </label>
        <select id="type" name="type" onchange="changeLink()">
            <option value="no_selection" selected>Please Select</option>
        </select>
        <p>Relataionship defines the nature of the relationship between the parent and the child object.</p>
        <h6 class="errorMsg" id='errorSelection'></h6>
    </div>
</div>  <!-- relationshipDetails - Collapsable div -->

<br>
<h3 class="underlined_heading"></h3> <!-- just want the line -->

<div id="buttonwrapper">
    <#if parentObject??>
        <a href="${baseUrl}/object/${parentObject.getPid()}/add" id="continue_link">Continue</a>
        <a href="${baseUrl}/object/${parentObject.getPid()}">Cancel</a>
    <#else>
        <a href="${baseUrl}/project">Cancel</a>
    </#if>
</div>

</form>
</div>  <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>
<script type="text/javascript">
	animatedcollapse.addDiv('parentDetails', 'fade=1,hide=0')
    animatedcollapse.addDiv('relationshipDetails', 'fade=1,hide=0')
	animatedcollapse.addDiv('hierarchy', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
</script>

<script type="text/javascript">
    function changeChildList(chosen) {
        var selbox = document.getElementById('type');
        selbox.options.length = 0;
        if (chosen == "no_selection") {
          selbox.options[selbox.options.length] = new Option('Please Select','no_selection');
        }
        <#list relationshipList as relationship>
        if (chosen == "${relationship.getEncodedURI()}") {
            <#list relationship.getChildObjectList() as child>
                selbox.options[selbox.options.length] = new Option('${child.getLabel()}','${child.getOwlClass()}');
            </#list>
        }
        </#list>
    }

    function changeLink() {
        var type = document.getElementById('type').value;
        var relationship = document.getElementById('relationship').value;
        <#if parentObject??>
        var parentPid = "${parentObject.getPid()}";
        <#else>
        var parentPid = "";
        </#if>
        document.getElementById('continue_link').href
                = "${baseUrl}/object/new?type=" + type + "&parent=" + parentPid + "&relationship=" + relationship;
    }

    $('#continue_link').click( function (event) {
        var type = document.getElementById('type').value;
        var relationship = document.getElementById('relationship').value;
        if (relationship == "no_selection" || type == "no_selection") {
            document.getElementById('errorSelection').innerHTML = 'Both a relationship and a child object need to be selected.';
            event.preventDefault();
        }
    })
</script>