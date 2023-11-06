Projeto Cotação Dólar


Visão Geral

Este projeto é um sistema que se comunica com a API do Banco Central do Brasil para obter cotações do dólar americano (USD) em relação ao Real brasileiro (BRL) em diferentes contextos. 
Ele fornece funcionalidades para obter cotações atuais, cotações em um período específico, cotações menores que a cotação atual, cotações maiores que a cotação atual, 
salvar cotações em um banco de dados e recuperar cotações do banco de dados por data específica.
O projeto é desenvolvido em Java, utilizando o framework Spring Boot.


Funcionalidades

Obtenção de Cotação Atual
Rota: /moeda/atual
Descrição: Retorna a cotação mais recente do dólar em relação ao Real. Se a cotação atual não estiver disponível, ele recupera a última cotação disponível.

Obtenção de Cotações em um Período Específico
Rota: /moeda/{data1}&{data2}
Descrição: Obtém uma lista de cotações do dólar para o período especificado.

Cotações Menores que a Cotação Atual em um Período Específico
Rota: /moeda/{data1}&{data2}/cotacoes-menores-atual
Descrição: Retorna uma lista de cotações do dólar no período especificado, incluindo apenas as cotações menores que a cotação atual ou a última disponível.

Cotações Maiores que a Cotação Atual em um Período Específico
Rota: /moeda/{data1}&{data2}/cotacoes-maiores-atual
Descrição: Retorna uma lista de cotações do dólar no período especificado, incluindo apenas as cotações maiores que a cotação atual ou a última disponível.

Salvar Cotações em um Banco de Dados
Rota: /moeda/{data1}&{data2}/salvar
Descrição: Obtém cotações para o período especificado e as salva em um banco de dados.

Buscar Cotação por Data no Banco de Dados
Rota: /moeda/cotacao-data/{data}
Descrição: Busca no banco de dados a cotação de determinada data.


Pré-requisitos

Java
Spring Boot
Dependências especificadas no arquivo pom.xml
Banco de dados configurado (PostgreSql ou outro de sua preferência)


Configuração

1º) Clone o repositório para a sua máquina local: "git clone https://github.com/rdnrubens97/cotacao-dolar.git" (sem as aspas)
2º) Importe o projeto em sua IDE favorita.
3º) Configure as propriedades do banco de dados no arquivo application.properties ou application.yml.
4º) Execute a aplicação Spring Boot: "./mvnw spring-boot:run" (sem as aspas)


Uso
Após configurar e executar a aplicação, você pode acessar as rotas definidas no controlador para obter cotações do dólar ou salvar cotações no banco de dados.
Exemplo de uso da rota /moeda/atual para obter a cotação atual: GET "http://localhost:8080/moeda/atual" (sem as aspas)


Agradecimentos:

Agradeço a equipe SHX sistemas pela oportunidade dada.
