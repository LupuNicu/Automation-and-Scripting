# Sarcină Laborator 4: Configurarea Jenkins pentru Automatizarea Sarcinilor DevOps

## Obiectiv

Învață cum să configurezi Jenkins pentru automatizarea sarcinilor DevOps, inclusiv crearea și gestionarea pipeline-urilor CI/CD.

## Pregătire

Creează un folder `lab4` în repository-ul tău GitHub pentru a stoca toate fișierele legate de acest laborator. Ar trebui să ai Docker și Docker Compose instalate pentru a completa sarcina.

## Crearea Fișierelor de Configurare

Am creat fișierul **docker-compose.yml**:

```yaml
services:
  jenkins-controller:
    image: jenkins/jenkins:lts
    container_name: jenkins-controller
    ports:
      - "8081:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
    networks:
      - jenkins-network

  ssh-agent:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ssh-agent
    environment:
      - JENKINS_AGENT_SSH_PUBKEY=${JENKINS_AGENT_SSH_PUBKEY}
    volumes:
      - jenkins_agent_volume:/home/jenkins/agent
    depends_on:
      - jenkins-controller
    networks:
      - jenkins-network

volumes:
  jenkins_home:
  jenkins_agent_volume:

networks:
  jenkins-network:
    driver: bridge
```

Am creat fișierul **Dockerfile**:

```dockerfile
FROM jenkins/ssh-agent

# Install PHP-CLI, Git, curl, unzip and PHP extensions
RUN apt-get update && apt-get install -y \
    php-cli \
    php-xml \
    php-curl \
    php-mbstring \
    php-zip \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Composer
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer \
    && chmod +x /usr/local/bin/composer

# Verify installations
RUN php --version && composer --version && git --version && php -m | grep -E "(dom|curl|xml)"
```

Am creat fișierul **Jenkinsfile**:

```groovy
pipeline {
    agent {
        label 'php-agent'
    }
    
    stages {        
        stage('Install Dependencies') {
            steps {
                echo 'Instalare dependențe...'
                sh 'composer install --no-interaction --prefer-dist'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Rulare teste unitare...'
                sh 'vendor/bin/phpunit tests/ --testdox'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finalizat.'
        }
        success {
            echo 'Toate etapele au fost finalizate cu succes!'
        }
        failure {
            echo 'Erori detectate în pipeline.'
        }
    }
}
```

---

Rulez următoarea comandă în terminal:

```bash
docker-compose up -d jenkins-controller
```

![](images/1.png)
![](images/2.png)

Deschide browserul și accesează: `http://localhost:8081`

În terminal, rulez pentru a vedea parola in terminal:

```bash
docker exec jenkins-controller cat /var/jenkins_home/secrets/initialAdminPassword
```
![](images/3.png)

Am introdus parola:

![](images/4.png)

Alegem instalarea la suggested plugin

![](images/5.png)

Asteptam inslarea

![](images/6.png)

Creem userul de admin

![](images/7.png)

Lasam URL la 8081

![](images/8.png)

În terminal crem folderul pentru cheie

```bash
mkdir secrets
cd secrets
```
![](images/9.png)

Generam cheia ssh

```bash
ssh-keygen -f jenkins_agent_ssh_key
```
![](images/10.png)

Citim cheia publica

Rulează:

```bash
cat jenkins_agent_ssh_key.pub
```

![](images/11.png)

Înapoi în directorul rădăcină al proiectului creez un fișier `.env` cu următorul conținut:

```
JENKINS_AGENT_SSH_PUBKEY=ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAID1YJmLWIBVI9fIqRYZb+d8QwtZk5kFhaaUXOPJki5hI nicu@DESKTOP-7EFFO4G
```

Pornirea serviciilor complete

```bash
docker-compose up -d --build
```
![](images/12.png)
![](images/13.png)

Conectarea SSH Agent la Jenkins prin interfata grafica

Accesez Jenkins la `http://localhost:8081`

Mergem la Setari Jenkins

![](images/14.png)

Cautăm "SSH Agents Plugin" în listă pentru a verifica daca e instalat
![](images/15.png)

Apoi înregistram cheia SSH în Jenkins

Mergem la **Управление Jenkins** 
Click pe **Добавить учетные данные**

![](images/16.png)

