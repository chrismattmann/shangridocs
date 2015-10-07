# Shangridocs

<img src="https://github.com/chrismattmann/shangridocs/blob/convert-wicket/shangridocs-webapp/src/main/java/org/shangridocs/shangridocs_logo.gif" align="right" valign="top" width="175" height="150" />
<br/><br/><br/>

# Introduction
Shangridocs is a document exploration tool for the biomedical domain which takes inspiration
from [Utopiadocs](http://utopiadocs.com/) but provides the following killer features
 * A fully functional Java EE Web Application (.war) for deployment in application servers such as Apache Tomcat
 * Shangridocs consults many more biomedical data sources from which knowledge augmentation occurs and users can benefit
 * Leverages the powerful [Apache cTAKES](http://ctakes.apache.org/) technology for undertaking natural language processing and extraction of information from electronic medical record clinical free-text.
 * Maintains pluggable execution webservices e.g. [Apache Tika](http://tika.apache.org) + [cTAKES](http://ctakes.apache.org) or [Tika](http://tika.apache.org) + [Apache Spark](http://spark.apache.org) which run complete pipelines for  annotating clinical documents in plain text format using the built in UMLS (SNOMEDCT and RxNORM) dictionaries. 

# Prerequisites

1. A new [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. [Apache Maven](http://maven.apache.org)
3. [Git](https://git-scm.com/downloads) 
4. [SBT](http://www.scala-sbt.org/download.html)

# Build Instructions

Firts lets get Shangridocs:
```
$ git clone https://github.com/chrismattmann/shangridocs
$ cd shangridocs
$ mvn install
```

# Starting instructions
Then, to start Shangridocs, you need the following:

## Apache Tika Server ##

The [Tika REST Server](http://wiki.apache.org/tika/TikaJAXRS) is used to extract text from the documents we upload and view within Shangridocs.

```
$ cd shangridocs
$ mkdir -p tika/server
$ mkdir -p tika/ctakes
$ cd tika/server
$ git clone https://github.com/apache/tika.git
$ cd tika && mvn install
$ java -jar tika-server/target/tika-server-1.11-SNAPSHOT.jar > ../tika-server.log 2>&1&
```
This starts Tika Server on http://localhost:9998

## Spark JobServer ##

[Spark JobServer](https://github.com/lewismc/spark-jobserver) is a REST job server for Apache Spark. We've made some it is used to execute annotation pipelines over clinical documents:

```
$ git clone -b shangridocs https://github.com/lewismc/spark-jobserver.git
$ cd spark-jobserver 
$ sbt assembly
$ sbt
$ reStart
```
This produces a server-side 'uber-jar' including all of the cTAKES dependencies required to run the ctakes-clinical-pipeline over data fed into Shangridocs.
It also starts the spark-jobserver REST service and provides an administration webapp on http://localhost:8090

## Start Shangridocs web app in testing mode
```
$ cd shangridocs/shangridocs-webapp
$ mvn tomcat7:run
```
Now visit http://localhost:8181/shangridocs/ and you should see the Shangridocs web app!

# Deploying and Installing the Web Application

# Testing out the Services
# Query PubMed for IDs matching the text, “organ”
curl -X PUT -d "organ" -H "Content-type: text/plain" http://localhost:8181/shangridocs/services/pubmed/text

# Query PubMed for abstract info on those IDs
curl -X PUT -d "26368927,26368857,26368567,26368552,26368505,26368301,26368042,26368024,26367958,26367780,26367742,26367591,26367387,26367361,26367113,26367090,26367026,26366861,26366794,26366793" -H "Content-type: text/plain" http://localhost:8181/shangridocs/services/pubmed/ids

# License
Shangridocs source code and project is licensed and released under the [Apache License v2.0]() 

