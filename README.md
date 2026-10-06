# Spring Boot User Management

## Rakenduse kirjeldus

Lihtne Spring Boot CRUD veebirakendus kasutajate lisamiseks, kuvamiseks, muutmiseks ja kustutamiseks. Kasutajaliides on eestikeelne ning andmed salvestatakse MySQL andmebaasi. Hibernate loob ja uuendab `users` tabelit automaatselt. MySQL-i salvestatud andmed jäävad alles ka pärast rakenduse taaskäivitamist.

## Funktsionaalsus

- Kasutaja lisamine: eesnimi, perekonnanimi ja e-post.
- Kasutajate kuvamine tabelis.
- Kasutaja muutmine sama vormi kaudu.
- Kasutaja kustutamine koos brauseri kinnitusküsimusega.
- Vormide valideerimine ja eestikeelsed veateated.
- Puuduva kasutaja korral HTTP 404 vastus.

## Kasutatud tehnoloogiad

- Java 17
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA ja Hibernate
- Thymeleaf
- Jakarta Validation
- Bootstrap 5 (CDN-ist)
- MySQL
- Maven (Maven Wrapper on projektis kaasas)
- JUnit Jupiter ja Mockito
- H2 ainult testides

Olemasoleva Spring Initializri projekti Spring Boot versioon on säilitatud. Spring Boot 4 kasutab JUnit Jupiter 6, mille testide annotatsioonid on jätkuvalt `org.junit.jupiter.api` paketis. JUnit 5 sõltuvusi ei ole juurde segatud, sest Spring Framework 7 eeldab JUnit 6. Versioonide selgitus: [Spring Framework 7 väljalaskemärkmed](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes).

## Projekti struktuur

Projekti juurkaustas on `compose.yaml`, mis käivitab MySQL 8.4 koos püsiva andmemahuga.

```text
src/main/java/ee/voco/usermanagement/
├── UserManagementApplication.java
├── controller/
│   ├── HomeController.java
│   └── UserController.java
├── model/User.java
├── repository/UserRepository.java
└── service/UserService.java
src/main/resources/
├── application.properties
└── templates/
    ├── users.html
    └── user-form.html
src/test/java/ee/voco/usermanagement/
├── UserManagementApplicationTests.java
├── controller/UserControllerTest.java
├── repository/UserRepositoryTest.java
└── service/UserServiceTest.java
src/test/resources/application-test.properties
```

`User` kirjeldab andmebaasi tabeli kirjet. `UserRepository` annab JPA CRUD-meetodid. `UserService` tegeleb salvestamise ja puuduvate kasutajate kontrollimisega. `UserController` võtab vastu veebipäringud ning tagastab Thymeleafi vaated. Sõltuvused antakse teenusele ja kontrollerile konstruktorite kaudu.

## Projekti käivitamine

1. Paigalda JDK 17 ja käivita Docker Desktop.
2. Käivita projekti juurkaustas MySQL ning kontrolli konteineri olekut:

   ```bash
   docker compose up mysql -d
   docker compose ps
   ```

   Oota, kuni `user-management-mysql` olek on `healthy`. Esimesel käivitusel laaditakse MySQL 8.4 image alla. Compose loob automaatselt andmebaasi `user_management` ja kasutaja `appuser`, kelle kohaliku arenduse parool on `apppassword`. Root-kasutaja kohaliku arenduse parool on `rootpassword`. MySQL on kättesaadav pordil 3306.

   Kui kasutad Docker'i asemel enda MySQL serverit, loo seal andmebaas:

   ```sql
   CREATE DATABASE user_management;
   ```

3. Compose'iga käivitamisel pole keskkonnamuutujaid vaja: Springi vaikimisi ühendus on `jdbc:mysql://localhost:3306/user_management`, kasutajanimi `appuser` ja parool `apppassword`. Enda MySQL serveri puhul seadista vajadusel keskkonnamuutujad.

   | Muutuja | Tähendus |
      | --- | --- |
   | `DB_URL` | MySQL-i JDBC ühenduse URL |
   | `DB_USERNAME` | MySQL-i kasutajanimi |
   | `DB_PASSWORD` | MySQL-i parool |

   macOS-i või Linuxi terminalis näiteks:

   ```bash
   export DB_URL='jdbc:mysql://localhost:3306/user_management'
   export DB_USERNAME='root'
   read -s DB_PASSWORD
   export DB_PASSWORD
   ```

   Käsk `read -s DB_PASSWORD` ootab parooli sisestamist ja Enteri vajutamist; sisestatud parooli ei kuvata ekraanil. IntelliJ IDEA-s saab muutujad lisada **Run → Edit Configurations → UserManagementApplication → Environment variables** kaudu. Kasutajal peab olema õigus andmebaasi tabeleid luua ja muuta.

