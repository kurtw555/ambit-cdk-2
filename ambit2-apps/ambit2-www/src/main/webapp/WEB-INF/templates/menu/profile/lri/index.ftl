<#include "/html.ftl" >
<head>
<#include "/header_updated.ftl" >

<script type='text/javascript'>

$(document)
		.ready(
				function() {
						loadHelp("${ambit_root}","about");
						jQuery("#breadCrumb").hide();
				});
</script>

</head>
<body>


<div class="container" style="margin:0;padding:0;">

<!-- banner -->

<#include "/banner_crumbs.ftl">

	
<div class="three columns" id="query">
&nbsp;
</div>	

<div class="twelve columns " style="padding:0;" >
	<div class="row add-bottom">
	&nbsp;
	</div>
	<div class="row add-bottom">
		
			<div class="fifteen columns remove-bottom" id="query">
			<div class="alpha">
				<div class="remove-bottom h2">
						${custom_title!"Welcome to AMBIT"} 
				</div>
			    <div class='help'>${custom_description!"Chemical structures database, properties prediction & machine learning with OpenTox REST web services API"}</div>			
			</div>
			</div>
	</div>	
		
	<div class="row add-bottom">&nbsp;</div>	
	
	<div class="row add-bottom">
		<form action='${ambit_root}/ui/_search?option=auto}' id="searchForm"  method="GET" >	
			<div class="fifteen columns remove-bottom" id="query">
			<div class="alpha">
				<div class="remove-bottom h4">
						Simple search
				</div>
			    <div class='chelp'>Enter chemical name, identifiers, SMILES, InChI</div>			
			</div>
			</div>
		<div class='row add-bottom'>

			<input class='eight columns omega half-bottom' type="text" id='search' value='${custom_query!""}' name='search'>
			<input class='three columns omega submit' type='submit' value='Search'>
		</div>

		<div class="fifteen columns remove-bottom" id="query">
			<div class="alpha">
			    <div class='chelp'>Advanced: 
			    <a href='${ambit_root}/ui/_search'>Structure search</a> |
			    <a href='${ambit_root}/substance'>Search substances by identifiers</a> |
			    <a href='${ambit_root}/query/study'>Search substances by endpoint data</a>
			    <#if service_search??>
			    | <a href='${service_search}'>Free text search</a>
			    </#if>
			    
			    </div>			
		</div>
		</div>		
	</form>				
	</div>	

	
    
    

</div>   		



<div class='row add-bottom' style="height:400px;">&nbsp;</div>

    	<div  id="footer_logo">
			<#include "/menu/profile/lri/agreement.ftl">
		</div>


<#include "/footer.ftl" >
</div> <!-- container -->
</body>
</html>
