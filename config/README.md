# Configuration
Below is a sample configuration json with its datatypes defined. 

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
    "keys": {
        "passwords": [
            
        ],
        "keyData": [
            {
                "config": {
                "data": {
                    "aopts": {
                    "variant": "Enum : id,d or i",
                    "memory": int,
                    "iterations": int,
                    "parallelism": int,
                    "version": "decimal string"
                },
                "snonce": "String",
                "asalt": "String",
                "sbox": "String"
            },
            "type": "Enum: argon2sbox or unlocked. If unlocked is defined in config data is required. "
            },
                "privateKey": "String",
                "privateKeyPath":"Path",
                "publicKey": "String",
                "publicKeyPath":"Path"
            }
        ]
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
    "hostName" : "[Hostname to bind to includes url scheme and port e.g. http://myhost.com:9999]",
    "port" : [Port that will use to expose tessera services (Same as port defined in hostName],
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


### KeyData


```
"keys": {
"passwords": [],
"keyData": [
    {
    "privateKey": "[The direct value of the private key itself]",
    "privateKeyPath": "[The path to the private key file that contains (see Private Key File). If privateKey is define this is ignored]",
    "publicKey": "[The direct value of the public key itself]",
    "publicKeyPath": "[Path to a file containing the public key value. If publicKey is define this is ignored]"
    }
]
}
```

Alternatively rather than providing a Path to the private key file, it can be nested directly into the configuration. 

```
"keyData": [
    {
        "config": {
            "data": {
                "aopts": {
                "variant": "id",
                "memory": 1048576,
                "iterations": 10,
                "parallelism": 4,
                "version": "1.3"
                },
                "snonce": "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                "asalt": "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                "sbox": "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc"
            },
            "type": "argon2sbox"
            },
            "privateKey": "PRIVATE_KEY",
            "publicKey": "PUBLIC_KEY"
    }
]
```

#### Private Key File
The private key file is a json fragament containing the information required to generate a private key value. 
```
{
    "config": {
        "data": {
        "aopts": {
        "variant": "id",
        "memory": 1048576,
        "iterations": 10,
        "parallelism": 4,
        "version": "1.3"
        },
        "snonce": "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
        "asalt": "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
        "sbox": "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc"
        },
        "type": "argon2sbox"
}
```

### Peers
A list of urls where tessera can communicate with other nodes rolled out as part of the same cluster. Peer info is registered between nodes during runtime, but at least 1 url must be provided so the node being configured can take part in the dialogue. 
```
"peer" : [ 
{
    "url" : "http://mydomaon.com:9000/",
},
{
    "url" : "http://mydomaon.com:9001/",
},
{
    "url" : "http://mydomaon.com:9002/",
}
],
```

### Unix socket file
Path to the unix domain socket file used to communicate between geth and tessera.

```
"unixSocketFile" : "/somepath/somename.ipc"
```



