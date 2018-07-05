#!/usr/bin/env bash

./gradlew jacocoTestReport --info

xdg-open build/reports/jacoco/test/html/index.html
open build/reports/jacoco/test/html/index.html
start "" build/reports/jacoco/test/html/index.html