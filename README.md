# KRITIS-ELFe
Die KRITIS-ELFe soll den Prozess der Erstellung des gemeinsamen Lagebild Bevölkerungsschutz digitalisieren.Die Webanwendung bietet die Technologie für die Erstellung und Auswertung der Lageberichte.

## Server Konfiguration
Es wird ein Server benötigt. Am besten ein Ubuntu Server, da die Konfigurationserklärung hierfür erstellt wurde. <br/>
### Java
Auf diesem sollte Java der Version 17 installiert sein. Falls dies nicht der Fall ist, dann kann Java mit diesem Befehl installiert werden: 
```console
sudo apt install openjdk-17-jdk openjdk-17-jre
```
### Datenbank
Es wird weiterhin eine Lokale Datenbank benötigt. Es wird empfohlen MySql zu nutzen. Es kann so installiert werden:
```console
sudo apt-get install mysql-server 
```
Der Service wird dann gestartet:
```console
sudo systemctl start mysql.service
```
Um einen neuen Nutzer und eine neue Datenbank für die Anwendung zu erstellen, muss zu erst auf die mysql ausgeführt werden:
```console
sudo mysql
```
Die Datenbank wird so erstellt:
```sql
CREATE DATABASE datenbank_name;
```` 
Der neue Nutzer kann so erstellt werden:
```sql
CREATE USER 'username'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```
Die richtigen Rechte für die Datenbank werden ihm so zugeteilt:
```sql
GRANT ALL PRIVILEGES ON datenbank_name . * TO 'username'@'localhost';
```
Und danach:
```sql
FLUSH PRIVILEGES;
```
MySql kann so verlassen werden:
```sql
quit;
```
### Projekteinrichtung
Es sollte zu erst ein neues Verzeichnis angelegt werden. In diesem Beispiel passiert dies unter "/var/", wenn ein anderes Verzeichnis gewünscht ist, dann muss das bei allen anderen Schritten mit beachtet werden. Das Verzeichnis anlegen geht so:
```console
sudo mkdir /var/KRITIS-ELFe
```
Innerhalb dieses Ordner wird ein weiterer Ordner für die Hilfe-PDF benötigt:
```console
sudo mkdir /var/KRITIS-ELFe/help
```
Initial wird die JAR der Webanwendung von GitHub in den erstellten Ordner heruntergeladen:
```console
sudo wget -O /var/KRITIS-ELFe/KRITIS-ELFe.jar https://github.com/leonxs2001/KRITIS-ELFe/raw/master/KRITIS-ELFe/target/KRITIS_ELFe-0.0.1-SNAPSHOT.jar
```
Damit der Server funktioniert muss eine application.properties erstellt werden:
```console
sudo nano /var/KRITIS-ELFe/application.properties
```
In die Datei kommen alle wichtigen Konfigurationen (nicht vergessen die richtigen Daten auszufüllen). Die auszufüllenden Teile sind mit <> umschlossen:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/<datenbank_name>
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.username=<username>
spring.datasource.password=<passwort>

# RECREATE DB
spring.jpa.hibernate.ddl-auto=update

#Errorpages
server.error.whitelabel.enabled = false

server.port=8080

#set path for help pdf
kritiselfe.helpPath = /var/KRITIS-ELFe/help

#set domainname
kritiselfe.url = <url>

#mail
spring.mail.host=<host>
spring.mail.port=<port>
spring.mail.username=<mail>
spring.mail.password=<passwort, wenn nötig>
spring.mail.properties.mail.smtp.auth=<true, wenn mit passwort>
spring.mail.properties.mail.smtp.starttls.enable=true
```
Bestätigt werden kann das ganze mit Strg X (und dann noch zustimmen).<br/>
Tets

