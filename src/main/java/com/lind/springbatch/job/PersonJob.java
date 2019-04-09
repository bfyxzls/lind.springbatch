package com.lind.springbatch.job;

import com.lind.springbatch.config.BeanValidator;
import com.lind.springbatch.entity.Person;
import com.lind.springbatch.listener.PersonJobListener;
import com.lind.springbatch.processor.PersonItemProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableBatchProcessing
public class PersonJob {
  @Autowired
  private DataSource dataSource;
  @Autowired
  private JobBuilderFactory job;
  @Autowired
  private StepBuilderFactory step;
  @Autowired
  @Qualifier("primaryJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  /**
   * .
   */
  @Bean
  public Job syncCustomerBaseInfoJob() throws Exception {
    return job.get("syncCustomerBaseInfo").incrementer(new RunIdIncrementer())
        .start(syncCustomerBaseInfo())
        .listener(new PersonJobListener())
        .build();
  }

  @Bean
  public ItemReader<Person> reader() throws Exception {
    StringBuffer sb = new StringBuffer();
    sb.append("select * from person");
    String sql = sb.toString();
    JdbcCursorItemReader<Person> jdbcCursorItemReader =
        new JdbcCursorItemReader<>();
    jdbcCursorItemReader.setSql(sql);
    jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>(Person.class));
    jdbcCursorItemReader.setDataSource(dataSource);

    return jdbcCursorItemReader;
  }

  @Bean
  public ItemProcessor<Person, Person> processor() {
    PersonItemProcessor processor = new PersonItemProcessor();
    processor.setValidator(csvBeanValidator());
    return processor;
  }


  @Bean
  public ItemWriter<Person> writer() {
    JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
    String sql = "insert into person_export " + "(id,name,age,nation,address) "
        + "values(:id, :name, :age, :nation,:address)";
    writer.setSql(sql); //3
    writer.setDataSource(dataSource);
    return writer;
  }


  /**
   * .
   *
   * @return
   */
  @Bean
  public ItemProcessor<Person, Person> baseInfoProcessor() {
    PersonItemProcessor processor = new PersonItemProcessor();
    processor.setValidator(csvBeanValidator());
    return processor;
  }


  /**
   * .
   */
  @Bean
  @JobScope
  public Step syncCustomerBaseInfo() throws Exception {
    return step.get("syncCustomerBaseInfo")
        .<Person, Person>chunk(5000)
        .reader(reader())
        .processor(baseInfoProcessor())
        .writer(writer())
        .build();
  }

  @Bean
  public PersonJobListener csvJobListener() {
    return new PersonJobListener();
  }

  @Bean
  public Validator<Person> csvBeanValidator() {
    return new BeanValidator<Person>();
  }
}
