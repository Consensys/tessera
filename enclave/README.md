### Enclave

Some deployments may wish to restrict physical access to private keys enclave can run as as standalone server and tessera instances can connect to the enclave server. There are two server options rest or websockets. 



`java -jar enclave-jaxrs-[version]-server.jar -configfile enclave-rest-config.json`

enclave-rest-config.json
```
{
    "useWhiteList": false,
    "disablePeerDiscovery": false,
    "serverConfigs": [{
            "app": "ENCLAVE",
            "enabled": true,
            "serverAddress":"http://somedomain:8080",
            "communicationType": "REST",
            "bindingAddress": "http://0.0.0.0:8080"
        }],
    "keys": {
        "keyData": [{
                "privateKey": "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=",
                "publicKey": "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="
            }]
    },
    "alwaysSendTo": []
}
```

Tessera applications need to be configured to connect to it's corresponding server instance. When tessera loads it's configuration file if it discovers a server config with apptype ENCLAVE it creates a client that connects to the enclave server instance. 

```
"serverConfigs" : [ {
    "app" : "ENCLAVE",
    "enabled" : true,
    "communicationType" : "REST",
    "bindingAddress" : "http://0.0.0.0:8080",
    "serverAddress" : "http://somedomain:8080"
} ],
... 
```

If no enclave server is defined tessera will assume that its using the deafult in process enclave and that tessera's private keys are colocated with tessera itself. 

