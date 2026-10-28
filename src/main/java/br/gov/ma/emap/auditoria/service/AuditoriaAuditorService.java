package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
import br.gov.ma.emap.auditoria.dto.request.DistribuicaoRequest;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.Auditoria;
import br.gov.ma.emap.auditoria.model.AuditoriaAuditor;
import br.gov.ma.emap.auditoria.model.Campanha;
import br.gov.ma.emap.auditoria.model.Usuario;
import br.gov.ma.emap.auditoria.repository.AuditoriaAuditorRepository;
import br.gov.ma.emap.auditoria.repository.AuditoriaRepository;
import br.gov.ma.emap.auditoria.repository.CampanhaRepository;
import br.gov.ma.emap.auditoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditoriaAuditorService {

    private final AuditoriaAuditorRepository repository;
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CampanhaRepository campanhaRepository;
    private final EcmService ecmService;

    @Transactional
    public void adicionar(Long auditoriaId, Long auditorId) {

        Auditoria auditoria = buscarAuditoria(auditoriaId);

        Campanha campanha = buscarCampanhaAprovada(auditoria.getCampanha().getId());

        Usuario auditor = buscarAuditorParaAtribuicao(auditorId);
        verificarDuplicidade(auditoria, auditor);
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        AuditoriaAuditor vinculo = AuditoriaAuditor.criar(auditoria, auditor, usuarioLogado);
        repository.save(vinculo);

        ecmService.adicionarAuditor(campanha, auditoria, auditor);
        campanha.setEcmLastSync(OffsetDateTime.now());
    }

    @Transactional
    public void encerrar(Long auditoriaId, Long auditorId, String motivoEncerramento) {

        Auditoria auditoria = buscarAuditoria(auditoriaId);
        Usuario auditor = buscarUsuario(auditorId);

        AuditoriaAuditor vinculo = repository.findByAuditoriaAndAuditorAndFimIsNull(auditoria, auditor).orElseThrow(() ->
                new BusinessException("Auditor não possui vínculo ativo com a auditoria")
        );

        Campanha campanha = buscarCampanhaAprovada(auditoria.getCampanha().getId());

        OffsetDateTime agora = OffsetDateTime.now();

        vinculo.setFim(agora);
        vinculo.setMotivoEncerramento(motivoEncerramento);
        vinculo.setUpdatedBy(usuarioService.getUsuarioLogado());

        ecmService.revogarAuditor(campanha, auditoria, auditor);
        campanha.setEcmLastSync(agora);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaAuditor> listarAtivos(Long auditoriaId) {
        Auditoria auditoria = buscarAuditoria(auditoriaId);
        return repository.findByAuditoriaAndFimIsNull(auditoria);
    }

    @Transactional
    public void distribuirUniformemente(Long campanhaId) {
        Campanha campanha = buscarCampanhaAprovada(campanhaId);

        if (repository.existsByAuditoriaCampanha(campanha)) {
            throw new BusinessException("A campanha já possui distribuição de auditores");
        }

        List<Auditoria> auditorias = auditoriaRepository.findByCampanha(campanha);

        if (auditorias.isEmpty()) {
            throw new BusinessException("A campanha não possui auditorias para distribuição");
        }

        //List<Usuario> auditores = usuarioRepository.findDistinctByAtivoTrueAndPerfisNome(TipoPerfil.AUDITOR);
        List<Usuario> auditores = usuarioRepository.findAtivosByPerfilComPerfis(TipoPerfil.AUDITOR);

        if (auditores.isEmpty()) {
            throw new BusinessException("Não existem auditores disponíveis para distribuição");
        }

        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        List<AuditoriaAuditor> vinculos = new ArrayList<>();

        for (int i = 0; i < auditorias.size(); i++) {
            Auditoria auditoria = auditorias.get(i);
            Usuario auditor = auditores.get(i % auditores.size());
            AuditoriaAuditor vinculo = AuditoriaAuditor.criar(auditoria, auditor, usuarioLogado);
            vinculos.add(vinculo);
        }

        repository.saveAll(vinculos);
        Long codAuditEcm = ecmService.criarAuditoria(campanha, vinculos);
        campanha.setEcmCodAudit(codAuditEcm);
        campanha.setEcmLastSync(OffsetDateTime.now());
    }

    private void verificarDuplicidade(Auditoria auditoria, Usuario auditor) {
        if (repository.existsByAuditoriaAndAuditorAndFimIsNull(auditoria, auditor)) {
            throw new BusinessException("Auditor já está vinculado a esta auditoria");
        }
    }

    private Auditoria buscarAuditoria(Long id) {
        return auditoriaRepository.findById(id).orElseThrow(() ->new BusinessException("Auditoria não encontrada"));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    private Campanha buscarCampanhaAprovada(Long id) {
        return campanhaRepository.findByIdAndAprovacaoIsNotNull(id).orElseThrow(() ->
            new BusinessException("Campanha não encontrada ou não aprovada")
        );
    }

    private Usuario buscarAuditorParaAtribuicao(Long id) {
        Usuario auditor = buscarUsuario(id);
        boolean possuiPerfilAuditor = auditor.getPerfis().stream().anyMatch(p -> p.getNome() == TipoPerfil.AUDITOR);
        if (!possuiPerfilAuditor)
            throw new BusinessException("Usuário não possui perfil de auditor");

        if (!Boolean.TRUE.equals(auditor.getAtivo()))
            throw new BusinessException("Auditor está inativo");

        return auditor;
    }

    @Transactional
    public void distribuirManualmente(Long campanhaId, List<DistribuicaoRequest> distribuicoes) {

        Campanha campanha = buscarCampanhaAprovada(campanhaId);

        if (campanha.getEcmCodAudit() != null) {
            throw new BusinessException("A campanha já possui distribuição de auditores");
        }

        if (distribuicoes == null || distribuicoes.isEmpty()) {
            throw new BusinessException("Nenhuma distribuição de auditor foi informada");
        }

        List<Auditoria> auditorias = auditoriaRepository.findByCampanha(campanha);

        if (auditorias.isEmpty()) {
            throw new BusinessException("A campanha não possui auditorias para distribuição");
        }

        List<Usuario> auditores = usuarioRepository.findAtivosByPerfilComPerfis(TipoPerfil.AUDITOR);

        if (auditores.isEmpty()) {
            throw new BusinessException("Não existem auditores disponíveis para distribuição");
        }

        Map<Long, Auditoria> auditoriasPorId = auditorias.stream().collect(Collectors.toMap(
            Auditoria::getId, Function.identity()
        ));

        Map<Long, Usuario> auditoresPorId = auditores.stream().collect(Collectors.toMap(
            Usuario::getId, Function.identity()
        ));

        Usuario usuarioLogado = usuarioService.getUsuarioLogado();

        List<AuditoriaAuditor> vinculos = new ArrayList<>();
        Set<Long> auditoriasInformadas = new HashSet<>();
        for (DistribuicaoRequest distribuicao : distribuicoes) {

            if (!auditoriasInformadas.add(distribuicao.auditoriaId())) {
                throw new BusinessException("Auditoria " + distribuicao.auditoriaId() + " informada mais de uma vez");
            }

            Auditoria auditoria = auditoriasPorId.get(distribuicao.auditoriaId());

            if (auditoria == null) {
                throw new BusinessException("Auditoria " + distribuicao.auditoriaId() + " não pertence à campanha");
            }

            if (distribuicao.auditoresIds() == null || distribuicao.auditoresIds().isEmpty()) {
                continue;
                //throw new BusinessException("A auditoria " + distribuicao.auditoriaId() + " não possui auditor");
            }

            Set<Long> auditoresInformados = new HashSet<>();
            for (Long auditorId : distribuicao.auditoresIds()) {
                if (!auditoresInformados.add(auditorId)) {
                    throw new BusinessException("Auditor " + auditorId + " informado mais de uma vez para a auditoria " + distribuicao.auditoriaId());
                }

                Usuario auditor = auditoresPorId.get(auditorId);
                if (auditor == null) {
                    throw new BusinessException("Auditor " + auditorId + " não existe, está inativo ou não possui perfil de auditor");
                }
                vinculos.add(AuditoriaAuditor.criar(auditoria, auditor, usuarioLogado));
            }
        }

        repository.saveAll(vinculos);
        Long codAuditEcm = ecmService.criarAuditoria(campanha, vinculos);
        campanha.setEcmCodAudit(codAuditEcm);
        campanha.setEcmLastSync(OffsetDateTime.now());// dirt check faz o update
    }

}