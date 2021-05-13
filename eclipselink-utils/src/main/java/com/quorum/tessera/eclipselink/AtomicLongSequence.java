package com.quorum.tessera.eclipselink;

import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.Session;

/**
 * Quick and dirty sequence generator for staging data. Cannot be used for non staging use cass as
 * sequence only lives for duration of process.
 *
 * <h6>Usage</h6>
 *
 * <pre>
 * @Id
 * @GeneratedValue(generator = "ATOMIC_LONG",strategy = GenerationType.AUTO)
 * private Long id;
 * </pre>
 *
 * <p>In persistance.xml or properties map
 *
 * <pre>
 * eclipselink.session.customizer=com.quorum.tessera.eclipselink.AtomicLongSequence
 * </pre>
 */
public class AtomicLongSequence extends Sequence implements SessionCustomizer {

  private static final AtomicLong SEQUENCE = new AtomicLong();

  protected static final String SEQUENCE_NAME = "ATOMIC_LONG";

  public AtomicLongSequence() {
    super(SEQUENCE_NAME);
  }

  @Override
  public void customize(Session session) throws Exception {

    Optional.of(session).map(Session::getLogin).ifPresent(l -> l.addSequence(this));
  }

  @Override
  public boolean shouldAcquireValueAfterInsert() {
    return false;
  }

  @Override
  public boolean shouldUseTransaction() {
    return false;
  }

  @Override
  public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {
    return SEQUENCE.incrementAndGet();
  }

  @Override
  public Vector getGeneratedVector(
      Accessor accessor, AbstractSession writeSession, String seqName, int size) {
    throw new UnsupportedOperationException("");
  }

  @Override
  public void onConnect() {}

  @Override
  public void onDisconnect() {}

  @Override
  public boolean shouldUsePreallocation() {
    return false;
  }
}
