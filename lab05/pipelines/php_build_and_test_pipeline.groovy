pipeline {
    agent {
        label 'php-agent'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Clonarea repository-ului...'
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/LupuNicu/phpProject.git']]
                    ])
                }
            }
        }
        
        stage('Install Dependencies') {
            steps {
                echo 'Instalare dependențe...'
                sh 'composer install --no-interaction --prefer-dist'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Rulare teste unitare...'
                sh 'mkdir -p tests/_output'
                sh 'vendor/bin/phpunit tests/ --testdox --log-junit tests/_output/junit.xml'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finalizat.'
            // Publish test results
            script {
                if (fileExists('tests/_output/junit.xml')) {
                    junit 'tests/_output/junit.xml'
                } else {
                    echo 'Nu s-au găsit rapoarte JUnit. Testele au fost rulate cu succes, dar raportul nu a fost generat.'
                }
            }
        }
        success {
            echo 'Toate etapele au fost finalizate cu succes!'
        }
        failure {
            echo 'Erori detectate în pipeline.'
        }
    }
}

