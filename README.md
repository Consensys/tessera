# Nexus
A stateless JAVA application responsible for encryption and decryption of private transaction data and for off-chain private messaging.It is also responsible for generating and managing private key locally in each node in Quorum Network.

##Running Nexus
Usage:

`java -jar nexus-app/target/nexus-app-${version}-app.jar -c config.json`

See the section on 'Configuration' for a description of the available properties.

##Interface Details

Nexus has two interfaces which allow endpoints from the API to be called.

#####HTTP (Public API)

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

#####Unix Domain Socket (Private API)
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

##API Details

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

##Configuration

####Configuration sources

Configuration can be specified in multiple ways, in the following priority:
- system properties (-DprivateKeys=...)
- environment variables (export privateKeys=...)
- command line properties (--privateKeys ...)
- config files (-Dconfig.file=conf.properties, -Dconfig.file=conf.yml)

####Configuration properties

* publicKeys: comma-separated list of public key file locations to use
* privateKeys: comma separated list of private key file locations to use
* url: URL of this Nexus node (used by Quorum and also advertised to remote Nexus nodes)
* port: port used by this node to listen to remote Nexus nodes
* workdir: directory where work files are placed (must match value specified to Quorum)
* socket: name of the unix domain socket used for communication with Quorum
* othernodes: comma seperated list of known Nexus nodes
* keygenBasePath: the base path for new generated keys to be placed - can be absolute or relative
* passwords: a list of passwords used to unlock encrypted private keys

(n.b. if a private key isn't encrypted, give it an empty password, e.g. passwords=abc,,def)

##Building Nexus

Checkout nexus from github and build using maven.
Nexus can be built with different nacl implementations:

####jnacl

* mvn --batch-mode install

#####kalium

* mvn --batch-mode install -Pkalium

Note that the Kalium implementation requires that you have sodium installed at runtime (see runtime dependencies below).

##Runtime Dependencies
Nexus has the folllowing runtime dependencies which must be installed.

####junixsocket

1. Get junixsocket-1.3-bin.tar.bz2 from https://code.google.com/archive/p/junixsocket/downloads
2. Unpack it
4. sudo mkdir -p /opt/newsclub/lib-native
5. sudo cp junixsocket-1.3/lib-native/libjunixsocket-macosx-1.5-x86_64.dylib /opt/newsclub/lib-native/

####sodium

This is only required if Nexus is built to use the Kalium implementation.
* brew install libsodium
