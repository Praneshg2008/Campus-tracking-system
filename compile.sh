#!/bin/bash
# Compilation script for Campus Tracking System
set -e

echo "Compiling Java source files..."
mkdir -p bin
javac -cp "lib/mysql-connector-j-8.3.0.jar:src" -d bin src/campus/models/*.java src/campus/db/*.java src/campus/dao/*.java src/campus/Main.java

echo "Build successful! Binaries placed in 'bin/' directory."
