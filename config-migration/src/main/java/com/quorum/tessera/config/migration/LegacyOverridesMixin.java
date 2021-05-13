package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslTrustMode;
import java.util.List;
import picocli.CommandLine.Option;

public class LegacyOverridesMixin {

  @Option(
      names = "--workdir",
      arity = "1",
      description =
          "Working directory to use (relative paths specified for other options are relative to the working directory)")
  public String workdir;

  @Option(
      names = "--url",
      arity = "1",
      description = "URL for this node (i.e. the address you want advertised)")
  public String url;

  @Option(names = "--port", arity = "1", description = "Port to listen on for the public API")
  public Integer port;

  @Option(
      names = "--socket",
      arity = "1",
      description = "Path to IPC socket file to create for private API access")
  public String socket;

  @Option(
      names = "--othernodes",
      split = ",",
      description = "Comma-separated list of other node URLs to connect at startup")
  public List<String> othernodes;

  @Option(
      names = "--publickeys",
      split = ",",
      description = "Comma-separated list of paths to public keys to advertise")
  public List<String> publickeys;

  @Option(
      names = "--privatekeys",
      split = ",",
      description =
          "Comma-separated list of paths to private keys (these must be given in the same corresponding order as --publickeys)")
  public List<String> privatekeys;

  @Option(
      names = "--passwords",
      arity = "1",
      description = "The file containing the passwords for the privatekeys")
  public String passwords;

  @Option(
      names = "--alwayssendto",
      split = ",",
      description =
          "Comma-separated list of paths to public keys that are always included as recipients")
  public List<String> alwayssendto;

  @Option(
      names = "--storage",
      arity = "1",
      description = "Storage string specifying a storage engine and/or path")
  public String storage;

  @Option(names = "--tls", arity = "1", description = "TLS status (strict, off)")
  public SslAuthenticationMode tls;

  @Option(
      names = "--tlsservercert",
      arity = "1",
      description = "TLS certificate file to use for the public API")
  public String tlsservercert;

  @Option(
      names = "--tlsserverchain",
      split = ",",
      description = "Comma separated list of TLS chain certificate files to use for the public API")
  public List<String> tlsserverchain;

  @Option(
      names = "--tlsserverkey",
      arity = "1",
      description = "TLS key file to use for the public API")
  public String tlsserverkey;

  @Option(
      names = "--tlsservertrust",
      arity = "1",
      description =
          "TLS server trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
  public SslTrustMode tlsservertrust;

  @Option(
      names = "--tlsknownclients",
      arity = "1",
      description =
          "TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes")
  public String tlsknownclients;

  @Option(
      names = "--tlsclientcert",
      arity = "1",
      description = "TLS client certificate file to use for connections to other nodes")
  public String tlsclientcert;

  @Option(
      names = "--tlsclientchain",
      split = ",",
      description =
          "Comma separated list of TLS chain certificate files to use for connections to other nodes")
  public List<String> tlsclientchain;

  @Option(
      names = "--tlsclientkey",
      arity = "1",
      description = "TLS key file to use for connections to other nodes")
  public String tlsclientkey;

  @Option(
      names = "--tlsclienttrust",
      arity = "1",
      description =
          "TLS client trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
  public SslTrustMode tlsclienttrust;

  @Option(
      names = "--tlsknownservers",
      arity = "1",
      description =
          "TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes")
  public String tlsknownservers;

  @Option(
      names = "--ipwhitelist",
      description = "If provided, Tessera will use the othernodes as a whitelist.")
  public boolean whitelist;
}
