
The certificates have been generated using this "recipe": https://jamielinux.com/docs/openssl-certificate-authority/introduction.html
On top of that recipe I've altered the intermediate openssl.cnf to add the SANs.

caRoot.pem - Quorum Root certificate
caInt.pem - Quorum Intermediate certificate
caIntAndRoot.pem - Int concatenated with root

localhost-with-san.pem - localhost certificate with a lot of SANs
localhost-with-san.key - localhost certificate private key (no password)
localhost-with-san-chain.pem - localhost concatenated with caInt and caRoot

quorum-client.pem - quorum client certificate. CN=quorum-client
[4]: ObjectId: 2.5.29.37 Criticality=false
ExtendedKeyUsages [
  clientAuth
  emailProtection
]
quorum-client.key - quorum client private key
quorum-client-chain.pem - quorum-client concatenated with caInt and caRoot


For all the jks/p12 the password is: testtest

localhost-with-san-keystore.jks - java key store containing the localhost with SANs certificate, private key and chain
localhost-with-san.p12 - same as above but different format (pkcs12)
quorum-client-keystore.jks - java key store containing the quorum client certificate, private key and chain
quorum-client.p12 - same as above but different format (pkcs12)
truststore.jks - java key store containing the caRoot certificate.


Full list of SANs for the localhost certificate:
[7]: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  DNSName: localhost
  DNSName: localhost.localdomain
  DNSName: quorum1.quorum.net
  DNSName: quorum2.quorum.net
  DNSName: quorum3.quorum.net
  DNSName: quorum4.quorum.net
  DNSName: quorum5.quorum.net
  DNSName: quorum6.quorum.net
  DNSName: quorum7.quorum.net
  DNSName: quorum8.quorum.net
  DNSName: quorum9.quorum.net
  DNSName: quorum10.quorum.net
  DNSName: tessera1.quorum.net
  DNSName: tessera2.quorum.net
  DNSName: tessera3.quorum.net
  DNSName: tessera4.quorum.net
  DNSName: tessera5.quorum.net
  DNSName: tessera6.quorum.net
  DNSName: tessera7.quorum.net
  DNSName: tessera8.quorum.net
  DNSName: tessera9.quorum.net
  DNSName: tessera10.quorum.net
  DNSName: node1
  DNSName: node2
  DNSName: node3
  DNSName: node4
  DNSName: node5
  DNSName: node6
  DNSName: node7
  DNSName: txmanager1
  DNSName: txmanager2
  DNSName: txmanager3
  DNSName: txmanager4
  DNSName: txmanager5
  DNSName: txmanager6
  DNSName: txmanager7
  IPAddress: 127.0.0.1
  IPAddress: 127.0.1.1
  IPAddress: 127.0.1.2
  IPAddress: 127.0.1.3
  IPAddress: 127.0.1.4
  IPAddress: 127.0.1.5
  IPAddress: 127.0.1.6
  IPAddress: 127.0.1.7
  IPAddress: 127.0.1.8
  IPAddress: 127.0.1.9
  IPAddress: 127.0.1.10
  IPAddress: 127.0.1.11
  IPAddress: 127.0.1.12
  IPAddress: 127.0.1.13
  IPAddress: 127.0.1.14
  IPAddress: 127.0.1.15
  IPAddress: 127.0.1.16
  IPAddress: 127.0.1.17
  IPAddress: 127.0.1.18
]
