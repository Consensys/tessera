#Kalium encryption
This module is provide as a sample of how developers can provide CUSTOM encryptor types.

A encryption implementation that uses org.abstractj.kalium:kalium:0.8.0 ro 
encrypt using NACL. 

Kalium requires libsodium to be installed on execution env. Install libsodium as detailed on the [kalium project page](https://github.com/abstractj/kalium).  Add the `net.consensys.quorum.tessera:encryption-kalium` jar to the classpath when running Tessera:


[kalium](https://github.com/abstractj/kalium)