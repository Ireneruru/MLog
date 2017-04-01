#!/bin/bash

mvn clean
mvn assembly:assembly
cp target/Beryl-1.0-jar-with-dependencies.jar .
