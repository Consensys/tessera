package com.quorum.tessera.encryption;

import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.quorum.tessera.nacl.Nonce;
import java.util.HashMap;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class RawTransactionTest {

    @Test
    public void openPojoTests() {

        Validator pojoValidator = ValidatorBuilder.create()
                .with(new GetterTester())
                .with(new EqualsAndHashCodeMatchRule())
                .build();

        pojoValidator.validate(RawTransaction.class.getPackage().getName(), new FilterClassName("RawTransaction"));
    }

    @Test
    public void equalsAndHashCode() {

        Nonce nonce = new Nonce("NONCE".getBytes());
        PublicKey someKey = PublicKey.from("SOMEKEY".getBytes());
        RawTransaction txn = new RawTransaction("ONE".getBytes(), "KEY".getBytes(), nonce, someKey);

        assertThat(txn).isEqualTo(txn);
        assertThat(txn).isNotEqualTo(null);
        assertThat(txn).isNotEqualTo(new HashMap());
        assertThat(txn.hashCode()).isEqualTo(txn.hashCode());

        RawTransaction othertxn = new RawTransaction("ONE".getBytes(), "KEY".getBytes(), nonce, someKey);
        assertThat(txn).isEqualTo(othertxn);

        RawTransaction othertxnDifferentKey = new RawTransaction("ONE".getBytes(), "OTHER".getBytes(), nonce, someKey);
        assertThat(txn).isNotEqualTo(othertxnDifferentKey);

        RawTransaction othertxnDifferentPayload = new RawTransaction("OTHER".getBytes(), "KEY".getBytes(), nonce, someKey);
        assertThat(txn).isNotEqualTo(othertxnDifferentPayload);

        RawTransaction othertxnDifferentNonce = new RawTransaction("ONE".getBytes(), "KEY".getBytes(), new Nonce("Nonce".getBytes()), someKey);
        assertThat(txn).isNotEqualTo(othertxnDifferentNonce);

        RawTransaction othertxnDifferentFrom = new RawTransaction("ONE".getBytes(), "KEY".getBytes(), nonce, PublicKey.from("OTHER".getBytes()));

        assertThat(txn).isNotEqualTo(othertxnDifferentFrom);

    }

}
