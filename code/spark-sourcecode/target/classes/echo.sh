#!/bin/bash

echo "Running shell script";
RESULT="";
while read LINE; do
RESULT=${RESULT}" "${LINE}
done

echo ${RESULT} > out123.txt