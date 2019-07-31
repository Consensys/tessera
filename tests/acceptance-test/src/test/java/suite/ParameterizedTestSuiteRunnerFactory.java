package suite;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

public class ParameterizedTestSuiteRunnerFactory implements ParametersRunnerFactory {

    public Runner createRunnerForTestWithParameters(final TestWithParameters test) throws InitializationError {

        final TestSuite suite =
                new TestSuite(test.getTestClass().getJavaClass(), new AllDefaultPossibilitiesBuilder(true));

        suite.withConfiguration((ProcessConfiguration) test.getParameters().get(0));

        return suite;
    }
}
