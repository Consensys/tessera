# Nexus
A stateless java application responsible for encryption and decryption of private transaction data and for off-chain private messaging.It is also responsible for generating and managing private key locally in each node in Quorum Network.

## Running Nexus

`java -jar nexus-app/target/nexus-app-${version}-app.jar -configfile config.json`


Probably best to copy the jar somewhere and create an alias

`alias nexus="java -jar /somewhere/nexus-app.jar"`

And add the nexus to your PATH.

If you want to use an alternative database then you'll need to add the drivers to the classpath

`java -cp some-jdbc-driver.jar -jar /somewhere/nexus-app.jar`


## Configuration

A configuration file must be specified using the `-configfile /path/to/config.json`
command line property.

A sample configuration can be found [here](/nexus-config/src/test/resources/sample_full.json)

Keys can be provided using direct values, like in the example above,
or by providing the format produced by previous versions. Just replace the
`privateKey` field with the data in those files under a `config` key.

Below is a sample snippet:

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

If the keys dont already exist they can be generated using the -keygen option. 

```
nexus -configfile config.json -keygen /path/to/config1 /path/to/config2
```

This will check the given paths for configuration of the private keys.
The configuration for these is the same as what is produced for a key, e.g.

Plaintext key:

/path/to/config1
```
{
    "type": "unlocked"
}
```

Password protected key:

/path/to/config2
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

Nexus has two interfaces which allow endpoints from the API to be called.

##### HTTP (Public API)

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

##### Unix Domain Socket (Private API)
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

## API Details

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

## Building Nexus

Checkout nexus from github and build using maven.
Nexus can be built with different nacl implementations:

#### jnacl

`mvn install`

##### kalium

`mvn install -Pkalium`

Note that the Kalium implementation requires that you have sodium installed at runtime (see runtime dependencies below).

## Runtime Dependencies
Nexus has the folllowing runtime dependencies which must be installed.

#### junixsocket
JUnixSocket will unpack required dependencies if they are not found
By default, they get unpacked to /tmp, but this can be
changed by setting the system property "org.newsclub.net.unix.library.path"

Alternatively, you can install the dependency yourself, and point the 
above system property to the install location.

1. Get junixsocket-1.3-bin.tar.bz2 from https://code.google.com/archive/p/junixsocket/downloads
2. Unpack it
4. sudo mkdir -p /opt/newsclub/lib-native
5. sudo cp junixsocket-1.3/lib-native/libjunixsocket-macosx-1.5-x86_64.dylib /opt/newsclub/lib-native/

#### sodium

This is only required if Nexus is built to use the Kalium implementation.
* brew install libsodium

# Swagger api doc

## HTTP | HTTPS://localhost:8080/

**Version** 1.0-SNAPSHOT

[**Terms of Service**]()


# APIs


## /api


### GET

<a id="api"></a>



#### Request



##### Parameters


#### Response

**Content-Type: ** application/json, text/html


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Returns json or html openapi document |  - |

















## /delete






### POST

-deprecated-
<a id="delete">Delete key provided in request. Deprecated: Deletes will be done with DELETE http method</a>









#### Request


**Content-Type: ** application/json

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>deleteRequest</th>
        <td>body</td>
        <td>yes</td>
        <td></td>
        <td> - </td>

        <td>

            <a href="#/definitions/DeleteRequest">DeleteRequest</a> 
        </td>

    </tr>


</table>



#### Response

**Content-Type: ** text/plain


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Status message |  - |
| 404    | If the entity doesn&#x27;t exist |  - |














## /partyinfo






### POST


<a id="partyInfo"></a>









#### Request


**Content-Type: ** application/octet-stream

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>body</th>
        <td>body</td>
        <td>yes</td>
        <td></td>
        <td> - </td>

        <td>
            Array[<a href=""></a>]

        </td>

    </tr>


</table>



#### Response

**Content-Type: ** application/octet-stream


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Endcoded PartyInfo Data |  - |














## /push






### POST


<a id="push"></a>









#### Request


**Content-Type: ** application/octet-stream

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>payload</th>
        <td>body</td>
        <td>yes</td>
        <td>Key data to be stored.</td>
        <td> - </td>

        <td>
            Array[<a href=""></a>]

        </td>

    </tr>


</table>



#### Response




| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 201    | Key created status |  - |
| 500    | General error |  - |














## /receive


### GET
-deprecated-
<a id="receive"></a>









#### Request


**Content-Type: ** application/json

##### Parameters






#### Response

**Content-Type: ** application/json


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| default    | successful operation |  - |

















## /receiveraw


### GET

<a id="receiveRaw">Summit keys to retrieve payload and decrypt it</a>









#### Request


**Content-Type: ** application/octet-stream

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>c11n-key</th>
        <td>header</td>
        <td>yes</td>
        <td>Encoded Sender Public Key</td>
        <td> - </td>


        <td>string </td>


    </tr>

    <tr>
        <th>c11n-to</th>
        <td>header</td>
        <td>no</td>
        <td>Encoded Recipient Public Key</td>
        <td> - </td>


        <td>string </td>


    </tr>


</table>



#### Response

**Content-Type: ** application/octet-stream


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Raw payload |  - |

















## /resend






### POST


<a id="resend"></a>









#### Request


**Content-Type: ** application/json

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>resendRequest</th>
        <td>body</td>
        <td>yes</td>
        <td></td>
        <td> - </td>

        <td>

            <a href="#/definitions/ResendRequest">ResendRequest</a> 
        </td>

    </tr>


