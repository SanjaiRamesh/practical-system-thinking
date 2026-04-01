package circuitbreaker;

import java.util.ArrayDeque;
import java.util.Deque;

enum State {
    OPEN,
    CLOSE,
    HALF_OPEN
}

interface CircuitBreaker {
    public boolean allowRequest();
    public void recordSuccess();
    public void recordFailure();
    public State getState();
}
public class SlidingWindowCircuitBreaker  implements CircuitBreaker {

    private State state = State.CLOSE;
    private Deque<Long> slidingWindow = new ArrayDeque<>();
    private Deque<Long> halfOpenSlidingWindow = new ArrayDeque<>();
    private long openWindow = 10*1000;
    private long halfOpenWindow = 9*1000;
    private long openDuration = 5000;
    private int failureCount = 3;
    private int halfOpenFailureCount = 1;
    private long openTime= -1;
    private int halfOpenSuccessCount = 0;
    private int halfOpenMinSuccessCount = 2;



    public boolean allowRequest() {

        long now = System.currentTimeMillis();
        if(state == State.OPEN) {
            if(now - openTime >=openDuration){
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    public void recordSuccess() {
        if(state == State.HALF_OPEN) {
            halfOpenSuccessCount++;
            if( halfOpenSuccessCount >= halfOpenMinSuccessCount) {
                state = State.CLOSE;
                slidingWindow .clear();
                halfOpenSlidingWindow.clear();
            }

        }
    }

    public void recordFailure() {

        long now = System.currentTimeMillis();

        if( state == State.CLOSE) {
            slidingWindow.offerLast(now);
            while(!slidingWindow.isEmpty() && now-slidingWindow.peekFirst() > openWindow) {
                slidingWindow.pollFirst();
            }
            if(slidingWindow.size() >= failureCount) {
                openTime = now;
                state = State.OPEN;
                return;
            }
        } else if( state == State.HALF_OPEN) {
            halfOpenSlidingWindow.offerLast(now);
            while(!halfOpenSlidingWindow.isEmpty() && now-halfOpenSlidingWindow.peekFirst() > halfOpenWindow) {
                halfOpenSlidingWindow.pollFirst();
            }

            if(halfOpenSlidingWindow.size() >= halfOpenFailureCount) {
                openTime = now;
                state = State.OPEN;
            }
        }
    }

    public State getState() {
        return state;
    }

    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker circuitBreaker = new SlidingWindowCircuitBreaker();

        System.out.println("State: "+circuitBreaker.getState());
        System.out.println("Request allowed: "+ circuitBreaker.allowRequest());
        System.out.println("Request allowed: "+ circuitBreaker.allowRequest());
        System.out.println("State: "+circuitBreaker.getState());
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure();
        System.out.println("State: "+circuitBreaker.getState());
        System.out.println("Request allowed: "+ circuitBreaker.allowRequest());
        Thread.sleep(50);
        System.out.println("State: "+circuitBreaker.getState());
        System.out.println("Request allowed: "+ circuitBreaker.allowRequest());
        System.out.println("State: "+circuitBreaker.getState());
        circuitBreaker.recordSuccess();
        circuitBreaker.recordFailure();
        System.out.println("State: "+circuitBreaker.getState());
        circuitBreaker.recordSuccess();
        System.out.println("State: "+circuitBreaker.getState());
        System.out.println("Request allowed: "+ circuitBreaker.allowRequest());
        System.out.println((int)Math.ceil(10*10/100));

    }
}
