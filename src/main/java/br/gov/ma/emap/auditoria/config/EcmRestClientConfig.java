package br.gov.ma.emap.auditoria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration
public class EcmRestClientConfig {

    @Bean
    public RestClient ecmRestClient(OAuth2AuthorizedClientManager authorizedClientManager, @Value("${ecm.base-url}") String baseUrl) {
        OAuth2ClientHttpRequestInterceptor oauth2 = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        return RestClient.builder().baseUrl(baseUrl)
                .requestInterceptor(oauth2)
                .defaultRequest(request -> request.attributes(clientRegistrationId("ecm")))
                .build();
    }
}
