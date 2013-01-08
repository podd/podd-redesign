<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="started" type="boolean" -->
<#-- @ftlvariable name="time" type="long" -->
<#-- @ftlvariable name="number" type="long" -->

<!--
  ~ Copyright (c) 2009 - 2010. School of Information Technology and Electrical
  ~ Engineering, The University of Queensland.  This software is being developed
  ~ for the "Phenomics Ontoogy Driven Data Management Project (PODD)" project.
  ~ PODD is a National e-Research Architecture Taskforce (NeAT) project
  ~ co-funded by ANDS and ARCS.
  ~
  ~ PODD is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ PODD is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with PODD.  If not, see <http://www.gnu.org/licenses/>.
  -->

<div id="title_pane">
    <h3>Triple Store Management</h3>
</div>

<div id="content_pane">

<#include "admin_aux.html.ftl"/>

<div id="main">

<#if number??>
    <div id="result">
        <p>The triple store has been re-populated successfully. ${number} triples were added, total time taken
        ${time} milliseconds.</p>
    </div>
</#if>

<#if errorMessage??>
    <p>
    <h4 class="errorMsg">${errorMessage!""}</h4>
</#if>

<form method="post" action="/podd/admin/rebuild" name="populate">
    <div id="buttonwrapper">
        <#if started?? && started>
            <p>A rebuilding process is running, please wait until it finishes to run it again.</p>
        <#else>
        <h4>Rebuild the PODD triple store?</h4>
        <br>
        <button type="submit" name="populate" onclick="if (confirm('Repopulating the triple store may take long. Are you sure you wish to continue?')) submit();">Repopulate</button>
        </#if>
        <a href="${baseUrl}/admin/user/list">Cancel</a>
    </div>
</form>

</div>  <!-- main -->
</div>  <!-- content pane -->