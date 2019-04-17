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

```
主要规定了公用方法的执行策略，而具体的job名称，读，写还是需要具体JOB去实现的。

### 具体Job实现
```
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
   
  }

```

### 添加一个api，手动触发
```
 @Autowired
  SyncPersonJob syncPersonJob;

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
```