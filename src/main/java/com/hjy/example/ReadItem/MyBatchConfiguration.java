package com.hjy.example.ReadItem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class MyBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public MyBatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job myBatchJob() throws Exception {
        return this.jobBuilderFactory.get("myBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(this.myBatchStep())
                .build();
    }

    @Bean
    public Step myBatchStep() throws Exception {
        return this.stepBuilderFactory.get("myBatchStep")
                .<Person, Person>chunk(10)
                .reader(this.csvFileItemReader())
                .processor(itemProcessor(null))
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super Person> itemWriter() {
//        return items -> items.forEach(x->log.info("저는 {} 입니다", x.getName()));

        CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates()
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<? super Person, ? extends Person> itemProcessor(
            @Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) {
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
    }

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
                .resource(new ClassPathResource("test1.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

}
