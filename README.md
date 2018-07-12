[![Build Status](https://travis-ci.org/google/leveldb.svg?branch=master)](https://travis-ci.com/QuorumEngineering/tessera)

# Tessera

A stateless Java implementation responsible for encryption and decryption of private transaction data and for off-chain private messaging. Each Tessera node:

  - Generates and maintains a number of private/public key pairs
  
  - Self manages and discovers all nodes (i.e. their public keys) in the network by connecting to as little as one other node
    
  - Provides Private and Public API interfaces for communication
    - Private API - This is used for communication with Quorum
    - Public API - This is used for communication between Tessera peer nodes
    
  - Provides two way SSL using TLS certificates and various trust models like Trust On First Use (TOFU), whitelist, 
    certificate authority, etc.
    
  - Supports IP whitelist
  
  - Connects to any SQL DB which supports the JDBC client


## Building Tessera

1. Install the required runtime dependencies
2. Checkout the project from GitHub
3. Build using Maven


### Runtime Dependencies
Tessera has the following runtime dependencies which must be installed.

#### junixsocket
1. Get `junixsocket-1.3-bin.tar.bz2` from https://code.google.com/archive/p/junixsocket/downloads
2. Unpack it
3. `sudo mkdir -p /opt/newsclub/lib-native`
4. `sudo cp junixsocket-1.3/lib-native/libjunixsocket-macosx-1.5-x86_64.dylib /opt/newsclub/lib-native/`

junixsocket will unpack the required dependencies if they are not found.
By default, they get unpacked to `/tmp`, but this can be
changed by setting the system property `"org.newsclub.net.unix.library.path"`.

Alternatively, you can install the dependency yourself and point the 
above system property to the install location.

### Selecting an NaCl Implementation 
Tessera can be built with different NaCl cryptography implementations:

#### jnacl

`mvn install`

#### kalium
 
 Install kalium as detailed on the [kalium project page](https://github.com/abstractj/kalium), then run
 
`mvn install -Pkalium`


## Running Tessera
`java -jar tessera-app/target/tessera-app-${version}-app.jar -configfile config.json`

Once Tessera has been configured and built, you may want to copy the .jar to another location, create an alias and add it to your PATH:

`alias tessera="java -jar /somewhere/application-${version}-app.jar"`

You will then be able to more concisely use the Tessera CLI commands, such as:

```
tessera -configfile /path/to/config.json
```

and

```
tessera help
```

By default, Tessera uses an H2 database.  To use an alternative database, add the necessary drivers to the classpath:

`java -cp some-jdbc-driver.jar -jar /somewhere/tessera-app.jar`

## Configuration

### Config File

A config file including database, server and network peer information must be provided using the `-configfile`
command line property.  A sample configuration file can be found [here](/config/src/test/resources/sample_full.json).

### Cryptographic Keys
Tessera uses cryptographic keys to provide transaction privacy.  You can use an existing private/public key pair or have Tessera generate a new key pair for you.

#### Using existing keys
Existing keys can be included in `config.json` in one of two ways:
- __Directly__ (preferred): 
```
    "keys": [
        {
            "privateKey": "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=",
            "publicKey": "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="
        }
```                                                                    
- __Indirectly__ (as used in legacy implementations of Tessera):  
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
- Plaintext key:
```
{
    "type": "unlocked"
}
```

- Password protected key:
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

##### HTTP (Public API)

Tessera Nodes communicate with each other for:

- Node/Network discovery
- Sending/Receiving encrypted payload.

The following endpoints are advertised on this interface:

- /version
- /upcheck
- /push
- /resend
- /partyinfo
- /delete

##### Unix Domain Socket (Private API)

Quorum uses this API to:
- Check if the local Tessera node is running.
- Send and receive details of private transactions(hash).

The following endpoints are advertised on this interface:
- /version
- /upcheck
- /sendraw
- /receiveraw

## API Details

**version** - _Get Tessera version_

- Returns the version of Tessera that is running.

**upcheck** - _Check Tessera node is running_

- Returns the text "I'm up!"

**push** - _Push transactions between nodes_

- Persist encrypted payload received from another node.

**resend** - _Resend transaction_

- Resend all transactions for given key or given hash/recipient.

**partyinfo** - _Retrieve details of known nodes_

- Request public keys/url of all known peer nodes.

**sendraw** - _Send transaction bytestring_

- Send transaction payload bytestring from Quorum to Tessera node. Tessera send transaction hash in the response back. 

**receiveraw** - _Receive transaction bytestring_ 

- Receive decrypted bytestring of the transaction payload from Tessera to Quorum for transations it is party to.

**delete** - _Delete a transaction_ 

- Delete hashed encrypted payload stored in Tessera nodes.

## How It Works

### Quorum - Tessera Data Privacy Flow Diagram

<img src='https://github.com/QuorumEngineering/tessera/blob/master/Tessera%20Privacy%20Flow.jpeg'/>


- Each Tessera node hosts a number of key pairs and public keys are advertised 
over public API

- Nodes can be started with information from just one other node to achieve network 
synchronization

- When a node starts up, it will reach out to every other node to 
share/receive the public keys hosted. The heartbeat is sent every 
two seconds for resynchronization. Each node will maintain the same public key 
registry after synchronization and can start sending messages to any known public key.

- When Quorum node starts up it connects to its local Tessera node using the
/upcheck API and is now ready to process private transactions.

- When Quorum sends transaction to its local node using /sendraw API, 

    1. The local node first validates the sender's public key.
    
    2. The local node checks for private key and once validated begins to encrypts
       the payload by
       
       - Generating a symmetric key and random nonce
       - Generate a recipient nonce 
       - Encrypt the payload using the symmetric key and random nonce
       - Hash this encrypted payload using SHA3 algorithm
       - For each recipient, encrypt the symmetric key with recipient nonce 
         and recipient public key 
    
    3. The local node stores the payload locally and transmits the encrypted 
       payload and the encrypted symmetric key to each recipient using public 
       API '/push'. Each recipient will regenerate the hash using same algorithm
       for local storage.

    4. Once all nodes have confirmed receipt and storage of the payload,
       the local node returns the '/sendraw' API call successfully with the
       hash of encrypted payload to Quorum node.

    5. Quorum then propogates the transaction hash to rest of the network
    
    6. The leader Quorum node then creates the block with this transaction 
       which is distributed across the network as well and they all attempt 
       to process it using '/receiveraw' API.
       
    7. Each local node will look for the hash and abort if not found. When 
       found, the node will use information about sender and its private key
       to decrypt the symmetric key and the decrypt the transaction payload
       using the now decrypted symmetric key and return the '/receiveraw' API
       successfully with the decrypted transaction data. which is then stored
       in the private store DB.
