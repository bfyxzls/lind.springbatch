package com.lind.springbatch.job;

import com.lind.springbatch.config.BeanValidator;
import com.lind.springbatch.entity.Person;
import com.lind.springbatch.listener.PersonJobListener;
import com.lind.springbatch.processor.PersonItemProcessor;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableBatchProcessing
public class SyncPersonJob extends JobBase<Person> {
  @Autowired
  private DataSource dataSource;
  @Autowired
  @Qualifier("primaryJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  /**
   * 初始化，规则了job名称和监视器.
   */
  public SyncPersonJob() {
    super("personJob", new PersonJobListener(), new PersonItemProcessor(), new BeanValidator<>());
  }

  @Override
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


  @Override
  @Bean("personJobWriter")
  public ItemWriter<Person> writer() {
    JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
    String sql = "insert into person_export " + "(id,name,age,nation,address) "
        + "values(:id, :name, :age, :nation,:address)";
    writer.setSql(sql);
    writer.setDataSource(dataSource);
    return writer;
  }

}
