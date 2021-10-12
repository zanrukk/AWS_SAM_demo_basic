
# AWS SAM Java Windows Kurulum

## Gerekli Programlar

|İndirme Linkleri | 
| ------ | 
|[Java Development Kit 8](https://www.oracle.com/tr/java/technologies/javase/javase-jdk8-downloads.html)  |
|[AWS SAM CLI](https://github.com/aws/aws-sam-cli/releases/latest/download/AWS_SAM_CLI_64_PY3.msi)  |
|[AWS CLI version 2](https://awscli.amazonaws.com/AWSCLIV2.msi)  |
|[Docker](https://desktop.docker.com/win/stable/amd64/Docker%20Desktop%20Installer.exe?utm_source=docker&amp;utm_medium=webreferral&amp;utm_campaign=dd-smartbutton&amp;utm_location=module)  |
|[Maven](https://ftp.itu.edu.tr/Mirror/Apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.zip)  |
|[IntelliJ IDEA](https://www.jetbrains.com/idea/download/#section=windows)  |

## Java için Windows Ayarları
- Java Development Kit kurulduktan sonra, Başlat üzerinden "Gelişmiş sistem ayarlarını görüntüle"yi açın.
- "Gelişmiş" sekmesi üzerinden "Ortam Değişkenleri"ni açın.
- "Sistem Değişkenleri" için "Yeni..." butonuna bastıktan sonra "Değişken adı" için "JAVA_HOME", "Değişken değeri" için JDK dosyasının kurulu olduğu dosya yolunu girin. (Örn: "C:\Program Files\Java\jdk1.8.0_281")
- Yine "Sistem Değişkenleri" üzerinde "Path" adlı değişkeni "Düzenle" diyerek açılan yeni pencere üzerinden "Yeni" diyerek "%JAVA_HOME%\bin" girdisini ekleyin.

## Maven için Windows Ayarları
- İndirdiğiniz Maven dosyalarını unzip ettikten sonra, Başlat üzerinden "Gelişmiş sistem ayarlarını görüntüle"yi açın.
- "Gelişmiş" sekmesi üzerinden "Ortam Değişkenleri"ni açın.
- "Sistem Değişkenleri" için "Yeni..." butonuna bastıktan sonra "Değişken adı" için "MAVEN_HOME", "Değişken değeri" için Maven dosyalarının çıkartıldığı dosya yolunu girin. (Örn "C:\Program Files\apache-maven-3.8.1")
- Yine "Sistem Değişkenleri" üzerinde "Path" adlı değişkeni "Düzenle" diyerek açılan yeni pencere üzerinden "Yeni" diyerek "%MAVEN_HOME%\bin" girdisini ekleyin.

## Docker ile localde DynamoDB Kurulumu
- Docker kurulduktan sonra içeriği aşağıdaki gibi olan docker-compose.yml dosyasını oluşturun.
```
version: '3.8'
services:
  dynamodb-local:
    command: "-jar DynamoDBLocal.jar -sharedDb -optimizeDbBeforeStartup -dbPath ./data"
    image: "amazon/dynamodb-local:latest"
    container_name: dynamodb-local
    ports:
      - "8000:8000"
    volumes:
      - "./docker/dynamodb:/home/dynamodblocal/data"
    working_dir: /home/dynamodblocal
```
- Daha sonrasında bu dosyayı terminal üzerinden `docker-compose up` komutu ile çalıştırarak, DynamoDB'yi ayağa kaldırabilirsiniz.
- Tablo oluşturmak için örnek komut: `aws dynamodb create-table --table-name ITEMS --attribute-definitions AttributeName=CATEGORY,AttributeType=S AttributeName=TITLE,AttributeType=S --key-schema AttributeName=CATEGORY,KeyType=HASH AttributeName=TITLE,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 --endpoint-url http://127.0.0.1:8000`
- Bu haliyle DynamoDB, localhost:8000 üzerinden erişilebilir.
- Detaylı incelemek isterseniz: [kaynak](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html)

## IntelliJ IDEA için AWS Toolkit Plugin Kurulumu
- IDE'yi açtıktan sonra Ctrl + Alt + S yaparak ayarlar penceresini açın.
- "Plugins" sekmesinde "Marketplace" üzerinden AWS Toolkit kurun.

## AWS SAM Hello World Template Oluşturulması ve Localde Run/Debug
- Hatırlatma: Localde lambda fonksiyonlarını çalıştırabilmemiz için Docker programının çalışır durumda olması gerekmektedir.
- Intellij IDEA üzerinde AWS Toolkit pluginini kurduktan sonra,
- "File -> New -> Project.. -> AWS (AWS Serverless Application) -> Runtime: java8, SAM Template: AWS SAM Hello World (Maven)" yolunu izleyerek SAM dosyalarını oluşturabilirsiniz.
- Fonksiyonu çalıştırabilmek ve debug edebilmek için "Run -> Edit Configurations" pencerisinden input text için dummy bir input girin. Örn: {"var1": "value1"}
- Daha sonrasında "Run" diyerek çalıştırabilir, kod satırına breakpointler koyduktan sonra "Debug" diyerek kolayca debug edebilirsiniz.
- Api gatewayi çalıştırmak için terminal üzerinden `sam local start-api` komutunu kullanabilirsiniz.

## template.yaml dosyası içeriği
- Temel database operasyonlarının örnek kodlarına [buradan](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaDocumentAPIWorkingWithTables.html) ulaşabilirsiniz.
- Oluşturduğunuz lambda fonksiyonunu çalıştırabilmek için template.yaml dosyasına tanımlamamız gerekmektedir.
- Örneğin, Insert isminde oluşturduğumuz java sınıfımızda bir lambda fonksiyonumuz var. Aşağıda verilen örnekteki gibi bir tanımlamayı dosyada bulunun "Resources:" bölümüne eklememiz gerekmektedir.
```
  InsertItemFunction:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: HelloWorldFunction
      Handler: dboperations.Insert::handleRequest
      Environment:
        Variables:
          SampleVariable: SampleValue
      Events:
        InsertItem:
          Type: Api
          Properties:
            Path: /insert
            Method: post
```
- Bu örnekteki, 
  - InsertItemFunction: bizim lambda fonksiyonumuza verdiğimiz isimdir.
  - CodeUri: src dosyamızın bulunduğu modüldür.
  - Handler: dboperations, kütüphane ismi ve Insert, fonksiyonumuzu içeren java sınıfımızın ismidir.
  - Environment: Variables: ile ortam değişkenleri tanımlayabiliriz.
  - Events: içerisinde bu örnekte olduğu gibi Api ve birçok farklı event tanımlayabiliriz.

- template.yaml dosyasının formatı hakkında detaylı bilgiye [buradan](https://github.com/aws/serverless-application-model/blob/master/versions/2016-10-31.md) ulaşabilirsiniz.
