/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.keyless;

import static io.gravitee.gateway.reactive.api.context.InternalContextAttributes.ATTR_INTERNAL_SECURITY_TOKEN;
import static io.gravitee.gateway.reactive.api.policy.SecurityToken.TokenType.NONE;
import static io.gravitee.policy.v3.keyless.KeylessPolicyV3.APPLICATION_NAME_ANONYMOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.reactive.api.context.ContextAttributes;
import io.gravitee.gateway.reactive.api.context.HttpExecutionContext;
import io.gravitee.gateway.reactive.api.context.Request;
import io.gravitee.gateway.reactive.api.policy.SecurityToken;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class KeylessPolicyTest {

    protected static final String REMOTE_ADDRESS = "remoteAddress";

    @Mock
    private Request request;

    @Mock
    private HttpExecutionContext ctx;

    private KeylessPolicy cut;

    @BeforeEach
    void init() {
        cut = new KeylessPolicy();
    }

    @Test
    void shouldSetContextAttributes() {
        when(ctx.request()).thenReturn(request);
        when(request.remoteAddress()).thenReturn(REMOTE_ADDRESS);

        final TestObserver<Void> obs = cut.onRequest(ctx).test();

        obs.assertResult();

        verify(ctx).setAttribute(ContextAttributes.ATTR_APPLICATION, APPLICATION_NAME_ANONYMOUS);
        verify(ctx).setAttribute(ContextAttributes.ATTR_SUBSCRIPTION_ID, REMOTE_ADDRESS);
    }

    @Test
    @DisplayName("Should return a SecurityToken.none() if no SecurityToken was extracted from a previous plan")
    void shouldReturnNoneTypedSecurityTokenWhenNoSecurityTokenInInternalAttributes() {
        final TestObserver<SecurityToken> obs = cut.extractSecurityToken(ctx).test();

        obs.assertValue(token -> token.getTokenType().equals(NONE.name()));
    }

    @Test
    @DisplayName(
        "Should return a SecurityToken.none() if no SecurityToken with a type different from NONE was extracted from a previous plan"
    )
    void shouldReturnNoneTypedSecurityTokenWhenNoneTypedSecurityTokenInInternalAttributes() {
        when(ctx.getInternalAttribute(ATTR_INTERNAL_SECURITY_TOKEN)).thenReturn(SecurityToken.none());
        final TestObserver<SecurityToken> obs = cut.extractSecurityToken(ctx).test();

        obs.assertValue(token -> token.getTokenType().equals(NONE.name()));
    }

    @ParameterizedTest
    @EnumSource(value = SecurityToken.TokenType.class, names = { "CLIENT_ID", "API_KEY" })
    @DisplayName("Should return an empty Maybe if there was a SecurityToken extracted by the previous plan")
    void shouldReturnAnEmptyWhenExtractingSecurityToken(SecurityToken.TokenType type) {
        when(ctx.getInternalAttribute(ATTR_INTERNAL_SECURITY_TOKEN)).thenReturn(new SecurityToken(type, "tokenValue"));
        final TestObserver<SecurityToken> obs = cut.extractSecurityToken(ctx).test();

        obs.assertNoValues();
    }

    @Test
    void shouldReturnOrder1000() {
        assertEquals(1000, cut.order());
    }

    @Test
    void shouldReturnKeylessPolicyId() {
        assertEquals("keyless", cut.id());
    }

    @Test
    void shouldNotValidateSubscription() {
        assertFalse(cut.requireSubscription());
    }
}
