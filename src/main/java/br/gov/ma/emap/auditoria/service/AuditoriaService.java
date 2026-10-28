package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.AmostraDTO;
import br.gov.ma.emap.auditoria.dto.enums.CriterioAmostra;
import br.gov.ma.emap.auditoria.dto.enums.StatusAuditoria;
import br.gov.ma.emap.auditoria.dto.request.AuditoriaRequest;
import br.gov.ma.emap.auditoria.dto.response.AuditoriaResponse;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.*;
import br.gov.ma.emap.auditoria.repository.AuditoriaAuditorRepository;
import br.gov.ma.emap.auditoria.repository.AuditoriaHistoricoRepository;
import br.gov.ma.emap.auditoria.repository.AuditoriaLegadoRepository;
import br.gov.ma.emap.auditoria.repository.AuditoriaRepository;
import br.gov.ma.emap.auditoria.utils.AuditoriaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository repository;
    private final UsuarioService usuarioService;
    private final AuditoriaLegadoRepository auditoriaLegadoRepository;
    private final AuditoriaHistoricoRepository auditoriaHistoricoRepository;
    private final AuditoriaAuditorRepository auditoriaAuditorRepository;

    /*@Transactional(readOnly = true)
    public Page<AuditoriaResponse> listarOld(Long campanhaId, Pageable pageable) {
        return repository.findByCampanhaId(campanhaId, pageable).map(AuditoriaResponse::from);
    }*/

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> listar(Long campanhaId, Pageable pageable) {
        Page<Auditoria> auditorias = repository.findByCampanhaId(campanhaId, pageable);
        List<Long> auditoriasIds = auditorias.getContent().stream()
                .map(Auditoria::getId).toList();

        Map<Long, List<AuditoriaAuditor>> auditoresPorAuditoria = auditoriaAuditorRepository.findAtivosByAuditoriasIds(auditoriasIds)
                .stream() .collect(Collectors.groupingBy(aa -> aa.getAuditoria().getId()));

        return auditorias.map(auditoria ->
                AuditoriaResponse.from(auditoria, auditoresPorAuditoria.getOrDefault(auditoria.getId(), List.of()))
        );
    }

    @Transactional(readOnly = true)
    public List<Auditoria> buscarPorCampanha(Campanha campanha) {
        return repository.findByCampanha(campanha);
    }

    @Transactional
    public AuditoriaResponse substituir(Long auditoriaId, AuditoriaRequest request) {
        Auditoria anterior = repository.findById(auditoriaId).orElseThrow(() -> new BusinessException("Auditoria não encontrada"));

        if(anterior.getCampanha().getAprovacao() != null){
            throw new BusinessException("A campanha já foi aprovada e não permite mais substituição");
        }

        if(anterior.getCriterio() != CriterioAmostra.ALEATORIEDADE){
            throw new BusinessException("A substituição só deve ocorrer em processos selecionado pelo critério da aleatoriedade");
        }

        List<Auditoria> auditorias = repository.findByCampanhaAndProcesso(anterior.getCampanha(), request.processo());

        if(!auditorias.isEmpty()){
            throw new BusinessException("Processo já existe nessa campanha");
        }

        Usuario usuario = usuarioService.getUsuarioLogado();

        Auditoria nova = criarNovaAuditoria(anterior, request, usuario);
        nova = repository.save(nova);
        AuditoriaHistorico historico = criarHistorico(anterior, nova, request.observacao(), usuario);
        auditoriaHistoricoRepository.save(historico);
        repository.delete(anterior);

        return AuditoriaResponse.from(nova, List.of());
    }

    private Auditoria criarNovaAuditoria(Auditoria anterior, AuditoriaRequest request, Usuario usuario) {
        return Auditoria.builder().criterio(anterior.getCriterio()).campanha(anterior.getCampanha())
                .processo(request.processo()).contrato(request.contrato()).fornecedor(request.fornecedor())
                .classificacao(request.classificacao()).diretoria(request.diretoria()).valor(request.valor())
                .pagamento(request.pagamento()).status(StatusAuditoria.ABERTA).createdBy(usuario)
                .updatedBy(usuario).build();
    }

    private AuditoriaHistorico criarHistorico(Auditoria origem, Auditoria nova, String motivo, Usuario usuario) {
        return AuditoriaHistorico.builder().auditoriaOriId(origem.getId()).auditoriaDesId(nova.getId())
                .processo(origem.getProcesso()).contrato(origem.getContrato())
                .fornecedor(origem.getFornecedor()).classificacao(origem.getClassificacao()).diretoria(origem.getDiretoria())
                .createdBy(usuario).valor(origem.getValor()).pagamento(origem.getPagamento()).motivo(motivo).build();
    }

    public Set<String> getContratosAuditados(List<Integer> competencias) {
        Set<String> contratosAuditados = new HashSet<>();
        auditoriaLegadoRepository.findByReferenciaIn(competencias)
                .stream().map(AuditoriaLegado::getContrato)
                .map(AuditoriaUtils::normalizar).filter(Objects::nonNull).forEach(contratosAuditados::add);

        repository.findByCampanhaCompetenciaIn(competencias)
                .stream().map(Auditoria::getContrato).map(AuditoriaUtils::normalizar)
                .filter(Objects::nonNull).forEach(contratosAuditados::add);

        return contratosAuditados;
    }

    @Transactional
    public void salvarAuditorias(List<AmostraDTO> amostra, Campanha campanha, Usuario usuario) {
        List<Auditoria> auditorias = amostra.stream().map(dto -> toAuditoria(dto, campanha, usuario)).toList();
        repository.saveAll(auditorias);
    }

    @Transactional
    public void deletarPorCampanhaId(Long campanhaId) {
        repository.deleteByCampanhaId(campanhaId);
    }

    private Auditoria toAuditoria(AmostraDTO dto, Campanha campanha, Usuario usuario) {
        return Auditoria.builder()
                .campanha(campanha)
                .processoId(dto.processoid())
                .processo(dto.processo())
                .contrato(dto.contrato())
                .objeto(dto.objeto())
                .fornecedor(dto.fornecedor())
                .classificacao(dto.classificacao())
                .pagamento(dto.pagamento())
                .departamento(dto.departamento())
                .diretoria(dto.diretoria())
                .valor(dto.valor())
                .criterio(dto.criterio())
                .status(StatusAuditoria.ABERTA)
                .createdBy(usuario)
                .updatedBy(usuario).build();
    }
}