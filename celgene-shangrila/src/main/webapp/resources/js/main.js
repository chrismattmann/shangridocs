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

$(document).ready( function(){

	//instantiating Dropzone plugin to upload files
	Dropzone.options.dropFileArea = {
		method:"put",
		init: function() {
			//handling success of file upload
	    	this.on("success", function(file, responseText) {
	    		//remove the file from the dropped zone before showing the received content
		      	this.removeAllFiles();
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
				//ui changes
				$(".introduction").hide();
				$(".tabs").removeClass("hide");
				$(".extracted-text").addClass("pdf-view");
				$(".extracted-text").html( "<pre>" + fileContent + "</pre>");

				$(".extractedDataPanel").html( $(".loading-animation-code").html() );

				$.ajax({
					headers: { 
				        'Content-Type': 'text/plain' 
				    },
					url:"http://54.153.2.23/celgene-shangrila/services/tika/ctakes", 
					method:"put",
					data: responseText[0]["X-TIKA:content"],
					success:function( result){
						$(".all-selection-option").removeClass("hide");
						$(".loading-img").remove();
						ctakesData = result[0];
						showCtakesData( ctakesData, []);
					}
				})
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

	//setting up the popup for Searchh
	$(".extracted-text").popover({
	    placement: 'bottom',
	    trigger: "manual",
	    html: "true"
	});
	
	//Showing popover for search when text is selected
	$(".extracted-text").mouseup( function( e){

		var selectedText = window.getSelection();
		if( selectedText != "")
		{
			$(".extracted-text").popover("show");
			$(".popover").offset({ top: e.pageY + 20, left: e.pageX-90});
			$(".popover-content").html( "<div class='btn-group'>" +
											"<input type='button' value='Search' class='searchSelected btn btn-primary'>" +
											"<input type='button' value='Copy' class='btn btn-primary'>" +
										"</div>"
									  );

			//sending Selected text to search component on the right.
			$(".searchSelected").click( function(){
				$(".extractedSearchPanel").html( "Searching for - " + "<label class='label-warning'>'" + window.getSelection() + "</label>");
			})
		}
		else
			$(".extracted-text").popover("hide");
	});

	//Handling event when checkbox is changed on an annotation
	$(".extractedDataPanel").on("change", ".key-highlight", function(){
		var changedKey = $(this).val();
		if( $(this).is(":checked"))
		{
			uncheckedKeys = $.grep( uncheckedKeys, function( value){
				return value != changedKey;
			})
		}
		else
			uncheckedKeys.push( changedKey);

		showCtakesData( ctakesData, uncheckedKeys);
	});

	//Handling event when user wants to select/deselect all annotations
	$(".deselect-all-ctakes").click( function(){
		uncheckedKeys = [];
		if( ! $(this).hasClass(".select-all-ctakes"))
		{
			for( var key in studyData)
			{
				if( key.substring(0,7) == "ctakes:"){
					uncheckedKeys.push( key.replace("ctakes:",""));
				}
			}
			$(this).addClass(".select-all-ctakes");
		}
		else
			$(this).removeClass(".select-all-ctakes");

		showCtakesData( studyData, uncheckedKeys);
	});
		
});

//add highlighting color to a value and update text on the left
function colorText( value, color){
	
	var selectedColor = color;
	if( color == "")
	{
		selectedColor = colorSwatch[swatchCounter];
		swatchCounter++;
	}
	//Our swatch color list of selected colors is limited. if ctakes list exceeds the limit, get a random color.
	if( selectedColor == undefined)
		selectedColor = '#'+Math.floor(Math.random()*16777215).toString(16);

	if( textToColor == "")
		textToColor = studyText;
	else
		textToColor = coloredText;

	splitText = textToColor.split( value);

	coloredText = splitText[0];
	for( var i =0; i< splitText.length-1; i++)
	{
		coloredText += 	"<span style='background-color:" + selectedColor + ";'>" + value + "</span>" + splitText[i+1];
	}
	$(".extracted-text").html( "<pre>" + coloredText + "</pre>");
	return selectedColor;

}

//create the annotation list from ctakes json.
function showCtakesData( data, uncheckedKeys ) {

	studyData = data;
	var value = valueHTML = "";
	//Every time function is called, swatch colors get reinstantiated.
	swatchCounter = 0; textToColor = "";
	//empty the ctakes list on the right to refill it again.
	$(".extractedDataPanel").html("");

	for( var key in studyData){
		if( key.substring(0,7) == "ctakes:"){
			var color = "";
			extractedKey = key.replace("ctakes:","");

			if( $.inArray(extractedKey, ignoredKeys) == -1)
			{
				var checked = false; checkedAttribute = "";

			 	if( $.inArray( extractedKey, uncheckedKeys) == -1)
			 	{
			 		checked = true;
			 		checkedAttribute = "checked = 'checked'";
			 	}
			 	else
			 		swatchCounter++;

				if( studyData[key].constructor === Array){

				 		valueHTML = "";
				 		for( var i=0; i< studyData[key].length; i++)
				 		{
				 			valueArray = studyData[key][i].split(":");
				 			value = valueArray[0];
				 			if( checked)
				 				color = colorText( value, color);
				 			valueHTML += "<input type='checkbox' " + checkedAttribute + "> " + value + "<br/>"
				 		}
				}
			 	else
			 	{
	 	 			valueArray = studyData[key].split(":");
		 			value = valueArray[0];
		 			if( checked)
				 		color = colorText( value, color);

		 			valueHTML = "<input type='checkbox' " + checkedAttribute + "> " + value + "<br/>"
			 	}

				var extractedData = "<div class='panel panel-default'>" +
									"<div class='panel-heading' role='tab' id='heading" + extractedKey + "'>"+
										"<h4 class='panel-title'>" +
										"<div class='checkbox-inline no_indent'>" +
									   "<span  style='background-color:" + color + "; height:10px; width:10px; border-radius:10px; float:left; margin-top:1%; margin-right:2%;'></span>" +
											"<label>" +
											"<input class='key-highlight' type='checkbox'  " + checkedAttribute + " value='" + extractedKey + "'>" + 
									   "<a data-toggle='collapse' data-parent='#accordion' href='#collapse" + extractedKey + "' aria-expanded='true' aria-controls='collapse" + extractedKey + "'>" + 
									   extractedKey + "</a>" +
									   "</label>" +
										"</div>" +
										"</h4> </div>  <div id='collapse" + extractedKey + "' class='panel-collapse collapse' role='tabpanel' aria-labelledby='headingOne'>"+
				"<div class='panel-body'>" + valueHTML + "</div></div> </div>";
				$(".extractedDataPanel").append( extractedData);

			}

		}

	}		
	//if at the end, textToColor doesnt get filled up, we fill it with the initial text.
	if( textToColor == "")
	{
		$(".extracted-text").html( "<pre>" + studyText + "</pre>");
	}
}
