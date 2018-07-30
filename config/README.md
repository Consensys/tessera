# Configuration
Below is a sample configuration json with iyts' datatypes define. 

```
{
"useWhiteList" : boolean,
"jdbc" : {
    "url" : "string",
    "username" : "string",
    "password" : "string"
},
    "server" : {
    "hostName" : "url e.g. http://127.0.0.1:9001/",
    "port" : int,
        "sslConfig" : {
        "tls" : "enum STRICT,OFF",
        "generateKeyStoreIfNotExisted" : boolean,
        "serverKeyStore" : "Path",
        "serverTlsKeyPath" : "Path",
        "serverTlsCertificatePath" : "Path",
        "serverTrustStore" : "Path",
        "serverTrustCertificates" : [Path...],
        "serverTrustMode" : "Enumeration: CA, TOFU, WHITELIST, CA_OR_TOFU, NONE",
        "clientKeyStore" : "Path",
        "clientTlsKeyPath" : "Path",
        "clientTlsCertificatePath" : "Path",
        "clientKeyStorePassword" : "string",
        "clientTrustStore" : "Path",
        "clientTrustCertificates" : [ ],
        "clientTrustMode" : "Enumeration: CA, TOFU, WHITELIST, CA_OR_TOFU, NONE",
        "knownClientsFile" : "Path",
        "knownServersFile" : "Path"
        }
    },
    "peer" : [ {
    "url" : "url e.g. http://127.0.0.1:9000/"
    } ],
    "keys" : {
    "passwords" : [ ],
    "keyData" : [ ]
    },
    "alwaysSendTo" : [ ],
    "unixSocketFile" : "Path"
}
```


## Database settings



### Use white list
Use the peers list as the the urls that the tessera node can connect. 
```
{
"useWhiteList" : false,
....
```

```
"jdbc" : {
"url" : "[JDBC URL]",
"username" : "[JDBC UserName]",
"password": "[JDBC Password]"
}
```

## Server config

```
    "server" : {
    "hostName" : "[Hostname to bind to includes url schem and port e.g. http://myhost.com:9999]",
    "port" : [Port that will use use to expose tessera services (Same as port defined in hostName],
    "sslConfig" : {}
    ...
```

## SSL Config 

```
"sslConfig" : {
    "tls" : "[Authenticatoion mode : OFF,STRICT]",
    "generateKeyStoreIfNotExisted" : [boolean: Create keystore if it doesn't exist. Only works for jks not pem],
    "serverKeyStore" : "[Path to server keystore]",
    "serverTlsKeyPath" : "[Path to server tls key path]",
    "serverTlsCertificatePath" : "[Path to server tls cert path]",
    "serverTrustStore" : "[Server trust store path]",
    "serverTrustCertificates" : [
        [Array of truststore certificates if no truststore is defined. ]
    ],
    "serverTrustMode" : "[Possible values CA, TOFU, WHITELIST, CA_OR_TOFU, NONE]",

    "clientKeyStore" : "[Path to client keystore. The keystore that is used when communicating to other nodes.]",
    "clientTlsKeyPath" : "[Path to Client Tls Key]",
    "clientTlsCertificatePath" : "[Path to client tls cert path]",
    "clientKeyStorePassword" : "[Password required for Client KeyStore]",
    "clientTrustStore" : "[Path to client TrustStore]",
    "clientTrustCertificates" : [
        [Array of truststore certificates if no truststore is defined. ]
    ],
    "clientTrustMode" : "[Possible values CA, TOFU, WHITELIST, CA_OR_TOFU, NONE]",

    "knownClientsFile" : "[TLS known clients file for the server. This contains the fingerprints of  public keys of other nodes that are allowed to connect to this one.]",
    "knownServersFile" : "[TLS known servers file for the client. This contains the fingerprints of public keys of other nodes that this node has encountered.]"
}
```

