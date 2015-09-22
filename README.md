# Shangridocs

<img src="https://github.com/chrismattmann/shangridocs/blob/convert-wicket/shangridocs-webapp/src/main/java/org/shangridocs/shangridocs_logo.gif" align="right" valign="top" width="175" height="150" />
<br/><br/><br/>

# Introduction
Shangridocs is a document exploration tool for the biomedical domain which takes inspiration
from [Utopiadocs](http://utopiadocs.com/) but provides the following killer features
 * A fully functional Java EE Web Application (.war) for deployment in application servers such as Apache Tomcat
 * Shangridocs consults many more biomedical data sources from which knowledge augmentation occurs and users can benefit

# Build Instructions
1. git clone -b convert-wicket https://github.com/chrismattmann/shangridocs
2. cd shangridocs
3. cd shangridocs-services && mvn install; cd ..
4. cd shangridocs-webcomponents && mvn install; cd ..
5. cd shangridocs-webapp && mvn install

# Starting instructions
Then, to start Shangridocs, you need:

## Apache Tika Server ##

1. cd shangridocs
2. mkdir tika
3. curl -kO http://repo1.maven.org/maven2/org/apache/tika/tika-server/1.10/tika-server-1.10.jar
4. java -jar tika-server-1.10.jar > tika-server.log 2>&1&

## Apache cTAKES Server ##

1. See guide for installing cTAKES and Tika here:
https://wiki.apache.org/tika/cTAKESParser
2. Start the server on port 9999

## Start Shangridocs web app in testing mode

1. cd shangridocs/shangridocs-webapp
2. mvn tomcat7:run

Now visit http://localhost:8181/shangridocs/ and you 
should see the web app!

# Deploying and Installing the Web Application

# Testing out the Services
# Query PubMed for IDs matching the text, “organ”
curl -X PUT -d "organ" -H "Content-type: text/plain" http://localhost:8181/shangridocs-services/services/pubmed/text

# Query PubMed for abstract info on those IDs
curl -X PUT -d "26368927,26368857,26368567,26368552,26368505,26368301,26368042,26368024,26367958,26367780,26367742,26367591,26367387,26367361,26367113,26367090,26367026,26366861,26366794,26366793" -H "Content-type: text/plain" http://localhost:8181/shangridocs-services/services/pubmed/ids

# License
Shangridocs source code and project is licensed and released under the [Apache License v2.0]() 

