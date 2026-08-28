#!/usr/bin/env sh
set -eu

mkdir -p out/classes out/test-classes
javac -d out/classes src/*.java
javac -cp out/classes -d out/test-classes test/*.java
java -cp out/classes:out/test-classes HealthcareSystemTest

if [ "${1:-}" = "run" ]; then
    java -cp out/classes Main
fi
