package br.gov.ma.emap.auditoria.integration;
import br.gov.ma.emap.auditoria.integration.dto.EcmAddAuditorRequest;
import br.gov.ma.emap.auditoria.integration.dto.EcmAddAuditoriaRequest;
import br.gov.ma.emap.auditoria.integration.dto.EcmAuditoriaResponse;
import br.gov.ma.emap.auditoria.exception.EcmException;
import br.gov.ma.emap.auditoria.integration.dto.EcmRemAuditorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import org.springframework.http.HttpRequest;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.oauth2.client.web.client
        .RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
@Component
@RequiredArgsConstructor
public class EcmClient {

    private final RestClient ecmRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ecm.api-path}")
    private String apiPath;

    public String buscarAuditoria(String titulo) {
        return ecmRestClient.get().uri(apiPath + "&audit_titulo={titulo}", titulo)
                .retrieve().body(String.class);
    }

    public EcmAuditoriaResponse criarAuditoria(EcmAddAuditoriaRequest request) {
        return ecmRestClient.post().uri(apiPath)
                .body(request).retrieve().onStatus(status -> status.isError(), this::tratarErro)
                .body(EcmAuditoriaResponse.class);
    }

    public void adicionarAuditor(EcmAddAuditorRequest request) {
        ecmRestClient.post().uri(apiPath).body(request)
            .retrieve().onStatus(status -> status.isError(), this::tratarErro)
            .toBodilessEntity();
    }

    public void revogarAuditor(EcmRemAuditorRequest request) {
        ecmRestClient.post().uri(apiPath).body(request)
                .retrieve().onStatus(status -> status.isError(), this::tratarErro)
                .toBodilessEntity();
    }

    private void tratarErro(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            JsonNode json = objectMapper.readTree(body);
            String mensagem = json.path("erro").asText();

            if (mensagem.isBlank()) {
                mensagem = "Erro na comunicação com o ECM. HTTP " + response.getStatusCode().value();
            }

            throw new EcmException("ECM: " + mensagem);
        } catch (JacksonException e) {
            throw new EcmException("Erro na comunicação com o ECM. HTTP " +response.getStatusCode().value(), e);
        }
    }

}

