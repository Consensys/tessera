package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.reflect.ReflectCallback;
import java.lang.reflect.Proxy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PartyInfoChecker {

    Logger LOGGER = LoggerFactory.getLogger(PartyInfoChecker.class);

    boolean hasSynced();

    Map<CommunicationType, Class<? extends PartyInfoChecker>> COMMUNICATION_TYPE_TO_IMPL_CLASS_MAPPINGS =
            Optional.of(
                            new EnumMap<CommunicationType, Class<? extends PartyInfoChecker>>(CommunicationType.class) {
                                {
                                    put(CommunicationType.REST, RestPartyInfoChecker.class);
                                    put(CommunicationType.WEB_SOCKET, WebsocketPartyInfoChecker.class);
                                    put(CommunicationType.GRPC, GrpcPartyInfoCheck.class);
                                }
                            })
                    .map(Collections::unmodifiableMap)
                    .get();

    static PartyInfoChecker create(CommunicationType communicationType) {

        LOGGER.info("Creating checker for {}", communicationType);

        Class<? extends PartyInfoChecker> clazz = COMMUNICATION_TYPE_TO_IMPL_CLASS_MAPPINGS.get(communicationType);

        return Optional.of(clazz)
                .map(type -> ReflectCallback.execute(() -> type.getConstructor()))
                .map(c -> ReflectCallback.execute(() -> c.newInstance()))
                .map(
                        o ->
                                Proxy.newProxyInstance(
                                        PartyInfoChecker.class.getClassLoader(),
                                        new Class[] {PartyInfoChecker.class},
                                        (proxy, method, args) -> {
                                            LOGGER.info("Before {} {}", o, method);

                                            Object outcome = method.invoke(o, args);

                                            LOGGER.info("After {} {} Outcome : {}", o, method, outcome);

                                            return outcome;
                                        }))
                .map(PartyInfoChecker.class::cast)
                .get();
    }
}
