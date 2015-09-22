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

	$(".content").on("keyup", ".search", function(e){
	    if(e.keyCode == 13)
	    {
	        searchSelectedText($(this).val() );
	    }
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

//instantiating Dropzone plugin to upload files
var initFunc = function(){}
if (typeof Dropzone.options != undefined){
	initFunc = Dropzone.options.dropFileArea["init"];	
}
var onSuccessFunc = function(file, responseText){
	//wait for 1 second before checking if extracted data for previously uplodaded file has come or not.
	var checkAjax = setInterval(cTAKESPanel(responseText), 1000);
}
Dropzone.options.dropFileArea = {
		init: initFunc,
		method: "put",
		success: onSuccessFunc
};


$(".start-tour").click( function(){

		// Start the tour
		tour.end();
		if( filesArray.length > 0)
			$(".permTab").click();
		tour.restart();
})

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