<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pi" type="podd.model.user.User" -->
<#-- @ftlvariable name="adminList" type="java.util.ArrayList<podd.model.user.User>" -->
<#-- @ftlvariable name="memberList" type="java.util.ArrayList<podd.model.user.User>" -->
<#-- @ftlvariable name="observerList" type="java.util.ArrayList<podd.model.user.User>" -->
<#-- @ftlvariable name="participantsErrorMessage" type="java.lang.String" -->

<h3 class="underlined_heading">Project Participants
	<a href="javascript:animatedcollapse.toggle('participants')" icon="toggle" title="View Project Participants"></a>
</h3>
<div id="participants">  <!-- Collapsible div -->
    <#if  participantsErrorMessage?has_content>
    <p>
    <h6 class="errorMsg">${participantsErrorMessage!""}</h6>
    </#if>

    <div id="principle_investigator" class="fieldset">
    	<div class="legend">Principal Investigator</div>
        <div class="content">
    	<#if  pi??>
        <ol>
            <li>${pi.getFirstName()!""} ${pi.getLastName()!""}, ${pi.getAffiliation()!""}, ${pi.getEmail()!""}</li>
    	</ol>
        </#if>
        </div>
	</div>

    <div id="administrator_list" class="fieldset">
    	<div class="legend">Project Administrators</div>
        <div class="content">
    	<ol>
            <#list adminList as user>
                <li>${user.getFirstName()!""} ${user.getLastName()!""}, ${user.getAffiliation()!""}, ${user.getEmail()!""}</li>
            </#list>
    	</ol>
        </div>
	</div>

    <div id="member_list" class="fieldset">
    	<div class="legend">Project Members</div>
        <div class="content">
    	<ol>
            <#list memberList as user>
                <li>${user.getFirstName()!""} ${user.getLastName()!""}, ${user.getAffiliation()!""}, ${user.getEmail()!""}</li>
            </#list>
    	</ol>
        </div>
	</div>

    <div id="observer_list" class="fieldset">
    	<div class="legend">Project Observers</div>
    	<div class="content">
        <ol>
            <#list observerList as user>
                <li>${user.getFirstName()!""} ${user.getLastName()!""}, ${user.getAffiliation()!""}, ${user.getEmail()!""}</li>
            </#list>
    	</ol>
        </div>
	</div>
</div>  <!-- participants - Collapsable div -->