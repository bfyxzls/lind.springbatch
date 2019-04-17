package com.lind.springbatch.job;

import com.lind.springbatch.config.BeanValidator;
import com.lind.springbatch.entity.User;
import com.lind.springbatch.listener.PersonVerson2JobListener;
import com.lind.springbatch.processor.UserItemProcessor;
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
public class SyncPersonVerson2Job extends JobBase<User> {
  @Autowired
  private DataSource dataSource;
  @Autowired
  @Qualifier("primaryJdbcTemplate")
  private JdbcTemplate jdbcTemplate;

  public SyncPersonVerson2Job() {
    super("personVerson2Job", new PersonVerson2JobListener(), new UserItemProcessor(), new BeanValidator<>());
  }


  /**
   * 批量读数据.
   *
   * @return
   * @throws Exception
   */
  @Override
  public ItemReader<User> reader() throws Exception {
    StringBuffer sb = new StringBuffer();
    sb.append("select * from person limit 1");
    String sql = sb.toString();
    JdbcCursorItemReader<User> jdbcCursorItemReader =
        new JdbcCursorItemReader<>();
    jdbcCursorItemReader.setSql(sql);
    jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<>(User.class));
    jdbcCursorItemReader.setDataSource(dataSource);

    return jdbcCursorItemReader;
  }

  /**
   * 批量写数据.
   *
   * @return
   */
  @Override
  @Bean("personVerson2JobWriter")
  public ItemWriter<User> writer() {
    JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<>();
    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    String sql = "insert into person_export2 " + "(id,name) "
        + "values(:id, :name)";
    writer.setSql(sql);
    writer.setDataSource(dataSource);
    return writer;
  }

}
