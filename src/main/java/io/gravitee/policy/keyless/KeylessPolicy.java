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

import io.gravitee.gateway.jupiter.api.context.ContextAttributes;
import io.gravitee.gateway.jupiter.api.context.HttpExecutionContext;
import io.gravitee.gateway.jupiter.api.policy.SecurityPolicy;
import io.gravitee.gateway.jupiter.api.policy.SecurityToken;
import io.gravitee.policy.v3.keyless.KeylessPolicyV3;
import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class KeylessPolicy extends KeylessPolicyV3 implements SecurityPolicy {

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
    public Maybe<SecurityToken> extractSecurityToken(HttpExecutionContext ctx) {
        return Maybe.just(SecurityToken.none());
    }

    @Override
    public Completable onRequest(HttpExecutionContext ctx) {
        return handleSecurity(ctx);
    }

    private Completable handleSecurity(final HttpExecutionContext ctx) {
        return Completable.fromRunnable(
            () -> {
                ctx.setAttribute(ContextAttributes.ATTR_APPLICATION, APPLICATION_NAME_ANONYMOUS);
                ctx.setAttribute(ContextAttributes.ATTR_SUBSCRIPTION_ID, ctx.request().remoteAddress());
            }
        );
    }
}
