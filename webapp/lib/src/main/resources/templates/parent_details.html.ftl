<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="parentObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="relationshipToParent" type="java.lang.String" -->

<#if  parentObject??>
<h3 class="underlined_heading">Parent Details
    <a href="javascript:animatedcollapse.toggle('parent_details')" icon="toggle" title="View Parent Details"></a>
</h3>

<div id="parent_details">  <!-- Collapsible div -->
    <div id="parentInfo" class="fieldset">
        <div class="legend">Parent Object Summary Information</div>
        <#if parentObject??>
        <ol>
        <li><span class="bold">ID:</span>
        	<a href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${parentObject.uri?url}">${parentObject.uri!""}</a>
        </li>
        <li><span class="bold">Type: </span>${parentObject.type!""}</li>
        <li><span class="bold">Title: </span>${parentObject.label!""}</li>
        <li><span class="bold">Relationship: </span>${parentObject.relationship}</li>
        </ol>
        </#if>
    </div>
</div>  <!-- parentDetails - Collapsable div -->
<br>
</#if>