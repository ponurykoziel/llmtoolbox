QUARKUS_CONFIG_LOCATIONS=minimal.application.properties java -jar target/llmtoolbox-1.6.0-runner.jar

# run in screen
# honestly, screen is prefered over a docker; this is a host controlling tool with only few reasons to contenerize it.
#
# screen -dmS tools_for_my_llm sh -c 'QUARKUS_CONFIG_LOCATIONS=/absolute/path/application.properties java -jar /absolute/path/llmtoolbox-1.6.0-runner.jar'
#
# note that cron can launch the jar on @reboot, but this will work mostly for headless runs.
# if launched via cron, the jar will not reach your user's current session.
# your setup should provide startup applications manager.

# run in a docker
# the main reason for running this tool in a docker is restricting tool usage.
# Effectively, you trade mount codebase volume for network and host control.
# Note that docker will not inherit your timezone by default.
#
# for a dockered run, you want something like:
#
# CONTAINER="llmtoolbox"
# JARNAME="./target/llmtoolbox-1.6.0-runner.jar"
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

