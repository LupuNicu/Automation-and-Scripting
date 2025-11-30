pipeline {
    agent {
        label 'ansible-agent'
    }
    
    environment {
        ANSIBLE_HOST_KEY_CHECKING = 'False'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Clonarea repository-ului cu proiectul PHP...'
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/LupuNicu/phpProject.git']]
                    ])
                }
            }
        }
        
        stage('Deploy to Test Server') {
            steps {
                echo 'Deployarea proiectului PHP pe serverul de test...'
                script {
                    // Get the workspace directory
                    def workspaceDir = pwd()
                    
                    // Create temporary playbook for deployment
                    writeFile file: '/tmp/deploy.yml', text: """
---
- name: Deploy PHP Application
  hosts: test_servers
  become: yes
  become_user: root
  
  tasks:
    - name: Create application directory
      file:
        path: /var/www/html/app
        state: directory
        owner: www-data
        group: www-data
        mode: '0755'
    
    - name: Create temporary directory on test server
      file:
        path: /tmp/app_temp
        state: directory
        owner: ansible
        group: ansible
        mode: '0755'
        
    - name: Remove old host key
      shell: ssh-keygen -f /home/jenkins-ansible/.ssh/known_hosts -R test-server || true
      delegate_to: localhost
      become: no
      
    - name: Copy entire application directory structure
      shell: |
        rsync -avz --delete \
          --exclude=.git \
          --exclude=vendor \
          --exclude=node_modules \
          --exclude=tests \
          --exclude=.env \
          -e "ssh -i /home/jenkins-ansible/.ssh/id_rsa -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" \
          "${workspaceDir}/" ansible@test-server:/tmp/app_temp/
      delegate_to: localhost
      become: no
      
    - name: Move files to final location and set permissions
      shell: |
        sudo rm -rf /var/www/html/app/*
        sudo cp -r /tmp/app_temp/* /var/www/html/app/
        sudo chown -R www-data:www-data /var/www/html/app
        sudo chmod -R 755 /var/www/html/app
        sudo rm -rf /tmp/app_temp
"""
                    
                    sh 'ansible-playbook -i /home/jenkins-ansible/ansible/hosts.ini /tmp/deploy.yml -v'
                }
            }
        }
        
        stage('Configure Application') {
            steps {
                echo 'Configurarea aplicației pe server...'
                script {
                    // Create playbook for installing Composer and dependencies
                    writeFile file: '/tmp/configure_app.yml', text: """
---
- name: Configure PHP Application
  hosts: test_servers
  become: yes
  become_user: root
  
  tasks:
    - name: Install PHP, curl and required PHP extensions
      apt:
        name:
          - php
          - php-cli
          - php-common
          - php-xml
          - php-curl
          - php-mbstring
          - curl
          - unzip
        state: present
        update_cache: yes
      
    - name: Install Composer if not installed
      shell: |
        if ! command -v composer &> /dev/null; then
          curl -sS https://getcomposer.org/installer | php
          mv composer.phar /usr/local/bin/composer
          chmod +x /usr/local/bin/composer
        fi
      args:
        creates: /usr/local/bin/composer
      environment:
        PATH: /usr/bin:/usr/local/bin:/usr/local/sbin:/usr/sbin:/sbin:/bin
      
    - name: Install Composer dependencies
      shell: |
        cd /var/www/html/app
        if [ -f composer.json ]; then
          composer install --no-dev --optimize-autoloader
        fi
      environment:
        PATH: /usr/bin:/usr/local/bin:/usr/local/sbin:/usr/sbin:/sbin:/bin
"""
                    
                    sh 'ansible-playbook -i /home/jenkins-ansible/ansible/hosts.ini /tmp/configure_app.yml -v'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline de deploy finalizat.'
        }
        success {
            echo 'Aplicația PHP a fost deployată cu succes!'
            echo 'Accesează aplicația la: http://localhost:8082/app'
        }
        failure {
            echo 'Eroare la deployarea aplicației.'
        }
    }
}

