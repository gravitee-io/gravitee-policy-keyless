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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayConfigurationBuilder;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.ExecutionMode;
import io.gravitee.definition.model.Plan;
import io.gravitee.policy.api.PolicyConfiguration;
import io.reactivex.observers.TestObserver;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author GraviteeSource Team
 */
@GatewayTest
@DeployApi("/apis/keyless.json")
public class KeylessPolicyIntegrationTest extends AbstractPolicyTest<KeylessPolicy, PolicyConfiguration> {

    @Override
    protected void configureGateway(GatewayConfigurationBuilder gatewayConfigurationBuilder) {
        super.configureGateway(gatewayConfigurationBuilder);
        gatewayConfigurationBuilder.set("api.jupiterMode.enabled", "true");
    }

    /**
     * Override api plans to have a published KEY_LESS one.
     * @param api is the api to apply this function code
     */
    @Override
    public void configureApi(Api api) {
        Plan keylessPlan = new Plan();
        keylessPlan.setId("plan-id");
        keylessPlan.setApi(api.getId());
        keylessPlan.setSecurity("KEY_LESS");
        keylessPlan.setStatus("PUBLISHED");
        api.setPlans(Collections.singletonList(keylessPlan));
        api.setExecutionMode(ExecutionMode.JUPITER);
    }

    @Test
    @DisplayName("Should access API")
    void shouldAccessApi(WebClient client) {
        wiremock.stubFor(get("/team").willReturn(ok("response from backend")));

        final TestObserver<HttpResponse<Buffer>> obs = client.get("/test").rxSend().test();

        awaitTerminalEvent(obs)
            .assertComplete()
            .assertValue(
                response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.bodyAsString()).isEqualTo("response from backend");
                    return true;
                }
            )
            .assertNoErrors();

        wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
    }
}
