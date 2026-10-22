package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.ProcessoDTO;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.repository.clickhouse.ClickHouseRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
public class ClickHouseService {

    private final ClickHouseRepository repository;

    public ClickHouseService(ClickHouseRepository repository) {
        this.repository = repository;
    }

    public List<ProcessoDTO> buscarDadosCubo(YearMonth mes) {
        return repository.buscarDadosCubo(mes);
    }

    public List<ProcessoDTO> buscarProcesso(String processo) {
        if(processo == null || processo.length() < 3)
            throw new BusinessException("O trecho do processo deve ter pelo menos 3 caracteres");
        return repository.buscarProcessos(processo);
    }

    public List<Map<String, Object>> version(){
        return repository.version();
    }

    public long buscarEcmUserId(String login) {
        Long ecmUserId = repository.buscarEcmUserId(login);
        if(ecmUserId == null)
            throw new BusinessException("Usuário não cadastrado no ECM ou não está ativo.");
        return ecmUserId;
    }
}