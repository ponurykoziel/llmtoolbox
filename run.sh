#!/usr/bin/env bash
# Run the built uber-jar. Output is teed to run.log (gitignored).
#
# Local overrides (master key, pepper, allowed root, ports, ...) are layered in
# from dev.application.properties, which is gitignored. Create it first:
#
#     cp dev.application.properties.sample dev.application.properties
#
# Build first with ./build.sh.
set -euo pipefail

QUARKUS_CONFIG_LOCATIONS=dev.application.properties \
  java -jar target/llmtoolbox-*-runner.jar | tee run.log

# ── Alternative launch modes ─────────────────────────────────────────
#
# minimal.application.properties is kept in the repo as a bare working config
# (default credentials, 127.0.0.1:8080). It is what the docker example mounts.
# To use it instead of dev.application.properties:
#
#     QUARKUS_CONFIG_LOCATIONS=minimal.application.properties java -jar target/llmtoolbox-*-runner.jar
#
# run in screen
# honestly, screen is prefered over a docker; this is a host controlling tool with only few reasons to contenerize it.
#
# screen -dmS tools_for_my_llm sh -c 'QUARKUS_CONFIG_LOCATIONS=/absolute/path/application.properties java -jar /absolute/path/llmtoolbox-*-runner.jar'
#
# note that cron can launch the jar on @reboot, but this will work mostly for headless runs.
# if launched via cron, the jar will not reach your user's current session.
# your setup should provide startup applications manager.
#
# run in a docker
# the main reason for running this tool in a docker is restricting tool usage.
# Effectively, you trade mount codebase volume for network and host control.
# Note that docker will not inherit your timezone by default.
#
# for a dockered run, you want something like:
#
# CONTAINER="llmtoolbox"
# JARNAME="./target/llmtoolbox-*-runner.jar"
#
# docker stop ${CONTAINER}
# docker rm ${CONTAINER}
# docker run -d \
#     --name ${CONTAINER} \
#     -v "${JARNAME}:/app/app.jar" \
#     -v "./minimal.application.properties:/app/application.properties" \
#     -e QUARKUS_CONFIG_LOCATIONS=/app/application.properties \
# eclipse-temurin:21-jre \
#     java -jar /app/app.jar
