package circuitbreaker;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowCount implements CircuitBreaker {

    Deque<Integer> window = new ArrayDeque<>();

    int windowSize = 5;
    int successCount = 0;
    int failureCount = 0;
    public State state = State.CLOSE;
    long openUntil ;
    int halfOpenRequestCount = 0;
    int halfOpenMaxCount = 3;
    int halfOpenSuccessCount = 0;
    int halfOpenFailCount = 0;
    @Override
    public boolean allowRequest() {
        long now = System.currentTimeMillis();
        if(state == State.CLOSE){
            return true;
        }
        if( state == State.OPEN){
            if( now-openUntil >= 0){
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        if(state == State.HALF_OPEN){
            halfOpenRequestCount++;
            return halfOpenRequestCount < halfOpenMaxCount;
        }

        return false;
    }

    @Override
    public void recordSuccess() {
        long now = System.currentTimeMillis();
        window.offerLast(1);
        successCount++;
        calculateCount();
        if(state == State.HALF_OPEN) {
            halfOpenSuccessCount++;
            if( halfOpenSuccessCount >= halfOpenMaxCount) {
                state = State.CLOSE;
            }

        }
        if( state == State.CLOSE){
            int count = (int)Math.floor(window.size()*(5.0/10));
            if( window.size() >=windowSize && failureCount>= count){
                state = State.OPEN;
                openUntil = now+5000;
            }
            halfOpenFailCount = 0;

        }

    }

    @Override
    public void recordFailure() {
        window.offerLast(0);
        failureCount++;
        calculateCount();
        long now = System.currentTimeMillis();
        if( state == State.CLOSE) {
            int count = (int)Math.floor(window.size()*(5.0/10));
            if( window.size() >=windowSize && failureCount>= count){
                state = State.OPEN;
                openUntil = now+5000;
            }
        }

        if(state == State.HALF_OPEN) {
            halfOpenFailCount++;
            if( halfOpenFailCount >= 2) {
                state = State.OPEN;
                openUntil = now+500;
            }

        }

    }

    private void calculateCount() {
        int poll = window.size() > windowSize ? window.pollFirst(): -1;
        if( poll == 0 ){
            failureCount--;
        }
        if( poll == 1 ) {
            successCount--;
        }
    }

    public State getState() {
        return state;
    }

    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker circuitBreaker = new SlidingWindowCount();
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
        System.out.println("State: " + circuitBreaker.getState());
        circuitBreaker.recordSuccess();
        circuitBreaker.recordSuccess();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
        Thread.sleep(5000);
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
        System.out.println("State: " + circuitBreaker.getState());
        circuitBreaker.recordSuccess();
        circuitBreaker.recordFailure();
        System.out.println("State: " + circuitBreaker.getState());
        circuitBreaker.recordSuccess();
        circuitBreaker.recordSuccess();
        Thread.sleep(5000);
        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
        circuitBreaker.recordSuccess();
        System.out.println("State: " + circuitBreaker.getState());

        System.out.println("Request allowed: " + circuitBreaker.allowRequest());
    }
}
