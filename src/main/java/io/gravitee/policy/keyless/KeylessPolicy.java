/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.keyless;

import static io.gravitee.gateway.reactive.api.context.InternalContextAttributes.ATTR_INTERNAL_SECURITY_TOKEN;

import io.gravitee.gateway.reactive.api.context.ContextAttributes;
import io.gravitee.gateway.reactive.api.context.base.BaseExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.context.kafka.KafkaConnectionContext;
import io.gravitee.gateway.reactive.api.policy.SecurityToken;
import io.gravitee.gateway.reactive.api.policy.http.HttpSecurityPolicy;
import io.gravitee.gateway.reactive.api.policy.kafka.KafkaSecurityPolicy;
import io.gravitee.policy.v3.keyless.KeylessPolicyV3;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class KeylessPolicy extends KeylessPolicyV3 implements HttpSecurityPolicy, KafkaSecurityPolicy {

    @Override
    public String id() {
        return "keyless";
    }

    @Override
    public int order() {
        return 1000;
    }

    @Override
    public boolean requireSubscription() {
        return false;
    }

    @Override
    public Maybe<SecurityToken> extractSecurityToken(HttpPlainExecutionContext ctx) {
        return getSecurityTokenFromContext(ctx);
    }

    @Override
    public Maybe<SecurityToken> extractSecurityToken(KafkaConnectionContext ctx) {
        return getSecurityTokenFromContext(ctx);
    }

    private Maybe<SecurityToken> getSecurityTokenFromContext(BaseExecutionContext ctx) {
        // This token is present in internal attributes if a previous SecurityPolicy has extracted a SecurityToken
        final SecurityToken securityToken = ctx.getInternalAttribute(ATTR_INTERNAL_SECURITY_TOKEN);
        // If it is present with a type different from NONE, then we should not execute this KeylessPlan.
        // Indeed, it means that a request was attempted with an authentication purpose, so we should not let it pass.
        return securityToken != null && !SecurityToken.TokenType.NONE.name().equals(securityToken.getTokenType())
            ? Maybe.empty()
            : Maybe.just(SecurityToken.none());
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return handleSecurity(ctx);
    }

    @Override
    public Completable authenticate(KafkaConnectionContext ctx) {
        return handleSecurity(ctx);
    }

    private Completable handleSecurity(final BaseExecutionContext ctx) {
        return Completable.fromRunnable(() -> {
            ctx.setAttribute(ContextAttributes.ATTR_APPLICATION, APPLICATION_NAME_ANONYMOUS);
            ctx.setAttribute(ContextAttributes.ATTR_SUBSCRIPTION_ID, ctx.remoteAddress());
        });
    }
}