4. Käivita projekti juurkaustas:

   ```bash
   ./mvnw spring-boot:run
   ```

   Windowsis:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   IntelliJ IDEA-s ava projekt Maven projektina, vali Project SDK-ks JDK 17, laadi Maven'i muudatused ning käivita `UserManagementApplication.main()`.

5. Ava [http://localhost:8080/users](http://localhost:8080/users). Ka [http://localhost:8080](http://localhost:8080) suunab kasutajate lehele.

Failides olevad `apppassword` ja `rootpassword` on kohaliku arenduse näidisparoolid. Enda MySQL serveri päris parool määra keskkonnamuutujaga. Kui oled varem määranud `DB_URL`, `DB_USERNAME` või `DB_PASSWORD`, siis need asendavad Springi vaikimisi väärtused; Compose'i näidisühenduse kasutamiseks eemalda need terminalist või IntelliJ käivitusseadistusest. `.env` on Gitist välistatud; Spring ei loe seda automaatselt. Bootstrap-i kujunduse laadimiseks peab brauseril olema internetiühendus.

MySQL-i peatamiseks kasuta `docker compose stop mysql`. Konteineri ja võrgu eemaldamiseks kasuta `docker compose down`; andmed jäävad nimega andmemahus alles. `docker compose down -v` eemaldab ka andmemahu ja selles olevad kasutajad.

## Testide käivitamine

```bash
./mvnw test
```

Windowsis kasuta `.\mvnw.cmd test` ja paigaldatud Maven'i korral `mvn test`.

Testid aktiveerivad `test` profiili, mille andmebaas on eraldatud H2 mälus. Need ei vaja töötavat MySQL-i ega kasuta `DB_URL`, `DB_USERNAME` või `DB_PASSWORD` väärtusi. Testid ei muuda päris MySQL-i andmeid. H2 sõltuvus on ainult `test` scope'is ega lähe rakenduse JAR-i.

- `contextLoads()` kontrollib Springi konteksti käivitumist.
- Repository testid kontrollivad `@DataJpaTest` ja AssertJ abil andmete lisamist, kõigi kirjete ja ühe kirje küsimist, muutmist ning kustutamist. Kirjed loetakse pärast JPA vahemälu tühjendamist uuesti andmebaasist ning iga testi muudatused pööratakse tagasi.
- Teenuse testid kontrollivad küsimist, lisamist, muutmist, kustutamist ja puuduvat kasutajat Mockito abil.
- Kontrolleri testid kontrollivad MockMvc abil HTML-i renderdamist, kogu CRUD-voogu koos JPA-ga, vigaseid vorme, andmete säilimist vigase muutmise korral ja puuduva kasutaja 404 vastust.

Build koos testidega ja käivitatava JAR-i loomine:

```bash
./mvnw clean verify
java -jar target/usermanagement-0.0.1-SNAPSHOT.jar
```

### Kontrollitud tulemused

Pärast video andmekihi peatüki järgi eraldi Repository-testide lisamist käivitati Java 17-ga `./mvnw verify`: kõik 23 testi läbisid, vigu ega vahele jäetud teste ei olnud ning käivitatav JAR loodi edukalt. Testide jaotus on 1 rakenduse konteksti test, 5 Repository testi, 8 teenuse testi ja 9 kontrolleri testi.

Compose'iga käivitatud MySQL 8.4 vastu kontrolliti päris HTTP-päringutega lisamist, kuvamist, muutmist ja kustutamist ning tulemusi võrreldi MySQL-i kirjetega. Kontrolliks loodud kasutaja eemaldati pärast kontrolli. Andmete säilimist pärast rakenduse peatamist ja taaskäivitamist ning vormivalideerimist kontrolliti lisaks eraldatud MySQL 9.5 testkonteineriga; see ajutine konteiner peatati ja eemaldati pärast kontrolli.

## CRUD-i käsitsi kontrollimine MySQL-iga

1. Ava `/users`, vajuta **Lisa uus kasutaja** ja salvesta täidetud vorm.
2. Veendu, et kasutaja on tabelis. MySQL-is saab salvestamist kontrollida käsuga `SELECT * FROM users;`.
3. Vajuta **Muuda**, muuda e-posti aadressi ning salvesta. Tabelis ja MySQL-is peab olema uus aadress.
4. Peata ja käivita rakendus uuesti. Kasutaja peab endiselt tabelis olema.
5. Vajuta **Kustuta** ja kinnita kustutamine. Kasutaja peab kaduma nii veebilehelt kui ka MySQL-ist.
