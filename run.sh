#!/bin/bash
# Compile and run the Campus Tracking System
set -e
trap "rm -f src/*.class" EXIT

javac -cp "lib/mysql-connector.jar:src" src/*.java
java -cp "src:lib/mysql-connector.jar:." Main
