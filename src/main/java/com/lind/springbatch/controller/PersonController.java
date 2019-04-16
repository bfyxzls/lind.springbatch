package com.lind.springbatch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {
  @Autowired
  Job personJob;
  @Autowired
  Job personVerson2Job;
  @Autowired
  JobLauncher jobLauncher;

  void exec(Job job) throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();
    new Thread(() -> {
      try {
        jobLauncher.run(job, jobParameters);
      } catch (JobExecutionAlreadyRunningException
          | JobRestartException
          | JobInstanceAlreadyCompleteException
          | JobParametersInvalidException e) {
        e.printStackTrace();
      }
    }).start();
  }

  @RequestMapping("/run1")
  public String run1() throws Exception {
    exec(personJob);
    return "personJob success";
  }

  @RequestMapping("/run2")
  public String run2() throws Exception {
    exec(personVerson2Job);
    return "personVerson2Job success";
  }

}
