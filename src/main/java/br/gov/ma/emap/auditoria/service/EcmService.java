package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.integration.dto.*;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.integration.EcmClient;
import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.AuditoriaAuditor;
import br.gov.ma.emap.auditoria.model.Campanha;
import br.gov.ma.emap.auditoria.model.Usuario;
import br.gov.ma.emap.auditoria.repository.AuditoriaAuditorRepository;
import br.gov.ma.emap.auditoria.repository.CampanhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EcmService {

    private final EcmClient ecmClient;
    private final CampanhaRepository campanhaRepo;
    private final AuditoriaAuditorRepository aaRepo;
/*
    public Long criarAuditoriaApagar(Long campanhaId) {
        Campanha campanha = campanhaRepo.findById(campanhaId).orElseThrow(() -> new BusinessException("Campanha nao encontrada"));

        List<AuditoriaAuditor> vinculos = aaRepo.findByCampanhaComAuditorEAuditoria(campanha);

        YearMonth competencia = YearMonth.of(campanha.getCompetencia() / 100,campanha.getCompetencia() % 100);

        LocalDate dataInicio = competencia.atDay(1);
        LocalDate dataFim = competencia.atEndOfMonth().plusDays(60);

        Map<Long, List<AuditoriaAuditor>> porAuditor = vinculos.stream().collect(Collectors.groupingBy(
                vinculo -> vinculo.getAuditor().getEcmUserId()
        ));

        List<EcmAuditorRequest> auditores = porAuditor.entrySet().stream().map(entry -> new EcmAuditorRequest(
                entry.getKey(), dataFim, entry.getValue().stream().map(v -> v.getAuditoria().getProcessoId()).toList()
        )).toList();

        EcmAddAuditoriaRequest request = new EcmAddAuditoriaRequest(
                "CRIAR_AUDITORIA",
                "CAMP_" + campanha.getCompetencia(),
                "Auditoria de processos - " + campanha.getCompetencia(),
                dataInicio, dataFim, auditores
        );

        EcmAuditoriaResponse response = ecmClient.criarAuditoria(request);

        return response.cod_audit();
    }
*/
    public String buscarAuditoria(String titulo){
        return ecmClient.buscarAuditoria(titulo);
    }

    public Long criarAuditoria(Campanha campanha, List<AuditoriaAuditor> vinculos) {

        YearMonth competencia = YearMonth.of(campanha.getCompetencia() / 100,campanha.getCompetencia() % 100);

        LocalDate dataInicio = competencia.atDay(1);
        LocalDate dataFim = competencia.atEndOfMonth().plusDays(60);

        Map<Long, List<AuditoriaAuditor>> porAuditor = vinculos.stream().collect(Collectors.groupingBy(
        vinculo -> vinculo.getAuditor().getEcmUserId()
        ));

        List<EcmAuditorRequest> auditores = porAuditor.entrySet().stream().map(entry -> new EcmAuditorRequest(
                entry.getKey(), dataFim, entry.getValue().stream().map(v -> v.getAuditoria().getProcessoId()).toList()
        )).toList();

        EcmAddAuditoriaRequest request = new EcmAddAuditoriaRequest(
                "CRIAR_AUDITORIA",
                "CAMP_" + campanha.getCompetencia(),
                "Auditoria de processos - " + campanha.getCompetencia(),
                dataInicio, dataFim, auditores
        );

        EcmAuditoriaResponse response = ecmClient.criarAuditoria(request);

        return response.cod_audit();
    }

    public void adicionarAuditor(Campanha campanha, Auditoria auditoria, Usuario auditor) {
        YearMonth competencia = YearMonth.of(campanha.getCompetencia() / 100,campanha.getCompetencia() % 100);
        LocalDate dataPrazo = competencia.atEndOfMonth().plusDays(60);
        EcmAddAuditorRequest request = new EcmAddAuditorRequest(
                "ADD_AUDITOR",
                campanha.getEcmCodAudit(),
                auditor.getEcmUserId(),
                dataPrazo,
                List.of(auditoria.getProcessoId())
        );
        ecmClient.adicionarAuditor(request);
    }

    public void revogarAuditor(Campanha campanha, Auditoria auditoria, Usuario auditor) {
        EcmRemAuditorRequest request = new EcmRemAuditorRequest(
                "REVOGAR_PROCESSO",
                campanha.getEcmCodAudit(),
                auditor.getEcmUserId(),
                List.of(auditoria.getProcessoId())
        );
        ecmClient.revogarAuditor(request);
    }
}