Completează formularul:
  - **Тип (Kind):** Selectează **SSH имя пользователя с закрытым ключом** (SSH Username with private key)
  - **Область действия (Scope):** Lasă **Глобальная** (Global)
  - **Имя пользователя (Username):** `jenkins`
  - **Закрытый ключ (Private Key):** Selectează **Ввести напрямую** (Enter directly)
  - **Ключ (Key):** Deschide fișierul `secrets/jenkins_agent_ssh_key` și **copiază întregul conținut** 

![](images/17.png)

![](images/18.png)

Adăugăm un nou nod Jenkins agent

Mergi la Управление Jenkins
Click pe New Node

![](images/19.png)

Creez un nod nou

![](images/20.png)

![](images/21.png)

Folosim un proiect PHP pentru teste de pe git hub

Crearez un Pipeline Jenkins

![](images/22.png)

Si il configurez

![](images/23.png)

Apoi facem build la pepeline

![](images/24.png)

Dupa 2 erori din a 3 incercare vedem ca totul e ok

![](images/25.png)

![](images/26.png)

## Raspuns la interbari

### 6.5. Răspunsuri la întrebări

#### Care sunt avantajele utilizării Jenkins pentru automatizarea sarcinilor DevOps?

Jenkins permite automatizarea întregului proces de dezvoltare, de la commit până la deployment, reducând erorile umane și accelerând livrarea software-ului. Se integrează cu o gamă largă de tool-uri DevOps (Git, Docker, Kubernetes, cloud providers, etc.) și suportă mii de plugin-uri.
Jenkins este gratuit și open-source, cu o comunitate activă și suport continuu.

#### Ce alte tipuri de agenți Jenkins există?
* Permanent Agents
* Cloud Agents
* Docker Agents
* Kubernetes Agents
* SSH Agents
* JNLP Agents

#### Probleme întâlnite:

La prima rulare a pipeline-ului, am întâlnit următoarea eroare:
```
+ composer install --no-interaction --prefer-dist
/home/jenkins/agent/workspace/php-project-pipeline@tmp/durable-b58e389f/script.sh.copy: 1: composer: not found
ERROR: script returned exit code 127
```

**Cauză:** Composer nu era instalat în containerul SSH Agent. Dockerfile-ul inițial instala doar PHP-CLI, dar nu și Composer, care este necesar pentru instalarea dependențelor PHP prin `composer.json`.

**Soluție:**
Am actualizat `Dockerfile` pentru a include instalarea Composer:

```dockerfile
FROM jenkins/ssh-agent

# Install PHP-CLI, Git, curl, unzip and other dependencies
RUN apt-get update && apt-get install -y \
    php-cli \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Composer
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer \
    && chmod +x /usr/local/bin/composer

# Verify installations
RUN php --version && composer --version && git --version
```

Am reconstruit imaginea SSH Agent:

```bash
docker-compose down
docker-compose build --no-cache ssh-agent
docker-compose up -d
```

După rezolvarea problemei cu Composer, la rularea pipeline-ului am întâlnit următoarea eroare:
```
Your requirements could not be resolved to an installable set of packages.

Problem 1
  - phpunit/phpunit[9.5.0, ..., 9.6.30] require ext-dom * -> it is missing from your system.
  
Composer is operating significantly slower than normal because you do not have the PHP curl extension enabled.
```

**Cauză:** PHPUnit necesită extensia PHP `ext-dom` (inclusă în `php-xml`), iar Composer funcționează mai bine cu extensia `php-curl`. Aceste extensii nu erau instalate în containerul SSH Agent.

**Soluție:**
Am actualizat `Dockerfile` pentru a include extensiile PHP necesare:
```dockerfile
FROM jenkins/ssh-agent

# Install PHP-CLI, Git, curl, unzip and PHP extensions
RUN apt-get update && apt-get install -y \
    php-cli \
    php-xml \
    php-curl \
    php-mbstring \
    php-zip \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Composer
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer \
    && chmod +x /usr/local/bin/composer

# Verify installations
RUN php --version && composer --version && git --version && php -m | grep -E "(dom|curl|xml)"
```
Am reconstruit imaginea SSH Agent:
```bash
docker-compose down
docker-compose build --no-cache ssh-agent
docker-compose up -d
```

## Concluzie

Acest laborator demonstrează procesul complet de configurare a Jenkins pentru automatizarea sarcinilor DevOps, de la setup-ul inițial al Jenkins Controller până la crearea și rularea unui pipeline CI/CD pe un agent SSH specializat. Configurația permite execuția automată a testelor și build-urilor pentru proiecte PHP, facilitând procesul de dezvoltare continuă.