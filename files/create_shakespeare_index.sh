#!/bin/bash

ES_HOST=$1
ES_PORT=$2

echo "elasticsearch server is $ES_HOST:$ES_PORT"

echo "downloading data from github"

wget https://raw.githubusercontent.com/zq2599/blog_demos/master/files/shakespeare_only_one_type.json.tar.gz

echo "downloading data success, uncompressing ..."

tar -zxf shakespeare_only_one_type.json.tar.gz

echo "creating index ..."

curl -X PUT \
http://${ES_HOST}:${ES_PORT}/shakespeare \
-H 'Accept: application/json' \
-H 'Content-Type: application/json' \
-H 'cache-control: no-cache' \
-d '{
      "mappings": {
      "line": {
            "properties": {
                "line_id": {
                    "type": "long"
                   },
                "line_number": {
                     "type": "keyword"
                },
               "play_name": {
                    "type": "keyword"
               },
               "speaker": {
                    "type": "keyword"
               },
               "speech_number": {
                    "type": "long"
               },
               "text_entry": {
                    "type": "text"
               }
          }
       }
     }
   }'

echo ""
echo "loading data ..."

curl -H 'Content-Type: application/x-ndjson'  -s -XPOST http://$ES_HOST:$ES_PORT/_bulk --data-binary @shakespeare_only_one_type.json > result.log

echo "finish"
