package com.lind.springbatch.controller;

import com.lind.springbatch.job.SyncPersonJob;
import com.lind.springbatch.job.SyncPersonVerson2Job;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {
  @Autowired
  SyncPersonJob syncPersonJob;
  @Autowired
  SyncPersonVerson2Job syncPersonVerson2Job;

  @Autowired
  JobLauncher jobLauncher;

  void exec(Job job) throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();
    jobLauncher.run(job, jobParameters);
  }

  @RequestMapping("/run1")
  public String run1() throws Exception {
    exec(syncPersonJob.getJob());
    return "personJob success";
  }

  @RequestMapping("/run2")
  public String run2() throws Exception {
    exec(syncPersonVerson2Job.getJob());
    return "personVerson2Job success";
  }

}
