O gradle-wrapper.jar incluído contém um bootstrap Java mínimo do Gradle,
criado localmente porque não havia acesso à distribuição oficial durante a
montagem deste arquivo ZIP. Ele utiliza a URL HTTPS em gradle-wrapper.properties,
baixa gradle-8.9-bin.zip e o extrai em ~/.gradle/wrapper/dists/.

Não é o JAR oficial do Gradle Wrapper. O projeto continua sendo um projeto
Gradle/Android Studio convencional e pode utilizar o wrapper oficial depois.
Para substituí-lo (opcional): com Gradle 8.9 instalado, rode `gradle wrapper --gradle-version 8.9`.
O Android Studio pode baixar a distribuição pelo próprio mecanismo de sincronização.
