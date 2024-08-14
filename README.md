http://ambit.sourceforge.net

This is multi module maven project.

#Build

Prerequisites for building AMBIT2
JDK 1.7 or higher 
MySQL 5.6.5 or higher 
Maven 3.x

## Build ambit2 libraries:  

Retrieve sources from SVN
````
svn checkout svn://svn.code.sf.net/p/ambit/code/trunk/ambit2-all ambit-all
cd ambit2-all
mvn package -DskipTests=true
````


### The build process includes mandatory database tests and may take a while.  Use `-DskipTests=true` option to skip the tests.

####Database configuration 

The test database `ambit-test` must exist before the running the tests. Use the following MySQL commands to create and set rights. 

````
create database `ambit-test` character set utf8;
GRANT ALL ON `ambit-test`.* TO 'guest'@'localhost' IDENTIFIED BY 'guest';
````

The database name and the user are set via Maven profile, e.g. there should be "settings.xml" file in your ".m2" directory with the following minimal configuration:


````xml
<settings xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <profiles>
   <profile>
      <id>ambit-build</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
	 
	<!-- the database name, default ambit2  -->
			<ambit.db>ambit2</ambit.db>

	<!-- database user password and name -->
        <ambit.db.user.test>guest</ambit.db.user.test>
        <ambit.db.user.test.password>guest</ambit.db.user.test.password>
         </properties>
    </profile>
  </profiles>
</settings>
````

## Build applications - AMBIT REST web services and standalone applications:

````
cd ambit2-apps
mvn clean buildnumber:create package -P http -P ambit-release -P aa-enabled -P aa-admin-disabled -P license-in-text-column -DskipTests=true
````
See ambit2-all/ambit2-apps/README.txt for options

### Dependencies

* The Toxtree dependeniies are available as Maven artifacts at http://ambit.uni-plovdiv.bg:8083/nexus/content/repositories/releases and 
https://nexus.ideaconsult.net/content/repositories/snapshots

* Optionally, to build Toxtree version on your own, get the source 

````
svn checkout http://svn.code.sf.net/p/toxtree/svn/trunk/toxtree toxtree 
mvn install
````
