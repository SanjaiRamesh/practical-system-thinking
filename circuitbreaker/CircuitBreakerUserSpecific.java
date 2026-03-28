package circuitbreaker;


import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerUserSpecific {

    static class RequestInfo{
        private long openUntil;
        private int halfOpenSuccessCount;
        private int closedFailureCount;
        private int halfOpenRequestCount;
        private STATE state = STATE.CLOSED;
    }
    enum STATE {
        OPEN,
        HALF_OPEN,
        CLOSED
    }
    // will be initialized
    private int halfOpenSuccessThreshold;
    private int closeFailureMaxCount;
    private int halfOpenMaxReqCount;
    private int maxOpenWindow;


    private Map<String, RequestInfo> cbMap =   new HashMap<String, RequestInfo>();
    // will be updated on runtime


    public CircuitBreakerUserSpecific(int halfOpenSuccessThreshold, int closeFailureMaxCount, int halfOpenMaxReqCount, int maxOpenWindow) {
        this.halfOpenSuccessThreshold = halfOpenSuccessThreshold;
        this.closeFailureMaxCount = closeFailureMaxCount;
        this.halfOpenMaxReqCount = halfOpenMaxReqCount;
        this.maxOpenWindow = maxOpenWindow;
    }

    private boolean isRequestAllowed(String user){

        RequestInfo requestInfo = cbMap.get(user);

        long now =  System.currentTimeMillis();

        if(requestInfo.state == STATE.CLOSED){
            return true;
        }
        if(requestInfo.state == STATE.OPEN) {
            if(now - requestInfo.openUntil >= maxOpenWindow){
                moveToHalfOpen(user);
                return true;
            }

        }
        if(requestInfo.state == STATE.HALF_OPEN){
            if(requestInfo.halfOpenRequestCount >= halfOpenMaxReqCount){
                return false;
            }
            requestInfo.halfOpenRequestCount++;
            return true;
        }
        return false;
    }

    public void recordSuccess(String  user){

        RequestInfo requestInfo = cbMap.get(user);
        switch (requestInfo.state){
            case CLOSED-> {
                requestInfo.closedFailureCount=0;
            }
            case HALF_OPEN -> {
                requestInfo.halfOpenSuccessCount++;
                if(requestInfo.halfOpenSuccessCount >= halfOpenSuccessThreshold){
                    moveToClose(user);
                }

            }

        }
    }

    public void recordFailure(String user){
        RequestInfo requestInfo = cbMap.get(user);
        switch (requestInfo.state){
            case HALF_OPEN -> {
                moveToOpen(user);
            }
            case CLOSED -> {
                requestInfo.closedFailureCount++;
                if(requestInfo.closedFailureCount >= closeFailureMaxCount){
                    moveToOpen(user);
                }
            }
        }
    }
    public void moveToClose(String user){
        RequestInfo requestInfo = cbMap.get(user);
        requestInfo.halfOpenSuccessCount = 0;
        requestInfo.openUntil = 0L;
        requestInfo.halfOpenRequestCount = 0;
        requestInfo.closedFailureCount = 0;
        requestInfo.state = STATE.CLOSED;
    }
    public void moveToOpen(String user){
        RequestInfo requestInfo = cbMap.get(user);
        requestInfo.closedFailureCount = 0;
        requestInfo.openUntil = System.currentTimeMillis()+maxOpenWindow;
        requestInfo.halfOpenSuccessCount = 0;
        requestInfo.halfOpenRequestCount = 0;
        requestInfo.state = STATE.OPEN;
    }
    public void moveToHalfOpen(String user) {
        RequestInfo requestInfo = cbMap.get(user);
        requestInfo.state = STATE.HALF_OPEN;
        requestInfo.halfOpenSuccessCount = 0;
        requestInfo.halfOpenRequestCount = 0;

    }

    private STATE getState(String user){
        RequestInfo requestInfo = cbMap.getOrDefault(user, new RequestInfo());
        return requestInfo.state;
    }
    private void initMap(String user) {

        cbMap.putIfAbsent(user, new RequestInfo());
    }
    public static void main(String[] args) throws InterruptedException{
        System.out.println("Starting CircuitBreaker");

        CircuitBreakerUserSpecific cb = new CircuitBreakerUserSpecific(2, 3,3,2);
        String user1= "sanjai";
        String user2= "ramesh";
        String user3= "boopathi";
        cb.initMap(user1);
        cb.initMap(user2);
        cb.initMap(user3);

        System.out.println("Initial state: " + cb.getState(user1));

        simulateFailure(cb, user1);
        simulateFailure(cb,user1);
        simulateFailure(cb,user1);

        simulateFailure(cb, user2);
        simulateFailure(cb,user2);
        simulateFailure(cb,user2);

        System.out.println("State after failures: " + cb.getState(user1));
        System.out.println("State for user 2: " + cb.getState(user2));
        System.out.println("Allow request user 2: " + cb.isRequestAllowed(user2));

        System.out.println("Allow request while OPEN? " + cb.isRequestAllowed(user1));

        Thread.sleep(5000);

        System.out.println("Allow request after timeout? " + cb.isRequestAllowed(user1));
        System.out.println("State now: " + cb.getState(user1));

        cb.recordSuccess(user1);
        cb.recordSuccess(user1);

        System.out.println("Final state: " + cb.getState(user1));

    }

    private static void simulateFailure(CircuitBreakerUserSpecific cb, String user) {
        if (cb.isRequestAllowed(user)) {
            cb.recordFailure(user);
        }
    }
}
