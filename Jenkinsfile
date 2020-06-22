#!groovy

// how many time should we retry resolving dependencies
retryResolveCount = 4

def javaVersion = "1.8"
def javaToolID = "JDK-${javaVersion}"

def mavenVersion = "3.5.0"
def mavenToolID = "Maven-${mavenVersion}"
def mavenOpts = "--errors --strict-checksums --batch-mode -Dmaven.repo.local=m2-repository"
def mavenEnvOpt = "MAVEN_OPT=-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

// tell Jenkins to remove 7 days old artifacts/builds and keep only 7 last ones
//properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '7', daysToKeepStr: '7', numToKeepStr: '7']]]);


pipeline {
    agent {
        docker {
            image 'maven:3-jdk-8'
            args '-u root' // --dns 10.4.1.79 
        }
    }

    stages {
        stage('Fetch Source Code') {
        	steps{
            	checkout scm
            }
        }

        stage('Build & Unit Tests'){
			steps {		
                sh 'export PATH=$MVN_CMD_DIR:$PATH && mvn clean verify -B -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
		}
    }
}


