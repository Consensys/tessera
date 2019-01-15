backend "file" {
	path = "${vaultPath}"
}

listener "tcp" {
	tls_min_version = "tls12"
	tls_cert_file = "/Users/chrishounsom/Desktop/san1.crt"
	tls_key_file = "/Users/chrishounsom/Desktop/san1.key"
	tls_require_and_verify_client_cert = "true"
	tls_client_ca_file = "/Users/chrishounsom/Desktop/san2.crt"
}
