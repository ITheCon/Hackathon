#!/usr/bin/env bash



while getopts r: option
do
case "${option}"
in
r) REPO=${OPTARG};;
esac
done

WRAPPER_PROPS_IN='install/gradle-wrapper.properties.template'
WRAPPER_PROPS_OUT='gradle/wrapper/gradle-wrapper.properties'

GRADLE_PROPS_IN='install/gradle.properties.template'
GRADLE_PROPS_OUT='gradle.properties'

function configure_default_access(){
  GRADLE_DIST_URL='https://services.gradle.org/distributions'
  sed -e 's|${gradle_distributions_url}|'"${GRADLE_DIST_URL}"'|'  ${WRAPPER_PROPS_IN}>${WRAPPER_PROPS_OUT}
  echo "Gradle will be installed from ${GRADLE_DIST_URL} and Maven will use maven central"
  if [ -e "gradle.properties" ]; then grep "mavenProxyUrl" ${GRADLE_PROPS_OUT} > ${GRADLE_PROPS_OUT}; fi;

}

function configure_proxy_access(){
  GRADLE_DIST_URL="${REPO}/gradle-distributions"
  sed -e 's|${gradle_distributions_url}|'"${GRADLE_DIST_URL}"'|'  ${WRAPPER_PROPS_IN}>${WRAPPER_PROPS_OUT}
  MAVEN_URL="${REPO}/maven-public/"
  sed -e 's|${maven_url}|'"${MAVEN_URL}"'|'  ${GRADLE_PROPS_IN}>${GRADLE_PROPS_OUT}
  echo "Gradle will be installed from ${GRADLE_DIST_URL} and Maven will be proxied to ${MAVEN_URL}"
}

if [ -z ${REPO+x} ]; then configure_default_access; else configure_proxy_access; fi

echo "REPO: ${REPO}"



