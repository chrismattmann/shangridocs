<html  ng-app="shangrila" ng-controller="mainController" ng-init="init()">
<head>
	<title>Shangrila</title>

	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!-- INCLUDE REQUIRED THIRD PARTY LIBRARY JAVASCRIPT AND CSS -->
	<script type="text/javascript" src="resources/js/angular.min.js"></script>
	<link rel="stylesheet" href="resources/css/bootstrap.min.css">

	<!-- INCLUDE APPLICATION SPECIFIC CSS AND JAVASCRIPT -->
	<script type="text/javascript" src="resources/js/app.js"></script>
	<script type="text/javascript" src="resources/js/controllers/mainController.js"></script>
	<link rel="stylesheet" href="resources/css/main.css">
	<link rel="stylesheet" href="resources/css/dropzone.css">
</head>
<body>
	<nav class="navbar-default navbar-inverse">
	  <div class="container-fluid">
	    <!-- Brand and toggle get grouped for better mobile display -->
	    <div class="navbar-header">
	      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
	        <span class="sr-only">Toggle navigation</span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	      </button>
	      <ul class=" navbar-nav">
	      <li class="navbar-brand"><a href="http://54.153.2.23/red/" target="_blank">Celgene</a></li>
	      <li class="navbar-brand">></li>
	      <li class="active navbar-brand"><a href="#">Shangrila</a></li>
	      </ul>
	     
	    
	    </div>
	     <ul class="nav navbar-nav navbar-right">
	        	<li> <button class="btn btn-5 btn-5b icon-file"  data-toggle="modal" data-target="#fileModal"><span>New</span></button></li>
	       </ul>
		</div>
	</nav>

	<div class='container-fluid'>
		<div class="row">
	        <div class="col-md-8 border-right">
	        	<h1 class="text-center introduction">
	        		Shangri-La(Docs)
	        	</h1>
	        	<p class="introduction">
	        		Shangri-docs is a web application developed using AngularJS framework for automatically annotating a document based on medical and clinical domain.
	        	
	        		This is an advanced text reader tool that can take in data and extract important information out of it.
	        		It can take in PDFs, text files at the moment.
	        	</p>

	        	<div role="tabpanel" class="tabs hide">

  					<!-- Nav tabs -->
					<ul class="nav nav-tabs" role="tablist">
						<li role="presentation" class="active"><a href="#file1" aria-controls="home" role="tab" data-toggle="tab">File 1</a></li>
					</ul>

					<!-- Tab panes -->
					<div class="tab-content">
						<div role="tabpanel" class="tab-pane active extracted-text" id="file1"  data-container="body" data-toggle="popover" data-placement="bottom" data-content="test">
							
						</div>
					</div>
				</div>
	        	

	        </div>
	        <div class="col-md-4">
	        	

	       		<div class="col-md-12 extractedData">
       				<h4 class="text-center">Extracted Data</h4>
       				<div class="panel-group extractedDataPanel" id="accordion" role="tablist" aria-multiselectable="true"> 
       				</div>
	       		</div>
	       		<div class="col-md-12 searchResults">
       				<h4 class="text-center">Search Results</h4>
       				<div class="panel-group extractedSearchPanel" id="accordion" role="tablist" aria-multiselectable="true"> 
       					
       				</div>
	       		</div>
	        </div>
	    </div>
	</div>

	<div class="modal fade"  id="fileModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					<h4 class="modal-title">Upload a file to extract data</h4>
				</div>
				<div class="modal-body">
					<div class="text-center">
		        		<form action="http://54.153.2.23/celgene-shangrila/services/tika/rmeta/form" class="dropzone file-intake-area col-md-12 well " id="dropFileArea">
		        			<img src="resources/img/upload-3.png" class="upload-img" width="128" height="128"/><br/>
		    				<span>Maximum allowed filesize: 25MB</span><br/>
		    				<span>Allowed file formats: PDF, Txt, Doc, JPG</span><br/>
						</form>
		        	</div>
				</div>
				<div class="modal-footer">
				</div>
			</div><!-- /.modal-content -->
		</div><!-- /.modal-dialog -->
	</div><!-- /.modal -->

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
	<script src="resources/js/bootstrap.min.js"></script>
	<script src="resources/js/dropzone.js"></script>
	<script type="text/javascript">
	$(document).ready( function(){

		//to make remove uploadable files
		Dropzone.options.dropFileArea = {
			method:"put",
			init: function() {
		    	this.on("success", function(file, responseText) {
			      	// Handle the responseText here. For example, add the text to the preview element:
			      	this.removeAllFiles();
			      	$("#fileModal").modal("hide");
					fileContent = responseText[0]["X-TIKA:content"];

					//ui changes
					$(".introduction").hide();
					$(".tabs").removeClass("hide");
					$(".extracted-text").addClass("pdf-view");
					$(".extracted-text").html( fileContent);

					$(".extractedDataPanel").html( "<img class='loading-img' src='resources/img/load.gif'/>");

					$.ajax({
						headers: { 
					        'Content-Type': 'text/plain' 
					    },
						url:"http://54.153.2.23/celgene-shangrila/services/tika/ctakes", 
						method:"put",
						data: responseText[0]["X-TIKA:content"],
						success:function( result){
							ctakesData = result[0];
							showCtakesData( ctakesData);
						}
					})
			    });
			    this.on("complete", function(file) {
			      	// Handle the responseText here. For example, add the text to the preview element:
			      	$(".loading-img").remove();
			    });
	  		},
		};
		
		//to make upload image clickable inside dropzone
		$(".upload-img").click( function(){
			$(".dropzone").click();
		})

		//setting up the popup for Searchh
		var options = {
					    placement: 'bottom',
					    trigger: "manual",
					    html: "true"
			};
			$(".extracted-text").popover(options);
		
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
			
	});

