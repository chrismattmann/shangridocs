if (typeof openFileIndex == "undefined") {
	var openFileIndex = 0;
}

function cTAKESPanel(responseText, checkAjax) {
	if (typeof ajaxRunning == "undefined") {
		var ajaxRunning = false;
	}

	var regex = /^[a-z0-9]+$/i;
	var rightPane = $(".right-pane-default");
	rightPane.addClass("hide");
	var extractedDataPanel = rightPane.find(".extractedDataPanel");
	var tempFileIndex = openFileIndex;
	extractedDataPanel.html($(".loading-animation-code").html());
	$(".content > .container-fluid").append(
			"<div class='col-md-4 right-pane extractedPane" + openFileIndex
					+ "' data-fileindex='" + openFileIndex + "'>"
					+ rightPane.html() + "</div>");
	extractedDataPanel.html("");

	if (!ajaxRunning) {
		ajaxRunning = true;
		$
				.ajax(
						{
							headers : {
								'Content-Type' : 'text/plain'
							},
							url : "./services/tika/ctakes",
							method : "put",
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
									if ($.trim(filesArray[tempFileIndex]["studyText"]) == $.trim(fileContent) && typeof filesArray[tempFileIndex]["removed"] == "undefined" && typeof filesArray[tempFileIndex]["ctakesReturned"] == "undefined"){
										break;
									}
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
								clearInterval(checkAjax);
							}
						})
				.fail(
						function() {
							$(
									".extractedPane" + tempFileIndex
											+ " .extractedDataPanel")
									.html(
											"<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
							ajaxRunning = false;
							clearInterval(checkAjax);							
						});
	}

}

// Handling event when user wants to select/deselect all annotations
$(".content").on(
		"click",
		".deselect-all-ctakes",
		function() {
			fileIndex = $(
					$(".deselect-all-ctakes").parent().parent().parent()
							.parent()[1]).data("fileindex");
			uncheckedKeys = [];
			filesArray[fileIndex]["uncheckedKeys"] = [];

			if (!$(this).hasClass(".select-all-ctakes")) {
				for ( var key in filesArray[fileIndex]["ctakesData"]) {
					if (key.substring(0, 7) == "ctakes:") {
						filesArray[fileIndex]["uncheckedKeys"].push(key
								.replace("ctakes:", ""));
					}
				}
				$(this).addClass(".select-all-ctakes");
			} else
				$(this).removeClass(".select-all-ctakes");

			showCtakesData(filesArray[fileIndex]["ctakesData"], fileIndex,
					uncheckedKeys);
		});

// Handling event when checkbox is changed on an annotation
$(".content")
		.on(
				"change",
				".key-highlight",
				function() {
					var changedKey = $(this).val();
					if ($(this).is(":checked")) {
						filesArray[openFileIndex]["uncheckedKeys"] = $.grep(
								filesArray[openFileIndex]["uncheckedKeys"],
								function(value) {
									return value != changedKey;
								})
					} else
						filesArray[openFileIndex]["uncheckedKeys"]
								.push(changedKey);

					// after remaking if this panel should stay open if it was
					// currently open
					if ($("#collapse" + changedKey).hasClass("in"))
						showCtakesData(filesArray[openFileIndex]["ctakesData"],
								openFileIndex,
								filesArray[openFileIndex]["uncheckedKeys"],
								changedKey);
					else
						showCtakesData(filesArray[openFileIndex]["ctakesData"],
								openFileIndex,
								filesArray[openFileIndex]["uncheckedKeys"]);
				});

// add highlighting color to a value and update text on the left
function colorText(value, color, fileIndex) {

	var selectedColor = color;
	if (color == "") {
		selectedColor = colorSwatch[filesArray[fileIndex]["swatchCounter"]];
		filesArray[fileIndex]["swatchCounter"]++;
	}
	// Our swatch color list of selected colors is limited. if ctakes list
	// exceeds the limit, get a random color.
	if (selectedColor == undefined)
		selectedColor = '#' + Math.floor(Math.random() * 16777215).toString(16);

	if (filesArray[fileIndex]["textToColor"] == "")
		filesArray[fileIndex]["textToColor"] = filesArray[openFileIndex]["studyText"];
	else
		filesArray[fileIndex]["textToColor"] = filesArray[fileIndex]["coloredText"];

	splitText = filesArray[fileIndex]["textToColor"].split(value);

	var coloredText = splitText[0];
	for (var i = 0; i < splitText.length - 1; i++) {
		coloredText += "<span style='background-color:" + selectedColor + ";'>"
				+ value + "</span>" + splitText[i + 1];
	}
	$("#file" + fileIndex + ".extracted-text").html(
			"<pre>" + coloredText + "</pre>");
	filesArray[fileIndex]["coloredText"] = coloredText;

	return selectedColor;

}

