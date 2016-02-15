#!/bin/bash

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
 dbName=miannot \
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
 skipAnnotPreCreate=false \
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

#now you can do:

mv ./eva-pipeline/src/test/resources/preannot.sorted.gz /tmp

zcat out/variants.preannot.tsv.gz | sort | gzip > out/preannot.sorted.gz
mv out/preannot.sorted.gz ./eva-pipeline/src/test/resources/preannot.sorted.gz

#for the annot.tsv you need to run the same above job with skipAnnotCreate=false

mv ./eva-pipeline/src/test/resources/annot.tsv.gz /tmp

mv out/variants.annot.gz ./eva-pipeline/src/test/resources/annot.tsv.gz

"
