
$(".content").on("keyup", ".search", function(e){
    if(e.keyCode == 13)
    {
        searchSelectedText($(this).val() );
    }
});




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
			//alert('it works form func inside!');
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

//Getting search preferences from the config	
$.ajax({
	url:"./config/search-config.json", 
	headers:{"Content-Type":"application/json"},
	method:"GET",
	success:function( data){
		searchPreferences = data.services;
	}
});

function searchSelectedText(selectedText)
{
	//if no file has been uploaded or all files have been removed take the default right pane.
	
	var rightPaneClass = ".extractedPane" + openFileIndex;
	if( $(rightPaneClass).length == 0 ){
		rightPaneClass = ".right-pane";
	}
		
	
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
							$("#pubmedTitle .badge").html("<span class='badge'>"+ responseObjects.length +"</span>");
							
							var searchResultPub = "<ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							
							for (var i=0; i< responseObjects.length; i++) 
							{
								searchResultPub += "<ul class='pubMed'>";
								searchResultPub += "<li><a class='resultsTitle' href='#'>" +(i+1) +". "+ responseObjects[i]["title"] + "</a></li><li><strong>Author:</strong>" + responseObjects[i]["authors"]+".</li>"; 
								searchResultPub += 	responseObjects[i]["journalName"] + "</li>" +"<li><a class='results' href='#'>" + responseObjects[i]["pubtype"] + "</a></li></ul> <br />";
								
							}
								searchResultPub += "</li></ul></div>";
							$("#toggleDemo3").html("");
							$("#toggleDemo3").append( searchResultPub);
						},
						error: function( e){
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine1  + openFileIndex + "' data-toggle='tab'>" + currentEngine1 + " (Error)</a></li>");
							var searchResultPub = "<div role='tabpanel' class='tab-pane' id='" + currentEngine1  + openFileIndex + "'><ul class='searchList'>";
							
							//sometimes even correct results error out because response is a string instead of an object.
							searchResultPub += "<label> An error has occurred. Please refresh the page and try again.</label>";
							searchResultPub += "</ul></div>";
							$(".searchContent"+ openFileIndex).append( searchResultPub);
						}
					}).fail( function(){
						//Not adding a fail function here. If the search fails-> the search block will have blank title with 0 results.
					});
				}
				else if( engine == "StudySearch")
				{
					var currentEngine2 = engine;
					var urlPrefix = "";
					$.ajax({
						url : "./services/solr/config",
						method: "get",
						success:function(responseTxt){
							urlPrefix = responseTxt["resultUrlPrefix"];
						}
					});
					
					
					
					$.ajax({
						url: searchPreferences[engine]["restURL"] + selectedText, 
						method:"get",
						data: {},
						success:function( responseObjects){
							
							/// NEW VERSION
							
							responseObjects = responseObjects["response"]["docs"];
							$("#studySearch .badge").html("<span class='badge'>"+ responseObjects.length +"</span>");
							
							var searchResultPub = "<ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							
							for (var i=0; i< responseObjects.length; i++) 
							{
								searchResultPub += "<ul class='studySearch'>";
								searchResultPub += "<li><a class='resultsTitle' href='#'>" +(i+1) +". "+ responseObjects[i]["citation_title"] + "</a></li><li>" + responseObjects[i]["og_description"]+".</li></ul> <br />"; 
								//searchResultPub += 	responseObjects[i]["journalName"] + "</li>" +"<li><a class='results' href='#'>" + responseObjects[i]["pubtype"] + "</a></li></ul> <br />";
								
							}
								searchResultPub += "</li></ul></div>";
							$("#toggleDemo9").html("");
							$("#toggleDemo9").append( searchResultPub);
						
							
							}
					}).fail( function(){
						//Not adding a fail function here. If the search fails-> the search block will have blank title with 0 results.
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
							var wikiSearchLength = 0; searchResultWiki = "";
							
							var searchResultWiki = "<div style='margin-left: 2em; padding-bottom: 5px' class='nav nav-list'>";
							
							for (var key in responseObjects) {
								wikiSearchLength++;
								searchResultWiki += "<ul class='wikipedia'>" + "<lli><a class='resultsTitle' href='#'>"+(wikiSearchLength) +". " + key +"</a></li><li>" + 
								responseObjects[key]["desc"] + "</li><li>";
								sectionInfoLists = responseObjects[key]["sectionInfo"];
								subsectionLen = sectionInfoLists["sections"].length;
								for(var i = 0; i < sectionInfoLists["sections"].length; i++){
									
									if(sectionInfoLists["sections"][i]["level"] == "2"){
										searchResultWiki += "<a class='subResults' href='#'>"+sectionInfoLists["sections"][i]["line"]+",</a>";
									}
								}
								searchResultWiki += "</li><li><a class='results' href='"+responseObjects[key]["link"] +"'>View details...</a></li></ul><br />";
							}
							$("#wikiTitle .badge").html("<span class='badge'>"+ wikiSearchLength +"</span>");
							if($.isEmptyObject(responseObjects))
								searchResultWiki+= "No results";
							$("#toggleDemo4").html("");
							$("#toggleDemo4").append( searchResultWiki);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultWiki = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							searchResultWiki += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultWiki);

						}
					}).fail( function(){
						//Not adding a fail function here. If the search fails-> the search block will have blank title with 0 results.
					});
				}
				else if( engine == "GeneCard")
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
							
							var geneCardSearchLength = 0; geneCardResult = "";
							
							var searchResultGeneCard = "<br/><ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							
							for (var key in responseObjects) {
								geneCardSearchLength++;
								geneCardResult += "<ul class='geneCard'><li><strong>Symbol:</strong>"+responseObjects[key]["Symbol"] +"</li><li><strong>Description:</strong>" + 
								responseObjects[key]["Description"] + "</li><li><a class='results' href='#'>View Details...</a></li></ul> <br />";
							}
							$("#geneCard .badge").html("<span class='badge'>"+ geneCardSearchLength +"</span>");
							if($.isEmptyObject(responseObjects))
								geneCardResult+= "No results";
							else{
								geneCardResult+= "</li><li><a class='results' href='#'>GeneCard ResultsPage...</a></li></ul>"
							}
							$("#toggleDemo5").html("");
							$("#toggleDemo5").append( geneCardResult);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultWiki = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							searchResultWiki += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultWiki);

						}
					}).fail( function(){
						//Not adding a fail function here. If the search fails-> the search block will have blank title with 0 results.
					});
				}
				else if( engine == "StringDB")
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
							
							var stringDBSearchLength = 0; searchResultStringDB = "";
							
							var searchResultStringDB = "<ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							
							for (var key in responseObjects) {
								stringDBSearchLength++;
								searchResultStringDB += "<ul class='stringDB'><li><strong>Organism:</strong>"+responseObjects[key]["taxonName"] +"</li><li><strong>Description:</strong>" + 
								responseObjects[key]["description"] + "</li><li><a class='results' href='"+responseObjects[key]["link"]+"'>View Details...</a></li></ul> <br />";
							}
							$("#stringDB .badge").html("<span class='badge'>"+ stringDBSearchLength +"</span>");
							if($.isEmptyObject(responseObjects))
								searchResultStringDB+= "No results";
							else{
								searchResultStringDB+= "</li><li><a class='results' href='#'>View All Results...</a></li></ul>"
							}
							$("#toggleDemo6").html("");
							$("#toggleDemo6").append( searchResultStringDB);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultWiki = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							searchResultWiki += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultWiki);

						}
					}).fail( function(){
						//Not adding a fail function here. If the search fails-> the search block will have blank title with 0 results.
					});
				}
				else if( engine == "Uniprot")
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
							
							var uniprotSearchLength = 0; searchResultuniprot = "";
							
							var searchResultuniprot = "<ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							jsonResponseObj = responseObjects["Response"];
							for (var key in jsonResponseObj) {
								uniprotSearchLength++;
								searchResultuniprot += "<ul class='uniprot'><li><strong>Protein:</strong>"+jsonResponseObj[key]["Title"] +"</li><li><strong>Organism:</strong>" + 
								jsonResponseObj[key]["Properties"]["Organism"] + "</li><li><a class='results' href='"+jsonResponseObj[key]["Detail_link"]+"'>View Details...</a></li></ul> <br />";
							}
							$("#uniprot .badge").html("<span class='badge'>"+ uniprotSearchLength +"</span>");
							if($.isEmptyObject(responseObjects))
								searchResultuniprot+= "No results";
							else{
								searchResultuniprot+= "</li><li><a class='results' href='#'>View All Results...</a></li></ul>"
							}
							$("#toggleDemo8").html("");
							$("#toggleDemo8").append( searchResultuniprot);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultuniprot = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							searchResultuniprot += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultuniprot);

						}
					}).fail( function(){
						
					});
				}
				else if( engine == "Omim")
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
							
							var omimSearchLength = 0; omimSearchLength = "";
							
							var searchResultOmim = "<ul class='nav nav-list'><li style='list-style: none; display: inline'>";
							jsonResponseObj = responseObjects["Response"];
							for (var key in jsonResponseObj) {
								omimSearchLength++;
								searchResultOmim += "<ul class='omim'><li><strong>Title:</strong>"+jsonResponseObj[key]["Title"] +"</li><li><strong>Description:</strong>" + 
								jsonResponseObj[key]["Description"] + "</li><li><a class='results' href='"+jsonResponseObj[key]["Detail_link"]+"'>View Details...</a></li></ul> <br />";
							}
							$("#Omim .badge").html("<span class='badge'>"+ omimSearchLength +"</span>");
							if($.isEmptyObject(responseObjects))
								searchResultOmim+= "No results";
							else{
								searchResultOmim+= "</li><li><a class='results' href='#'>View All Results...</a></li></ul>"
							}
							$("#toggleDemo7").html("");
							$("#toggleDemo7").append( searchResultOmim);
						},
						error: function( e){
							//sometimes even correct results error out because response is a string instead of an object.
							$(".searchTab" + openFileIndex).append("<li role='presentation'><a href='#" + currentEngine3  + openFileIndex + "' data-toggle='tab'>" + currentEngine3 + "(Error)</a></li>");
							var searchResultuniprot = "<div role='tabpanel' class='tab-pane ' id='" + currentEngine3  + openFileIndex + "'><ul class='searchList'>";
							searchResultuniprot += "<label> Wikipedia results couldn't be fetched correctly. We are working on this issue.</label>";
							$(".searchContent" + openFileIndex).append( searchResultuniprot);

						}
					}).fail( function(){
						
					});
				}

			}
		}
	}
}



