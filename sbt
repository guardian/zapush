#!/bin/bash
# the "@" command line here forces sbt to use sbt.scalasources.boot.properties, which means that
# it will by default download scala sources too. See 
# http://stackoverflow.com/questions/3770125/how-do-i-get-sbt-to-use-a-local-maven-proxy-repository-nexus
# and
# http://code.google.com/p/simple-build-tool/wiki/GeneralizedLauncher#Configuration
java -Xmx512M -XX:MaxPermSize=250m \
       -XX:+UseConcMarkSweepGC \
       -XX:+CMSClassUnloadingEnabled \
       -XX:+UseCompressedOops \
	-Dzabbix.server=gnm40833.int.gnl \
	-Dzabbix.server.port=10051 \
	-Dzabbix.hostname=gnm40836 \
	-Dzabbix.appname=sbt \
	-jar `dirname $0`/sbt-launch-0.7.4.jar @sbt.scalasources.boot.properties "$@"


#             -XX:+PrintGCDetails \
#                -XX:+PrintGCDateStamps \
   
