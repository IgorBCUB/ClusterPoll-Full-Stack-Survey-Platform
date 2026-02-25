# ClusterPoll-Full-Stack-Survey-Platform
User-created surveys where responses are analyzed to detect similar user groups via clustering (k-means or similar). Full-stack implementation: frontend for survey creation/response, backend for data analysis and insights. Built collaboratively with a team.

# Compilar tot (clases de domini + test):
    desde l'arrel del projecte fer:  javac -cp ".;FONTS/src/domini/Tests/junit-4.13.2.jar;FONTS/src/domini/Tests/hamcrest-core-1.3.jar" FONTS/src/domini/*.java .\FONTS\src\domini\Tests\*.java

# Executar Programa (main):
    desde l'arrel del projecte fer: java -cp .\FONTS\src domini.Main

# Executar algun Test:
    desde l'arrel del projecte fer:
        java -cp ".;FONTS/src/domini/Tests/junit-4.13.2.jar;FONTS/src/domini/Tests/hamcrest-core-1.3.jar;FONTS/src" org.junit.runner.JUnitCore domini.Tests.NomDelTestAExecutar
        -per exemple per TestUsuari seria: java -cp ".;FONTS/src/domini/Tests/junit-4.13.2.jar;FONTS/src/domini/Tests/hamcrest-core-1.3.jar;FONTS/src" org.junit.runner.JUnitCore domini.Tests.TestUsuari
