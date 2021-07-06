# Enclave jaxrs

A remotable Tessera Enclave that proxies calls to Enclave over http and can be used to separate Transaction Manager and Enclave processes.

## Installing Remote Enclave

Download and unpack distribution:
```
$ tar xvf enclave-jaxrs-[version].tar
$ tree enclave-jaxrs-[version]
enclave-jaxrs-[version]
├── bin
│   ├── enclave-jaxrs
│   └── enclave-jaxrs.bat
└── lib
    ├── aopalliance-repackaged-2.6.1.jar
    ...
```
Run remote Enclave (use correct `/bin` script for your system):
```
./enclave-jaxrs-[version]/bin/enclave-jaxrs --help
```
