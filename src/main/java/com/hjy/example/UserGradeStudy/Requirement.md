* User 등급을 4개로 구분
<br> : NORMAL, SILVER, GOLD, VIP


* User 등급 상향 조건
<br> : 200,000원 이상인 경우 실버
<br> : 300,000원 이상인 경우 골드
<br> : 500,000원 이상인 경우 VIP
<br> : 등급 하향은 없음


* 총 2개의 Step으로 회원 등급 Job생성
<br> : saveUserStep - User 데이터 저장
<br> : userLevelUpStep - User 등급 상향


* JobExecutionListener.afterJob 메소드에서 처리 로그 출력


* User의 totalAmount를 Orders Entity로 만들어 하나의 User는 n개의 Orders 포함


* '-date=yyyy-mm' JobParameters
<br> : 'yyyy년_mm월_주문금액.csv' 파일로 주문 내역 저장
<br> : 'date' 파라미터가 없는 경우, Step실행 X.