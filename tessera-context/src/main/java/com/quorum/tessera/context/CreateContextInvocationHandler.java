package com.quorum.tessera.context;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.reflect.ReflectCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

class CreateContextInvocationHandler implements InvocationHandler {

    private RuntimeContextFactory runtimeContextFactory;

    private final Method target =
            ReflectCallback.execute(() -> RuntimeContextFactory.class.getDeclaredMethod("create", Config.class));

    protected CreateContextInvocationHandler(RuntimeContextFactory runtimeContextFactory) {
        this.runtimeContextFactory = runtimeContextFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object outcome = method.invoke(runtimeContextFactory, args);

        if (Objects.equals(method, target)) {

            RuntimeContext runtimeContext = (RuntimeContext) outcome;
            ContextHolder.INSTANCE.setContext(runtimeContext);

            return runtimeContext;
        }

        return outcome;
    }
}
