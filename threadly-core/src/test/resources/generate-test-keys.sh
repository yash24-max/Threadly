#!/usr/bin/env bash
# Run this once to generate RSA keys used by integration tests.
# Keys are committed to the repo since they are test-only and carry no secret value.
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

openssl genrsa -out tmp-private-pkcs1.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -in tmp-private-pkcs1.pem -out test-private.pem -nocrypt
openssl rsa -in tmp-private-pkcs1.pem -pubout -out test-public.pem
rm tmp-private-pkcs1.pem
echo "Test RSA keys written to $DIR"
