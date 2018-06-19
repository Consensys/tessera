# Nexus
A stateless JAVA application responsible for encryption and decryption of private transaction data and for off-chain private messaging.It is also responsible for generating and managing private key locally in each node in Quorum Network.

Usage:
`java -jar nexus-app/target/nexus-app-1.0-SNAPSHOT-app.jar` 

<h2>Interface Details</h2>
Nexus has two interfaces which allow endpoints from the API to be called.

<h5>HTTP (Public API)</h5>
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

<h5>Unix Domain Socket (Private API)</h5>
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

<h2>API Details</h2>

<b>version</b> - <i>Get Nexus version</i>

Returns the version of Nexus that is running.

<b>upcheck</b> - <i>Check that Nexus is running</i>

Returns the text "I'm up!"

<b>push</b> - <i>Details to be provided</i>

Details to be provided.

<b>resend</b> - <i>Details to be provided</i>

Details to be provided

<b>partyinfo</b> - <i>Retrieve details of known nodes</i>

Details to be provided

<b>send</b> - <i>Send transaction</i>

Allows you to send a bytestring to one or more public keys,
returning a content-addressable identifier.
This bytestring is encrypted transparently and efficiently (at symmetric encryption speeds)
before being transmitted over the wire to the correct recipient nodes (and only those nodes).
The identifier is a hash digest of the encrypted payload that every receipient node receives.
Each recipient node also receives a small blob encrypted for their public key which contains
the Master Key for the encrypted payload.

<b>sendraw</b> - <i>Details to be provided</i>

Details to be provided

<b>receive</b> - <i>Receive a transaction</i>

Allows you to receive a decrypted bytestring based on an identifier.
Payloads which your node has sent or received can be decrypted and retrieved in this way.

<b>receiveraw</b> - <i>Details to be provided</i> 

Details to be provided

<b>delete</b> - <i>Delete a transaction</i> 

Details to be provided

<h2>Configuration</h2>
<h4>Configuration sources</h4>
Configuration can be specified in multiple ways, in the following priority:
- system properties (-DprivateKeys=...)
- environment variables (export privateKeys=...)
- command line properties (--privateKeys ...)
- config files (-Dconfig.file=conf.properties, -Dconfig.file=conf.yml)

<h4>Configuration properties</h4>
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

<h2>Building Nexus</h2>
Checkout nexus from github and build using maven.
<p>Nexus can be built with different nacl implementations:
<h5>jnacl</h5>
* mvn --batch-mode install

<h5>kalium</h5>
* mvn --batch-mode install -Pkalium

Note that the Kalium implementation requires that you have sodium installed at runtime:
* brew install libsodium
