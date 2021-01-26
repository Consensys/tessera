package net.consensys.tessera.migration.data;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OrionEventTest {

    private Disruptor<OrionEvent> disruptor;

    @Before
    public void beforeTest() {
        disruptor = new Disruptor<>(
            OrionEvent.FACTORY,
            16,
            (ThreadFactory) Thread::new,
            ProducerType.SINGLE,
            new BlockingWaitStrategy());
    }

    @After
    public void afterTest() {
        disruptor.shutdown();
    }

    @Test
    public void eventTranslatorWorksAsExpected() throws Exception {


        PayloadType payloadType = PayloadType.ENCRYPTED_PAYLOAD;
        JsonObject jsonObject = mock(JsonObject.class);


        Map<PublicKey, RecipientBox> recipientBoxMap = Map.of();

        OrionEvent orionEvent = new OrionEvent(payloadType,jsonObject,"KEY".getBytes(),1L,1L, recipientBoxMap);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            assertThat(event.getKey()).isEqualTo(orionEvent.getKey());
            assertThat(event.getJsonObject()).isSameAs(jsonObject);
            assertThat(event.getPayloadType()).isEqualTo(payloadType);
            countDownLatch.countDown();
        });

        disruptor.start();
        disruptor.publishEvent(orionEvent);

        assertThat(countDownLatch.await(2, TimeUnit.SECONDS)).isTrue();

    }

}
