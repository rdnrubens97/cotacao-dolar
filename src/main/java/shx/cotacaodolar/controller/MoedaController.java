package shx.cotacaodolar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shx.cotacaodolar.dto.MoedaDto;
import shx.cotacaodolar.service.MoedaService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;


@RestController
@RequestMapping(value = "/")
public class MoedaController {
    @Autowired
    private MoedaService moedaService;


    // Obtém a cotação de moeda atual ou a última disponível.
    @GetMapping("/moeda/atual")
    public ResponseEntity<MoedaDto> getCotacaoAtual() throws IOException, MalformedURLException, ParseException {
        MoedaDto moedaDto = moedaService.getCotacaoAtual();
        return ResponseEntity.ok().body(moedaDto);
    }

    // Obtém uma lista de cotações de moeda no período especificado.
    @GetMapping("/moeda/{data1}&{data2}")
    public ResponseEntity<List<MoedaDto>> getCotacoesPeriodo(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesPeriodo(startDate, endDate);
        return ResponseEntity.ok().body(moedaDtoList);
    }

    // Obtém uma lista de cotações de moeda no período especificado, incluindo apenas as cotações menores que a cotação atual ou a última disponível.
    @GetMapping("/moeda/{data1}&{data2}/cotacoes-menores-atual")
    public ResponseEntity<List<MoedaDto>> getCotacoesMenoresAtual(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesMenoresAtual(startDate, endDate);
        return ResponseEntity.ok().body(moedaDtoList);
    }

    // Obtém uma lista de cotações de moeda no período especificado, incluindo apenas as cotações maiores que a cotação atual ou a última disponível.
    @GetMapping("/moeda/{data1}&{data2}/cotacoes-maiores-atual")
    public ResponseEntity<List<MoedaDto>> getCotacoesMaioresAtual(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {
        List<MoedaDto> moedaDtoList = moedaService.getCotacoesMaioresAtual(startDate, endDate);
        return ResponseEntity.ok().body(moedaDtoList);
    }

    // Obtém uma lista de cotações de moeda no período especificado, salvando-as no banco de dados.
    @GetMapping("/moeda/{data1}&{data2}/salvar")
    public ResponseEntity<String> getCotacoesPeriodoESalvarNoBanco(@PathVariable("data1") String startDate, @PathVariable("data2") String endDate) throws IOException, MalformedURLException, ParseException {
        String mensagem = moedaService.salvarNoBdCotacoesDeDolarPorPeriodo(startDate, endDate);
        return ResponseEntity.ok().body(mensagem);
    }

    // Busca no banco de dados a cotação de determinada data.
    @GetMapping("/moeda/cotacao-data/{data}")
    public ResponseEntity<MoedaDto> retornaCotacaoJaSalvaPorData(@PathVariable("data") String data) throws Exception {
        MoedaDto moedaDto = moedaService.retornaCotacaoJaSalvaPorData(data);
        return ResponseEntity.ok().body(moedaDto);
    }

}
