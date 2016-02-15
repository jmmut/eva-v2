#!/bin/bash

# be sure that you have a mongo running and don't have an already existent db named VariantExporterTest


java -jar eva-pipeline/target/eva-pipeline-0.1.jar \
 --spring.batch.job.names=variantJob \
 input=eva-pipeline/src/test/resources/small20.vcf.gz \
 outputDir= \
 fileId=5 \
 aggregated=NONE \
 studyType=COLLECTION \
 studyName=studyName \
 studyId=7 \
 pedigree= \
 dbName=VariantExporterTest \
 storageEngine=mongodb \
 overwriteStats=true \
 compressGenotypes=true \
 compressExtension=.gz \
 includeSrc=FIRST_8_COLUMNS \
 vepInput=out/variants.preannot.tsv.gz \
 vepPath="/nfs/production2/eva/VEP/ensembl-tools-release-78/scripts/variant_effect_predictor/variant_effect_predictor.pl" \
 vepParameters="--force_overwrite --cache --cache_version 78 -dir /nfs/production2/eva/VEP/cache_1 --offline -o STDOUT --species homo_sapiens --everything" \
 vepFasta="--fasta /nfs/production2/eva/VEP/cache_1/homo_sapiens/78_GRCh37/Homo_sapiens.GRCh37.75.dna.primary_assembly.fa" \
 vepOutput=out/variants.annot.tsv.gz \
 skipAnnotPreCreate=true \
 skipAnnotCreate=true \
 skipAnnotLoad=true \
 skipStatsCreate=true \
 skipStatsLoad=true \
 skipLoad=false \
 --logging.level.embl.ebi.variation.eva=DEBUG \
 --logging.level.org.opencb.opencga=DEBUG \
 --logging.level.org.springframework=INFO \
 opencga.app.home=/opt/opencga


java -jar eva-pipeline/target/eva-pipeline-0.1.jar \
 --spring.batch.job.names=variantJob \
 input=eva-pipeline/src/test/resources/small20.vcf.gz \
 outputDir= \
 fileId=6 \
 aggregated=NONE \
 studyType=COLLECTION \
 studyName=studyName \
 studyId=8 \
 pedigree= \
 dbName=VariantExporterTest \
 storageEngine=mongodb \
 overwriteStats=true \
 compressGenotypes=true \
 compressExtension=.gz \
 includeSrc=FIRST_8_COLUMNS \
 vepInput=out/variants.preannot.tsv.gz \
 vepPath="/nfs/production2/eva/VEP/ensembl-tools-release-78/scripts/variant_effect_predictor/variant_effect_predictor.pl" \
 vepParameters="--force_overwrite --cache --cache_version 78 -dir /nfs/production2/eva/VEP/cache_1 --offline -o STDOUT --species homo_sapiens --everything" \
 vepFasta="--fasta /nfs/production2/eva/VEP/cache_1/homo_sapiens/78_GRCh37/Homo_sapiens.GRCh37.75.dna.primary_assembly.fa" \
 vepOutput=out/variants.annot.tsv.gz \
 skipAnnotPreCreate=true \
 skipAnnotCreate=true \
 skipAnnotLoad=true \
 skipStatsCreate=true \
 skipStatsLoad=true \
 skipLoad=false \
 --logging.level.embl.ebi.variation.eva=DEBUG \
 --logging.level.org.opencb.opencga=DEBUG \
 --logging.level.org.springframework=INFO \
 opencga.app.home=/opt/opencga


echo "
#now you can do :

mongodump -d VariantExporterTest

mv ./eva-tools/src/test/resources/dump/ /tmp

mv dump/ eva-tools/src/test/resources/
"


