package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface PrivacyGroup {

  Id getId();

  String getName();

  String getDescription();

  List<PublicKey> getMembers();

  byte[] getSeed();

  Type getType();

  State getState();

  interface Id {

    byte[] getBytes();

    String getBase64();

    static Id fromBytes(final byte[] data) {

      return new Id() {

        @Override
        public byte[] getBytes() {
          return data;
        }

        @Override
        public String getBase64() {
          return Base64.getEncoder().encodeToString(data);
        }

        @Override
        public boolean equals(Object arg0) {
          return getClass().isInstance(arg0)
              && Arrays.equals(data, getClass().cast(arg0).getBytes());
        }

        @Override
        public int hashCode() {
          return Arrays.hashCode(data);
        }

        @Override
        public String toString() {
          final String typeName =
              Stream.of(getClass())
                  .map(Class::getInterfaces)
                  .flatMap(Stream::of)
                  .map(Class::getSimpleName)
                  .findFirst()
                  .get();
          return String.format("%s[%s]", typeName, getBase64());
        }
      };
    }

    static Id fromBase64String(final String base64Data) {
      return fromBytes(Base64.getDecoder().decode(base64Data));
    }
  }

  enum Type {
    LEGACY,
    PANTHEON,
    RESIDENT
  }

  enum State {
    ACTIVE,
    DELETED
  }

  class Builder {

    private Id privacyGroupId;

    private String name = "";

    private String description = "";

    private List<PublicKey> members;

    private byte[] seed = new byte[0];

    private Type type;

    private State state;

    public static Builder create() {
      return new Builder() {};
    }

    public Builder withPrivacyGroupId(final Id privacyGroupId) {
      this.privacyGroupId = privacyGroupId;
      return this;
    }

    public Builder withPrivacyGroupId(final byte[] privacyGroupId) {
      return withPrivacyGroupId(PrivacyGroup.Id.fromBytes(privacyGroupId));
    }

    public Builder withPrivacyGroupId(final String privacyGroupId) {
      return withPrivacyGroupId(PrivacyGroup.Id.fromBase64String(privacyGroupId));
    }

    public Builder withName(final String name) {
      this.name = name;
      return this;
    }

    public Builder withDescription(final String description) {
      this.description = description;
      return this;
    }

    public Builder withMembers(final List<PublicKey> members) {
      this.members = members;
      return this;
    }

    public Builder withSeed(final byte[] seed) {
      this.seed = seed;
      return this;
    }

    public Builder withType(final Type type) {
      this.type = type;
      return this;
    }

    public Builder withState(final State state) {
      this.state = state;
      return this;
    }

    public Builder from(final PrivacyGroup privacyGroup) {
      this.privacyGroupId = privacyGroup.getId();
      this.name = privacyGroup.getName();
      this.description = privacyGroup.getDescription();
      this.members = privacyGroup.getMembers();
      this.seed = privacyGroup.getSeed();
      this.type = privacyGroup.getType();
      this.state = privacyGroup.getState();
      return this;
    }

    public static PrivacyGroup buildResidentGroup(
        String name, String desc, List<PublicKey> members) {
      return create()
          .withPrivacyGroupId(name.getBytes())
          .withName(name)
          .withDescription(desc)
          .withMembers(members)
          .withState(State.ACTIVE)
          .withType(Type.RESIDENT)
          .build();
    }

    public PrivacyGroup build() {

      Objects.requireNonNull(privacyGroupId);
      Objects.requireNonNull(members);
      Objects.requireNonNull(type);
      Objects.requireNonNull(state);

      return new PrivacyGroup() {
        @Override
        public Id getId() {
          return privacyGroupId;
        }

        @Override
        public String getName() {
          return name;
        }

        @Override
        public String getDescription() {
          return description;
        }

        @Override
        public List<PublicKey> getMembers() {
          return List.copyOf(members);
        }

        @Override
        public byte[] getSeed() {
          return Arrays.copyOf(seed, seed.length);
        }

        @Override
        public Type getType() {
          return type;
        }

        @Override
        public State getState() {
          return state;
        }
      };
    }
  }
}
