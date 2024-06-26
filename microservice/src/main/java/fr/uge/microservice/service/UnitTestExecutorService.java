package fr.uge.microservice.service;

import fr.uge.microservice.information.UnitTestResultInformation;
import fr.uge.microservice.tool.ByteClassLoader;
import fr.uge.microservice.tool.DynamicJavaClass;
import fr.uge.microservice.tool.JavaClassCompiler;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Service
public class UnitTestExecutorService {
    private static final int TIMEOUT = 10;


    private static String findClassName(String code){
        Pattern classPattern = Pattern.compile("class\\s+([\\w$]+)\\s*(?:implements|extends)?\\s*([\\w$]+)?\\s*\\{");
        Matcher matcher = classPattern.matcher(code);
        if (!matcher.find()){
            throw new IllegalArgumentException("file is not a java class");
        }
        return matcher.group(1);
    }

    public UnitTestResultInformation executeWithVerification(byte[] javaCode, byte[] unitCode) throws TimeoutException {
        UnitTestResultInformation unitTestResultInformation = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<UnitTestResultInformation> task = () -> execute(javaCode, unitCode);
        Future<UnitTestResultInformation> future = executor.submit(task);
        try {
            unitTestResultInformation = future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            future.cancel(true);
        }
        executor.shutdownNow();
        return unitTestResultInformation;
    }


    private UnitTestResultInformation execute(byte[] javaCode, byte[] unitCode){
        try {
            String javaFileCode = new String(javaCode, StandardCharsets.UTF_8);
            String unitFileCode = new String(unitCode, StandardCharsets.UTF_8);
            var javaName = findClassName(javaFileCode);
            var unitName = findClassName(unitFileCode);

            DynamicJavaClass javaFileClass = new DynamicJavaClass(javaName, javaFileCode);
            DynamicJavaClass unitFileClass = new DynamicJavaClass(unitName, unitFileCode);

            Map<String, byte[]> classBytesMap = JavaClassCompiler.compileJavaCodeAndGetBytes(List.of(javaFileClass));
            byte[] classBytes = classBytesMap.get(javaName);


            Map<String, byte[]> testBytesMap = JavaClassCompiler.compileJavaCodeAndGetBytes(List.of(javaFileClass, unitFileClass));
            byte[] testBytes = testBytesMap.get(unitName);

            Class<?> calculatorClass = new ByteClassLoader().defineClass(javaName, classBytes);
            Class<?> calculatorTestClass = new ByteClassLoader(calculatorClass.getClassLoader()).defineClass(unitName, testBytes);

            return executeJUnitTests(calculatorTestClass);

        } catch (IOException e) {
            throw new RuntimeException("compilation error");
        }
    }

    private static UnitTestResultInformation executeJUnitTests(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        TestPlan testPlan = launcher.discover(request);
        launcher.execute(testPlan);
        var summary = listener.getSummary();
        long totalTime = summary.getTimeFinished() - summary.getTimeStarted();
        String failures = summary.getFailures().stream()
                .map(failure -> failure.getTestIdentifier().getDisplayName() + " - " + failure.getException().getLocalizedMessage())
                .collect(Collectors.joining("\n"));
        return new UnitTestResultInformation(
                summary.getTestsFoundCount(),
                summary.getTestsSucceededCount(),
                summary.getTestsFailedCount(),
                totalTime,
                failures
        );
    }
}
