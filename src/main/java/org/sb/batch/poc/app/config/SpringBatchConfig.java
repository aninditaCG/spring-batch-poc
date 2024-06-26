package org.sb.batch.poc.app.config;


import lombok.AllArgsConstructor;
import org.sb.batch.poc.app.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

    public FlatFileItemReader<Customer> itemReader(){
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csv-reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("id","name","email");
        tokenizer.setStrict(false);

        BeanWrapperFieldSetMapper mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Customer.class);

        lineMapper.setFieldSetMapper(mapper);
        lineMapper.setLineTokenizer(tokenizer);
        return lineMapper;
    }

    @Bean
    public CustomerProcessor processor(){
        return new CustomerProcessor();
    }

    private MongoTemplate mongoTemplate;
    @Bean
    public MongoItemWriter<Customer> mongoItemWriter(){
        MongoItemWriter<Customer> mongoItemWriter = new MongoItemWriter<>();
        mongoItemWriter.setTemplate(mongoTemplate);
        mongoItemWriter.setCollection("customer");
        return mongoItemWriter;
    }

    private TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);//TBD to test
        return asyncTaskExecutor;
    }

    @Bean
    public Step step(JobRepository repository, PlatformTransactionManager transactionManager){
        return new StepBuilder("csv-step",repository)
                .<Customer,Customer>chunk(10,transactionManager)
                .reader(itemReader())
                .processor(processor())
                .writer(mongoItemWriter())
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean(name="csvJob")
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new JobBuilder("csv-job",jobRepository)
                .flow(step(jobRepository,transactionManager)).end().build();
    }




    ///////////////SFTP Integration//////////
    @Bean
    public Job sftpJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        return jobBuilderFactory.get("sftpJob")
                .incrementer(new RunIdIncrementer())
                .start(sftpStep(stepBuilderFactory))
                .build();
    }

    @Bean
    public Step sftpStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("sftpStep")
                .<String, String>chunk(10)
                .reader(fileItemReader())
                .writer(list -> list.forEach(System.out::println))
                .build();
    }

    @Bean
    public FlatFileItemReader<String> fileItemReader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("local/directory"));
        reader.setLineMapper(new DefaultLineMapper<String>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("data");
            }});
            setFieldSetMapper(fieldSet -> fieldSet.readString(0));
        }});
        return reader;
    }

}