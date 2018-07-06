# Nexus
A stateless java application responsible for encryption and decryption of private transaction data and for off-chain private messaging.It is also responsible for generating and managing private key locally in each node in Quorum Network.

## Running Nexus

`java -jar nexus-app/target/nexus-app-${version}-app.jar -configfile config.json`


Probably best to copy the jar somewhere and create an alias

`alias nexus="java -jar /somewhere/nexus-app.jar"`

And add the nexus to your PATH.

If you want to use an alternative database then you'll need to add the drivers to the classpath

`java -cp some-jdbc-driver.jar -jar /somewhere/nexus-app.jar`


## Configuration

A configuration file must be specified using the `-configfile /path/to/config.json`
command line property.

A sample configuration can be found [here](/nexus-config/src/test/resources/sample_full.json)

Keys can be provided using direct values, like in the example above,
or by providing the format produced by previous versions. Just replace the
`privateKey` field with the data in those files under a `config` key.

Below is a sample snippet:

```

{
    "config": {
        "data": {
            "bytes": "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM="
        },
        "type": "unlocked"
    },
    "publicKey": "+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="
}

{
    "config": {
        "data": {
            "aopts": {
                "variant": "id",
                "memory": 1048576,
                "iterations": 10,
                "parallelism": 4,
            },
            "password": "password",
            "snonce": "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
            "asalt": "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
            "sbox": "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc"
        },
        "type": "argon2sbox"
    },
    "publicKey": "+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="
}

```

If the keys dont already exist they can be generated using the -keygen option. 

```
nexus -configfile config.json -keygen /path/to/config1 /path/to/config2
```

This will check the given paths for configuration of the private keys.
The configuration for these is the same as what is produced for a key, e.g.

Plaintext key:

/path/to/config1
```
{
    "type": "unlocked"
}
```

Password protected key:

/path/to/config2
```
{ 
    "data": {
        "aopts": {
            "variant": "id",
            "memory": 1048576,
            "iterations": 10,
            "parallelism": 4,

        },
        "password": "passwordToUse",
    },
    "type": "argon2sbox"
}
```


## Interface Details

Nexus has two interfaces which allow endpoints from the API to be called.

##### HTTP (Public API)

This is used for communication between Nexus instances.
Nexus instances communicate with each other for:
- Learning what nodes to connect to.
- Sending and receiving public key information.
- Sending private transactions to the other party(ies) in a transaction.

The following endpoints are advertised on this interface:
- version
- upcheck
- push
- resend
- partyinfo

##### Unix Domain Socket (Private API)
This is used for communication with Quorum.
Quorum needs to be able to:
- Check if the local Nexus node is running.
- Send and receive details of private transactions.

The following endpoints are advertised on this interface:
- version
- upcheck
- send
- sendraw
- receive
- receiveraw
- delete

## API Details

**version** - _Get Nexus version_

Returns the version of Nexus that is running.

**upcheck** - _Check that Nexus is running_

Returns the text "I'm up!"

**push** - _Details to be provided_

Details to be provided.

**resend** - _Details to be provided_

Details to be provided

**partyinfo** - _Retrieve details of known nodes_

Details to be provided

**send** - _Send transaction_

Allows you to send a bytestring to one or more public keys,
returning a content-addressable identifier.
This bytestring is encrypted transparently and efficiently (at symmetric encryption speeds)
before being transmitted over the wire to the correct recipient nodes (and only those nodes).
The identifier is a hash digest of the encrypted payload that every receipient node receives.
Each recipient node also receives a small blob encrypted for their public key which contains
the Master Key for the encrypted payload.

**sendraw** - _Details to be provided_

Details to be provided

**receive** - _Receive a transaction_

Allows you to receive a decrypted bytestring based on an identifier.
Payloads which your node has sent or received can be decrypted and retrieved in this way.

**receiveraw** - _Details to be provided_ 

Details to be provided

**delete** - _Delete a transaction_ 

Details to be provided

## Building Nexus

Checkout nexus from github and build using maven.
Nexus can be built with different nacl implementations:

#### jnacl

`mvn install`

##### kalium

`mvn install -Pkalium`

Note that the Kalium implementation requires that you have sodium installed at runtime (see runtime dependencies below).

## Runtime Dependencies
Nexus has the folllowing runtime dependencies which must be installed.

