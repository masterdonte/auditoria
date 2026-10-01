package br.gov.ma.emap.auditoria.service;

import br.gov.ma.emap.auditoria.dto.AmostraDTO;
import br.gov.ma.emap.auditoria.dto.ProcessoDTO;
import br.gov.ma.emap.auditoria.dto.request.CampanhaRequest;
import br.gov.ma.emap.auditoria.dto.response.AuditoriaResponse;
import br.gov.ma.emap.auditoria.dto.response.CampanhaResponse;
import br.gov.ma.emap.auditoria.exception.BusinessException;
import br.gov.ma.emap.auditoria.model.*;
import br.gov.ma.emap.auditoria.repository.*;
import br.gov.ma.emap.auditoria.utils.AuditoriaUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampanhaService {

    private final CampanhaRepository repository;
    private final UsuarioService usuarioService;
    private final ParametrizacaoRepository paramRepository;
    private final ClickHouseService clickHouseService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public CampanhaResponse salvar(CampanhaRequest request) {

        Optional<Campanha> optCampanha = repository.findByCompetencia(request.competencia());
        if (optCampanha.isPresent())
            throw new BusinessException("Já existe uma campanha para essa competencia");

        Parametrizacao param = paramRepository.findByAtivoTrue().orElseThrow(() -> new BusinessException("Não existe parametrização ativa"));

        Usuario usuario = usuarioService.getUsuarioLogado();

        Campanha campanha = Campanha.builder().competencia(request.competencia())
                .parametrizacao(param).createdBy(usuario).updatedBy(usuario).build();

        return CampanhaResponse.from(repository.save(campanha));
    }

    @Transactional(readOnly = true)
    public Page<CampanhaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(CampanhaResponse::from); //.map(camp -> CampanhaResponse.from(camp));
    }

    public CampanhaResponse buscarPorId(Long campanhaId) {
        return CampanhaResponse.from(this.buscarCampanha(campanhaId));
    }

    public CampanhaResponse aprovar(Long campanhaId) {
        Campanha campanha = this.buscarCampanha(campanhaId);
        if(campanha.getAprovacao() != null)
            throw new BusinessException("A campanha já foi aprovada");

        if(campanha.getExecucaoMotor() == null)
            throw new BusinessException("Não é possivel aprovar uma campanha cujo motor não foi executado");

        campanha.aprovar(usuarioService.getUsuarioLogado());
        return CampanhaResponse.from(repository.save(campanha));
    }

    @Transactional
    public Page<AuditoriaResponse> executarMotor(Long campanhaId, Pageable pageable) {
        Campanha campanha = this.buscarCampanha(campanhaId);

        if (campanha.getExecucaoMotor() != null) throw new BusinessException("o motor já foi executado para essa campanha");

        Parametrizacao param = campanha.getParametrizacao();
        YearMonth ref = YearMonth.of(campanha.getCompetencia() / 100, campanha.getCompetencia() % 100);

        List<ProcessoDTO> universo = clickHouseService.buscarDadosCubo(ref);
        Map<String, List<ProcessoDTO>> processosPorDiretoria = universo.stream().collect(Collectors.groupingBy(ProcessoDTO::diretoria));

        List<AmostraDTO> amostras = new ArrayList<>();

        List<Integer> competenciasMaterialidade = calcularCompetenciasAnteriores(ref, param.getRecorrenciaMaterial());
        Set<String> auditadosMaterialidade = auditoriaService.getContratosAuditados(competenciasMaterialidade);
        List<Integer> competenciasTemporalidade = calcularCompetenciasAnteriores(ref, param.getTempoSemAuditoria());
        Set<String> auditadosTemporalidade = auditoriaService.getContratosAuditados(competenciasTemporalidade);

        processosPorDiretoria.forEach((diretoria, processos) -> {

            int qtdAmostra = Math.max(calcularQuantidade(processos.size(), param.getPercDiretoria()), 1);
            int qtdMaterialidadePrev = calcularQuantidade(qtdAmostra, param.getPercMaterial());
            int qtdTemporalidadePrev = calcularQuantidade(qtdAmostra, param.getPercTemporal());
            //int qtdAleatoriedadePrev = qtdAmostra - qtdMaterialidadePrev - qtdTemporalidadePrev;

            List<ProcessoDTO> elegiveisMaterialidade = processos.stream().filter(processo -> {
                String contrato = AuditoriaUtils.normalizar(processo.contrato());
                return contrato == null || !auditadosMaterialidade.contains(contrato);
            }).toList();

            List<ProcessoDTO> selecionadosMaterialidade = elegiveisMaterialidade.stream().sorted(Comparator.comparing(
                    ProcessoDTO::valorOriginal,
                    Comparator.nullsLast(Comparator.reverseOrder())
            )).limit(qtdMaterialidadePrev).toList();

            Set<Long> idsMaterialidade = selecionadosMaterialidade.stream().map(ProcessoDTO::ecmId).collect(Collectors.toSet());
            List<ProcessoDTO> restantes = processos.stream().filter(p -> !idsMaterialidade.contains(p.ecmId())).toList();

            List<ProcessoDTO> elegiveisTemporalidade = restantes.stream().filter(p -> {
                String contrato = AuditoriaUtils.normalizar(p.contrato());
                return contrato == null || !auditadosTemporalidade.contains(contrato);
            }).toList();

            List<ProcessoDTO> selecionadosTemporalidade = elegiveisTemporalidade.stream().limit(qtdTemporalidadePrev).toList();
            Set<Long> idsTemporalidade = selecionadosTemporalidade.stream().map(ProcessoDTO::ecmId).collect(Collectors.toSet());
            restantes = restantes.stream().filter(p -> !idsTemporalidade.contains(p.ecmId())).toList();

            int qtdAleatoriedade = qtdAmostra - selecionadosMaterialidade.size() - selecionadosTemporalidade.size();

            List<ProcessoDTO> embaralhados = new ArrayList<>(restantes);
            Collections.shuffle(embaralhados);
            List<ProcessoDTO> selecionadosAleatoriedade = embaralhados.stream().limit(qtdAleatoriedade).toList();

            amostras.addAll(selecionadosMaterialidade.stream().map(AmostraDTO::materialidade).toList());
            amostras.addAll(selecionadosTemporalidade.stream().map(AmostraDTO::temporalidade).toList());
            amostras.addAll(selecionadosAleatoriedade.stream().map(AmostraDTO::aleatoriedade).toList());
        });

        Usuario usuario = usuarioService.getUsuarioLogado();

        campanha.setQtdUniverso(universo.size());
        campanha.setQtdAmostra(amostras.size());
        campanha.setExecucaoMotor(OffsetDateTime.now(ZoneId.of("America/Fortaleza")));
        campanha.setUpdatedBy(usuario);

        auditoriaService.salvarAuditorias(amostras, campanha, usuario);
        repository.save(campanha);

        return auditoriaService.listar(campanha.getId(), pageable);
    }

    private int calcularQuantidade(int totalProcessos, BigDecimal percentual) {
        return BigDecimal.valueOf(totalProcessos).multiply(percentual).movePointLeft(2).setScale(0, RoundingMode.HALF_EVEN).intValue();
    }

    private List<Integer> calcularCompetenciasAnteriores(YearMonth referencia, int quantidadeMeses) {
        List<Integer> competencias = new ArrayList<>();
        for (int i = 1; i <= quantidadeMeses; i++) {
            YearMonth mes = referencia.minusMonths(i);
            competencias.add(mes.getYear() * 100 + mes.getMonthValue());
        }
        return competencias;
    }

    private Campanha buscarCampanha(Long id){
        return repository.findById(id).orElseThrow(() -> new BusinessException("Não existe campanha com o id " + id));
    }

    @Transactional
    public void deletar(Long id) {
        Campanha campanha = repository.findById(id).orElseThrow(() -> new BusinessException("Campanha não encontrada"));
        auditoriaService.deletarPorCampanhaId(id);
        repository.delete(campanha);
    }

    public byte[] exportarExcel(Long campanhaId) {
        Campanha campanha = buscarCampanha(campanhaId);
        List<Auditoria> auditorias = auditoriaService.buscarPorCampanha(campanha);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Auditorias");

            Row header = sheet.createRow(0);

            String[] colunas = {
                    "#",
                    "Fornecedor",
                    "Processo",
                    "Contrato",
                    "Objeto",
                    "Valor",
                    "Data Pagto",
                    "Setor",
                    "Diretoria",
                    "Tipo",
                    "Criterio",
                    "Status"
            };

            for (int i = 0; i < colunas.length; i++) {
                header.createCell(i).setCellValue(colunas[i]);
            }

            int linha = 1;

            for (Auditoria auditoria : auditorias) {

                Row row = sheet.createRow(linha);

                row.createCell(0).setCellValue(String.format("%03d", linha++));
                row.createCell(1).setCellValue(auditoria.getFornecedor());
                row.createCell(2).setCellValue(auditoria.getProcesso());
                row.createCell(3).setCellValue(auditoria.getContrato());
                row.createCell(4).setCellValue(auditoria.getObjeto());
                if (auditoria.getValor() != null)
                    row.createCell(5).setCellValue(auditoria.getValor().doubleValue());

                if (auditoria.getPagamento() != null)
                    row.createCell(6).setCellValue(auditoria.getPagamento().toString());

                row.createCell(7).setCellValue(auditoria.getDepartamento());
                row.createCell(8).setCellValue(auditoria.getDiretoria());
                row.createCell(9).setCellValue(auditoria.getClassificacao());
                row.createCell(10).setCellValue(auditoria.getCriterio().name());
                row.createCell(11).setCellValue(auditoria.getStatus().name());
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Erro ao gerar arquivo Excel");
        }
    }

    /**
    * Os métodos abaixo devem ser excluidos
    */

    /*@Transactional
    @Deprecated
    public List<Auditoria> executarMotorDeprecated(Long campanhaId) {
        Campanha campanha = repository.findById(campanhaId).orElseThrow(() -> new BusinessException("Campanha não encontrada"));

        if (campanha.getExecucaoMotor() != null) {
            throw new BusinessException("o motor já foi executado para essa campanha");
        }

        Parametrizacao param = campanha.getParametrizacao();
        YearMonth ref = YearMonth.of(campanha.getCompetencia() / 100, campanha.getCompetencia() % 100);

        ZoneId zone = ZoneId.of("America/Fortaleza");
        OffsetDateTime ini = ref.atDay(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime fim = ref.plusMonths(1).atDay(1).atStartOfDay(zone).toOffsetDateTime();

        List<ProcessoRmEcm> universo = processoRepository.buscarPorPeriodo(ini, fim);
        Map<String, List<ProcessoRmEcm>> processosPorDiretoria = universo.stream().collect(Collectors.groupingBy(ProcessoRmEcm::getDiretoria));

        List<AmostraDTO> amostra = new ArrayList<>();

        processosPorDiretoria.forEach((diretoria, processos) -> {

            int qtdAmostra = Math.max(calcularQuantidade(processos.size(), param.getPercDiretoria()), 1);
            int qtdMaterialidadePrev = calcularQuantidade(qtdAmostra, param.getPercMaterial());
            int qtdTemporalidadePrev = calcularQuantidade(qtdAmostra, param.getPercTemporal());
            //int qtdAleatoriedadePrev = qtdAmostra - qtdMaterialidadePrev - qtdTemporalidadePrev;

            List<Integer> competenciasMaterialidade = calcularCompetenciasAnteriores(ref, param.getRecorrenciaMaterial());
            Set<String> auditadosMaterialidade = auditoriaService.getContratosAuditados(competenciasMaterialidade);

            List<ProcessoRmEcm> elegiveisMaterialidade = processos.stream().filter(processo -> {
                String contrato = AuditoriaUtils.normalizar(processo.getContrato());
                return contrato == null || !auditadosMaterialidade.contains(contrato);
            }).toList();

            List<ProcessoRmEcm> selecionadosMaterialidade = elegiveisMaterialidade.stream().sorted(Comparator.comparing(
                    ProcessoRmEcm::getValororiginal,
                    Comparator.nullsLast(Comparator.reverseOrder())
            )).limit(qtdMaterialidadePrev).toList();

            Set<Long> idsMaterialidade = selecionadosMaterialidade.stream().map(ProcessoRmEcm::getId).collect(Collectors.toSet());
            List<ProcessoRmEcm> restantes = processos.stream().filter(p -> !idsMaterialidade.contains(p.getId())).toList();

            List<Integer> competenciasTemporalidade = calcularCompetenciasAnteriores(ref, param.getTempoSemAuditoria());
            Set<String> auditadosTemporalidade = auditoriaService.getContratosAuditados(competenciasTemporalidade);

            List<ProcessoRmEcm> elegiveisTemporalidade = restantes.stream().filter(p -> {
                String contrato = AuditoriaUtils.normalizar(p.getContrato());
                return contrato == null || !auditadosTemporalidade.contains(contrato);
            }).toList();

            List<ProcessoRmEcm> selecionadosTemporalidade = elegiveisTemporalidade.stream().limit(qtdTemporalidadePrev).toList();
            Set<Long> idsTemporalidade = selecionadosTemporalidade.stream().map(ProcessoRmEcm::getId).collect(Collectors.toSet());
            restantes = restantes.stream().filter(p -> !idsTemporalidade.contains(p.getId())).toList();

            int qtdAleatoriedade = qtdAmostra - selecionadosMaterialidade.size() - selecionadosTemporalidade.size();

            List<ProcessoRmEcm> embaralhados = new ArrayList<>(restantes);
            Collections.shuffle(embaralhados);
            List<ProcessoRmEcm> selecionadosAleatoriedade = embaralhados.stream().limit(qtdAleatoriedade).toList();

            amostra.addAll(selecionadosMaterialidade.stream().map(AmostraDTO::materialidade).toList());
            amostra.addAll(selecionadosTemporalidade.stream().map(AmostraDTO::temporalidade).toList());
            amostra.addAll(selecionadosAleatoriedade.stream().map(AmostraDTO::aleatoriedade).toList());
        });

        campanha.setQtdUniverso(universo.size());
        campanha.setQtdAmostra(amostra.size());
        campanha.setExecucaoMotor(OffsetDateTime.now(zone));

        List<Auditoria> auditorias = auditoriaService.salvarAuditorias(amostra, campanha, getUsuario());
        repository.save(campanha);
        return auditorias;//return amostra;
    }*/
