/*
 *  Lift Simulator Individual Coursework 1
 *  Concurrent Programming COMP2007 Part 2.
 */
package lift;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class MyLiftController implements LiftController {

   private ReentrantLock lock=new ReentrantLock();
   private Condition condition=lock.newCondition();

   private Direction liftDirection=Direction.UNSET;
   private int liftOpenAtFloor=-1;

   private int[][] peopleWaiting=new int[2][Main.NUMBER_FLOORS];//0 for up direction and 1 for down
   private int[] peopleWaitingForAFloor=new int[Main.NUMBER_FLOORS];

    /* Interface for People */
    public void callLift(int floor, Direction direction) throws InterruptedException {
        try{
            lock.lock();
            
            int[] peopleControl;
            peopleControl=(direction==Direction.DOWN)?peopleWaiting[1]:peopleWaiting[0];
            peopleControl[floor]++;

            while(liftOpenAtFloor!=floor || direction!=liftDirection)
                condition.await();

            peopleControl[floor]--;

            if(peopleControl[floor]==0)
                condition.signal();

          }
        finally{
               lock.unlock();
               }

    }

    public void selectFloor(int floor) throws InterruptedException{
         try{
             lock.lock();

             peopleWaitingForAFloor[floor]++;

             while(liftOpenAtFloor!=floor)
                 condition.await();

             peopleWaitingForAFloor[floor]--;

             if(peopleWaitingForAFloor[floor]==0)
                 condition.signal();
         }
         finally{
                lock.unlock();
                }
    }


    /* Interface for Lifts */
    public boolean liftAtFloor(int floor, Direction direction) {
    
    liftDirection=direction;
  
     return((direction==Direction.UP && peopleWaiting[0][floor]>0)||
            (direction==Direction.DOWN && peopleWaiting[1][floor]>0)||
             peopleWaitingForAFloor[floor]>0);
 
     }

    public void doorsOpen(int floor) throws InterruptedException {
        try{
           lock.lock();

           liftOpenAtFloor=floor;

           condition.signalAll();

        while((liftDirection==Direction.UP && peopleWaiting[0][liftOpenAtFloor]>0)||
            (liftDirection==Direction.DOWN && peopleWaiting[1][liftOpenAtFloor]>0)||
             peopleWaitingForAFloor[liftOpenAtFloor]>0)
            condition.await();
        }
        finally{
        lock.unlock();
        }
    }
    public void doorsClosed(int floor) {
   
    }

}
