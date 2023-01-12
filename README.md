### Sobre

A aplicação foi desenvolvida com as seguintes tecnologias:
- Java 19
- Spring Boot 2.7.7
- H2 Database
- Apache Kafka
- Redis

Além disto, alguns recursos interessantes são utilizados na solução:
- OpenAPI
- Swagger
- Mapper Struct
- Lombok

A aplicação roda na porta 8085, podendo ser alterado no seu arquivo de configurações. Ao ser executatada é disponibilizada uma API REST, onde sua documentação pode ser visualizada em seu Swagger na URL http://{host}:{porta}/swagger-ui/index.html#/.

Para que seus recursos sejam usufruidos de maneira completa, se faz necessário a execução de uma instância do Apache Kafka, para mensageria, e do Redis, para cache. No diretório 'extras' é disponibilizado um arquivo para ser utilizado no docker-compose e subir as instâncias do Apache Kafka e Redis. Caso opte por utilizar alguma outra instância, se atente as configurações de porta e host dos serviços e o nome do tópico do kafka a ser utilizado no arquivo de configuração da aplicação.
Se desejar utilizar o docker-compose, é necessário criar o tópico do Kafka com o seguinte comando:

```console
docker-compose exec kafka kafka-topics --bootstrap-server kafka:9092 --create --topic agendas-final-result
```

No diretório 'extras' também é possível encontrar uma collection do Postman para importação com todos os endpoints da aplicação.

[//]: # (Existem aperfeiçoamentos que podem ser realizados na aplicação, entre elas estão:)
[//]: # (- ExceptionHandler personalizado)
[//]: # (- Testes unitários exclusivos para Redis e Apache Kafka)
[//]: # (- Testes de perfomance integrados)
