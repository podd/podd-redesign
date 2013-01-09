<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="duration" type="long" -->
<#-- @ftlvariable name="numberIndexed" type="long" -->

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
    <h3>Index Management</h3>
</div>

<div id="content_pane">

<#include "admin_aux.html.ftl"/>

<div id="main">

    <#if numberIndexed??>
        <div id="result">
            <p>The search index has been regenerated successfully. ${numberIndexed} objects were indexed, total time taken
            ${duration} seconds.</p>
        </div>
    </#if>

    <form method="post" action="/podd/admin/index">
    <div id="buttonwrapper">
        <h4>Rebuild the PODD search index?</h4>
        <br>
        <button type="submit">Rebuild</button>
        <a href="${baseUrl}/admin/user/list">Cancel</a>
    </div>
</form>

</div>
</div>