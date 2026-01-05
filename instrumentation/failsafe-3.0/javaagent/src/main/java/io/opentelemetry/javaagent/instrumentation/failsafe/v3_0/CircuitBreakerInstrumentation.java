/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.failsafe.v3_0;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import dev.failsafe.CircuitBreakerConfig;
import dev.failsafe.PolicyConfig;
import dev.failsafe.internal.CircuitBreakerImpl;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.failsafe.v3_0.FailsafeTelemetry;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.lang.reflect.Field;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

final class CircuitBreakerInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("dev.failsafe.CircuitBreakerBuilder");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("build").and(takesNoArguments()), this.getClass().getName() + "$BuildAdvice");
  }

  static final class BuildAdvice {
    @Advice.OnMethodExit
    public static void onExit(@Advice.Return Object circuitBreakerImpl)
        throws NoSuchFieldException, IllegalAccessException {
      CircuitBreakerImpl<?> impl = (CircuitBreakerImpl<?>) circuitBreakerImpl;
      FailsafeTelemetry failsafeTelemetry = FailsafeTelemetry.create(GlobalOpenTelemetry.get());

      Field failureListenerField = PolicyConfig.class.getDeclaredField("failureListener");
      failureListenerField.setAccessible(true);
      failureListenerField.set(
          impl.getConfig(),
          failsafeTelemetry.createInstrumentedFailureListener(impl.getConfig(), impl.toString()));

      Field successListenerField = PolicyConfig.class.getDeclaredField("successListener");
      successListenerField.setAccessible(true);
      successListenerField.set(
          impl.getConfig(),
          failsafeTelemetry.createInstrumentedSuccessListener(impl.getConfig(), impl.toString()));

      Field openListenerField = CircuitBreakerConfig.class.getDeclaredField("openListener");
      openListenerField.setAccessible(true);
      openListenerField.set(
          impl.getConfig(),
          failsafeTelemetry.createInstrumentedOpenListener(impl.getConfig(), impl.toString()));

      Field halfOpenListenerField = CircuitBreakerConfig.class.getDeclaredField("halfOpenListener");
      halfOpenListenerField.setAccessible(true);
      halfOpenListenerField.set(
          impl.getConfig(),
          failsafeTelemetry.createInstrumentedHalfOpenListener(impl.getConfig(), impl.toString()));

      Field closeListenerField = CircuitBreakerConfig.class.getDeclaredField("closeListener");
      closeListenerField.setAccessible(true);
      closeListenerField.set(
          impl.getConfig(),
          failsafeTelemetry.createInstrumentedCloseListener(impl.getConfig(), impl.toString()));
    }
  }
}
