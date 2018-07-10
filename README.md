# Tessera
A stateless Java application responsible for encryption and decryption of private transaction data and for off-chain private messaging.  Tessera is also responsible for generating and managing private keys locally in each node in a Quorum network.

## Running Tessera

Once Tessera has been configured and built, you may want to copy the .jar to another location, create an alias and add it to your PATH:

`alias tessera="java -jar /somewhere/application-${version}-app.jar"`

You will then be able to more concisely use the Tessera CLI commands, such as:
```
tessera help
```

and
```
tessera -configfile /path/to/config.json
```

By default, Tessera uses an H2 database.  To use an alternative database, add the necessary drivers to the classpath:

`java -cp some-jdbc-driver.jar -jar /somewhere/tessera-app.jar`

## Initial Configuration

### Config File

A config file including database, server and network peer information must be provided using the `-configfile`
command line property.  A sample configuration file can be found [here](/config/src/test/resources/sample_full.json).

### Cryptographic Keys
Tessera uses cryptographic keys to provide transaction privacy.  You can use an existing private/public key pair or have Tessera generate a new key pair for you.

#### Using existing keys
Existing keys can be included in `config.json` in one of two ways:
* __Directly__ (preferred): 
```
    "keys": [
        {
            "privateKey": "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=",
            "publicKey": "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="
        }
```                                                                    
* __Indirectly__ (as used in legacy implementations of Tessera):  
 The private key is provided indirectly through additional config, e.g.
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
```

```
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

#### Generating keys
If keys do not already exist they can be generated using the `-keygen` option. 

```
tessera -configfile config.json -keygen /path/to/config1 /path/to/config2
```

Where `/path/to/config*` are configuration files used in the creation of private keys.  Example configs include:
* Plaintext key:
```
{
    "type": "unlocked"
}
```

* Password protected key:
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

## Building

To build and use Tessera: 
1. Install the required runtime dependencies
2. Checkout the project from GitHub
3. Build using Maven
4. Run the .jar

For further details, see below.

### Runtime Dependencies
Tessera has the following runtime dependencies which must be installed.

#### junixsocket
junixsocket will unpack the required dependencies if they are not found.
By default, they get unpacked to `/tmp`, but this can be
changed by setting the system property `"org.newsclub.net.unix.library.path"`.

Alternatively, you can install the dependency yourself and point the 
above system property to the install location.

1. Get `junixsocket-1.3-bin.tar.bz2` from https://code.google.com/archive/p/junixsocket/downloads
2. Unpack it
3. `sudo mkdir -p /opt/newsclub/lib-native`
4. `sudo cp junixsocket-1.3/lib-native/libjunixsocket-macosx-1.5-x86_64.dylib /opt/newsclub/lib-native/`

### Selecting an NaCl Implementation 
Tessera can be built with different NaCl cryptography implementations:

#### jnacl

`mvn install`

#### kalium
 
 Install kalium as detailed on the [kalium project page](https://github.com/abstractj/kalium), then run
 
`mvn install -Pkalium`

## Interface Details

Tessera has two interfaces which allow endpoints from the API to be called.

### HTTP (Public API)

The Public API is used for communication between Tessera instances.
Tessera instances communicate with each other using this API to:
- Learn about other Tessera nodes in the network
- Send and receive public key information
- Send private transactions to the other party(ies) in a transaction

The following endpoints are advertised on this interface:
- version
- upcheck
- push
- resend
- partyinfo

### Unix Domain Socket (Private API)
The Private API is used for communication with Quorum.
Quorum uses this API to:
- Check if the local Tessera node is running
- Send and receive details of private transactions

The following endpoints are advertised on this interface:
- version
- upcheck
- send
- sendraw
- receive
- receiveraw
- delete

## API Details

**version** - _Get Tessera version_

Returns the version of Tessera that is running.

**upcheck** - _Check that Tessera is running_

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
The identifier is a hash digest of the encrypted payload that every recipient node receives.
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


