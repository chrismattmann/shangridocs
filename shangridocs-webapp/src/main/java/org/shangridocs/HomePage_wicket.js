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

});

//instantiating Dropzone plugin to upload files
var initFunc = function(){}
if (typeof Dropzone.options.dropFileArea != "undefined"){
	initFunc = Dropzone.options.dropFileArea["init"];	
}
var onSuccessFunc = function(file, responseText){
	//wait for 1 second before checking if extracted data for previously uplodaded file has come or not.
	var checkAjax = setInterval(cTAKESPanel(responseText, checkAjax), 1000);
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
