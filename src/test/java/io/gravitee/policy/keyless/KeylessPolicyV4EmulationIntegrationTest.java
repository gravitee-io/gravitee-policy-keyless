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
import static io.vertx.core.http.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayConfigurationBuilder;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.ExecutionMode;
import io.gravitee.definition.model.Plan;
import io.gravitee.policy.api.PolicyConfiguration;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author GraviteeSource Team
 */
@GatewayTest
@DeployApi("/apis/keyless.json")
public class KeylessPolicyV4EmulationIntegrationTest extends AbstractPolicyTest<KeylessPolicy, PolicyConfiguration> {

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
    }

    @Test
    @DisplayName("Should access API")
    void shouldAccessApi(HttpClient client) throws InterruptedException {
        wiremock.stubFor(get("/team").willReturn(ok("response from backend")));

        client
            .rxRequest(GET, "/test")
            .flatMap(HttpClientRequest::rxSend)
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.toFlowable();
            })
            .test()
            .await()
            .assertComplete()
            .assertValue(body -> {
                assertThat(body).hasToString("response from backend");
                return true;
            });

        wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
    }
}
