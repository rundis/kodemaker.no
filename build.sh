#!/bin/bash

function format-date() {
  timestamp=$1
  format="$2"
  res=$(date -d @$timestamp +"$format" 2> /dev/null)

  if [ $? -eq 0 ]; then
    echo $res
  else
    date -r $timestamp -u +"$format"
  fi
}

role_creds=$(aws sts assume-role \
                 --role-arn "arn:aws:iam::195221715009:role/Deployer" \
                 --role-session-name DeployKodemakerWeb)

prod-env() {
  echo "AWS_ACCESS_KEY_ID=$(echo "$role_creds" | jq -cr .Credentials.AccessKeyId)"
  echo "AWS_SECRET_ACCESS_KEY=$(echo "$role_creds" | jq -cr .Credentials.SecretAccessKey)"
  echo "AWS_SESSION_TOKEN=$(echo "$role_creds" | jq -cr .Credentials.SessionToken)"
}

bucket="s3://kodemaker-www/"
target="build"

echo "Syncing down production site for accurate build diffs"
mkdir -p "$target"
pushd "$target" > /dev/null
env $(prod-env) aws s3 sync $bucket .
popd > /dev/null

echo "Building site"
diffs=$(lein build-site :json)

echo "Generating PDFs"
$(aws ecr get-login --region eu-west-1 --no-include-email)
docker run --rm -v $(cd $(dirname $0)/build && pwd):/site 575778107697.dkr.ecr.eu-west-1.amazonaws.com/html2pdf:130bd62355 /site/cv /site

echo "Syncing assets, cacheable for a year"
pushd "$target" > /dev/null

ts=$(date -d "+1 year" +%s 2> /dev/null)

if [ $? -ne 0 ]; then
  ts=$(date -v +1y +%s)
fi

expires="$(format-date $ts "%a, %d %b %Y %H:%M:%S GMT")"
env $(prod-env) aws s3 sync . $bucket --expires "$expires" --exclude "*" --include "assets/*"

echo "Syncing remaining files and deleting removed files"
env $(prod-env) aws s3 sync . $bucket --delete --exclude "assets/*"

echo "Purging old assets"
env $(prod-env) aws s3 sync . $bucket --expires "$expires" --exclude "*" --include "assets/*" --delete

changed=$(for file in $(echo "${diffs}" | jq -cr '.changed[]'); do
            echo ${file/"index.html"/""}
            echo $file
          done)

removed=$(for file in $(echo "${diffs}" | jq -cr '.removed[]'); do
            echo ${file/"index.html"/""}
            echo $file
          done)

if [ "$changed$removed" != "" ]; then
  paths=$(echo "$changed $removed")
  echo "Purging Cloudfront caches for $paths"
  env $(prod-env) aws cloudfront create-invalidation --distribution-id E377BQUYES9DH7 --paths $paths
fi
