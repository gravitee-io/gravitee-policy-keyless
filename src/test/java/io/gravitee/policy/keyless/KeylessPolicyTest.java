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

import static io.gravitee.gateway.jupiter.api.policy.SecurityToken.TokenType.NONE;
import static io.gravitee.policy.v3.keyless.KeylessPolicyV3.APPLICATION_NAME_ANONYMOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.jupiter.api.context.ContextAttributes;
import io.gravitee.gateway.jupiter.api.context.HttpExecutionContext;
import io.gravitee.gateway.jupiter.api.context.Request;
import io.gravitee.gateway.jupiter.api.policy.SecurityToken;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void shouldAlwaysReturnEmptySecurityToken() {
        final TestObserver<SecurityToken> obs = cut.extractSecurityToken(ctx).test();

        obs.assertValue(token -> token.getTokenType().equals(NONE.name()));
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
