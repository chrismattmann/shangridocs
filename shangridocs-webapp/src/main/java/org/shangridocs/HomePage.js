/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/



//colors array for highlighting
var colorSwatch = ["E08283", "D2527F", "AEA8D3", "BF55EC", "81CFE0", "FDE3A7", "03C9A9", "87D37C", "D35400", "F27935", "ABB7B7"];
//highlighted text color counter
var swatchCounter = 0;
//globally defining variables that will be used everywhere.
var ctakesData = studyText = coloredText = textToColor = "";
//stores keys that users unchecks.
var uncheckedKeys = [];
//stores keys that currently need to be ignored while showing annotations.
var ignoredKeys = ["schema", "RomanNumeralAnnotation"];
//stores search preferences object from config file
var searchPreferences = "";
//filesArray that would store data for all uploaded files
var filesArray = [];
//variable that contains index of open file
var openFileIndex = 0;
//global variable to let one ctakes ajax call to finish before the next
var ajaxRunning = false;
//defining the tour object
var metaDataIgnoredKeys = ["X-TIKA:content"];
var tour = new Tour({
  steps: [
  {
    element: "#dropFileArea",
    title: "Upload a file",
    content: "You can drop a file in this space or browse from your computer to extract data.",
    placement: "top"
  },
  {
    element: "#extractedDataTour",
    title: "Annotations",
    content: "Find annotated data from the uploaded file here.",
    placement: "left"
  },
  {
    element: "#searchResultsTour",
    title: "Search",
    content: "Search content from Wikipedia, PubMed and StudySearch by entering your search words or select words from uploaded file to search.",
    placement: "left"
  }
]});

		

