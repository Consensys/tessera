# Tessera
A stateless java implementation responsible for encryption and decryption of private transaction data and for off-chain private messaging.Each Tessera node

  - Generate and maintain a number of private/public key pairs
  - Self managing and discovery of all nodes and their public keys in network using '/partyinfo' API 
    by connecting to as little as one other node
  - Provides Private and Public API interfacts for communication
    - Private API - This is used for communication with Quorum
    - Public API - Thisis used for communication between Tessera peer nodes


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


## Quorum - Tessera Privacy Transaction Flow

<img src='https://github.com/QuorumEngineering/tessera/blob/Krish1979-patch-1/Tessera%20Privacy%20Flow.jpeg/'>


## Running Tessera

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


