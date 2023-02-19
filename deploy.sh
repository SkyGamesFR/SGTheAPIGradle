#!/bin/bash

CURRENT_PATH=./
FILE=SGTheAPI.jar

JENKINS_SERVER=https://jenkins.skygames.fr
JOB="SGTheAPI"
JOB_QUERY=/job/${JOB}
LASTSUCCESS_QUERY=/lastSuccessfulBuild

LOCAL_BUILD_NUMBER_FILE=.jenkins_build_number

get_localBuildNumber() {
	if [ -f "$CURRENT_PATH/$LOCAL_BUILD_NUMBER_FILE" ]; then
		local LOCAL_BUILD_NUMBER=`cat ${CURRENT_PATH}/${LOCAL_BUILD_NUMBER_FILE}`
		echo "$LOCAL_BUILD_NUMBER"
	else
		echo "-1"
	fi
}

get_lastSuccessBuildNumber() {
	local BUILD_QUERY=/api/json
	local API_RESULT=`curl --silent ${JENKINS_SERVER}${JOB_QUERY}${LASTSUCCESS_QUERY}${BUILD_QUERY}`

	local BUILD_NUMBER=`echo ${API_RESULT} | sed -n 's/.*"id":\([\"0-9]*\),.*/\1/p' | head -1`
	echo $BUILD_NUMBER
}

set_localBuildNumber(){
	echo "$1" > "$CURRENT_PATH/$LOCAL_BUILD_NUMBER_FILE"
}

download_lastSuccessArtifact() {
	echo "Downloading JAR"
	if [ -f "$CURRENT_PATH/$FILE" ]; then
		rm $CURRENT_PATH/SGTheAPI.jar
	fi
    curl --silent ${JENKINS_SERVER}${JOB_QUERY}${LASTSUCCESS_QUERY}/artifact/target/SGTheAPI.jar -o ${CURRENT_PATH}/SGTheAPI.jar
}

if [ -f "$CURRENT_PATH/$FILE" ]; then
	echo "$FILE exist, update ?"

	BUILD_QUERY=/api/json
	API_RESULT=`curl --silent ${JENKINS_SERVER}${JOB_QUERY}${LASTSUCCESS_QUERY}${BUILD_QUERY}`

	echo "curl ok"

	LOCAL_BUILD_NB=$(get_localBuildNumber)
	echo $LOCAL_BUILD_NB
	LAST_BUILD_NB=$(get_lastSuccessBuildNumber)
	echo $LAST_BUILD_NB

	if [ "$LOCAL_BUILD_NB" != "$LAST_BUILD_NB" ]; then
		echo "SGTheAPI update needed"

		set_localBuildNumber "$LAST_BUILD_NB"
		download_lastSuccessArtifact

		echo "Nouvelle version de l'api déployée"
		exit
	else
		echo "No update needed"
	fi
else
	download_lastSuccessArtifact
	LAST_BUILD_NB=$(get_lastSuccessBuildNumber)
	echo $LAST_BUILD_NB
	set_localBuildNumber "$LAST_BUILD_NB"
fi
