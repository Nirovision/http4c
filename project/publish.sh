#!/bin/bash
sbt -Dbintray.user=$BINTRAY_USER -Dbintray.pass=$BINTRAY_PASSWORD +publish
