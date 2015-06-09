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
	      <li class="navbar-brand"><a href="#">Celgene</a></li>
	      <li class="navbar-brand">></li>
	      <li class="active navbar-brand"><a href="#">Shangrila</a></li>
	    </ul>
	    </div>
		</div>
	</nav>

	<div class='container-fluid'>
        <div class="col-md-8 border-right">
        	<h1 class="text-center">
        		Shangri-La(Docs)
        	</h1>
        	<p>
        		Shangri-docs is a web application developed using AngularJS framework for automatically annotating a document based on medical and clinical domain.
        	
        		This is an advanced text reader tool that can take in data and extract important information out of it.
        		It can take in PDFs, text files at the moment.
        	</p>
        	

        </div>
        <div class="col-md-4">
        	<div class="text-center col-md-12">
        		<button class="btn btn-5 btn-5b icon-file"  data-toggle="modal" data-target="#fileModal"><span>New</span></button>
       		</div>
       		<hr/>
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
		        		<form action="#" class="dropzone file-intake-area col-md-12 well " id="dropFileArea">
		  					<div class="fallback">
		    					<input name="file" type="file" multiple />
		  					</div>
		        			<img src="resources/img/upload-3.png" width="128" height="128"/><br/>
		    				<span>Maximum allowed filesize: 25MB</span><br/>
		    				<span>Allowed file formats: PDF, Txt, Doc, JPG</span>
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

		Dropzone.options.dropFileArea = {
			addRemoveLinks : true
		};
		
		$("")
	});
	</script>
</body>
</html>