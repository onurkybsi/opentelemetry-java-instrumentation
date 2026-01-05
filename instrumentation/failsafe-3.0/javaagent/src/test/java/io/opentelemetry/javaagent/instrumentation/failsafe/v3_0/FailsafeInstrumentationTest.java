/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.failsafe.v3_0;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.RetryPolicy;
import io.opentelemetry.instrumentation.failsafe.AbstractFailsafeInstrumentationTest;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class FailsafeInstrumentationTest extends AbstractFailsafeInstrumentationTest {
  @RegisterExtension
  static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

  @Override
  protected InstrumentationExtension testing() {
    return testing;
  }

  @Override
  protected CircuitBreaker<Object> configure(CircuitBreaker<Object> userCircuitBreaker) {
    return userCircuitBreaker;
  }

  @Override
  protected RetryPolicy<Object> configure(RetryPolicy<Object> userRetryPolicy) {
    return userRetryPolicy;
  }

  @Test
  public void captureCircuitBreakerMetrics() {
    captureCircuitBreakerMetrics(null);
  }

  @Test
  void captureRetryPolicyMetrics() {
    captureRetryPolicyMetrics(null);
  }
}
