#!/usr/bin/env bash

set -e

__change_directory() {
  if [[ -d ${PACKAGE_NAME} ]]; then
      echo "Navigate to ${PACKAGE_NAME}"
      cd ${PACKAGE_NAME}/component
  fi
}

__build_project() {
  echo "---> Build the project ..."
  mkdir -p dist
  mvn clean install -q
  PACKAGE_NAME=$(basename `PWD`)
  TARGET_FILE="target/${PACKAGE_NAME}-1.0-SNAPSHOT.jar"
  if [ ! "$?" -eq 0 ]; then
    echo "ERROR : ❌ > THE JAR CAN'T BE GENERATED‼️"
    exit 1
  else
    echo "✅   $TARGET_FILE was successfully generated"
    cp "${TARGET_FILE}" dist/"${PACKAGE_NAME}".jar
  fi
}

## Publish
__copy_delivery_package() {
  echo "---> Copy the delivery package for the deployment ..."
  PACKAGE_NAME=$(basename `PWD`)
  S3_URL_PACKAGE="${S3_BUCKET}/${PACKAGE_NAME}"
  echo "${S3_URL_PACKAGE}"
  aws s3 sync ${AWS_OPTIONS} ./dist "${S3_URL_PACKAGE}"
}

__create_lambda_function() {
  PACKAGE_NAME=$(basename `PWD`)
  S3Key=${PACKAGE_NAME}/"${PACKAGE_NAME}".jar

  aws lambda create-function ${AWS_OPTIONS} \
    --function-name ${PROJECT_NAME}-${PACKAGE_NAME} \
    --runtime python3.7 \
    --code S3Bucket=${S3_BUCKET},S3Key=${S3Key} \
    --handler app.lambda_handler \
    --role ${IAM_ROLE}
}

__update_lambda_function() {

  S3Key=${PACKAGE_NAME}/"${PACKAGE_NAME}".jar

  aws lambda update-function-code ${AWS_OPTIONS} \
    --function-name ${PROJECT_NAME}-${PACKAGE_NAME} \
    --s3-bucket ${S3_BUCKET} \
    --s3-key ${S3Key}

}

### Main ###
__usage() {
  echo "Usage: $0 [ --build ] [ --deploy ] [ --publish ] [ --update ] [--clean]" 1>&2
  exit 1
}

if [[ $# -eq 0 ]]
then
  __usage
  exit 1
fi

OPTION=$1
while [[ -n ${OPTION} ]];
do
  case "${OPTION}" in
    --build | -b)
      echo "--> Building ..."
      __build_project
      break
      ;;
    --publish | -p)
      echo "--> Publishing ..."
      __copy_delivery_package
      break
      ;;
    --update | -u)
      echo "--> Update function code ..."
      __build_project
      cd .. && __copy_delivery_package
      __update_lambda_function
      break
      ;;
    --clean | -c)
      echo "--> Clean up ..."
      __delete_directories
      break
      ;;
    *)
      echo "Unknown option: ${OPTION}"
      exit 1
      ;;
  esac
done