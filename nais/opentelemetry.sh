#!/usr/bin/env bash

if [ ! -z "${OTEL_EXPORTER_OTLP_ENDPOINT}" ]; then
    JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
    export JAVA_TOOL_OPTIONS
fi