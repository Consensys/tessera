package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivacyGroupEntityTest {

  @Test
  public void createInstance() {
    final byte[] id = "id".getBytes();
    final byte[] lookupId = "lookupId".getBytes();
    final byte[] data = "data".getBytes();

    final PrivacyGroupEntity entity = new PrivacyGroupEntity();
    entity.setId(id);
    entity.setLookupId(lookupId);
    entity.setData(data);

    assertThat(entity.getId()).isSameAs(id);
    assertThat(entity.getLookupId()).isSameAs(lookupId);
    assertThat(entity.getData()).isSameAs(data);

    final PrivacyGroupEntity anotherEntity = new PrivacyGroupEntity(id, lookupId, data);

    assertThat(anotherEntity.getId()).isSameAs(id);
    assertThat(anotherEntity.getLookupId()).isSameAs(lookupId);
    assertThat(anotherEntity.getData()).isSameAs(data);
  }

  @Test
  public void timestampOnPersist() {
    PrivacyGroupEntity entity = new PrivacyGroupEntity();
    entity.onPersist();
    assertThat(entity.getTimestamp()).isNotNull();
  }

  @Test
  public void testEquals() {
    final Object other = "OTHER";
    final PrivacyGroupEntity pg = new PrivacyGroupEntity();

    assertThat(pg.equals(other)).isFalse();

    pg.setId("id".getBytes());
    assertThat(pg.equals(new PrivacyGroupEntity())).isFalse();

    final PrivacyGroupEntity another = new PrivacyGroupEntity();
    another.setId("id".getBytes());

    assertThat(pg.equals(another)).isTrue();
  }

  @Test
  public void sameObjectHasSameHash() {

    final PrivacyGroupEntity pg = new PrivacyGroupEntity();

    assertThat(pg.hashCode()).isEqualTo(pg.hashCode());
  }
}
