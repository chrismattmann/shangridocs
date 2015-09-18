# Shangridocs

<img src="https://github.com/darth-pr/shangrila/blob/master/doc/logo.png" align="right" width="300" />

# Introduction
Shangridocs is a document exploration tool for the biomedical domain which takes inspiration
from [Utopiadocs](http://utopiadocs.com/) but provides the following killer features
 * A fully functional Java EE Web Application (.war) for deployment in application servers such as Apache Tomcat
 * Shangridocs consults many more biomedical data sources from which knowledge augmentation occurs and users can benefit
 
# Build and Install
To build you require [Apache Maven](https://maven.apache.org/), the Maven package will most likely 
be available through your OS package manager so check there first before installing from the site.
Once you have Maven, generating the web application deployment arifact is very easy
```
$ cd shangrila/celgene-shangrila
$ mvn install
```
The artifact can then be located in the **/target** directory as follows
```
$ cd target
$ ls 
celgene-shangrila	celgene-shangrila.war	classes			m2e-wtp			maven-archiver		test-classes
```

# Deploying and Installing the Web Application

# Testing out the Services
# Query PubMed for IDs matching the text, “organ”
curl -X PUT -d "organ" -H "Content-type: text/plain" http://localhost:8181/shangridocs-services/services/pubmed/text

# Query PubMed for abstract info on those IDs
curl -X PUT -d "26368927,26368857,26368567,26368552,26368505,26368301,26368042,26368024,26367958,26367780,26367742,26367591,26367387,26367361,26367113,26367090,26367026,26366861,26366794,26366793" -H "Content-type: text/plain" http://localhost:8181/shangridocs-services/services/pubmed/ids

# License
Shangridocs source code and project is licensed and released under the [Apache License v2.0]() 