$(document).ready( function(){
	// Initialize the tour
	tour.init();

	//Let the height of of content div be minimum the height of the right pane so the footer does not come up.
	$(".content").css("min-height",$(".right-pane").height() + 150);

	//instantiating Dropzone plugin to upload files
	Dropzone.options.dropFileArea = {
		method:"put",
		init: function() {
			//handling success of file upload
	    	this.on("success", function(file, responseText) {
	    		//remove the file from the dropped zone before showing the received content
		      	this.removeAllFiles();
	    		//get current files counter
	    		openFileIndex = filesArray.length ? filesArray.length : 0;
	    		//add a new object to existing array of files.
	    		if( filesArray.length == 0)
				{
					//Remove introduction text.
					$(".intro-text").addClass("hide");
					//show header tabs for file names
					$(".fileList").removeClass("hide");
					//.permTab belongs to the div containing tab to add a file.
					$(".permTab").children(".active").removeClass("active");
				}
				$("#introText").removeClass("active");
				//create a new object for the newly uploaded file inside filesArray
	    		filesArray.push( {});
	    		//add a new header tab
				filesArray[openFileIndex]["metaData"] = responseText[0];
	    		$(".permTab").before( getFileTabHeaderHTML() );
	    		$(".details-" + openFileIndex).popover({
				    placement: 'bottom',
				    trigger: 'click',
				    html: "true"
				});
	    		//define what should happen on clicking this tab.
	    		$(".fileTitle" + openFileIndex).click( function(){
	    			openFileIndex = $(this).data("fileindex");
	    			//hide all other right pages and just open right pane for this file.
	    			$(".right-pane").addClass("hide");
	    			$(".extractedPane" + openFileIndex).removeClass("hide");
	    		});
	    		// add extracted file text on left.
	    		$(".filesContent").append( getFileTabContentHTML() );

	    		// add file name
	    		var fileName = "Untitled " + openFileIndex;
	    		if( responseText[0]["title"] != undefined)
	    			fileName = responseText[0]["title"];
	    		$(".fileTitle" + openFileIndex).append( fileName);

		      	//hide the upload modal
		      	$("#fileModal").modal("hide");
				fileContent = responseText[0]["X-TIKA:content"];
				//remove initial new lines from file content
				var regex = /^[a-z0-9]+$/i;
				var init = 0;
				for(  init=0; init<fileContent.length; init++)
				{
					if( fileContent[init].match( regex)){
						break;
					}
				}
				fileContent = fileContent.slice( init);
				//saving it globally for future use.
				studyText = fileContent;
				//if this is the first file upload

				filesArray[ openFileIndex]["studyText"] = studyText;
				$("#file" + openFileIndex + ".extracted-text").addClass("pdf-view");
				$("#file" + openFileIndex + ".extracted-text").html( "<pre>" + fileContent + "</pre>");


				var rightPane = $(".right-pane-default");
				rightPane.addClass("hide");
				var extractedDataPanel = rightPane.find(".extractedDataPanel");
				var tempFileIndex = openFileIndex;
				extractedDataPanel.html( $(".loading-animation-code").html() );
				$(".content > .container-fluid").append("<div class='col-md-4 right-pane extractedPane" + openFileIndex + "' data-fileindex='" + openFileIndex +"'>" + rightPane.html() + "</div>");
				extractedDataPanel.html( "");

				//wait for 1 second before checking if extracted data for previously uplodaded file has come or not.
				var checkAjax = setInterval( function(){
					
					if( ! ajaxRunning){

						ajaxRunning = true;
						$.ajax({
							headers: { 
						        'Content-Type': 'text/plain' 
						    },
							url:"services/tika/ctakes", 
							method:"put",
							data: responseText[0]["X-TIKA:content"],
							success:function( result){
								ctakesData = result[0];
								fileContent = ctakesData["X-TIKA:content"];
								
								//remove initial new line characters from returned XTIKA content.
								for(  init=0; init<fileContent.length; init++)
								{
									if( fileContent[init].match( regex))
										break;
								}
								fileContent = fileContent.slice( init);

								// check to find out extracted data belongs to which extracted text.
								// Currently, object for removed file is not removed. So, it is important to check if file object was removed.
								for(var tempFileIndex=0; tempFileIndex<filesArray.length; tempFileIndex++)
								{
									if( $.trim( filesArray[tempFileIndex]["studyText"]) == $.trim( fileContent) && typeof filesArray[tempFileIndex]["removed"] == "undefined" && typeof filesArray[tempFileIndex]["ctakesReturned"] == "undefined")
										break;
								}
								//set this for this file future use of above ^
								filesArray[tempFileIndex]["ctakesReturned"] = true;
								// make Select/Deselect option for extracted data options available.
								$(".extractedPane" + tempFileIndex + " .all-selection-option").removeClass("hide");

								filesArray[ tempFileIndex]["ctakesData"] = ctakesData;
								showCtakesData( ctakesData, tempFileIndex, []);
								//all should be unselected for the first time.
								$(".extractedPane" + tempFileIndex + " .deselect-all-ctakes").click();
								ajaxRunning = false;
								clearInterval( checkAjax);
							}
						}).fail( function(){
							$(".extractedPane" + tempFileIndex + " .extractedDataPanel").html( "<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
							ajaxRunning = false;	
							clearInterval( checkAjax);
						})
					}

				}, 1000);

		    });
		    this.on("complete", function(file) {
		      	// Handle the responseText here. For example, add the text to the preview element:
		    });
  		},
	};
		
	//to make upload image clickable inside dropzone
	$(".upload-img").click( function(){
		$(".dropzone").click();
	})
	
	//Showing popover for search when text is selected
	$(".filesContent").on( "mouseup", ".extracted-text", function( e){

		var selectedText = window.getSelection();
		if( selectedText.toString() != "")
		{
			var elem = $(this);
			elem.popover("show");
			$(".popover").offset({ top: e.pageY + 20, left: e.pageX-90});
			$(".popover-content").html( "<div class='btn-group'>" +
											"<input type='button' value='Search' class='searchSelected btn btn-primary'>" +
										"</div>"
									  );

			//sending Selected text to search component on the right.
			$(".searchSelected").click( function()
			{
				elem.popover("hide");
				$(".extractedPane" + openFileIndex + " .search").val(selectedText);
				
				searchSelectedText(selectedText.toString() ); 
			});
		}
		else{
			$(".extracted-text").popover("hide");
			$(".popover").hide();
			$(".filesContent").click();
		}

	});

	$(".permTab").click( function(){
		//if permanent tab that allows uploading a new file is clicked.
		$(".fileList li").removeClass("active");
		$(this).addClass("active");
		$(".filesContent .tab-pane").removeClass("active");
		$("#introText").addClass("active");
		$(".right-pane").addClass("hide");
		$(".right-pane-default").removeClass("hide");
	})

	$(".content").on("keyup", ".search", function(e){
	    if(e.keyCode == 13)
	    {
	        searchSelectedText($(this).val() );
	    }
	});

	//Handling event when checkbox is changed on an annotation
	$(".content").on("change", ".key-highlight", function(){
		var changedKey = $(this).val();
		if( $(this).is(":checked"))
		{
			filesArray[ openFileIndex]["uncheckedKeys"] = $.grep( filesArray[ openFileIndex]["uncheckedKeys"], function( value){
				return value != changedKey;
			})
		}
		else
			filesArray[ openFileIndex]["uncheckedKeys"].push( changedKey);

		//after remaking if this panel should stay open if it was currently open
		if( $("#collapse" + changedKey).hasClass("in"))
			showCtakesData( filesArray[ openFileIndex]["ctakesData"], openFileIndex, filesArray[ openFileIndex]["uncheckedKeys"], changedKey);
		else
			showCtakesData( filesArray[ openFileIndex]["ctakesData"], openFileIndex, filesArray[ openFileIndex]["uncheckedKeys"]);
	});

	//Handling event when user wants to select/deselect all annotations
	$(".content").on("click", ".deselect-all-ctakes", function(){
		fileIndex = $(this).parent().parent().data("fileindex");
		uncheckedKeys = [];
		filesArray[ fileIndex]["uncheckedKeys"] = [];

		if( ! $(this).hasClass(".select-all-ctakes"))
		{
			for( var key in filesArray[ fileIndex]["ctakesData"])
			{
				if( key.substring(0,7) == "ctakes:"){
					filesArray[ fileIndex]["uncheckedKeys"].push( key.replace("ctakes:",""));
				}
			}
			$(this).addClass(".select-all-ctakes");
		}
		else
			$(this).removeClass(".select-all-ctakes");

		showCtakesData( filesArray[ fileIndex]["ctakesData"], fileIndex, uncheckedKeys);
	});


	$(".content").on( "click", ".closeTab", function(){

		var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $(tabContentId).remove(); //remove respective tab content
       	fileIndex = $(this).data("fileindex");
       	$(".extractedPane" + fileIndex).remove();

       	//removing content from filesArray will confuse class and id names so filesArray is kept as it is
       	//assuming not many files will be loaded at the same time.
       	//Better solution required in the future.
       	filesArray[ fileIndex]["removed"] = true;
       	//open tab for uploading a new file by default.
       	$(".permTab").click();
	});

	//Getting search preferences from the config
	
	$.ajax({
		url:"search-config.json", 
		headers:{"Content-Type":"application/json"},
		method:"GET",
		success:function( data){
			searchPreferences = data.services;
		}
	});

});

$(".start-tour").click( function(){

		// Start the tour
		tour.end();
		if( filesArray.length > 0)
			$(".permTab").click();
		tour.restart();
})
//add highlighting color to a value and update text on the left
function colorText( value, color, fileIndex){
	
	var selectedColor = color;
	if( color == "")
	{
		selectedColor = colorSwatch[ filesArray[ fileIndex]["swatchCounter"] ];
		filesArray[ fileIndex]["swatchCounter"]++;
	}
	//Our swatch color list of selected colors is limited. if ctakes list exceeds the limit, get a random color.
	if( selectedColor == undefined)
		selectedColor = '#'+Math.floor(Math.random()*16777215).toString(16);

	if( filesArray[ fileIndex]["textToColor"] == "")
		filesArray[ fileIndex]["textToColor"] = filesArray[ openFileIndex]["studyText"];
	else
		filesArray[ fileIndex]["textToColor"] = filesArray[ fileIndex]["coloredText"];

	splitText = filesArray[ fileIndex]["textToColor"].split( value);

	var coloredText = splitText[0];
	for( var i =0; i< splitText.length-1; i++)
	{
		coloredText += 	"<span style='background-color:" + selectedColor + ";'>" + value + "</span>" + splitText[i+1];
	}
	$("#file" + fileIndex + ".extracted-text").html( "<pre>" + coloredText + "</pre>");
	filesArray[ fileIndex]["coloredText"] = coloredText;

	return selectedColor;

}

//create the annotation list from ctakes json.
function showCtakesData( data, fileIndex, uncheckedKeys, changedKey ) {

	var studyData = data;
	var value = valueHTML = ctakesHTML = "";
	//Every time function is called, swatch colors get reinstantiated.
	filesArray[ fileIndex]["swatchCounter"] = 0; filesArray[ fileIndex]["textToColor"] = "";
	//to remain in context to which key has been checked/unchecked
	changedKey = changedKey || null;

	for( var key in studyData){
		if( key.substring(0,7) == "ctakes:"){
			var color = "";

			extractedKey = key.replace("ctakes:","");

			//this array should contain all children inside this key.
			var allChildren = [];

			//checked if key has to be ignored.
			if( $.inArray(extractedKey, ignoredKeys) == -1)
			{
				var checked = false; checkedAttribute = "";

				//check for this key is currently checked.
			 	if( $.inArray( extractedKey, filesArray[ fileIndex]["uncheckedKeys"]) == -1)
			 	{
			 		checked = true;
			 		checkedAttribute = "checked = 'checked'";
			 	}
			 	else
			 		filesArray[ fileIndex]["swatchCounter"]++;

			 	var metaValueCount = 1;
				if( studyData[key].constructor === Array){

				 		valueHTML = "";
				 		metaValueCount = studyData[key].length;
				 		for( var i=0; i< metaValueCount; i++)
				 		{
				 			valueArray = studyData[key][i].split(":");
				 			value = valueArray[0];

				 			// color the text on the left
				 			if( checked)
				 				color = colorText( value, color, fileIndex);

				 			// for extracted data on the right
						    allChildren.push(value.toLowerCase() );
						}
						
						//remove duplicate data in extracted data
						allChildren = $.unique(allChildren);
						for( var i=0; i< allChildren.length; i++)
				 		{
				 			valueHTML += "<input type='checkbox' " + checkedAttribute + "> " + allChildren[i] + "<br/>"
						}
						metaValueCount = allChildren.length;
				}
			 	else
			 	{
	 	 			valueArray = studyData[key].split(":");
		 			value = valueArray[0];
		 			if( checked)
				 		color = colorText( value, color, fileIndex);

		 			valueHTML = "<input type='checkbox' " + checkedAttribute + "> " + value + "<br/>"
			 	}

				var openPanel = "";
				if( changedKey == extractedKey)
					openPanel = " in "

				var extractedData = "<div class='panel panel-default'>" +
									"<div class='panel-heading' role='tab' id='heading" + extractedKey + "'>"+
										"<h4 class='panel-title'>" +
										"<div class='checkbox-inline no_indent'>" +
									   "<span  style='background-color:" + color + "; height:10px; width:10px; border-radius:10px; float:left; position: absolute; margin-top:3%;'></span>" +
											"<label>" +
											"<input class='key-highlight' type='checkbox'  " + checkedAttribute + " value='" + extractedKey + "'>" + 
									   "<a data-toggle='collapse' data-parent='#accordion' style='margin-left:20px;' href='#collapse" + extractedKey + "-" + fileIndex + "' aria-expanded='true' aria-controls='collapse" + extractedKey + "'>" + 
									   extractedKey + " (" + metaValueCount + ")</a>" +
									   "</label>" +
										"</div>" +
										"</h4> </div>  <div id='collapse" + extractedKey + "-" + fileIndex + "' class='panel-collapse collapse" + openPanel + "' role='tabpanel' aria-labelledby='headingOne'>"+
				"<div class='panel-body'>" + valueHTML + "</div></div> </div>";
				ctakesHTML += extractedData;

			}

		}

	}		
	$(".extractedPane" + fileIndex + " .extractedDataPanel").html( ctakesHTML);
	filesArray[ fileIndex]["ctakesHTML"] = ctakesHTML;
	//if at the end, textToColor doesnt get filled up, we fill it with the initial text.
	if( filesArray[ fileIndex]["textToColor"] == "")
	{
		$("#file" + fileIndex + ".extracted-text").html( "<pre>" + filesArray[ fileIndex]["studyText"] + "</pre>");
	}
}

function getFileTabHeaderHTML(){
	$( ".fileList li" ).each( function( index, element ){
	    $(element).removeClass("active");
	    $(element).children(".active").removeClass("active");
	});
	
	activeClass = " active ";

	metaDataHTML = "<table class='table table-striped table-bordered table-condensed'>";
	for( var key in filesArray[openFileIndex]["metaData"]){
		if( $.inArray(key, metaDataIgnoredKeys) == -1 )
		{
			metaDataHTML += "<tr><td>" + key + "</td><td>" + filesArray[openFileIndex]["metaData"][ key] + "</td></tr>";
		}
	}
	metaDataHTML += "</table>";
	return "<li role='presentation' class='" + activeClass + "'><a href='#file" + openFileIndex + "' class='fileTitle" + openFileIndex + activeClass + "' data-fileindex='" + openFileIndex + "' role='tab' data-toggle='tab'><i class='fa fa-info-circle details-" + openFileIndex + "' data-toggle='popover' data-trigger='focus' title='MetaData'  data-container='body' data-placement='bottom' data-content=\"" + metaDataHTML + "\"></i>&nbsp;&nbsp;<button class='close closeTab' data-fileindex='" + openFileIndex + "' type='button'>x</button></a></li>";
}

function getFileTabContentHTML(){
	$(".extracted-text").removeClass("active");

	activeClass = " active ";
	return "<div role='tabpanel' class='tab-pane " + activeClass + " extracted-text' id='file" + openFileIndex + "' data-container='body' data-toggle='popover' data-trigger='focus' data-placement='bottom' data-content='Select text to search'></div>";

	//setting up the popup for Searchh
	$("#file" + openFileIndex).popover({
	    placement: 'bottom',
	    trigger: "manual",
	    html: "true"
	});
}

function searchSelectedText( selectedText)
{
	//if no file has been uploaded or all files have been removed take the default right pane.
	var rightPaneClass = ".extractedPane" + openFileIndex;
	if( $(rightPaneClass).length == 0 )
		rightPaneClass = ".right-pane";

	//add tabs header for searches in different search engines.
	if ($(".searchTab" + openFileIndex).length == 0){
		$( rightPaneClass + " .extractedSearchPanel").append( "<ul class='nav nav-tabs nav-justified searchTab" + openFileIndex + "'></ul>");
		$( rightPaneClass + " .extractedSearchPanel").append( "<div class='tab-content searchContent" + openFileIndex + "'></div>");
	}

	$(".searchTab" + openFileIndex).html("");
    $(".searchContent" + openFileIndex).html("");
	if ( searchPreferences != "") 
	{
		for (var engine in searchPreferences) 
		{
			if(searchPreferences[engine]["set"])
			{
				if( engine == "PubMed")
				{
					var currentEngine1 = engine;
					$.ajax({
						headers : {
							"Content-Type" : "text/plain"
						},
						url: searchPreferences[engine]["restURL"], 
						method:"put",
						data: selectedText,
						success:function( responseObjects){
							//$(".loading-img").remove();
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine1 + openFileIndex + "' data-toggle='tab'>" + currentEngine1 + " (" + responseObjects.length + ")</a></li>");
							
							var searchResultPub = "<div role='tabpanel' class='tab-pane' id='" + currentEngine1 + openFileIndex + "'><ul class='searchList'>";
							for (var i=0; i< responseObjects.length; i++) 
							{
							
								searchResultPub += "<li><a href=\"" + responseObjects[i]["url"] + "\" target='_blank'>" + responseObjects[i]["title"] + "</a></li><hr/>";
							}
							searchResultPub += "</ul></div>";
							$(".searchContent"+ openFileIndex).append( searchResultPub);
						},
						error: function( e){
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine1  + openFileIndex + "' data-toggle='tab'>" + currentEngine1 + " (Error)</a></li>");
							var searchResultPub = "<div role='tabpanel' class='tab-pane' id='" + currentEngine1  + openFileIndex + "'><ul class='searchList'>";
							
							//sometimes even correct results error out because response is a string instead of an object.
							/*
							responseObjects = $.parseJSON( e.responseText );
							for (var i=0; i< responseObjects.length; i++) 
							{
							
								searchResultPub += "<li><a href=\"" + responseObjects[i]["url"] + "\" target='_blank'>" + responseObjects[i]["title"] + "</a></li><hr/>";
							}
							*/
							searchResultPub += "<label> An error has occurred. Please refresh the page and try again.</label>";
							searchResultPub += "</ul></div>";
							$(".searchContent"+ openFileIndex).append( searchResultPub);
						}
					}).fail( function(){
						//$(".searchContent"+ openFileIndex).html( "<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
					});
				}
				else if( engine == "StudySearch")
				{
					var currentEngine2 = engine;
					$.ajax({
						url: searchPreferences[engine]["restURL"] + selectedText, 
						method:"get",
						data: {},
						success:function( responseObjects){
							//$(".loading-img").remove();
							responseObjects = $.parseJSON( responseObjects);
							responseObjects = responseObjects["response"]["docs"];
							$(".searchTab" + openFileIndex).append("<li role='presentation' class='active'><a href='#" + currentEngine2  + openFileIndex + "' data-toggle='tab'>" + currentEngine2 + " (" + responseObjects.length + ")</a></li>");
							var searchResultStudy = "<div role='tabpanel' class='tab-pane active' id='" + currentEngine2  + openFileIndex + "'><ul class='searchList'>";
							for (var i=0; i< responseObjects.length; i++) {
							
								searchResultStudy +="<li><a href=\"" + "../facetview/studyview/index.html?id=" + responseObjects[i]["id"] + "\" target='_blank'>" + responseObjects[i]["Combined_Study_Title"] + "</a></li><hr/>";
							}
							if( responseObjects.length == 0)
								searchResultStudy += "No results";
							searchResultStudy += "</ul></div>";

							$(".searchContent" + openFileIndex).append( searchResultStudy);
						}
					}).fail( function(){
						//$(".searchContent"+ openFileIndex).html( "<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
					});
				}
				else if( engine == "Wikipedia")
				{
					var currentEngine3 = engine;
					$.ajax({
						headers : {
							"Content-Type" : "text/plain"
						},
						url: searchPreferences[engine]["restURL"], 
						method:"put",
						data: selectedText,
						success:function( responseObjects){
							//$(".loading-img").remove();
							
							var wikiSearchLength = 0; searchResultWiki = "";
							for (var key in responseObjects) {
								wikiSearchLength++;
								searchResultWiki += "<li><a href=\"" + responseObjects[key]["link"] + "\" target='_blank'>" + key  + " - " + responseObjects[key]["desc"] + "</a></li><hr/>";
							}
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + " (" + wikiSearchLength + ")</a></li>");
							var searchResultWiki = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>" + searchResultWiki;
							
							if($.isEmptyObject(responseObjects))
								searchResultWiki+= "No results";
							searchResultWiki += "</ul></div>";
							$(".searchContent" + openFileIndex).append( searchResultWiki);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultWiki = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							
							/*
							responseObjects = $.parseJSON( e.responseText );
							for (var key in responseObjects) {
							
								searchResultWiki += "<li><a href=\"" + responseObjects[key]["link"] + "\" target='_blank'>" + key + "</a></li><hr/>";
							}
							*/
							searchResultWiki += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultWiki);

						}
					}).fail( function(){
						//$(".searchContent"+ openFileIndex).html( "<label class='alert alert-danger'> An error has occurred. Please refresh the page and try again.</label>");
					});
				}
			}
		}
	}
}