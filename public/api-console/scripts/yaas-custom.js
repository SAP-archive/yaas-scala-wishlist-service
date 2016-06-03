'use strict';

jQuery(function($) {

	//
	// retrieve API version from respective REST endpoint
	//

	var versionContainer = $('footer.yaas-footer .app-version');

	function versionResponseHandler(data) {
		var versionString = 'Developer version';
		if (data && data.version) {
			versionString = 'Version: ' + data.version;
			if (data.buildTime) {
				versionString += ' - Build: ' + data.buildTime;
			}
		}
		versionContainer.text(versionString);
	};

	function versionErrorResponseHandler() {
		versionContainer.text('Developer version');
	};

	$.getJSON('app-version', versionResponseHandler).fail(versionErrorResponseHandler);


	//
	// copy API title to the title of the HTML document
	//

	function apiTitleHandler() {
		var apiTitle = $(this).text();
		if(apiTitle)
		{
			if(document.title)
			{
				document.title = apiTitle + ' - ' + document.title;
			}
			else
			{
				document.title = apiTitle;
			}
		}
	};

	$('body').one('DOMSubtreeModified', 'main h1.raml-console-title', apiTitleHandler);

	// invoke handler function right away, in case the API title was loaded before the above handler was attached
	($.proxy(apiTitleHandler, $('body main h1.raml-console-title').get(0)))();

})
