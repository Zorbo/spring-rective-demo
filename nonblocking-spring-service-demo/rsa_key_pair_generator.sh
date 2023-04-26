#!/bin/sh

openssl genrsa -out privateKey.pem 4096
openssl pkcs8 -topk8 -inform pem -in privateKey.pem -outform der -nocrypt -out privateKey.der
openssl rsa -in privateKey.pem -out publicKey.der -pubout -outform DER
rm privateKey.pem
