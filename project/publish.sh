#!/bin/bash
mkdir -p ~/.bintray
eval "echo \"$(< ./project/bintray.template)\"" > ~/.bintray/.credentials
sbt publish