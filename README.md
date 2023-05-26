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
# should end with /
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
Um die Anwendung testweise zu starten wird der folgende Befehl ausgeführt:
```console
java -jar /var/KRITIS-ELFe/KRITIS-ELFe.jar --spring.config.location="/var/KRITIS-ELFe/application.properties"
```
Wenn die JAR-Datei ohne Fehler ausgeführt werden kann, dann ist die Datenbank und die application.properties richtig konfiguriert. Die Anwendung kann dann erst einmal mit Strg C geschlossen werden.
### NGINX konfiguration
Um die Anwendung von außen erreichbar zu machen muss jede HTTP Anfrage witergeleitet werden an die Anwendung über nginx. <br/>
Zu erst muss nginx installiert werden:
```console
sudo apt-get install nginx
```
Dann muss das weiterleiten konfiguriert werden. Dafür wird die Konfigurationsdatei aufgerufen:
```console
sudo nano /etc/nginx/sites-available/default
```
Und folgende Konfiguration eingefügt:
```console
server {
    listen 80 default_server;
    listen [::]:80 default_server;

    server_name _ your_domain;

    location / {
            proxy_pass http://localhost:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_cache_bypass $http_upgrade;
    }
}
```
### Service erstellen
Um einen Service zu erstellen muss erste eine Servicedatei eruegt werden:
```console:
sudo nano /etc/systemd/system/kritis-elfe.service
```
Dort muss die Konfiguratiun des Services eingefügt werden:
```service
[Unit]
Description=Start of the KRITIS-ELFe.

[Service]
Type=simple
ExecStart=java -jar /var/KRITIS-ELFe/KRITIS-ELFe.jar --spring.config.location="/var/KRITIS-ELFe/application.properties"
Restart=no

[Install]
WantedBy=multi-user.target
```
Wieder bestätigen mit Strg X.<br/>
Um den Service zu Nutzen muss das Ganze neu geladen werden:
```console
sudo systemctl daemon-reload 
```
Danach kann der Service so gestartet:
```console
sudo systemctl start kritis-elfe
```
, so gestoppt:
```console
sudo systemctl stop kritis-elfe
```
, so neu gestartet:
```console
sudo systemctl restart kritis-elfe
```
und so der Status abgefragt werden:
```console
sudo systemctl status kritis-elfe
```
Wenn der Service nun gestartet wird, sollte nach ca. 2 Minuten die Webanwendung erreichbar sein.

### Delpoy Skript
Um das Aufnehmen von Änderungen von GitHub einfacher zu gestalten, sollte ein deploy Skript erzeugt werden:
```console
sudo nano /var/KRITIS-ELFe/deploy.sh
```
In diesem Skript wird der Service gestoppt, die alte JAR gelöscht, die JAR neu heruntergeladen und der Service wieder gestartet:
```bash
systemctl stop kritis-elfe
rm /var/KRITIS-ELFe/KRITIS-ELFe.jar
wget -O /var/KRITIS-ELFe/KRITIS-ELFe.jar https://github.com/leonxs2001/KRITIS-ELFe/raw/master/KRITIS-ELFe/target/KRITIS_ELFe-0.0.1-SNAPSHOT.jar
systemctl start kritis-elfe
```
Und das Ganze bestätigen mit Strg X.
Ausführrechte geben:
```console 
sudo chmod ugo+x /var/KRITIS-ELFe/deploy.sh
```
Ausgeführt werden kann diese Datei dann mit:
```console
sudo /var/KRITIS-ELFe/deploy.sh
```
