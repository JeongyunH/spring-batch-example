package com.hjy.example.ReadItem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@Slf4j
public class MyBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public MyBatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job myBatchJob() throws Exception {
        return this.jobBuilderFactory.get("myBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(this.myBatchStep(null))
                .listener(new SavePersonListener.SavePersonJobExecutionListener())
                .listener(new SavePersonListener.SavePersonAnnotationJobExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step myBatchStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
        return this.stepBuilderFactory.get("myBatchStep")
                .<Person, Person>chunk(10)
                .reader(this.csvFileItemReader())
                .processor(itemProcessor(allowDuplicate)
                        /*new DuplicateCheckProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate))*/)
                .writer(itemWriter())
                .listener(new SavePersonListener.SavePersonStepExecutionListener())
                .faultTolerant()
                .skip(NotFoundNameException.class)
                .skipLimit(2)
                .build();
    }

    private ItemProcessor<? super Person, ? extends Person> itemProcessor(String allow) throws Exception {
        DuplicateCheckProcessor<Person> duplicateCheckProcessor = new DuplicateCheckProcessor<>(Person::getName, Boolean.parseBoolean(allow));

        ItemProcessor<Person, Person> validationProcessor = item -> {
            if(item.isNotEmptyName()){
                return item;
            }
            throw new NotFoundNameException();
        };

        CompositeItemProcessor<Person, Person> itemProcessor = new CompositeItemProcessorBuilder<Person, Person>()
                .delegates(new PersonValidationRetryProcessor(), validationProcessor, duplicateCheckProcessor)
                .build();

        itemProcessor.afterPropertiesSet();
        return itemProcessor;
    }

    private ItemWriter<? super Person> itemWriter() throws Exception {
//        return items -> items.forEach(x->log.info("저는 {} 입니다", x.getName()));
        // Jpa에 쓰기위한 itemWriter
        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(entityManagerFactory)
                .build();

        // 로그를 쓰기위한 itemWriter
        ItemWriter<Person> logWriter = items -> log.info("person.size : {}", items.size());

        CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates(jpaItemWriter, logWriter)
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

/*    @Bean
    @StepScope
    public ItemProcessor<? super Person, ? extends Person> itemProcessor(
            @Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) {
        // 나는 인자 전달을 Processor에다가 받았지만, Step단계에서 받아서 넘기는게 나은듯...
        boolean allow = Boolean.parseBoolean(allowDuplicate);
        Map<String, String> map = new HashMap<>();

        return item -> {
            if(allow){
                return item;
            }
            else{
                if(map.containsKey(item.getName())){
                    return null;
                }
                map.put(item.getName(), item.getName());
                return item;
            }
        };
    }*/

    private FlatFileItemReader<Person> csvFileItemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setStrict(false);
        tokenizer.setNames("id", "name", "age", "address");
        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> new Person(
                fieldSet.readString(0),     // 이름
                fieldSet.readString(1),     // 나이
                fieldSet.readString(2)));   // 거주지

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("csvFileItemReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("skip_test.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

}
