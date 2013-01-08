<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="success" type="java.lang.Boolean" -->
<#-- @ftlvariable name="statusList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="self_registration_status" type="java.lang.String" -->
<#-- @ftlvariable name="self_registration_status_error" type="java.lang.String" -->
<#-- @ftlvariable name="self_registration_user_status" type="java.lang.String" -->
<#-- @ftlvariable name="self_registration_user_status_error" type="java.lang.String" -->

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
    <h3>User Self Registration Management</h3>
</div>

<div id="content_pane">

<#include "admin_aux.html.ftl"/>

<div id="main">

    <#if success?? && success>
        <h4>User self registration settings updated successfully.</h4>
    </#if>

<form name="manage_self_registration" enctype="multipart/form-data" action="${baseUrl}/admin/user/selfRegistrationManagement" method="POST">

    <div id="self_reg_status" class="fieldset radioGroup">
		<div class="legend">User Self Registration Status</div>
		<ol>
            <li>
                <#if self_registration_status?? && self_registration_status == "enabled">
                <input id="self_reg_status_enabled" class="narrow" name="self_registration_status" type="radio" value="enabled" checked>
                <#else>
                <input id="self_reg_status_enabled" class="narrow" name="self_registration_status" type="radio" value="enabled">
                </#if>

                <label for="self_reg_status_enabled">Enabled</label>
            </li>
            <li>
                <#if self_registration_status?? && self_registration_status == "disabled">
                <input id="self_reg_status_disabled" class="narrow" name="self_registration_status" type="radio" value="disabled" checked>
                <#else>
                <input id="self_reg_status_disabled" class="narrow" name="self_registration_status" type="radio" value="disabled">
                </#if>
                <label for="self_reg_status_disabled">Disabled</label>
            </li>
            <#if self_registration_status_error??>
                <h6 class="errorMsg" ${self_registration_status_error}></h6>
            </#if>
        </ol>
    </div>

    <div id="default_user_status" class="fieldset radioGroup">
		<div class="legend">Default User Status</div>
		<ol>
            <#if statusList??>
            <#list statusList as status>
                <li>
                    <#if self_registration_user_status?? && self_registration_user_status == status>
                        <input id="${status}" class="narrow" name="self_registration_user_status" type="radio" value="${status}" checked>
                    <#else>
                        <input id="${status}" class="narrow" name="self_registration_user_status" type="radio" value="${status}">
                    </#if>
                    <label for="${status}">${status}</label>
                </li>
            </#list>
            </#if>
            <#if self_registration_user_status_error??>
                <h6 class="errorMsg" ${self_registration_user_status_error}></h6>
            </#if>
        </ol>
    </div>

    <br>
    <div id="buttonwrapper">
        <button type="submit" name="submit">Submit</button>
        <a href="${baseUrl}/admin/user/list">Cancel</a>
    </div>
</form>

</div>  <!-- main -->
</div>  <!-- content pane -->