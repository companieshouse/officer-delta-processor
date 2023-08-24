package uk.gov.companieshouse.officer.delta.processor.consumer;

import java.util.concurrent.CountDownLatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class KafkaMessageConsumerAspect {

    private final CountDownLatch latch;

    public KafkaMessageConsumerAspect(CountDownLatch latch) {
        this.latch = latch;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object aroundReceiver(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            return joinPoint.proceed();
        } finally {
            latch.countDown();
        }
    }
}
