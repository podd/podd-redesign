<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->

<script type="text/javascript">
	podd.objectTypeUri = '${objectType.objectURI!"Not Found"}';
	// FIXME: Insert the parent URI using freemarker
	podd.parentUri = undefined;
	// The object URI is always undefined for a new object initially,
	// until the first valid save event to the server
	podd.objectUri = undefined;
</script>


<div id="title_pane">
    <h3>${title!""}</h3>
</div>

<div id="content_pane">
<#if errorMessage?? && errorMessage != "">
<h4 class="errorMsg">${errorMessage!""}</h4>
</#if>

<#-- add general error messages -->
<#if generalErrorList?? && generalErrorList?has_content>
<ol>
    <#list generalErrorList as errorMsg>
    <li class="errorMsg">${errorMsg}</li>
    </#list>
</ol>
</#if>
	<h3 class="underlined_heading">${objectType.label!""} Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
	</h3>	
	<div id='details'>  <!-- Collapsible div -->
			<!-- TODO: Generate these using javascript -->
			<!-- standard attributes -->
			<ol>
				<!-- object name -->
	        	<li>
		            <label for="object_name" class="bold">Title:
						<span icon="required"></span>
					</label>
                </li>
                <li>
	                <input id="object_name" name="object_name" type="text" value="">
	                <h6 class="errorMsg">${objectNameError!""}</h6>
	            </li>
	            
	            <!-- object description -->
	            <li>
		            <label for="object_description" class="bold">Description:</label>
                </li>
                <li>
					<textarea id="object_description" name="object_description" cols="30" rows="2"></textarea>
	                <span id="object_desc_text_limit"></span>
	                <h6 class="errorMsg">${objectDescriptionError!""}</h6>
				</li>
			<!--  other attributes -->

            </ol>

	    </div>  <!-- details - Collapsible div -->


		
		<div id="header">
		<ul id='list_attributes'>
		</ul>
		</div>
		
		<br><br>
		
    <script type="text/javascript">
	    animatedcollapse.addDiv('details', 'fade=1,hide=0');
    </script>
		