#### junixsocket
JUnixSocket will unpack required dependencies if they are not found
By default, they get unpacked to /tmp, but this can be
changed by setting the system property "org.newsclub.net.unix.library.path"

Alternatively, you can install the dependency yourself, and point the 
above system property to the install location.

1. Get junixsocket-1.3-bin.tar.bz2 from https://code.google.com/archive/p/junixsocket/downloads
2. Unpack it
4. sudo mkdir -p /opt/newsclub/lib-native
5. sudo cp junixsocket-1.3/lib-native/libjunixsocket-macosx-1.5-x86_64.dylib /opt/newsclub/lib-native/

#### sodium

This is only required if Nexus is built to use the Kalium implementation.
* brew install libsodium

## Swagger api doc

```
{
  "swagger" : "2.0",
  "info" : {
    "version" : "1.0-SNAPSHOT",
    "title" : "Nexus rest"
  },
  "host" : "{host}:{port}",
  "basePath" : "/",
  "tags" : [ {
    "name" : "Provided access to openapi schema documentation."
  } ],
  "schemes" : [ "http", "https" ],
  "paths" : {
    "/api" : {
      "get" : {
        "tags" : [ "Provided access to openapi schema documentation." ],
        "operationId" : "api",
        "produces" : [ "application/json", "text/html" ],
        "responses" : {
          "200" : {
            "description" : "Returns json or html openapi document"
          }
        }
      }
    },
    "/delete" : {
      "post" : {
        "summary" : "Delete key provided in request. Deprecated: Deletes will be done with DELETE http method",
        "description" : "",
        "operationId" : "delete",
        "consumes" : [ "application/json" ],
        "produces" : [ "text/plain" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "deleteRequest",
          "required" : true,
          "schema" : {
            "$ref" : "#/definitions/DeleteRequest"
          }
        } ],
        "responses" : {
          "200" : {
            "description" : "Status message"
          },
          "404" : {
            "description" : "If the entity doesn't exist"
          }
        },
        "deprecated" : true
      }
    },
    "/partyinfo" : {
      "post" : {
        "operationId" : "partyInfo",
        "consumes" : [ "application/octet-stream" ],
        "produces" : [ "application/octet-stream" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "body",
          "required" : true,
          "schema" : {
            "type" : "array",
            "items" : {
              "type" : "string",
              "format" : "byte"
            }
          }
        } ],
        "responses" : {
          "200" : {
            "description" : "Endcoded PartyInfo Data"
          }
        }
      }
    },
    "/push" : {
      "post" : {
        "operationId" : "push",
        "consumes" : [ "application/octet-stream" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "payload",
          "description" : "Key data to be stored.",
          "required" : true,
          "schema" : {
            "type" : "array",
            "items" : {
              "type" : "string",
              "format" : "byte"
            }
          }
        } ],
        "responses" : {
          "201" : {
            "description" : "Key created status"
          },
          "500" : {
            "description" : "General error"
          }
        }
      }
    },
    "/receive" : {
      "get" : {
        "operationId" : "receive",
        "consumes" : [ "application/json" ],
        "produces" : [ "application/json" ],
        "responses" : {
          "default" : {
            "description" : "successful operation"
          }
        },
        "deprecated" : true
      }
    },
    "/receiveraw" : {
      "get" : {
        "summary" : "Summit keys to retrieve payload and decrypt it",
        "description" : "",
        "operationId" : "receiveRaw",
        "consumes" : [ "application/octet-stream" ],
        "produces" : [ "application/octet-stream" ],
        "parameters" : [ {
          "name" : "c11n-key",
          "in" : "header",
          "description" : "Encoded Sender Public Key",
          "required" : true,
          "type" : "string"
        }, {
          "name" : "c11n-to",
          "in" : "header",
          "description" : "Encoded Recipient Public Key",
          "required" : false,
          "type" : "string"
        } ],
        "responses" : {
          "200" : {
            "description" : "Raw payload"
          }
        }
      }
    },
    "/resend" : {
      "post" : {
        "operationId" : "resend",
        "consumes" : [ "application/json" ],
        "produces" : [ "text/plain" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "resendRequest",
          "required" : true,
          "schema" : {
            "$ref" : "#/definitions/ResendRequest"
          }
        } ],
        "responses" : {
          "200" : {
            "description" : "Encoded payload when TYPE is INDIVIDUAL"
          },
          "500" : {
            "description" : "General error"
          }
        }
      }
    },
    "/send" : {
      "post" : {
        "operationId" : "send",
        "consumes" : [ "application/json" ],
        "produces" : [ "application/json" ],
        "parameters" : [ {
          "in" : "body",
          "name" : "sendRequest",
          "required" : true,
          "schema" : {
            "$ref" : "#/definitions/SendRequest"
          }
        } ],
        "responses" : {
          "200" : {
            "description" : "Send response",
            "schema" : {
              "$ref" : "#/definitions/SendResponse"
            }
          },
          "400" : {
            "description" : "For unknown and unknown keys"
          }
        }
      }
    },
    "/sendraw" : {
      "post" : {
        "operationId" : "sendRaw",
        "consumes" : [ "application/octet-stream" ],
        "produces" : [ "text/plain" ],
        "parameters" : [ {
          "name" : "c11n-from",
          "in" : "header",
          "required" : false,
          "type" : "string"
        }, {
          "name" : "c11n-to",
          "in" : "header",
          "required" : false,
          "type" : "string"
        } ],
        "responses" : {
          "200" : {
            "description" : "Encoded Key"
          },
          "500" : {
            "description" : "Unknown server error"
          }
        }
      }
    },
    "/transaction/{hash}" : {
      "get" : {
        "operationId" : "receive",
        "produces" : [ "application/json" ],
        "parameters" : [ {
          "name" : "hash",
          "in" : "path",
          "description" : "Encoded hash used to decrypt the payload",
          "required" : true,
          "type" : "string"
        }, {
          "name" : "to",
          "in" : "query",
          "description" : "Encoded recipient key",
          "required" : false,
          "type" : "string"
        } ],
        "responses" : {
          "200" : {
            "description" : "Receive Response object",
            "schema" : {
              "$ref" : "#/definitions/ReceiveResponse"
            }
          }
        }
      }
    },
    "/transaction/{key}" : {
      "delete" : {
        "operationId" : "deleteKey",
        "parameters" : [ {
          "name" : "key",
          "in" : "path",
          "description" : "Encoded hash",
          "required" : true,
          "type" : "string"
        } ],
        "responses" : {
          "204" : {
            "description" : "Successful deletion"
          },
          "404" : {
            "description" : "If the entity doesn't exist"
          }
        }
      }
    },
    "/upcheck" : {
      "get" : {
        "operationId" : "upCheck",
        "produces" : [ "text/plain" ],
        "responses" : {
          "200" : {
            "description" : "I'm up!",
            "schema" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "/version" : {
      "get" : {
        "operationId" : "getVersion",
        "produces" : [ "text/plain" ],
        "responses" : {
          "200" : {
            "description" : "Current application version ",
            "schema" : {
              "type" : "string"
            }
          }
        }
      }
    }
  },
  "definitions" : {
    "DeleteRequest" : {
      "type" : "object",
      "required" : [ "Encoded public key" ],
      "properties" : {
        "Encoded public key" : {
          "type" : "string"
        }
      }
    },
    "ReceiveResponse" : {
      "type" : "object",
      "properties" : {
        "payload" : {
          "type" : "string",
          "description" : "Encode response servicing recieve requests"
        }
      }
    },
    "ResendRequest" : {
      "type" : "object",
      "properties" : {
        "type" : {
          "type" : "string",
          "description" : "Resend type INDIVIDUAL or ALL",
          "enum" : [ "ALL", "INDIVIDUAL" ]
        },
        "publicKey" : {
          "type" : "string",
          "description" : "TODO: Define this publicKey, what is it?"
        },
        "key" : {
          "type" : "string",
          "description" : "TODO: Define this key, what is it?"
        }
      }
    },
    "SendRequest" : {
      "type" : "object",
      "required" : [ "payload" ],
      "properties" : {
        "payload" : {
          "type" : "string",
          "description" : "Encyrpted payload to send to other parties."
        },
        "from" : {
          "type" : "string",
          "description" : "Sender public key"
        },
        "to" : {
          "type" : "array",
          "description" : "Recipient public keys",
          "items" : {
            "type" : "string"
          }
        }
      }
    },
    "SendResponse" : {
      "type" : "object",
      "properties" : {
        "key" : {
          "type" : "string",
          "description" : "TODO: Define this key as something"
        }
      }
    }
  }
}
```
