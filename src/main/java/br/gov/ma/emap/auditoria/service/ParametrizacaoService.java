package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.request.ParametrizacaoRequest;
import br.gov.ma.emap.auditoria.dto.response.ParametrizacaoResponse;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.Parametrizacao;
import br.gov.ma.emap.auditoria.model.Usuario;
import br.gov.ma.emap.auditoria.repository.ParametrizacaoRepository;
import br.gov.ma.emap.auditoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParametrizacaoService {

    private final ParametrizacaoRepository repository;
    private final UsuarioService usuarioService;

    @Transactional
    public ParametrizacaoResponse salvar(ParametrizacaoRequest request) {

        Usuario logado = usuarioService.getUsuarioLogado();

        Parametrizacao param = request.toEntity(logado);
        Optional<Parametrizacao> optParametrizacao = repository.findByAtivoTrue();

        if (optParametrizacao.isPresent()) {
            Parametrizacao antiga = optParametrizacao.get();
            antiga.setAtivo(false); // Inativa a versão anterior (nunca atualizamos os dados dela, só mudamos o status para inativo)
            antiga.setUpdatedBy(logado);
            repository.save(antiga);
        }

        return ParametrizacaoResponse.from(repository.save(param)) ;
    }

    public ParametrizacaoResponse buscarAtiva() {
        Parametrizacao param = repository.findByAtivoTrue().orElseThrow(() -> new BusinessException("Não existe parametrização ativa"));
        return ParametrizacaoResponse.from(param);
    }

    @Transactional(readOnly = true)
    public Page<ParametrizacaoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(ParametrizacaoResponse::from);//.map(param -> ParametrizacaoResponse.from(param))
    }

}