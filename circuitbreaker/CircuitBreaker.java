package circuitbreaker;


public class CircuitBreaker  {

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


    // will be updated on runtime
    private long openUntil;
    private int halfOpenSuccessCount;
    private int closedFailureCount;
    private int halfOpenRequestCount;
    private STATE  state = STATE.CLOSED;

    public CircuitBreaker(int halfOpenSuccessThreshold, int closeFailureMaxCount, int halfOpenMaxReqCount, int maxOpenWindow) {
        this.halfOpenSuccessThreshold = halfOpenSuccessThreshold;
        this.closeFailureMaxCount = closeFailureMaxCount;
        this.halfOpenMaxReqCount = halfOpenMaxReqCount;
        this.maxOpenWindow = maxOpenWindow;
    }

    private boolean isRequestAllowed(){

        long now =  System.currentTimeMillis();

        if(state == STATE.CLOSED){
            return true;
        }
        if(state == STATE.OPEN) {
            if(now - openUntil >= maxOpenWindow){
                moveToHalfOpen();
                return true;
            }

        }
        if(state == STATE.HALF_OPEN){
            if(halfOpenRequestCount >= halfOpenMaxReqCount){
                return false;
            }
            halfOpenRequestCount++;
            return true;
        }
        return false;
    }

    public void recordSuccess(){
        switch (state){
            case CLOSED-> {
                closedFailureCount=0;
            }
            case HALF_OPEN -> {
                halfOpenSuccessCount++;
                if(halfOpenSuccessCount >= halfOpenSuccessThreshold){
                    moveToClose();
                }

            }

        }
    }

    public void recordFailure(){
        switch (state){
            case HALF_OPEN -> {
                moveToOpen();
            }
            case CLOSED -> {
                closedFailureCount++;
                if(closedFailureCount >= closeFailureMaxCount){
                    moveToOpen();
                }
            }
        }
    }
    public void moveToClose(){
        halfOpenSuccessCount = 0;
        openUntil = 0L;
        halfOpenRequestCount = 0;
        closedFailureCount = 0;
        state = STATE.CLOSED;
    }
    public void moveToOpen(){
       closedFailureCount = 0;
       openUntil = System.currentTimeMillis()+maxOpenWindow;
        halfOpenSuccessCount = 0;
        halfOpenRequestCount = 0;
       state = STATE.OPEN;
    }
    public void moveToHalfOpen() {
        state = STATE.HALF_OPEN;
        halfOpenSuccessCount = 0;
        halfOpenRequestCount = 0;

    }

    private STATE getState(){
        return state;
    }
    public static void main(String[] args) throws InterruptedException{
        System.out.println("Starting CircuitBreaker");

        CircuitBreaker cb = new CircuitBreaker(2, 3,3,2);
        System.out.println("Initial state: " + cb.getState());

        simulateFailure(cb);
        simulateFailure(cb);
        simulateFailure(cb);

        System.out.println("State after failures: " + cb.getState());

        System.out.println("Allow request while OPEN? " + cb.isRequestAllowed());

        Thread.sleep(5000);

        System.out.println("Allow request after timeout? " + cb.isRequestAllowed());
        System.out.println("State now: " + cb.getState());

        cb.recordSuccess();
        cb.recordSuccess();

        System.out.println("Final state: " + cb.getState());

    }

    private static void simulateFailure(CircuitBreaker cb) {
        if (cb.isRequestAllowed()) {
            cb.recordFailure();
        }
    }
}
