package com.lind.springbatch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class PersonJobListener implements JobExecutionListener {

  long startTime;
  long endTime;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    startTime = System.currentTimeMillis();
    System.out.println("PersonJobListener 任务处理开始");
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    endTime = System.currentTimeMillis();
    System.out.println("PersonJobListener 任务处理结束,耗时:" + (endTime - startTime) + "ms");
  }

}