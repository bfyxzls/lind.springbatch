package com.lind.springbatch.job;

import com.lind.springbatch.entity.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * springBatch的job基础类.
 */
public abstract class JobBase {

  protected int chunkCount = 5000;
  JobExecutionListener jobExecutionListener;
  @Autowired
  private JobBuilderFactory job;
  @Autowired
  private StepBuilderFactory step;
  private String jobName;

  /**
   * 初始化.
   *
   * @param jobName              job名称
   * @param jobExecutionListener job监视器
   */
  public JobBase(String jobName, JobExecutionListener jobExecutionListener) {
    this.jobName = jobName;
    this.jobExecutionListener = jobExecutionListener;
  }

  /**
   * job初始化与启动.
   */
  public Job jobInitialization() throws Exception {
    return job.get(jobName).incrementer(new RunIdIncrementer())
        .start(syncStep())
        .listener(jobExecutionListener)
        .build();
  }

  /**
   * 执行步骤.
   *
   * @return
   */
  public Step syncStep() throws Exception {
    return step.get("step1")
        .<Person, Person>chunk(chunkCount)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .build();
  }

  /**
   * 批量读数据.
   *
   * @return
   * @throws Exception
   */
  public abstract ItemReader<Person> reader() throws Exception;

  /**
   * 单条处理数据.
   *
   * @return
   */
  public abstract ItemProcessor<Person, Person> processor();

  /**
   * 批量写数据.
   *
   * @return
   */
  @Bean
  public abstract ItemWriter<Person> writer();

  /**
   * 校验数据.
   *
   * @return
   */
  @Bean
  public abstract Validator<Person> csvBeanValidator();
}
