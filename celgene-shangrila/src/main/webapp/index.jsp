	<html  ng-app="shangrila" ng-controller="mainController" ng-init="init()">

		<head>
			<title>Shangrila</title>

			<meta charset="utf-8">
			<meta http-equiv="X-UA-Compatible" content="IE=edge">
			<meta name="viewport" content="width=device-width, initial-scale=1">
			<?xml version="1.0"?>
				<!--
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
				-->
			<!-- INCLUDE REQUIRED THIRD PARTY LIBRARY JAVASCRIPT AND CSS -->
			<script type="text/javascript" src="resources/js/angular.min.js"></script>
			<link rel="stylesheet" href="resources/css/bootstrap.min.css">

			<!-- INCLUDE APPLICATION SPECIFIC CSS AND JAVASCRIPT -->
			<script type="text/javascript" src="resources/js/app.js"></script>
			<script type="text/javascript" src="resources/js/controllers/mainController.js"></script>
			<link rel="stylesheet" href="resources/css/main.css">
			<link rel="stylesheet" href="resources/css/dropzone.css">
			<link rel="stylesheet" href="resources/fonts/font-awesome/css/font-awesome.css">
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
						<li>
							<a href="#">
		                        <i class="fa fa-magnet fa-stack-2x text-primary shangrila-logo"></i>
		     				 </a>
						</li>
						<li class="active navbar-brand"><a href="#">Shangrila</a></li>
						<li class="navbar-brand"></li>
						<li class="navbar-brand"><a href="/red/" target="_blank">Home</a></li>
						
						</ul>
					</div>

			     	<ul class="nav navbar-nav navbar-right">
			        	<li> <button class="btn btn-5 btn-5b icon-file"  data-toggle="modal" data-target="#fileModal"><span>New</span></button></li>
			        </ul>
				</div>
			</nav>

			<div class="row content">
				<div class='container-fluid'>
			        <div class="col-md-8 border-right">
			        	<h1 class="text-center introduction">
			        		Shangri-La(Docs)
			        	</h1>
			        	<p class="introduction extracted-text">
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

			        <div class="col-md-4 right-pane">
		       			<h4 class="text-center">Extracted Data</h4>
		       			<h6 class="all-selection-option hide"><input type="checkbox" checked class="deselect-all-ctakes"> Select/Deselect All </h6>
			       		<div class="col-md-12 extractedData">
			       			<div class="panel-group extractedDataPanel" id="accordion" role="tablist" aria-multiselectable="true"> 
			       			</div>
			       		</div>
		       			<h4 class="text-center">Search Results</h4>
			       		<div class="col-md-12 searchResults">
		       				<div class="panel-group extractedSearchPanel" id="accordion" role="tablist" aria-multiselectable="true"> 
		       				</div>
			       		</div>
			        </div>
			    </div> <!-- container fluid ends here -->
			</div> <!-- row ends here -->
			<div class="row">
				<!-- Site footer -->
			    
			    <div class="bottom container-fluid">
			        
			            <div class="col-md-4">
			                <ul> <h5>Contact Us : <a href="mailto:chris.a.mattmann@nasa.gov" > Chris A Mattmann </a> or <a href="mailto:lintagliata@celgene.com" > Lauren Intagliata </a> </h5>
			                
			    			</ul>
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
				        		<form action="services/tika/rmeta/form" class="dropzone file-intake-area col-md-12 well " id="dropFileArea">
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

			<div class="hide loading-animation-code">
				<div class='loading-img'>
					<div class='loading-gif text-center'>
						<div class='bubblingG'>
							<span id='bubblingG_1'></span>
							<span id='bubblingG_2'></span>
							<span id='bubblingG_3'></span>
						</div>
					</div>
				</div>
			</div>

		    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
			<script src="resources/js/bootstrap.min.js"></script>
			<script src="resources/js/dropzone.js"></script>
			<script src="resources/js/main.js"></script>

		</body>

	</html>
