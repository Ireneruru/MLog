#!/bin/bash

java -Djava.library.path=/usr/local/lib -Xss128m -Xmx6000m -cp Beryl-1.0-jar-with-dependencies.jar org.antlr.v4.gui.TestRig DS3Lab.Beryl.parser.Beryl program $1
