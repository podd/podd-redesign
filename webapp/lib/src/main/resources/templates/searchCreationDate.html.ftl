<#-- @ftlvariable name="searchCriteriaWeb" type="podd.search.web.SearchCriteriaWeb" -->
<#-- @ftlvariable name="startDateCreatedError" type="java.lang.String" -->
<#-- @ftlvariable name="endDateCreatedError" type="java.lang.String" -->

<li>
    <label for="startDateCreated" class="bold">Start date created:</label>
    <input id="startDateCreated" name="startDateCreated" type="text" value="${(searchCriteriaWeb.startCreationDate)!""}" searchText='true'> (dd/mm/yyyy)
    <h6 class="errorMsg">${startDateCreatedError!""}</h6>
</li>
<li>
    <label for="endDateCreated" class="bold">End date created:</label>
    <input id="endDateCreated" name="endDateCreated" type="text" value="${(searchCriteriaWeb.startCreationDate)!""}" searchText='true'> (dd/mm/yyyy)
    <h6 class="errorMsg">${endDateCreatedError!""}</h6>
</li>