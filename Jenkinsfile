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

        stage('Get dependecies'){
			stages {
				stage('Fetch RCPTT') {
		        	steps{
						sh './releng/fetch-rcptt-runner.sh m2-repository'
					}
				}
				stage ('Validate POM') {
		        	steps{
						sh "mvn ${mavenOpts} -Dtycho.mode=maven help:help"
					}
				}
				stage ('Resolve Maven Dependencies') {
	        		steps{
						sh "mvn ${mavenOpts} dependency:go-offline -Dtycho.mode=maven"
					}
				}
			}
		}

        stage('Build'){
			parallel{
				stage ('Build') {
		        	steps{
						sh "mvn ${mavenOpts} package -DskipTests=true -Dmaven.test.skip=true"
					}
				}
				stage ('Checkstyle') {
		        	steps{
						sh "releng/run_checkstyle.sh"
					}
				}	
			}
		}

		stage ('Test') {
			// run tests and findbugs
			// note: findbugs need everything packaged
			// note: fail at end to gather as much traces as possible
        	steps{
				sh "mvn --fail-at-end ${mavenOpts} verify "
			}	
		}

		stage ('Post Build') {
			parallel {
				// run sonar
				// note: run on same node to get test results and findbugs reports
				// note: never fail on that stage (report warning only)
				// note: executing in parallel with 'package' should not interfere
				stage ('Sonar') {
	        		steps{
						sh "mvn ${mavenOpts} sonar:sonar \
							  -Dsonar.projectKey=preesm \
							  -Dsonar.host.url=http://172.18.0.4:9000 \
							  -Dsonar.login=1e6332d1efe1f0cbb7ad040ef36794b42ac865ea"
					}
				}

				stage ('Check Packaging') {
					// final stage to check that the products and site can be packaged
					// noneed to redo all tests there
	        		steps{
						sh "mvn ${mavenOpts} -Dmaven.test.skip=true package"
					}
				}
			}	
		}
    }
}


