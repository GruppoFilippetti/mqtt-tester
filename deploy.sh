#!/bin/bash

mvn -DskipTests clean package

scp target/mqtt-tester-1.0.1.jar smartpl01:/opt/smart-platform/mqtt-tester-1.0.1.jar
scp target/mqtt-tester-1.0.1.jar smartpl02:/opt/smart-platform/mqtt-tester-1.0.1.jar
scp target/mqtt-tester-1.0.1.jar smartpl07:/opt/smart-platform/mqtt-tester-1.0.1.jar
