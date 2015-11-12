/*
 * Copyright 2015 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package embl.ebi.variation.eva.pipeline.jobs;

import embl.ebi.variation.eva.pipeline.steps.VariantsLoad;
import embl.ebi.variation.eva.pipeline.steps.VariantsStatsCreate;
import embl.ebi.variation.eva.pipeline.steps.VariantsStatsLoad;
import org.apache.commons.collections.list.SynchronizedList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.StorageManagerFactory;
import org.opencb.opencga.storage.core.variant.VariantStorageManager;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static embl.ebi.variation.eva.pipeline.jobs.JobTestUtils.*;
import static org.junit.Assert.*;

/**
 * Created by jmmut on 2015-10-14.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VariantConfiguration.class})
public class splitJobTest {

    private static final Logger logger = LoggerFactory.getLogger(splitJobTest.class);
    private final Ticket start1 = new Ticket("starting step1:  ");
    private final Ticket end1 = new Ticket("finishing step1: ");
    private final Ticket start2 = new Ticket("starting step2:  ");
    private final Ticket end2 = new Ticket("finishing step2: ");
    private final Ticket start3 = new Ticket("starting step3:  ");
    private final Ticket end3 = new Ticket("finishing step3: ");


    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobRepository jobRepository;

    private class Ticket {
        String message;
        Long time;

        public Ticket(String message) {
            this.message = message;
        }
        public Ticket(String message, Long time) {
            this.message = message;
            this.time = time;
        }

        @Override
        public boolean equals (Object o){
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Ticket ticket = (Ticket) o;

            return !(message != null ? !message.equals(ticket.message) : ticket.message != null);

        }

        @Override
        public int hashCode () {
            return message != null ? message.hashCode() : 0;
        }
    }
    List<Ticket> order = SynchronizedList.decorate(new ArrayList<Ticket>());

    /**
     * The idea in this test is to ensure that a variantJob with transform, load, stats and annotation; performs
     * transform and load sequentially, and from then, stats and annot independently (concurrently, without time
     * dependecies). Still we should decide what to do in cases like:
     * - annot failed but stats could finish. Will the abrupt termination of annot, stop as well the stats?
     */
    @Test
    public void testSplitJob() throws Exception {
        JobBuilder jobBuilder = jobBuilderFactory.get("splitJob");
        JobExecution execution = jobRepository.createJobExecution("flow", new JobParameters());
        Flow flow = new FlowBuilder<Flow>("splitflow")
                .split(new SimpleAsyncTaskExecutor())
//                .split(new ConcurrentTaskExecutor())

                .add(new FlowBuilder<Flow>("subflow1").from(step2()).end(),
                        new FlowBuilder<Flow>("subflow2").from(step3()).end())
                .build();
        Flow branchFlow = new FlowBuilder<Flow>("branchFlow").start(step1()).next(flow).build();

        jobBuilder.start(branchFlow).build().build().execute(execution);

        for (Ticket t : order) {
            logger.info(t.message + t.time);
        }
        assertTrue("order.indexOf(end1) < order.indexOf(start2)", order.indexOf(end1) < order.indexOf(start2));
        assertTrue("order.indexOf(end1) < order.indexOf(start3)", order.indexOf(end1) < order.indexOf(start3));
        assertTrue("order.indexOf(start2) < order.indexOf(end3)", order.indexOf(start2) < order.indexOf(end3));
        assertTrue("order.indexOf(start3) < order.indexOf(end2)", order.indexOf(start3) < order.indexOf(end2));

    }

    public Step step1() {
        StepBuilder step1 = stepBuilderFactory.get("step1");
        TaskletStepBuilder tasklet = step1.tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                order.add(new Ticket(start1.message, System.currentTimeMillis()));
                Thread.sleep(5000);
                order.add(new Ticket(end1.message, System.currentTimeMillis()));
                return RepeatStatus.FINISHED;
            }
        });
        return tasklet.build();
    }

    public Step step2() {
        StepBuilder step1 = stepBuilderFactory.get("step2");
        TaskletStepBuilder tasklet = step1.tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                order.add(new Ticket(start2.message, System.currentTimeMillis()));
                Thread.sleep(5000);
                order.add(new Ticket(end2.message, System.currentTimeMillis()));
                return RepeatStatus.FINISHED;
            }
        });
        return tasklet.build();
    }

    public Step step3() {
        StepBuilder step1 = stepBuilderFactory.get("step3");
        TaskletStepBuilder tasklet = step1.tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                order.add(new Ticket(start3.message, System.currentTimeMillis()));
                Thread.sleep(5000);
                order.add(new Ticket(end3.message, System.currentTimeMillis()));
                return RepeatStatus.FINISHED;
            }
        });
        return tasklet.build();
    }

}
