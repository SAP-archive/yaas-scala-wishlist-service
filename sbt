#!/bin/bash

sbtver=0.13.7
sbtjar=sbt-launch.jar
sbtsha128=b407b2a76ad72165f806ac7e7ea09132b951ef53

sbtrepo=http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch

if [ ! -f $sbtjar ]; then
  echo "downloading $sbtjar" 1>&2
  if ! wget --quiet -O  $sbtjar $sbtrepo/$sbtver/$sbtjar; then
    exit 1
  fi
fi

checksum=`openssl dgst -sha1 $sbtjar | awk '{ print $2 }'`
if [ "$checksum" != $sbtsha128 ]; then
  echo "bad $sbtjar.  delete $sbtjar and run $0 again."
  exit 1
fi

[ -f ~/.sbtconfig ] && . ~/.sbtconfig

if [ $JAVA_HOME ]
then
  JAVA_EXE=$JAVA_HOME/bin/java
else
  JAVA_EXE=java
fi

$JAVA_EXE -ea                     \
  $SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Djava.net.preferIPv4Stack=true \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -Xss8M                          \
  -Xms512M                        \
  -Xmx1G                          \
  -server                         \
  -jar $sbtjar "$@"
