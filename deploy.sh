#!/bin/bash

echo "TRAVIS_BRANCH $TRAVIS_BRANCH"
echo "JAVA_HOME $JAVA_HOME"
echo "TRAVIS_EVENT_TYPE $TRAVIS_EVENT_TYPE"
echo "TRAVIS_JOB_NAME $TRAVIS_JOB_NAME"
echo "TRAVIS_PULL_REQUEST $TRAVIS_PULL_REQUEST"
echo "TRAVIS_BUILD_NUMBER $TRAVIS_BUILD_NUMBER"
echo "TRAVIS_BUILD_STAGE_NAME $TRAVIS_BUILD_STAGE_NAME"
echo "TRAVIS_JDK_VERSION $TRAVIS_JDK_VERSION"
echo "JDK_SWITCHER_DEFAULT $JDK_SWITCHER_DEFAULT"

if [[ -z "${GPG_SECRET_KEYS}" ]]; then
    echo "GPG_SECRET_KEYS environment variable not found "
else 
    #echo $GPG_SECRET_KEYS | base64 --decode | gpg --import
    echo "GPG_SECRET_KEYS environment variable found"
fi

if [[ -z "${GPG_OWNERTRUST}" ]]; then
    echo "GPG_OWNERTRUST environment variable not found"
else 
    #echo $GPG_OWNERTRUST | base64 --decode | gpg --import-ownertrust
    echo "GPG_OWNERTRUST environment variable found"
fi

defaultJdk=${JDK_SWITCHER_DEFAULT:-"undefined"}
travisJdk=${TRAVIS_JDK_VERSION:-"undefined"}

if [ "${defaultJdk}" == "undefined" ] 
then
echo "No JDK_SWITCHER_DEFAULT environment variable found"
exit 1
fi

if [ "${travisJdk}" == "undefined" ] 
then
echo "No TRAVIS_JDK_VERSION environment variable found"
exit 1
fi

if [ "${defaultJdk}" != "${travisJdk}" ]
then
    echo "Ignoring deploy for ${travisJdk}"
    exit 0
fi

# Use branch name, job name and event type to infer if release or a snapshot deploy
