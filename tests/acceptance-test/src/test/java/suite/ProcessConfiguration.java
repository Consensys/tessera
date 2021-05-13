package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.test.DBType;

/**
 * Sets the runtime properties of the nodes used during the test. This allows the same tests to run
 * with different node configurations. Similar to {@link ProcessConfig}, but can be instantiated for
 * use with parameterized tests.
 */
public class ProcessConfiguration {

  private DBType dbType;

  private CommunicationType communicationType;

  private CommunicationType p2pCommunicationType;

  private SocketType socketType;

  private EnclaveType enclaveType = EnclaveType.LOCAL;

  private boolean admin = false;

  private String prefix = "";

  private boolean p2pSsl = false;

  private EncryptorType encryptorType;

  public ProcessConfiguration(
      final DBType dbType,
      final CommunicationType communicationType,
      final SocketType socketType,
      final EnclaveType enclaveType,
      final boolean admin,
      final String prefix,
      boolean p2pSsl,
      EncryptorType encryptorType) {
    this.dbType = dbType;
    this.communicationType = communicationType;
    this.socketType = socketType;
    this.enclaveType = enclaveType;
    this.admin = admin;
    this.prefix = prefix;
    this.p2pSsl = p2pSsl;
    this.encryptorType = encryptorType;
  }

  public ProcessConfiguration() {}

  public DBType getDbType() {
    return dbType;
  }

  public void setDbType(final DBType dbType) {
    this.dbType = dbType;
  }

  public CommunicationType getCommunicationType() {
    return communicationType;
  }

  public void setCommunicationType(final CommunicationType communicationType) {
    this.communicationType = communicationType;
  }

  public SocketType getSocketType() {
    return socketType;
  }

  public void setSocketType(final SocketType socketType) {
    this.socketType = socketType;
  }

  public EnclaveType getEnclaveType() {
    return enclaveType;
  }

  public void setEnclaveType(final EnclaveType enclaveType) {
    this.enclaveType = enclaveType;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(final boolean admin) {
    this.admin = admin;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public CommunicationType getP2pCommunicationType() {
    return p2pCommunicationType;
  }

  public void setP2pCommunicationType(final CommunicationType p2pCommunicationType) {
    this.p2pCommunicationType = p2pCommunicationType;
  }

  public boolean isP2pSsl() {
    return p2pSsl;
  }

  public EncryptorType getEncryptorType() {
    return encryptorType;
  }

  public void setEncryptorType(EncryptorType encryptorType) {
    this.encryptorType = encryptorType;
  }
}
