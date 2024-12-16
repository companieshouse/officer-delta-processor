#!/bin/bash
#
# Start script for officer-delta-processor

PORT=8080
exec java -jar -Dserver.port="${PORT}" "officer-delta-processor.jar"