function colorText( value, color){
	
	var randomHex = color;
	if( color == "")
		randomHex = '#'+Math.floor(Math.random()*16777215).toString(16);
	var textToColor = $(".extracted-text").html();
	splitText = textToColor.split( value);
	var coloredText = "";
	for( var i =0; i< splitText.length-1; i++)
	{
			
			coloredText += splitText[i] + "<span style='background-color:" + randomHex + "'>" + value + "</span>" + splitText[i+1];
	}
	if( coloredText == "")
		$(".extracted-text").html( textToColor);
	else
		$(".extracted-text").html( coloredText);

	return randomHex;

}

function showCtakesData( data ) {

	studyData = data;
	var value = valueHTML = "";
	for( var key in studyData){
		if( key.substring(0,7) == "ctakes:"){
					var color = "";
					extractedKey = key.replace("ctakes:","");

					 if( studyData[key].constructor === Array){

					 		valueHTML = "";
					 		for( var i=0; i< studyData[key].length; i++)
					 		{
					 			valueArray = studyData[key][i].split(":");
					 			value = valueArray[0];
					 			color = colorText( value, color);
					 			valueHTML += "<input type='checkbox' checked='true'> " + value + "<br/>"
					 		}
						}
				 	 else
				 	 {
		 	 			valueArray = studyData[key].split(":");
			 			value = valueArray[0];
					 	color = colorText( value, color);
			 			valueHTML = "<input type='checkbox' checked='true'> " + value + "<br/>"
				 	 }
			
		 var extractedData = "<div class='panel panel-default'>" +
								"<div class='panel-heading' role='tab' id='heading" + extractedKey + "'>"+
									"<h4 class='panel-title'>" +
									"<div class='checkbox-inline no_indent'>" +
								   "<span  style='background-color:" + color + "; height:10px; width:10px; border-radius:10px; float:left; margin-top:1%; margin-right:2%;'></span>" +
										"<label>" +
										"<input class='' type='checkbox' value='" + extractedKey + "'>" + 
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

	</script>
</body>
</html>