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
        	steps{
        		withMaven(
			        // Maven installation declared in the Jenkins "Global Tool Configuration"
			        maven: 'maven-3',
			        // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
			        // We recommend to define Maven settings.xml globally at the folder level using 
			        // navigating to the folder configuration in the section "Pipeline Maven Configuration / Override global Maven configuration"
			        // or globally to the entire master navigating to  "Manage Jenkins / Global Tools Configuration"
			        mavenSettingsConfig: 'MavenProxy') {
						sh 'releng/build_and_test.sh --ci'
			    } 
			}
		}
    }
}


