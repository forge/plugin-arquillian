[![No Maintenance Intended](http://unmaintained.tech/badge.svg)](http://unmaintained.tech/)

# IMPORTANT

This plugin is for JBoss Forge 1.x and is not maintained anymore. Newer version can be found in this repository: https://github.com/forge/addon-arquillian

Installation
============
The Arquillian plugin is listed in the Forge plugin repository so installation is trivial. 
In Forge type: 
	
	forge install-plugin arquillian

That's it! The plugin will be downloaded and installed.


Setting up an Arquillian profile
==============
Arquillian supports testing in multiple containers. This is done by using a Maven profile for each container. This makes it possible to have multiple containers configured side-by-side too.
To add a new profile you use the arquillian setup command:

	arquillian setup
	
This command will prompt which container to use. Remember that you can just re-run the arquillian setup command to install an additional container profile.
For managed containers it might make sense to automatically download & install the container during the build to make installation on a build server more easy. This is only supported for AS7 at this moment.



Generating tests
================
Writing Arquillian tests is trivial, and is documented well: https://docs.jboss.org/author/display/ARQ/Reference+Guide

Forge can help you get started however. 
	
	arquillian create-test --class demo.CoolBean.java
	
Of course you can use the TAB key to navigate to the class that you want to test.


Exporting tests
================
Arquillian uses the Shrinkwrap API to create micro deployments. A micro deployment is an actual package (e.g. war file). In some cases it's convenient to access those packages directly. You can do this by exporting a Deployment. 

First navigate to the test class that you want to export, then use the arquillian export command to create the jar/war file. 

	cd src/test/java/demo/CoolBeanTest.java
	arquillian export

Configuring arquillian.xml
================
Containers can be configured in arquillian.xml (hostnames, ports etc.). Forge can help you do so.
Simply type:

    arquillian configure-container --profile [maven-profile-id]

Forge will list all possible configuration options for that specific container. All you have to do is set a value.
