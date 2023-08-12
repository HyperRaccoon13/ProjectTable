#!/bin/bash
version_grep=$(grep mod_version "gradle.properties")
minecraft_grep=$(grep minecraft_version "gradle.properties")
minecraft_version=${minecraft_grep#minecraft_version=}
version="${version_grep#mod_version=}"

build="${GITHUB_RUN_NUMBER:-local}"

echo "$version-$build+mc${minecraft_version}"
