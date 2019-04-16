# springbatch
主要实现批量数据的处理，我对batch进行的封装，提出了jobBase类型，具体job需要实现它即可。Spring Batch 不仅提供了统一的读写接口、丰富的任务处理方式、灵活的事务管理及并发处理，同时还支持日志、监控、任务重启与跳过等特性，大大简化了批处理应用开发，将开发人员从复杂的任务配置管理过程中解放出来，使他们可以更多地去关注核心的业务处理过程。
### 几个组件
* job
* step
* read
* write
* listener
* process
* validator

### JobBase定义了几个公用的方法
```
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
```
主要规定了公用方法的执行策略，而具体的job名称，读，写，检验，处理器，监视器还是需要具体JOB去实现的。

### 具体Job实现
```
 /**
   * 初始化，规则了job名称和监视器.
   */
  public SyncPersonJob() {
    super("personJob", new PersonJobListener());
  }

  /**
   * 必须要定义一个bean，方法名称就是bean名称，在controller里注入时使用.
   *
   * @return
   * @throws Exception
   */
  @Bean
  public Job personJob() throws Exception {
    return super.jobInitialization();
  }
```
### 读和处理器根据自已业务实现即可
```$xslt
/**
   * 批量读数据.
   *
   * @return
   * @throws Exception
   */
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
```
###  写操作需要定义自己的bean的声明
> 注意，需要为每个job的write启个名称，否则在多job时，write将会被打乱
```$xslt
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

```
### 检验器也是一个bean
```$xslt
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
```