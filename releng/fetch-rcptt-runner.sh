#!/bin/bash -eu

###
### Utility script to prefetch the RCPTT Runner from a mirror different
### than the default location because it can be very slow (> 10 min)
### and make the continuous integration builds fail (timeout)
###
### Note that the projects will build without running this script
###

TMP=$1
M2DIR=${TMP:-$HOME/.m2}

echo $M2DIR

DIR=$(cd `dirname $0` && echo `git rev-parse --show-toplevel`)

MIRROR=$(cd ${DIR} && cat pom.xml | grep -o "<eclipse.mirror>\(.*\)</eclipse.mirror>" | sed 's#<eclipse.mirror>\(.*\)</eclipse.mirror>#\1#g')
RCPTTVER=$(cd ${DIR} && cat pom.xml | grep -o "<rcptt-runner-version>\(.*\)</rcptt-runner-version>" | sed 's#<rcptt-runner-version>\(.*\)</rcptt-runner-version>#\1#g')

# test if rcptt runner is already installed (in default M2 repo)
if [ ! -e "${M2DIR}/repository/org/eclipse/rcptt/runner/rcptt.runner/${RCPTTVER}/rcptt.runner-${RCPTTVER}.zip" ]; then

  URLVER=$(echo ${RCPTTVER} | sed -e 's#-#/#g')
  wget -nv ${MIRROR}/rcptt/release/${URLVER}/runner/rcptt.runner-${RCPTTVER}.zip -O rcptt.runner-${RCPTTVER}.zip
  mvn -Dtycho.mode=maven install:install-file -Dfile=rcptt.runner-${RCPTTVER}.zip -DgroupId=org.eclipse.rcptt.runner -DartifactId=rcptt.runner -Dversion=${RCPTTVER} -Dpackaging=zip -Dmaven.repo.local=${M2DIR}
  rm rcptt.runner-${RCPTTVER}.zip
fi

