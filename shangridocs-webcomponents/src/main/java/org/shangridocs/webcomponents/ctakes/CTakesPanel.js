if (typeof openFileIndex == undefined){
	var openFileIndex = 0;
}

function cTAKESPanel(responseText) {
    if (typeof ajaxRunning == undefined){
    	var ajaxRunning = false;
    }

    var regex = /^[a-z0-9]+$/i;
    var rightPane = $(".right-pane-default");
	rightPane.addClass("hide");
	var extractedDataPanel = rightPane.find(".extractedDataPanel");
	var tempFileIndex = openFileIndex;
	extractedDataPanel.html( $(".loading-animation-code").html() );
	$(".content > .container-fluid").append("<div class='col-md-4 right-pane extractedPane" + openFileIndex + "' data-fileindex='" + openFileIndex +"'>" + rightPane.html() + "</div>");
	extractedDataPanel.html( "");

    if (!ajaxRunning) {
		ajaxRunning = true;
		$.ajax({ headers : {
								'Content-Type' : 'text/plain'
							},
				url : "./services/tika/ctakes",
				method : "put",
				async: true,
				data : responseText[0]["X-TIKA:content"],
				success : function(result) {
					ctakesData = result[0];
					fileContent = ctakesData["X-TIKA:content"];
	
					// remove initial new line characters from
					// returned XTIKA content.
					for (init = 0; init < fileContent.length; init++) {
						if (fileContent[init].match(regex))
							break;
					}
					fileContent = fileContent.slice(init);
	
					// check to find out extracted data belongs to
					// which extracted text.
					// Currently, object for removed file is not
					// removed. So, it is important to check if file
					// object was removed.
					for (var tempFileIndex = 0; tempFileIndex < filesArray.length; tempFileIndex++) {
						if ($
								.trim(filesArray[tempFileIndex]["studyText"]) == $
								.trim(fileContent)
								&& typeof filesArray[tempFileIndex]["removed"] == "undefined"
								&& typeof filesArray[tempFileIndex]["ctakesReturned"] == "undefined")
							break;
					}
					// set this for this file future use of above ^
					filesArray[tempFileIndex]["ctakesReturned"] = true;
					// make Select/Deselect option for extracted
					// data options available.
					$(
							".extractedPane" + tempFileIndex
									+ " .all-selection-option")
							.removeClass("hide");
	
					filesArray[tempFileIndex]["ctakesData"] = ctakesData;
					showCtakesData(ctakesData, tempFileIndex, []);
					// all should be unselected for the first time.
					$(
							".extractedPane" + tempFileIndex
									+ " .deselect-all-ctakes")
							.click();
					ajaxRunning = false;
				}
			})
				.fail(function() {
							$(
									".extractedPane" + tempFileIndex
											+ " .extractedDataPanel")
									.html(
											"<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
							ajaxRunning = false;
						});
	   }

}