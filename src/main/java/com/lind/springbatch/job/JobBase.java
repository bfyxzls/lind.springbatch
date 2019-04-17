package com.lind.springbatch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * springBatch的job基础类.
 */
public abstract class JobBase<T> {

  /**
   * 批次.
   */
  protected int chunkCount = 5000;
  /**
   * 监听器.
   */
  private JobExecutionListener jobExecutionListener;
  /**
   * 处理器.
   */
  private ValidatingItemProcessor<T> validatingItemProcessor;
  /**
   * job名称.
   */
  private String jobName;
  /**
   * 检验器.
   */
  private Validator<T> validator;
  @Autowired
  private JobBuilderFactory job;
  @Autowired
  private StepBuilderFactory step;


  /**
   * 初始化.
   *
   * @param jobName                 job名称
   * @param jobExecutionListener    监听器
   * @param validatingItemProcessor 处理器
   * @param validator               检验
   */
  public JobBase(String jobName,
                 JobExecutionListener jobExecutionListener,
                 ValidatingItemProcessor<T> validatingItemProcessor,
                 Validator<T> validator) {
    this.jobName = jobName;
    this.jobExecutionListener = jobExecutionListener;
    this.validatingItemProcessor = validatingItemProcessor;
    this.validator = validator;
  }

  /**
   * job初始化与启动.
   */
  public Job getJob() throws Exception {
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
        .<T, T>chunk(chunkCount)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .build();
  }

  /**
   * 单条处理数据.
   *
   * @return
   */
  public ItemProcessor<T, T> processor() {
    validatingItemProcessor.setValidator(processorValidator());
    return validatingItemProcessor;
  }

  /**
   * 校验数据.
   *
   * @return
   */
  @Bean
  public Validator<T> processorValidator() {
    return validator;
  }

  /**
   * 批量读数据.
   *
   * @return
   * @throws Exception
   */
  public abstract ItemReader<T> reader() throws Exception;

  /**
   * 批量写数据.
   *
   * @return
   */
  @Bean
  public abstract ItemWriter<T> writer();

}