</table>



#### Response

**Content-Type: ** text/plain


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Encoded payload when TYPE is INDIVIDUAL |  - |
| 500    | General error |  - |














## /send






### POST


<a id="send"></a>









#### Request


**Content-Type: ** application/json

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>sendRequest</th>
        <td>body</td>
        <td>yes</td>
        <td></td>
        <td> - </td>

        <td>

            <a href="#/definitions/SendRequest">SendRequest</a> 
        </td>

    </tr>


</table>



#### Response

**Content-Type: ** application/json


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Send response | <a href="#/definitions/SendResponse">SendResponse</a>|
| 400    | For unknown and unknown keys |  - |














## /sendraw






### POST


<a id="sendRaw"></a>









#### Request


**Content-Type: ** application/octet-stream

##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>c11n-from</th>
        <td>header</td>
        <td>no</td>
        <td></td>
        <td> - </td>


        <td>string </td>


    </tr>

    <tr>
        <th>c11n-to</th>
        <td>header</td>
        <td>no</td>
        <td></td>
        <td> - </td>


        <td>string </td>


    </tr>


</table>



#### Response

**Content-Type: ** text/plain


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Encoded Key |  - |
| 500    | Unknown server error |  - |














## /transaction/{hash}


### GET

<a id="receive"></a>









#### Request



##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>hash</th>
        <td>path</td>
        <td>yes</td>
        <td>Encoded hash used to decrypt the payload</td>
        <td> - </td>


        <td>string </td>


    </tr>

    <tr>
        <th>to</th>
        <td>query</td>
        <td>no</td>
        <td>Encoded recipient key</td>
        <td> - </td>


        <td>string </td>


    </tr>


</table>



#### Response

**Content-Type: ** application/json


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Receive Response object | <a href="#/definitions/ReceiveResponse">ReceiveResponse</a>|

















## /transaction/{key}








### DELETE

<a id="deleteKey"></a>









#### Request



##### Parameters

<table border="1">
    <tr>
        <th>Name</th>
        <th>Located in</th>
        <th>Required</th>
        <th>Description</th>
        <th>Default</th>
        <th>Schema</th>
    </tr>



    <tr>
        <th>key</th>
        <td>path</td>
        <td>yes</td>
        <td>Encoded hash</td>
        <td> - </td>


        <td>string </td>


    </tr>


</table>



#### Response




| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 204    | Successful deletion |  - |
| 404    | If the entity doesn&#x27;t exist |  - |











## /upcheck


### GET

<a id="upCheck"></a>









#### Request



##### Parameters






#### Response

**Content-Type: ** text/plain


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | I&#x27;m up! | |

















## /version


### GET

<a id="getVersion"></a>









#### Request



##### Parameters






#### Response

**Content-Type: ** text/plain


| Status Code | Reason      | Response Model |
|-------------|-------------|----------------|
| 200    | Current application version  | |


















# Definitions

## <a name="/definitions/DeleteRequest">DeleteRequest</a>

<table border="1">
    <tr>
        <th>name</th>
        <th>type</th>
        <th>required</th>
        <th>description</th>
        <th>example</th>
    </tr>

    <tr>
        <td>Encoded public key</td>
        <td>


            string

        </td>
        <td>required</td>
        <td>-</td>
        <td></td>
    </tr>

</table>

## <a name="/definitions/ReceiveResponse">ReceiveResponse</a>

<table border="1">
    <tr>
        <th>name</th>
        <th>type</th>
        <th>required</th>
        <th>description</th>
        <th>example</th>
    </tr>

    <tr>
        <td>payload</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>Encode response servicing recieve requests</td>
        <td></td>
    </tr>

</table>

## <a name="/definitions/ResendRequest">ResendRequest</a>

<table border="1">
    <tr>
        <th>name</th>
        <th>type</th>
        <th>required</th>
        <th>description</th>
        <th>example</th>
    </tr>

    <tr>
        <td>type</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>Resend type INDIVIDUAL or ALL</td>
        <td></td>
    </tr>

    <tr>
        <td>publicKey</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>TODO: Define this publicKey, what is it?</td>
        <td></td>
    </tr>

    <tr>
        <td>key</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>TODO: Define this key, what is it?</td>
        <td></td>
    </tr>

</table>

## <a name="/definitions/SendRequest">SendRequest</a>

<table border="1">
    <tr>
        <th>name</th>
        <th>type</th>
        <th>required</th>
        <th>description</th>
        <th>example</th>
    </tr>

    <tr>
        <td>payload</td>
        <td>


            string

        </td>
        <td>required</td>
        <td>Encyrpted payload to send to other parties.</td>
        <td></td>
    </tr>

    <tr>
        <td>from</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>Sender public key</td>
        <td></td>
    </tr>

    <tr>
        <td>to</td>
        <td>


            array[string]

        </td>
        <td>optional</td>
        <td>Recipient public keys</td>
        <td></td>
    </tr>

</table>

## <a name="/definitions/SendResponse">SendResponse</a>

<table border="1">
    <tr>
        <th>name</th>
        <th>type</th>
        <th>required</th>
        <th>description</th>
        <th>example</th>
    </tr>

    <tr>
        <td>key</td>
        <td>


            string

        </td>
        <td>optional</td>
        <td>TODO: Define this key as something</td>
        <td></td>
    </tr>

</table>
