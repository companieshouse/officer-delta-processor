#!/bin/bash
#
# Start script for officer-delta-processor

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "officer-delta-processor.jar"
