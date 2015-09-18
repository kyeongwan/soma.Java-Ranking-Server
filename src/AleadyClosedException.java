/**
 * Created by lk on 2015. 9. 18..
 */
public class AleadyClosedException extends Exception {

    public AleadyClosedException(String msg){
        super(msg);
    }

    public AleadyClosedException(){
        super();
    }
}
