#!/usr/bin/env sh

set -eux
brew install scalacenter/bloop/bloop
brew services start scalacenter/bloop/bloop

CWD=$(dirs -l)
coursier bootstrap \
    --repository sonatype:releases \
    --repository bintray:scalameta/maven \
    com.github.duhemm:bloopstrap_2.12:3c6e215a \
    -o bloopstrap-launcher
./bloopstrap-launcher "$CWD/bloopstrap.conf" "$CWD"