/*

System.out.println("Diretoria: " + diretoria +
        " | Universo: " + processos.size() +
        " | Amostra: " + qtdAmostra +
        " | Materialidade Prevista: " + qtdMaterialidadePrev +
        " | Temporalidade Prevista: " + qtdTemporalidadePrev +
        " | Aleatoriedade Prevista: " + qtdAleatoriedadePrev +
        " | Materialidade Real: " + selecionadosMaterialidade.size() +
        " | Temporalidade Real: " + selecionadosTemporalidade.size() +
        " | Aleatoriedade Real: " + selecionadosAleatoriedade.size()
);
System.out.println("SELECIONADOS MATERIALIDADE: " + selecionadosMaterialidade.size());
selecionadosMaterialidade.forEach(p ->  System.out.println(p.lanId()  + " " +p.ecmId()  + " " +  p.processo()));
System.out.println("SELECIONADOS TEMPORALIDADE: " + selecionadosTemporalidade.size());
selecionadosTemporalidade.forEach(p ->  System.out.println(p.lanId()  + " " +p.ecmId()  + " " +  p.processo()));
System.out.println("SELECIONADOS ALEATORIEDADE: " + selecionadosAleatoriedade.size());
selecionadosAleatoriedade.forEach(p -> System.out.println(p.lanId()  + " " +p.ecmId()  + " " +  p.processo()));

    public List<ProcessoRmEcm> processos(YearMonth ref) {
        ZoneId zone = ZoneId.of("America/Fortaleza");
        OffsetDateTime inicio = ref.atDay(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime fim = ref.plusMonths(1).atDay(1).atStartOfDay(zone).toOffsetDateTime();
        return processoRepository.buscarPorPeriodo(inicio, fim);
    }
*/

}