#! /bin/sh

set -e

cat README.md \
    | sed '
        s_docs/index.md_https://github.com/un-nubivago/burning/tree/main/docs/index.md_g;'
