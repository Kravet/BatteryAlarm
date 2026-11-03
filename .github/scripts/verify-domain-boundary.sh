#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

compile_dependencies="$(./gradlew --no-daemon --quiet :app:dependencies --configuration debugCompileClasspath)"

for required_project in ':domain-api' ':domain-di'; do
    if ! grep -Fq "project $required_project" <<< "$compile_dependencies"; then
        echo "Missing required app compile dependency: $required_project" >&2
        exit 1
    fi
done

for forbidden_project in ':domain-impl' ':domain'; do
    if grep -Eq "project ${forbidden_project}([[:space:]]|$)" <<< "$compile_dependencies"; then
        echo "Forbidden app compile dependency: $forbidden_project" >&2
        exit 1
    fi
done

echo 'Domain compile boundary verified: app sees :domain-api and :domain-di, not implementation.'
