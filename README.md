![Build Status](https://github.com/jpmorganchase/tessera/workflows/Java%20CI%20with%20Maven/badge.svg)
[![codecov](https://codecov.io/gh/jpmorganchase/tessera/branch/master/graph/badge.svg?token=XMRVPC5FLQ)](https://codecov.io/gh/jpmorganchase/tessera)
![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/quorumengineering/tessera)
![Docker Pulls](https://img.shields.io/docker/pulls/quorumengineering/tessera)


# <img src="https://raw.githubusercontent.com/jpmorganchase/tessera/master/TesseraLogo.png" width="150" height="150"/>

> __Important: Release 0.9 Feature__ <br/>Tessera now supports remote enclaves for increased security. Please refer to the [wiki](https://github.com/jpmorganchase/tessera/wiki/What-is-an-Enclave%3F) for details. 

Tessera is a stateless Java system that is used to enable the encryption, decryption, and distribution of private transactions for [Quorum](https://github.com/consensys/quorum/).

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

## Prerequisites
- [Java](https://www.oracle.com/technetwork/java/javase/downloads/index.html)<br/>
    Install the correct JDK/JRE for the version of Tessera you are using: 

    | Tessera version    | Build method                                                                | JDK/JRE version |
    |--------------------|-----------------------------------------------------------------------------|:---------------:|
    | 0.10.3 and later   | [Pre-built release JARs](https://github.com/consensys/tessera/releases) |        11       |
    |                    | Building from source                                                        |        11       |
    | 0.10.2 and earlier | [Pre-built release JARs](https://github.com/consensys/tessera/releases) |        8        |
    |                    | Building from source                                                        |     8 or 11     |

- [Maven](https://maven.apache.org) (if building from source)
- [libsodium](https://download.libsodium.org/doc/installation/) (if using kalium as the NaCl implementation)

## Building Tessera from source
To build and install Tessera:
1. Clone this repo
1. Build using Maven (see below)


### Selecting an NaCl Implementation 
Tessera can use either the [jnacl](https://github.com/neilalexander/jnacl) or [kalium](https://github.com/abstractj/kalium) NaCl cryptography implementations.  The implementation to be used is specified when building the project:

#### jnacl (default)

`mvn install`

#### kalium

Install libsodium as detailed on the [kalium project page](https://github.com/abstractj/kalium), then run
 
`mvn install -P kalium`


## Running Tessera
`java -jar tessera-dist/tessera-app/target/tessera-app-${version}-app.jar -configfile /path/to/config.json`

> See the [`tessera-dist` README](tessera-dist) for info on the different distributions available.

Once Tessera has been configured and built, you may want to copy the .jar to another location, create an alias and add it to your PATH:

`alias tessera="java -jar /path/to/tessera-app-${version}-app.jar"`

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
java -cp some-jdbc-driver.jar:/path/to/tessera-app.jar:. com.quorum.tessera.launcher.Main
```
For example, to use Oracle database: 
```
java -cp ojdbc7.jar:tessera-app.jar:. com.quorum.tessera.launcher.Main -configfile config.json
```

[DDLs](ddls/create-table) have been provided to help with defining these databases.

Since Tessera 0.7 a timestamp is recorded with each encrypted transaction stored in the Tessera DB.  To update an existing DB to work with Tessera 0.7+, execute one of the provided [alter scripts](ddls/add-timestamp).

## Configuration

### Config File

A configuration file detailing database, server and network peer information must be provided using the `-configfile`
command line property.

An in-depth look at configuring Tessera can be found in the [Tessera Documentation](https://docs.tessera.consensys.net/en/latest/HowTo/Configure/Tessera) and includes details on all aspects of configuration including:
* Cryptographic key config:
    * Using existing private/public key pairs with Tessera
    * How to use Tessera to generate new key pairs 
* TLS config
    * How to enable TLS
    * Choosing a trust mode
    
#### Obfuscate database password in config file

Certain entries in Tessera config file must be obfuscated in order to prevent any attempts from attackers to gain access to critical part of the application (i.e. database). For the time being, Tessera users have the ability to enable encryption for database password to avoid it being exposed as plain text in the configuration file.

In Tessera, [jasypt](http://www.jasypt.org) library was used together with its Jaxb integration to encrypt/decrypt config values.

To enable this feature, simply replace your plain-text database password with its encrypted value and wrap it inside an `ENC()` function.

```json
    "jdbc": {
        "username": "sa",
        "password": "ENC(ujMeokIQ9UFHSuBYetfRjQTpZASgaua3)",
        "url": "jdbc:h2:/qdata/c1/db1",
        "autoCreateTables": true
    }
```

Being a Password-Based Encryptor, Jasypt requires a secret key (password) and a configured algorithm to encrypt/decrypt this config entry. This password can either be loaded into Tessera from file system or user input. For file system input, the location of this secret file needs to be set in Environment Variable `TESSERA_CONFIG_SECRET`

If the database password is not being wrapped inside `ENC()` function, Tessera will simply treat it as a plain-text password however this approach is not recommended for production environment.

* Please note at the moment jasypt encryption is only enabled on `jdbc.password` field.

##### Encrypt database password

Download and unzip the [jasypt](http://www.jasypt.org) package. Redirect to bin directory and the follow commands can be used to encrypt a string

```bash
bash-3.2$ ./encrypt.sh input=dbpassword password=quorum

----ENVIRONMENT-----------------

Runtime: Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 25.171-b11 



----ARGUMENTS-------------------

input: dbpassword
password: quorum



----OUTPUT----------------------

rJ70hNidkrpkTwHoVn2sGSp3h3uBWxjb

```

Pick up this output and wrap it inside `ENC()` function, we should have the following `ENC(rJ70hNidkrpkTwHoVn2sGSp3h3uBWxjb)` in the config json file.
 
### Migrating from Constellation to Tessera
Tessera is the service used to provide Quorum with the ability to support private transactions, replacing Constellation.  If you have previously been using Constellation, utilities are provided within Tessera to enable the migration of Constellation configuration and datastores to Tessera compatible formats.  Details on how to use these utilities can be found in the [Tessera Documentation](https://docs.tessera.consensys.net/en/latest/HowTo/MigrateFromConstellation/).

## Further reading
* The [Tessera Documentation](https://docs.tessera.consensys.net/en/latest/) provides additional information on how Tessera works, migrating from Constellation to Tessera, configuration details, and more.
* [Quorum](https://github.com/consensys/quorum/) is an Ethereum-based distributed ledger protocol that uses Tessera to provide transaction privacy.
* Follow the [Quorum Examples](https://github.com/consensys/quorum-examples) to see Tessera in action in a demo Quorum network.

## Reporting Security Bugs
Security is part of our commitment to our users. At Quorum we have a close relationship with the security community, we understand the realm, and encourage security researchers to become part of our mission of building secure reliable software. This section explains how to submit security bugs, and what to expect in return.

All security bugs in Quorum and its ecosystem (Tessera, Constellation, Cakeshop, ..etc) should be reported by email to security-quorum@consensys.net. Please use the prefix [security] in your subject. This email is delivered to Quorum security team. Your email will be acknowledged, and you'll receive a more detailed response to your email as soon as possible indicating the next steps in handling your report. After the initial reply to your report, the security team will endeavor to keep you informed of the progress being made towards a fix and full announcement.

If you have not received a reply to your email or you have not heard from the security team please contact any team member through [Quorum slack security channel](https://www.goquorum.com/slack-inviter). *Please note* that Quorum slack channels are public discussion forum. When escalating to this medium, please do not disclose the details of the issue. Simply state that you're trying to reach a member of the security team.

## Responsible Disclosure Process
Quorum project uses the following responsible disclosure process:

Once the security report is received it is assigned a primary handler. This person coordinates the fix and release process.
The issue is confirmed and a list of affected software is determined.
Code is audited to find any potential similar problems.
If it is determined, in consultation with the submitter, that a CVE-ID is required, the primary handler will trigger the process.
Fixes are applied to the public repository and a new release is issued.
On the date that the fixes are applied, announcements are sent to Quorum-announce.
At this point you would be able to disclose publicly your finding.

*Note:* This process can take some time. Every effort will be made to handle the security bug in as timely a manner as possible, however it's important that we follow the process described above to ensure that disclosures are handled consistently.

## Receiving Security Updates
The best way to receive security announcements is to subscribe to the Quorum-announce mailing list/channel. Any messages pertaining to a security issue will be prefixed with [security].

Comments on This Policy If you have any suggestions to improve this policy, please send an email to info@goquorum.com for discussion.

## Contributing
Tessera is built open source and we welcome external contribution on features and enhancements. Upon review you will be required to complete a Contributor License Agreement (CLA) before we are able to merge. If you have any questions about the contribution process, please feel free to send an email to [info@goquorum.com](mailto:info@goquorum.com). Please see the [Contributors guide](.github/CONTRIBUTING.md) in wiki for more information about the process.

# Getting Help
Stuck at some step? Please join our  <a href="https://www.goquorum.com/slack-inviter" target="_blank" rel="noopener">slack community</a> for support.
