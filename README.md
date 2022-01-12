![Build Status](https://github.com/consensys/tessera/actions/workflows/gradle.yml/badge.svg)
[![codecov](https://codecov.io/gh/ConsenSys/tessera/branch/master/graph/badge.svg?token=XMRVPC5FLQ)](https://codecov.io/gh/ConsenSys/tessera)
![Docker Pulls](https://img.shields.io/docker/pulls/quorumengineering/tessera)

# <img src="https://raw.githubusercontent.com/consensys/tessera/master/tessera-logo.png" width="150" height="36"/>

> __Important: Breaking change__ <br/>Users running on [21.10.0](https://github.com/ConsenSys/tessera/releases/tag/tessera-21.10.0) and previous versions will need to perform a database upgrade to work with the latest version of Tessera.<li> For non-H2 users, existing database schema will need to be updated. Execute the appropriate [alter script](ddls/add-codec) provided.</li> <li> For H2 users, a complete database migration is required before running the [alter script](ddls/add-codec). This is due to the considerable number of changes between version 1.4.200 and version 2.0.202 onwards. See more details from [H2 release](https://github.com/h2database/h2database/releases/tag/version-2.0.202) and their recommended [upgrade process](https://h2database.com/html/tutorial.html#upgrade_backup_restore). Example migration scripts can be found [here](ddls/scripts/h2-upgrade.sh)

> __Important: If using version 21.4.1 and earlier__ <br/>Tessera is now released as a zipped distribution instead of an uber jar.  If using version 21.4.1 and earlier, see the [previous README](https://github.com/ConsenSys/tessera/tree/tessera-21.4.1).

Tessera is a stateless Java system that is used to enable the encryption, decryption, and distribution of private transactions for [Quorum](https://github.com/consensys/quorum/) and/or [Besu](http://github.com/hyperledger/besu)

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

## Documentation
[Docs](https://docs.tessera.consensys.net/en/stable/)

## Artefacts

### Runnable distributions

#### Tessera
- [Tessera distribution](https://github.com/consensys/tessera/releases): Start a Tessera node

#### Remote Enclave Server
- [Remote Enclave Server distribution](enclave/enclave-jaxrs): Start a remote enclave

### Optional Artefacts

The following artefacts can be [added to a distribution](#supplementing-the-distribution) to provide additional functionality.

#### Key Vaults
- [Azure](key-vault/azure-key-vault): Add support for key pairs stored in Azure Key Vault 
- [AWS](key-vault/aws-key-vault): Add support for key pairs stored in AWS Secret Store
- [Hashicorp](key-vault/hashicorp-key-vault): Add support for key pairs stored in Hashicorp Vault

#### Encryptors
- [jnacl](encryption/encryption-jnacl): (already included in Tessera and Remote Enclave Server distributions) Add support for NaCl key pairs using [jnacl](https://github.com/neilalexander/jnacl) library
- [Elliptical Curve](encryption/encryption-ec): Add support for elliptic curve key pairs
- [kalium](encryption/encryption-kalium): Add support for NaCl key pairs using [kalium](https://github.com/abstractj/kalium) library

## Prerequisites
- [Java](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
    - Java 11+ (tested up to Java 14), code source is Java 11.

- [Optional: Gradle](https://gradle.org/install/)<br/>
    - If you want to use a locally installed Gradle rather than the included wrapper. Note: wrapper currently uses Gradle 7.0.2.

## Building Tessera from source
To build and install Tessera:
1. Clone this repo
1. Build using the included Gradle Wrapper file
    ```
    ./gradlew build   
    ```

## Installing Tessera
Download and unpack distribution:
```
$ tar xvf tessera-[version].tar
$ tree tessera-[version]
tessera-[version]
├── bin
│   ├── tessera
│   └── tessera.bat
└── lib
    ├── HikariCP-3.2.0.jar
    ...
```
Run Tessera (use correct `/bin` script for your system): 
```
./tessera-[version]/bin/tessera help
```

## Supplementing the distribution

Additional functionality can be added to a distribution by adding `.jar` files to the `/lib` directory.

### Adding Tessera artefacts

Download and unpack the artefact:
```
$ tar xvf aws-key-vault-[version].tar
$ tree aws-key-vault-[version]
aws-key-vault-[version].tar
└── lib
    ├── annotations-2.10.25.jar
    ...
```

Copy the contents of the artefact's `/lib` into the distribution `/lib` (make sure to resolve any version conflicts/duplicated `.jar` files introduced during the copy):

```
 cp -a aws-key-vault-[version]/lib/. tessera-[version]/lib/
```

### Supporting alternate databases

By default, Tessera uses an H2 database.  To use an alternative database, add the necessary drivers to the `lib/` dir:

For example, to use Oracle database:
```
cp ojdbc7.jar tessera-[version]/lib/
```

[DDLs](ddls/create-table) have been provided to help with defining these databases.

Since Tessera 0.7 a timestamp is recorded with each encrypted transaction stored in the Tessera DB.  To update an existing DB to work with Tessera 0.7+, execute one of the provided [alter scripts](ddls/add-timestamp).

## Docker images

* See [quorumengineering/tessera](https://hub.docker.com/repository/docker/quorumengineering/tessera) Docker repository for available images
    * See [docker/README.md](docker) for details on the various images available 

* To build images from source see [docker/README.md](docker)

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
Tessera is built open source and we welcome external contribution on features and enhancements. Upon review you will be required to complete a Contributor License Agreement (CLA) before we are able to merge. If you have any questions about the contribution process, please feel free to send an email to [info@goquorum.com](mailto:info@goquorum.com). Please see the [Contributors guide](.github/CONTRIBUTING.md) for more information about the process.

# Getting Help
Stuck at some step? Please join our  <a href="https://www.goquorum.com/slack-inviter" target="_blank" rel="noopener">slack community</a> for support.
