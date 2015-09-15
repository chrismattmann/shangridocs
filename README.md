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


# License
Shangridocs source code and project is licensed and released under the [Apache License v2.0]() 

