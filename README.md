[![Build Status](https://travis-ci.org/google/leveldb.svg?branch=master)](https://travis-ci.com/QuorumEngineering/tessera)
[![codecov](https://codecov.io/gh/QuorumEngineering/tessera/branch/master/graph/badge.svg?token=XMRVPC5FLQ)](https://codecov.io/gh/QuorumEngineering/tessera)


# <img src="TesseraLogo.png" width="150" height="150"/>

Tessera is a stateless Java system that is used to enable the encryption, decryption, and distribution of private transactions for [Quorum](https://github.com/QuorumEngineering/quorum-mirror).

Each Tessera node:

* Generates and maintains a number of private/public key pairs

* Self manages and discovers all nodes in the network (i.e. their public keys) by connecting to as few as one other node
    
* Provides Private and Public API interfaces for communication:
    * Private API - This is used for communication with Quorum
    * Public API - This is used for communication between Tessera peer nodes
    
* Provides two way SSL using TLS certificates and various trust models like Trust On First Use (TOFU), whitelist, 
    certificate authority, etc.
    
* Supports IP whitelist
  
* Connects to any SQL DB which supports the JDBC client


## Building Tessera
To build and install Tessera:
1. Clone this repo
1. Build using Maven (see below)


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

`alias tessera="java -jar /path/to/application-${version}-app.jar"`

You will then be able to more concisely use the Tessera CLI commands, such as:

```
tessera -configfile /path/to/config.json
```

and

```
tessera help
```

By default, Tessera uses an H2 database.  To use an alternative database, add the necessary drivers to the classpath:

```
java -cp some-jdbc-driver.jar -jar /path/to/tessera-app.jar
```

## Configuration

### Config File

A configuration file detailing database, server and network peer information must be provided using the `-configfile`
command line property.  

A sample configuration file can be found [here](https://github.com/QuorumEngineering/tessera/wiki/Sample-Configuration-File).

Tessera uses cryptographic keys to provide transaction privacy.  An in-depth look at configuring the cryptographic keys can be found on the [Tessera Wiki](https://github.com/QuorumEngineering/tessera/wiki/Configuration), and includes:
* How to configure an existing private/public key pair with Tessera
* How to use Tessera to generate a new key pair 
 
### Migrating from Constellation to Tessera
Tessera is the service used to provide Quorum with the ability to support private transactions, replacing Constellation.  If you have previously been using Constellation, utilities are provided within Tessera to enable the migration of Constellation configuration and datastores to Tessera compatible formats.  Details on how to use these utilities can be found in the [Tessera Wiki](https://github.com/QuorumEngineering/tessera/wiki/Migrating-from-Constellation-to-Tessera).

## Further reading
* The [Tessera Wiki](https://github.com/QuorumEngineering/tessera/wiki/) provides additional information on how Tessera works, migrating from Constellation to Tessera, configuration details, and more.
* [Quorum](https://github.com/QuorumEngineering/quorum-mirror) is an Ethereum-based distributed ledger protocol that uses Tessera to provide transaction privacy.
* Follow the [Quorum Examples](https://github.com/QuorumEngineering/quorum-ex) to see Tessera in action in a demo Quorum network.
