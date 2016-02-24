#!/bin/sh -xe

# Submit Spark job

SPARK_HOME=/Users/kellypet/PredictionIO/vendors/spark-1.5.1/bin

cd $SPARK_HOME; \
./spark-submit --master spark://localhost:7078 \
--class sparkDataExplorer.$1 \
--jars /Users/kellypet/Desktop/PredictionIO/open-academy-repo/KellyPeterson/sparkDataExplorer/target/scala-2.10/sparkDataExplorer_2.10-1.0.jar \
/Users/kellypet/Desktop/PredictionIO/open-academy-repo/KellyPeterson/sparkDataExplorer/target/scala-2.10/sparkDataExplorer-assembly-1.0-deps.jar

