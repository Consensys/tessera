This is the wiki for Tessera, a stateless Java system that is used to enable the encryption, decryption, and distribution of private transactions for Quorum.  

Quorum is an Ethereum-based distributed ledger protocol with transaction privacy. For Quorum-related information see the [Quorum project repository](https://github.com/jpmorganchase/quorum).

## Notation used in this Wiki
### JAR Paths
JAR execution commands are referenced in a short-hand form in this wiki.  For example:
```
tessera <OPTIONS>
```
can be interpreted as executing the Tessera jar, i.e.:
```
java -jar path/to/tessera-app-{version}-app.jar <OPTIONS>
```

As described in the [README](https://github.com/jpmorganchase/tessera), an alias can be created to achieve this on your machine.

The Tessera, Config Migration Utility and Data Migration Utility JARs can each be downloaded from the [releases page](https://github.com/jpmorganchase/tessera/releases), or they can be built from source using Maven (see the [README](https://github.com/jpmorganchase/tessera) for details on how to do this).

### File Paths

File paths are often referenced in this wiki as:
```
/path/to/file
```
where a specific path is not provided to account for your particular preferences and directory structure.