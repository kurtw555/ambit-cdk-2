<#if custom_logo??>
<#else>
	<#if menu_profile??>
		<#assign custom_logo='${ambit_root}/images/profile/${menu_profile}/logo.png'>	
	<#else>
		<#assign custom_logo='${ambit_root}/images/profile/default/logo.png'>
	</#if>	
</#if>


<!-- banner -->
<div class="row half-bottom" id="header" style="padding-top:5px">
	<#if menu_profile??>
		<#if menu_profile=='lri'>
		<div class="three columns">
		<#else>
		<div class="two columns">
		</#if>
			<a href="${ambit_root}/ui"><img class='scale-with-grid' border='0' src='${custom_logo}' title='Home' alt="${custom_title!'AMBIT'} logo"></a>
		</div>
		
		<#if menu_profile=='lri'>
		<div class="thirteen columns remove-bottom">
		<#else>
		<div class="fourteen columns remove-bottom">
		</#if>
		
			<#include "/menu/profile/${menu_profile}/smartmenu.ftl">
		</div>
	<#else>
		<div class="two columns">
			<a href="${ambit_root}/ui"><img class='scale-with-grid' border='0' src='${custom_logo}' title='Home' alt="${custom_title!'AMBIT'} logo"></a>
		</div>
		<div class="fourteen columns remove-bottom">
			<#include "/menu/profile/default/smartmenu.ftl">
		</div>
	</#if>

</div>
<div class="row remove-bottom" id="header">
	<#include "/toplinks.ftl">
</div>
<div class="row half-bottom">

		<div class="eight columns remove-bottom" style="padding-left:10px;">
			<div id="breadCrumb" class="breadCrumb module remove-bottom h5">
                    <ul>
                        <li>
                            <a href="${ambit_root}" title="AMBIT Home">Home</a>
                        </li>
                    </ul>
			</div>
		</div>
		
		<div class="seven columns remove-bottom"  id="_searchdiv">
		</div>

</div>	


<script type='text/javascript'>
	$(document).ready(function() {    
		$( "#smartmenu" ).smartmenus();    
	});
 </script>