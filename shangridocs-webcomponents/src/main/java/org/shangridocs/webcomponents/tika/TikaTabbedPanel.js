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
			

			//to make upload image clickable inside dropzone
			$(".upload-img").click( function(){
				$(".dropzone").click();
			})

    	});
	}

};

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


$(".permTab").click( function(){
	//if permanent tab that allows uploading a new file is clicked.
	$(".fileList li").removeClass("active");
	$(this).addClass("active");
	$(".filesContent .tab-pane").removeClass("active");
	$("#introText").addClass("active");
	$(".right-pane").addClass("hide");
	$(".right-pane-default").removeClass("hide");
})