// create the annotation list from ctakes json.
function showCtakesData(data, fileIndex, uncheckedKeys, changedKey) {

	var studyData = data;
	var value = valueHTML = ctakesHTML = "";
	// Every time function is called, swatch colors get reinstantiated.
	filesArray[fileIndex]["swatchCounter"] = 0;
	filesArray[fileIndex]["textToColor"] = "";
	// to remain in context to which key has been checked/unchecked
	changedKey = changedKey || null;

	for ( var key in studyData) {
		if (key.substring(0, 7) == "ctakes:") {
			var color = "";

			extractedKey = key.replace("ctakes:", "");

			// this array should contain all children inside this key.
			var allChildren = [];

			// checked if key has to be ignored.
			if ($.inArray(extractedKey, ignoredKeys) == -1) {
				var checked = false;
				checkedAttribute = "";

				// check for this key is currently checked.
				if ($.inArray(extractedKey,
						filesArray[fileIndex]["uncheckedKeys"]) == -1) {
					checked = true;
					checkedAttribute = "checked = 'checked'";
				} else
					filesArray[fileIndex]["swatchCounter"]++;

				var metaValueCount = 1;
				if (studyData[key].constructor === Array) {

					valueHTML = "";
					metaValueCount = studyData[key].length;
					for (var i = 0; i < metaValueCount; i++) {
						valueArray = studyData[key][i].split(":");
						value = valueArray[0];

						// color the text on the left
						if (checked)
							color = colorText(value, color, fileIndex);

						// for extracted data on the right
						allChildren.push(value.toLowerCase());
					}

					// remove duplicate data in extracted data
					allChildren = $.unique(allChildren);
					for (var i = 0; i < allChildren.length; i++) {
						valueHTML += "<input type='checkbox' "
								+ checkedAttribute + "> " + allChildren[i]
								+ "<br/>"
					}
					metaValueCount = allChildren.length;
				} else {
					valueArray = studyData[key].split(":");
					value = valueArray[0];
					if (checked)
						color = colorText(value, color, fileIndex);

					valueHTML = "<input type='checkbox' " + checkedAttribute
							+ "> " + value + "<br/>"
				}

				var openPanel = "";
				if (changedKey == extractedKey)
					openPanel = " in "

				var extractedData = "<div class='panel panel-default'>"
						+ "<div class='panel-heading' role='tab' id='heading"
						+ extractedKey
						+ "'>"
						+ "<h4 class='panel-title'>"
						+ "<div class='checkbox-inline no_indent'>"
						+ "<span  style='background-color:"
						+ color
						+ "; height:10px; width:10px; border-radius:10px; float:left; position: absolute; margin-top:3%;'></span>"
						+ "<label>"
						+ "<input class='key-highlight' type='checkbox'  "
						+ checkedAttribute
						+ " value='"
						+ extractedKey
						+ "'>"
						+ "<a data-toggle='collapse' data-parent='#accordion' style='margin-left:20px;' href='#collapse"
						+ extractedKey
						+ "-"
						+ fileIndex
						+ "' aria-expanded='true' aria-controls='collapse"
						+ extractedKey
						+ "'>"
						+ extractedKey
						+ " ("
						+ metaValueCount
						+ ")</a>"
						+ "</label>"
						+ "</div>"
						+ "</h4> </div>  <div id='collapse"
						+ extractedKey
						+ "-"
						+ fileIndex
						+ "' class='panel-collapse collapse"
						+ openPanel
						+ "' role='tabpanel' aria-labelledby='headingOne'>"
						+ "<div class='panel-body'>"
						+ valueHTML
						+ "</div></div> </div>";
				ctakesHTML += extractedData;

			}

		}

	}
	$(".extractedPane" + fileIndex + " .extractedDataPanel").html(ctakesHTML);
	filesArray[fileIndex]["ctakesHTML"] = ctakesHTML;
	// if at the end, textToColor doesnt get filled up, we fill it with the
	// initial text.
	if (filesArray[fileIndex]["textToColor"] == "") {
		$("#file" + fileIndex + ".extracted-text").html(
				"<pre>" + filesArray[fileIndex]["studyText"] + "</pre>");
	}
}
