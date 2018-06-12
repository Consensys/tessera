package com.github.nexus.configuration;

import org.junit.Before;
import org.junit.Test;

import javax.el.ELContext;
import javax.el.FunctionMapper;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class FileLoadingInterceptorTest {

    private FileLoadingInterceptor fileLoadingInterceptor;

    @Before
    public void init() throws NoSuchMethodException {
        this.fileLoadingInterceptor = new FileLoadingInterceptor();
    }

    @Test
    public void loadingFileSuccess() throws IOException {

        final String file = FileLoadingInterceptor.loadFile("./src/test/resources/other-config-file.yml");

        assertThat(file).isEqualTo("publicKeys: other-config-file-value\nprivateKeys: other-config-file-value");

    }

    @Test
    public void loadingFileFailureThrowsException() {

        final Throwable throwable = catchThrowable(() -> FileLoadingInterceptor.loadFile("./"));

        assertThat(throwable).isInstanceOf(IOException.class);

    }

    @Test
    public void methodRegisters() throws NoSuchMethodException {
        final Method expectedMethod = FileLoadingInterceptor.class.getDeclaredMethod("loadFile", String.class);

        final ELContext context = mock(ELContext.class);
        final FunctionMapper mapper = mock(FunctionMapper.class);

        doReturn(mapper).when(context).getFunctionMapper();

        fileLoadingInterceptor.register(context);

        verify(context).getFunctionMapper();
        verify(mapper).mapFunction("", "file", expectedMethod);

    }

}
