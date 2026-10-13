package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.enums.TipoPerfil;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaAuditorService {

    private final AuditoriaAuditorRepository repository;
    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CampanhaRepository campanhaRepository;

    @Transactional
    public void adicionar(Long auditoriaId, Long auditorId) {

        Auditoria auditoria = buscarAuditoria(auditoriaId);

        buscarCampanhaAprovada(auditoria.getCampanha().getId());

        Usuario auditor = buscarAuditorParaAtribuicao(auditorId);
        verificarDuplicidade(auditoria, auditor);
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();
        AuditoriaAuditor vinculo = AuditoriaAuditor.criar(auditoria, auditor, usuarioLogado);
        repository.save(vinculo);
    }

    @Transactional
    public void encerrar(Long auditoriaId, Long auditorId, String motivoEncerramento) {

        Auditoria auditoria = buscarAuditoria(auditoriaId);
        Usuario auditor = buscarUsuario(auditorId);

        AuditoriaAuditor vinculo = repository.findByAuditoriaAndAuditorAndFimIsNull(auditoria, auditor).orElseThrow(() ->
                new BusinessException("Auditor não possui vínculo ativo com a auditoria")
        );

        vinculo.setFim(OffsetDateTime.now());
        vinculo.setMotivoEncerramento(motivoEncerramento);
        vinculo.setUpdatedBy(usuarioService.getUsuarioLogado());
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

        List<Usuario> auditores = usuarioRepository.findDistinctByAtivoTrueAndPerfisNome(TipoPerfil.AUDITOR);

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

}