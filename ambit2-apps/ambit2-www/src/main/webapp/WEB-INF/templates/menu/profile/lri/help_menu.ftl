<ul>
	<li> <a href="#">User guide</a>
		<ul>
		<li><a href='http://ambit.sourceforge.net/intro.html' target='guide'>Introduction</a></li>
		<li><a href='http://ambit.sourceforge.net/usage.html' target='guide'>Search by chemical structure</a></li>
		<li><a href='http://ambit.sourceforge.net/usage_substance.html' target='guide'>Search by phys chem or biological effects</a></li>
		<li><a href='http://ambit.sourceforge.net/install_ambitrest.html.html' target='guide'>Downloads and Installation</a></li>
		</ul>
	</li>	
	<li><a href='http://ambit.sourceforge.net/support.html' target='report' title='Found an error? Report here'>Submit an issue <img src='${ambit_root}/images/profile/lri/logo.png' width='203' height='24'></a></li>
	
	<li><a href="#">For developers</a>
	<ul>
		<li><a href='${ambit_root}/api-docs' target=_blank title="API documentation in Swagger JSON format">API Documentation</a></li>
		<li><a href='http://ideaconsult.github.io/apps-ambit/apidocs/' class="qxternal" target=_blank title="API documentation via swagger-ui">API Documentation (public services)</a></li>
		<li><a href="${ambit_root}/compound" title="Chemical compound">Compound</a></li>
		<li><a href="${ambit_root}/dataset">Datasets</a></li>
		<li><a href="${ambit_root}/feature" title="Features (identifiers, measured and calculated properties)">Features</a></li>
		<li><a href="${ambit_root}/algorithm">Algorithm</a></li>
		<li><a href="${ambit_root}/model">Model</a></li>
		<li>
			<a href="${ambit_root}">Search</a>
			<ul>
				<li><a href="${ambit_root}/query/similarity?search=c1ccccc1Oc2ccccc2&threshold=0.9">Similarity</a></li>
				<li><a href="${ambit_root}/query/smarts?search=c1ccccc1CCCC">Substructure</a></li>
			
			</ul>
		</li>
	</ul>
	</li>
	<li>
		<a href="${ambit_root}/depict">Demo</a>
		<#include "/menu/profile/lri/demo_menu.ftl">
	</li>		
	<li><a href="#">About</a>
		<ul>
		<li><a href="#">Version</a>
			<ul class="mega-menu">
			<li>
				<div style="width:400px;max-width:100%;">
	          		<div style="padding:5px 24px;">
						<div class='h6' style='color:#729203;' title='${ambit_version_long}'>AMBIT v${ambit_version_short}</div>
					</div>
				</div>		
			</li>
			</ul>
		</li>
		<li><a href="http://www.ideaconsult.net/" title="Developed by IdeaConsult Ltd.">IdeaConsult</a>	</li>
		<li><a href="http://cefic-lri.org/" title='This project has received funding from CEFIC Long Range  Research Initiative'>CEFIC LRI</a>	</li>
		</ul>
	</li>

</ul>