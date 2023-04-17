# Spring Batch Study

Batch를 실행시키는 단위 -> Job
Job은 n개의 Step으로 실행할 수 있으며, 흐름을 관리할 수 있음.(조건에 따라 실행 설정한다던지..)

Job타입의 Bean이 생성되면, JobLauncher에 의해 Job이 실행 (JobLauncherTestUtils는 테스트코드에서 Job과 Step실행)

Step의 실행단위는 크게 2가지
1. Chunk 기반 : 하나의 큰 덩어리를 n개씩 나눠서 실행
* Chunk 기반 Step의 종류
- ItemReader (배치 처리 대상 객체를 읽음, ex)파일이나 DB에서 데이터 읽기)
- ItemProcessor (읽은 데이터 수정 또는 ItemWriter 대상인지 filtering, optional step)
- ItemWriter (대상 객체 처리, ex)DB 업데이트 또는 처리 대상 사용자에게 알림)

2. Task 기반 : 하나의 작업 기반으로 실행
- Tasklet을 이용

@JobScope, @StepScope

기본 제공되는 ItemReader 구현체
- file, jdbc, jpa, hibernate, kafka, etc..

JDBC를 이용한 DB를 읽는 개념
- Cursor 기반 조회 / JdbcCursorItemReader / sql 조회쿼리 설정
- Paging 기반 조회 / JdbcPagingItemReader / --Clause, queryProvider 조회쿼리 설정

Jpa 기반 DB 읽기 (entityManagerFactory 필요)
- Cursor 기반 조회 / JpaCursorItemReader / queryString 조회쿼리 설정
- Paging 기반 조회 / JpaPagingItemReader / --Clause 조회쿼리 설정

JdbcBatchItemWriter

전처리, 후처리 담당 interface : JobExecutionListener/StepExecutionListener
만드는 방법 : Interface 구현, @Annotation 정의
외에 SkipListener, ItemReadListener, ItemProcessorListener, ItemWriterListener, ChunkListener

Batch실행 도중 예외처리
- skip 예외처리, 예외처리 조건 : skipLimit(n) n 이하인 경우 skip 발생
- retry 예외처리, retryLimit(n), 호출 시, RetryListener로 추가 처리 가능