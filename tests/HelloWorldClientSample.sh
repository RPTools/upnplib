#!/bin/bash
# ---------------------------------------------------------------------------
# Environment Variable Prequisites
#
#   JAVA_HOME       (Optional)The home of your java JVM
#
#   JAVA_OPTS      (Optional) The java JVM options
#
# ---------------------------------------------------------------------------

# Setup the JVM
if [ -z "$JAVA" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVA="$JAVA_HOME/jre/sh/java"
    else
      JAVA="$JAVA_HOME/bin/java"
    fi
  else
    JAVA=`which java 2> /dev/null `
    if [ -z "$JAVA" ] ; then
        JAVA=java
    fi
  fi
fi
                                                                                
if [ ! -x "$JAVA" ] ; then
  echo "JAVA_HOME is not set. Unexpected results may occur."
  echo "Set JAVA_HOME to the directory of your local JDK to avoid this message."
  exit 1
fi

if [ -z "$JAVA_OPTS" ] ; then
  JAVA_OPTS="-Xms64m -Xmx128m -Dnetworkaddress.cache.ttl=0"
  echo "setting java OPTS to $JAVA_OPTS"
fi

# Prog dir
# need for external launch via "."
TEST=`basename $0`
if expr $TEST : '.*bash.*' >/dev/null ;
	then PRG=`history | tail -1 | sed 's/^[ ]*[0-9]* [ ]*//' | sed 's/^source [ ]*//' | sed 's/^\. [ ]*//' | sed 's/ *$//'`;
	else PRG="$0";
fi;
# need this for relative symlinks
while [ -h "$PRG" ] ; do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '.*-> \(.*\)$'`
	if expr "$link" : '/.*' > /dev/null; then
		PRG="$link"
	else
		PRG=`dirname "$PRG"`"/$link"
	fi
done
DIR=`dirname "$PRG"`
cd "$DIR"

exec $JAVA $JAVA_OPTS -cp sbbi-bootstrap-1.0.jar net.sbbi.bootstrap.BootStrap HelloWorldClient
