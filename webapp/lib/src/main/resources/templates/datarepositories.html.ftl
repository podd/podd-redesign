<#-- @ftlvariable name="dataRepositoriesList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Data Repositories</h3>
</div>

<div id="content_pane">

	<div id="error">
	    <h4 class="errorMsg">${errorMessage!""}</h4>
	</div>
	
	<div id="dataRepositories">
		<#if dataRepositoriesList?has_content>
			<ul>
				<#list dataRepositoriesList as dataRepository>
					<li>${dataRepository}</li>
				</#list>
			</ul>
		<#else>
			No data repositories currently available.
		</#if>
	</div>

</div>