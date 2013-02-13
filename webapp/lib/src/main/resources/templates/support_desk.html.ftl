<#-- @ftlvariable name="supportEmail" type="java.lang.String" -->
<#-- @ftlvariable name="supportPhone" type="java.lang.String" -->

<div id="title_pane">
    <h3>Support Desk</h3>
</div>

<div id="content_pane">
    <#include "support_aux.html.ftl"/>

    <div id="main" class="allow_space">
        <p><span class="bold">Support Email:</span> &nbsp;&nbsp;<a href="${supportEmail!""}">${supportEmail!""}</a></p>

        <p><span class="bold">Support Phone:</span> &nbsp;&nbsp;${supportPhone!""}</p>

        <p><span class="bold">Support Instructions:</span> &nbsp;&nbsp;When reporting a problem please include the following information.</p>
        <ul class="stylized">
            <li>the date and time the error occurred</li>
            <li>the function you were performing when the error occured</li>
            <li>the tile of the screen you where in at the time the error occurred</li>
            <li>the URL displayed in your browser when the error occurred</li>
            <li>the error messages (if any) given</li>
        </ul>

        <br>
        <p>If you wish to provide feedback on the system functionality, please provide appropriate suggestions on how the functionality might be enhanced.</p>
    </div>
</div>      <!-- content pane -->