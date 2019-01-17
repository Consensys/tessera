backend "file" {
	path = "${vaultPath}"
}

listener "tcp" {
	tls_min_version = "tls12"
	tls_cert_file = "${vaultCert}"
	tls_key_file = "${vaultKey}"
	tls_require_and_verify_client_cert = "true"
	tls_client_ca_file = "${clientCert}"
}

disable_mlock = "true"