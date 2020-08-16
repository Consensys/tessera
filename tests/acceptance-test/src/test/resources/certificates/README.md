## Insecure certificates for development

### Overview

**These certs should not be used in production - they are intended for dev/test use.**

These certs allow testing of client/server TLS communication in a variety of situations to save having to create new certs each time.  Examples include: testing Tessera TLS-enabled servers, and testing TLS communication between Quorum/Tessera and a Hashicorp Vault server.   

The certificates have been generated using this "recipe": https://jamielinux.com/docs/openssl-certificate-authority/introduction.html

The `server-*` certs are flexible, and can be used with `localhost` servers as well as numerous additional hostnames & IPs.  This was achieved by defining additional SANs in `intermediate/openssl.cnf` (created as part of the linked guide) - see Appendix for complete list of SANs defined.

### Files

The certs are provided in a variety of formats.

#### .pem
| File | Description |
| --- | --- |
| ca-root.cert.pem | Self-signed CA cert |
| ca-intermediate.cert.pem | Intermediate CA cert |
| ca-chain.cert.pem | Intermediate + root CA certs |
| server-localhost-with-san.cert.pem | Server cert with lots of SANs |
| server-localhost-with-san-ca-chain.cert.pem | Server cert + intermediate + root CA certs |
| server-localhost-with-san.key.pem | Server cert private key (no password) |
| client.cert.pem | Client cert |
| client-ca-chain.cert.pem | Client cert + intermediate + root CA certs |
| client.key.pem | Client cert private key (no password) |

#### .p12
Created using `openssl`.  All secured with password `testtest`:

| File | Description |
| --- | --- |
| server-localhost-with-san.p12 | PKCS 12 bundle of<br><ul><li>server-localhost-with-san-ca-chain.cert.pem</li><li>server-localhost-with-san.key.pem</li></ul> |
| client.p12 | PKCS 12 bundle of <br><ul><li>client-ca-chain.cert.pem</li><li>client.key.pem</li></ul>  |

#### .jks
Created from `.p12` (except truststore.jks) using `keytool`.  All secured with password `testtest`:

| File | Description |
| --- | --- |
| server-localhost-with-san.jks | Java KeyStore bundle of<br><ul><li>server-localhost-with-san-ca-chain.cert.pem</li><li>server-localhost-with-san.key.pem</li></ul> |
| client.jks | Java KeyStore bundle of <br><ul><li>client-ca-chain.cert.pem</li><li>client.key.pem</li></ul>  |
| truststore.jks | Java KeyStore bundle of <br><ul><li>ca-root.cert.pem</li></ul>  |

### Appendix
#### SANs
The following was added to the `server_cert` section of `intermediate/openssl.cnf`:
``` 
[ server_cert ]
...
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = localhost
DNS.2 = localhost.localdomain
DNS.3 = quorum1.quorum.net
DNS.4 = quorum2.quorum.net
DNS.5 = quorum3.quorum.net
DNS.6 = quorum4.quorum.net
DNS.7 = quorum5.quorum.net
DNS.8 = quorum6.quorum.net
DNS.9 = quorum7.quorum.net
DNS.10 = quorum8.quorum.net
DNS.11 = quorum9.quorum.net
DNS.12 = quorum10.quorum.net
DNS.13 = tessera1.quorum.net
DNS.14 = tessera2.quorum.net
DNS.15 = tessera3.quorum.net
DNS.16 = tessera4.quorum.net
DNS.17 = tessera5.quorum.net
DNS.18 = tessera6.quorum.net
DNS.19 = tessera7.quorum.net
DNS.20 = tessera8.quorum.net
DNS.21 = tessera9.quorum.net
DNS.22 = tessera10.quorum.net
DNS.23 = node1
DNS.24 = node2
DNS.25 = node3
DNS.26 = node4
DNS.27 = node5
DNS.28 = node6
DNS.29 = node7
DNS.30 = txmanager1
DNS.31 = txmanager2
DNS.32 = txmanager3
DNS.33 = txmanager4
DNS.34 = txmanager5
DNS.35 = txmanager6
DNS.36 = txmanager7
IP.1 = 127.0.0.1
IP.2 = 127.0.1.1
IP.3 = 127.0.1.2
IP.4 = 127.0.1.3
IP.5 = 127.0.1.4
IP.6 = 127.0.1.5
IP.7 = 127.0.1.6
IP.8 = 127.0.1.7
IP.9 = 127.0.1.8
IP.10 = 127.0.1.9
IP.11 = 127.0.1.10
IP.12 = 127.0.1.11
IP.13 = 127.0.1.12
IP.14 = 127.0.1.13
IP.15 = 127.0.1.14
IP.16 = 127.0.1.15
IP.17 = 127.0.1.16
IP.18 = 127.0.1.17
IP.19 = 127.0.1.18
```