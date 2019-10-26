# HSAC-scheduler
[![Maven Central](https://img.shields.io/maven-central/v/nl.hsac/hsac-scheduler.svg?maxAge=86400)](https://mvnrepository.com/artifact/nl.hsac/hsac-scheduler)

Sample/boilerplate web application using [Quartz scheduler](http://www.quartz-scheduler.org).
It allows you to configure a set of http calls to made according to a schedule.

The calls to make and their schedule are configured in an XML file 
`src/main/resources/jobs.xml` using the [standard Quartz format](http://www.quartz-scheduler.org/documentation/quartz-2.x/cookbook/JobInitPlugin.html).

## Http Calls
The http calls to be made can be:
- GET
- POST with body in `jobs.xml`
- POST with body from file
- PUT with body in `jobs.xml`
- PUT with body from file

Http calls are made using [Apache http client](http://hc.apache.org/httpcomponents-client-ga/), 
default timeouts and connection management are configured in `scheduler.properties`. The timeouts can be overridden
on a per job basis.

## Job Status
The status of the jobs (what was the result of last http call) and when their next invocation is planned can be accessed
via '<app_url>/statusCheck'.

## Logging

The application log can be accessed via '<app_url>/logs/log'. 
The log levels can be controlled on a per job(-group) basis (each job has its own logger: 
`nl.hsac.scheduler.jobs.<job-group>.<job>`). The job/trigger details are made available as MDC variables.
Logging is configured by updating `logback.xml` (see [logback's configuration manual](https://logback.qos.ch/manual/configuration.html)).

## Running
To run the scheduler, deploy the 'WAR' to a Java servlet engine (e.g. [Jetty](https://eclipse.org/jetty/) or 
[Tomcat](http://tomcat.apache.org)).

The easiest way to run the scheduler (after cloning) is via Maven using `mvn jetty:run`, which will start a Jetty instance 
(listening on port 8080) running the scheduler. It can then be accessed on 
[http://localhost:8080/](http://localhost:8080/). 

## Running as Docker Container
The scheduler can also be run as docker container using image hsac/scheduler:<version>.
The configuration can then be overridden by overwriting the config files (the same as present in src/main/resources) in 
/jetty/webapps/ROOT/WEB-INF/classes.
