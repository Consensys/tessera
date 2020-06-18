package com.quorum.tessera.context;

import com.quorum.tessera.reflect.ReflectCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class CreateContextInvocationHandler implements InvocationHandler {

    private RuntimeContextFactory runtimeContextFactory;

    private final Method target =
            ReflectCallback.execute(() -> RuntimeContextFactory.class.getDeclaredMethod("create", Object.class));

    protected CreateContextInvocationHandler(RuntimeContextFactory runtimeContextFactory) {
        this.runtimeContextFactory = runtimeContextFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        final Object outcome;
        try {
            outcome = method.invoke(runtimeContextFactory, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }

        if (Objects.equals(method, target)) {
            if(DefaultContextHolder.INSTANCE.getContext().isPresent()) {
                return outcome;
            }

            RuntimeContext runtimeContext = (RuntimeContext) outcome;
            DefaultContextHolder.INSTANCE.setContext(runtimeContext);

            return runtimeContext;
        }

        return outcome;
    }
}
