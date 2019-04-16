package com.lind.springbatch.job;

import com.lind.springbatch.config.BeanValidator;
import com.lind.springbatch.entity.Person;
import com.lind.springbatch.listener.PersonVerson2JobListener;
import com.lind.springbatch.processor.PersonItemProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
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
public class SyncPersonVerson2Job extends JobBase {
  @Autowired
  private DataSource dataSource;
  @Autowired
  @Qualifier("primaryJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  public SyncPersonVerson2Job() {
    super("personVerson2Job", new PersonVerson2JobListener());
  }

  @Bean
  public Job personVerson2Job() throws Exception {
    return super.jobInitialization();
  }

  /**
   * 批量读数据.
   *
   * @return
   * @throws Exception
   */
  @Override
  public ItemReader<Person> reader() throws Exception {
    StringBuffer sb = new StringBuffer();
    sb.append("select * from person limit 1");
    String sql = sb.toString();
    JdbcCursorItemReader<Person> jdbcCursorItemReader =
        new JdbcCursorItemReader<>();
    jdbcCursorItemReader.setSql(sql);
    jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>(Person.class));
    jdbcCursorItemReader.setDataSource(dataSource);

    return jdbcCursorItemReader;
  }

  /**
   * 单条处理数据.
   *
   * @return
   */
  @Override
  public ItemProcessor<Person, Person> processor() {
    PersonItemProcessor processor = new PersonItemProcessor();
    processor.setValidator(csvBeanValidator());
    return processor;
  }

  /**
   * 批量写数据.
   *
   * @return
   */
  @Override
  @Bean("personVerson2JobWriter")
  public ItemWriter<Person> writer() {
    JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
    String sql = "insert into person_export2 " + "(id,name,age,nation,address) "
        + "values(:id, :name, :age, :nation,:address)";
    writer.setSql(sql);
    writer.setDataSource(dataSource);
    return writer;
  }

  /**
   * 校验数据.
   *
   * @return
   */
  @Override
  @Bean
  public Validator<Person> csvBeanValidator() {
    return new BeanValidator<>();
  }
}
