<#-- @ftlvariable name="searchCriteriaWeb" type="podd.search.web.SearchCriteriaWeb" -->
<#-- @ftlvariable name="allTheseWordsError" type="java.lang.String" -->
<#-- @ftlvariable name="anyTheseWordsError" type="java.lang.String" -->
<#-- @ftlvariable name="phraseError" type="java.lang.String" -->
<#-- @ftlvariable name="excludeError" type="java.lang.String" -->
<li>
    <label for="allTheseWords" class="bold">Containing all of these words:</label>
    <input id="allTheseWords" name="allTheseWords" type="text" value="${(searchCriteriaWeb.getQueryAllTheseWords())!""}" searchText='true'>
    <h6 class="errorMsg">${allTheseWordsError!""}</h6>
</li>

<li>
    <label for="anyTheseWords" class="bold">Containing any of these words:</label>
    <input searchText='true' id="anyTheseWords" name="anyTheseWords" type="text" value="${(searchCriteriaWeb.getQueryAnyTheseWords())!""}" searchText='true'>
    <h6 class="errorMsg">${anyTheseWordsError!""}</h6>
</li>

<li>
    <label for="phrase" class="bold">Containing the phrase:</label>
    <input id="phrase" name="phrase" type="text" value="${(searchCriteriaWeb.getQueryPhrase())!""}" searchText='true'>
    <h6 class="errorMsg">${phraseError!""}</h6>
</li>

<li>
    <label for="exclude" class="bold">Excluding these words:</label>
    <input id="exclude" name="exclude" type="text" value="${(searchCriteriaWeb.getQueryExclude())!""}" searchText='true'>
    <h6 class="errorMsg">${excludeError!""}</h6>
</li>
