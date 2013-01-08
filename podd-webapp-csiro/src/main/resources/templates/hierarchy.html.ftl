<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="parentHierarchy" type="podd.resources.ObjectDetailsResource.HierarchyElement" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="childHierarchyList" type="java.util.ArrayList<podd.resources.ObjectDetailsResource.HierarchyElement>" -->

<h3 class="underlined_heading">Hierarchy
    <a href="javascript:animatedcollapse.toggle('hierarchy')" icon="toggle" title="View Hierarchy"></a>
</h3>
<div id="hierarchy">  <!-- Collapsible div -->

    <p>Hierarchy displays only the parent and existing children of the current object.</p>

        <ol>
            <#if  parentHierarchy??>
            <li class="hierarchy"><label class="parent"></label><a href="${baseUrl}/object/${parentHierarchy.getPoddObject().getPid()}" class="padded">${parentHierarchy.getPoddObject().getLocalName()}</a><span>: Type: ${parentHierarchy.getType()}, Children:
            	<#--
            		For performance reasons we don't want hibernate to fetch all the children just to count the number of children objects,
            		so if the childrenCountMap exists, then we use it to fetch the count, otherwise, load it from hibernate to do the count.            	
            	-->                          
            	<#if childrenCountMap?? && (childrenCountMap[parentHierarchy.poddObject.pid]??) >
            		${childrenCountMap[parentHierarchy.poddObject.pid]}	
            	<#else>
            		${parentHierarchy.getPoddObject().getMembers()?size}
            	</#if>
            	</span>
           	</li>
            </#if>
            <#if poddObject??>
            <ol>
            <li class="bold, hierarchy"><label class="current"></label>
            	<span class="padded">${poddObject.getLocalName()}: Type: ${objectType!"-"}, Children: ${poddObject.getMembers()?size} 

            	</span>
            </li>
            </ol>
            <ol>
            <#list childHierarchyList as child>
                <#if child.getState() == "A" || isAdmin>
                    <li class="hierarchy"><label class="child"></label><a href="${baseUrl}/object/${child.getPoddObject().getPid()}" class="padded">${child.getPoddObject().getLocalName()}</a>
                        <#if child.getState() != "A">
                            (<span class="descriptive">deleted</span>)
                        </#if>
                        <span>: Type: ${child.getType()}, Children: 
                        <#if childrenCountMap?? && (childrenCountMap[child.poddObject.pid]??) >
                        	${childrenCountMap[child.poddObject.pid]}
                        <#else>
                        	${child.getPoddObject().getMembers()?size}
                        </#if>
                         
                        </span>
                    </li>
                </#if>
            </#list>
            </ol>
        </ol>
    </#if>

</div>  <!-- hierarchy - Collapsable div -->