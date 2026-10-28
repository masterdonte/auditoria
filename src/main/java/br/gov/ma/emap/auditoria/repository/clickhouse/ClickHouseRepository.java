package br.gov.ma.emap.auditoria.repository.clickhouse;

import br.gov.ma.emap.auditoria.dto.ProcessoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Repository
public class ClickHouseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClickHouseRepository(@Qualifier("chJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProcessoDTO> buscarDadosCubo(YearMonth competencia) {

        String sql = """                               
                SELECT
                    MAX(A.ID_LAN) AS LANID,
                    B.COD_DOCUMENTO AS ECMID,
                    A.NOME AS FORNECEDOR,
                    A.NR_PROCESSO AS PROCESSO,
                    A.CONTRATO,
                    B.DOC_NOME AS OBJETO,
                    SUM(A.VALOR_ORIGINAL) AS VALOR_ORIGINAL,
                    A.DATA_BAIXA,
                    A.CLASSIFICACAO,
                    C.NOME AS DEPARTAMENTO,
                    SUBSTR(C.DIRETORIA, 1, 3) AS DIRETORIA
                FROM FINANCEIRO_SILVER.PAGAMENTOS A
                INNER JOIN PROCESSOS_SILVER.PROCESSOS B ON B.DOC_NUMERO = A.NR_PROCESSO AND B.TIPO_PROCESSO = 'PROCESSO'
                INNER JOIN PROCESSOS_SILVER.DEPARTAMENTOS C ON C.COD = B.COD_DEPARTAMENTO_CADASTRO AND C.COD_DIRETORIA IS NOT NULL              \s
                WHERE A.DATA_BAIXA >= ? AND A.DATA_BAIXA < ? AND ifNull(CLASSIFICACAO, '') <> 'Passagens/Diárias'
                GROUP BY B.COD_DOCUMENTO, A.NOME, A.NR_PROCESSO, A.CONTRATO, B.DOC_NOME, A.DATA_BAIXA, A.CLASSIFICACAO, C.NOME, C.DIRETORIA               \s
                ORDER BY A.DATA_BAIXA DESC                
                """;

        LocalDateTime inicio = competencia.atDay(1).atStartOfDay();
        LocalDateTime fim = competencia.plusMonths(1).atDay(1).atStartOfDay();

        return jdbcTemplate.query(sql, ProcessoRowMapper, inicio, fim);
    }

    public List<ProcessoDTO> buscarProcessos(String processo) {

        String sql = """
                SELECT
                    ((rand() % 10000) + 1) AS LANID,
                    B.COD_DOCUMENTO AS ECMID,
                    B.FORNECEDOR AS FORNECEDOR,
                    B.DOC_NUMERO AS PROCESSO,
                    B.DOC_NOME AS OBJETO,
                    B.NUMERO_CONTRATO AS CONTRATO,
                    0.0 AS VALOR_ORIGINAL,
                    B.DATA_PROCESSO AS DATA_BAIXA,
                    B.FLUXO AS CLASSIFICACAO,
                    C.NOME AS DEPARTAMENTO,
                    SUBSTR(C.DIRETORIA, 1, 3) AS DIRETORIA
                FROM PROCESSOS_SILVER.PROCESSOS B
                INNER JOIN PROCESSOS_SILVER.DEPARTAMENTOS C ON C.COD = B.COD_DEPARTAMENTO_CADASTRO AND C.COD_DIRETORIA IS NOT NULL
                WHERE B.TIPO_PROCESSO = 'PROCESSO' AND B.DOC_NUMERO LIKE ?
                ORDER BY B.DOC_NUMERO DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(sql, ProcessoRowMapper, "%" + processo + "%");
    }

    private final RowMapper<ProcessoDTO> ProcessoRowMapper =
            (rs, rowNum) -> {
                Timestamp dataBaixa = rs.getTimestamp("DATA_BAIXA");
                return new ProcessoDTO(
                        rs.getLong("LANID"),
                        rs.getLong("ECMID"),
                        rs.getString("FORNECEDOR"),
                        rs.getString("PROCESSO"),
                        rs.getString("CONTRATO"),
                        rs.getString("OBJETO"),
                        rs.getBigDecimal("VALOR_ORIGINAL"),
                        //rs.getBigDecimal("LIQUIDO"),
                        dataBaixa != null ? dataBaixa.toLocalDateTime() : null,
                        rs.getString("CLASSIFICACAO"),
                        //rs.getString("CLAS_FINANCEIRO"),
                        rs.getString("DEPARTAMENTO"),
                        rs.getString("DIRETORIA")
                );
            };

    public Long buscarEcmUserId(String login){
        String sql = "SELECT ID FROM PROCESSOS_SILVER.USUARIOS WHERE ATIVO = 'ATIVO' AND USER_NAME = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, login);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> version() {
        String sql = """                
                SELECT currentUser() as usuario, version() as version, hostName() as host, name, value FROM system.settings
                 WHERE name IN ('join_algorithm', 'enable_analyzer', 'query_plan_join_swap_table', 'query_plan_join_distributed_plan')
                """;
        return jdbcTemplate.queryForList(sql);
    }

}