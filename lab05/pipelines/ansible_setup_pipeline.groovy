pipeline {
    agent {
        label 'ansible-agent'
    }
    
    environment {
        ANSIBLE_HOST_KEY_CHECKING = 'False'
    }
    
    stages {
        stage('Verify Ansible') {
            steps {
                echo 'Verificare instalare Ansible...'
                sh 'ansible --version'
            }
        }
        
        stage('Test Connection') {
            steps {
                echo 'Testare conexiune la serverul de test...'
                sh 'ansible all -i /home/jenkins-ansible/ansible/hosts.ini -m ping'
            }
        }
        
        stage('Run Ansible Playbook') {
            steps {
                echo 'Executare playbook Ansible pentru configurarea serverului...'
                sh 'ansible-playbook -i /home/jenkins-ansible/ansible/hosts.ini /home/jenkins-ansible/ansible/setup_test_server.yml -v'
            }
        }
        
        stage('Verify Apache') {
            steps {
                echo 'Verificare că Apache rulează...'
                sh 'ansible all -i /home/jenkins-ansible/ansible/hosts.ini -m shell -a "pgrep -f apache2 && echo Apache is running || service apache2 status"'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline de configurare finalizat.'
        }
        success {
            echo 'Serverul de test a fost configurat cu succes!'
        }
        failure {
            echo 'Eroare la configurarea serverului de test.'
        }
    }
}

