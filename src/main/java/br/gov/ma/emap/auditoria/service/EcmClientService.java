package br.gov.ma.emap.auditoria.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client
        .RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
@Component
@RequiredArgsConstructor
public class EcmClientService {

    private final RestClient ecmRestClient;

    @Value("${ecm.api-path}")
    private String apiPath;

    public String buscarAuditoria(String titulo) {
        return ecmRestClient.get()
                .uri(apiPath + "&audit_titulo={titulo}", titulo)
                .attributes(clientRegistrationId("ecm"))
                .retrieve()
                .body(String.class);
    }
}

