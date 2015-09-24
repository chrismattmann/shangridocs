
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
							//$(".loading-img").remove();
							//responseObjects = $.parseJSON( responseObjects);
							responseObjects = responseObjects["response"]["docs"];
							$(".searchTab" + openFileIndex).append("<li role='presentation' class='active'><a href='#" + currentEngine2  + openFileIndex + "' data-toggle='tab'>" + currentEngine2 + " (" + responseObjects.length + ")</a></li>");
							var searchResultStudy = "<div role='tabpanel' class='tab-pane active' id='" + currentEngine2  + openFileIndex + "'><ul class='searchList'>";
							for (var i=0; i< responseObjects.length; i++) {
							
								searchResultStudy +="<li><a href=\"" + urlPrefix + "?id=" + responseObjects[i]["id"] + "\" target='_blank'>" + responseObjects[i]["Combined_Study_Title"] + "</a></li><hr/>";
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