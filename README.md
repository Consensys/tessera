# Nexus
Constellation in Java

Usage; 
`java -jar nexus-app/target/nexus-app-1.0-SNAPSHOT-app.jar` 

<h3>API</h3>
(Further details for each endpoint to be added)
- version
- upcheck
- push
- resend
- partyinfo
- send
- sendraw
- receive
- receiveraw
- delete

<h3>Configuration</h3>
<h4>Configuration sources</h4>
Configuration can be specified in multiple ways, in the following priority:
- system properties (-DprivateKeys=...)
- environment variables (export privateKeys=...)
- command line properties (--privateKeys ...)
- config files (-Dconfig.file=conf.properties, -Dconfig.file=conf.yml)

<h4>Configuration properties</h4>
* publicKeys: comma-separated list of public key file locations to use
* privateKeys: comma separated list of private key file locations to use