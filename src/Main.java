import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lk on 2015. 9. 18..
 */
public class Main {

    HashMap<Integer, User> map;
    List<User> list;

    public static void main(String argp[]){
        new Main();
    }

    public Main(){

        map = new HashMap<>();
        list = new ArrayList<>();

        User u1 = new User("aaa",10,1);
        User u2 = new User("bbb",10,1);
        User u3 = new User("ccc",10,1);

        map.put(1,u1);
        map.put(2,u2);
        map.put(3, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

         u1 = new User("aaa",10,2);
         u2 = new User("bbb",10,2);
         u3 = new User("ccc",10,2);

        map.put(4,u1);
        map.put(5,u2);
        map.put(6, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

         u1 = new User("aaa",10,3);
         u2 = new User("bbb",10,3);
         u3 = new User("ccc",10,3);

        map.put(7,u1);
        map.put(8,u2);
        map.put(9, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

         u1 = new User("aaa",10,4);
         u2 = new User("bbb",10,4);
         u3 = new User("ccc",10,4);

        map.put(10,u1);
        map.put(11,u2);
        map.put(12, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

         u1 = new User("aaa",10,5);
         u2 = new User("bbb",10,5);
         u3 = new User("ccc",10,5);

        map.put(13,u1);
        map.put(14,u2);
        map.put(15, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

         u1 = new User("aaa",10,6);
         u2 = new User("bbb",10,6);
         u3 = new User("ccc",10,6);

        map.put(16,u1);
        map.put(17,u2);
        map.put(18, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

        u1 = new User("aaa",10,7);
        u2 = new User("bbb",10,7);
        u3 = new User("ccc",10,7);

        map.put(19,u1);
        map.put(20,u2);
        map.put(21, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

        u1 = new User("aaa",10,8);
        u2 = new User("bbb",10,8);
        u3 = new User("ccc",10,8);

        map.put(22,u1);
        map.put(23,u2);
        map.put(24, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

        u1 = new User("aaa",10,9);
        u2 = new User("bbb",10,9);
        u3 = new User("ccc",10,9);

        map.put(25,u1);
        map.put(26,u2);
        map.put(27, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);

        u1 = new User("aaa",10,11);
        u2 = new User("bbb",10,10);
        u3 = new User("ccc",10,12);

        map.put(28,u1);
        map.put(29,u2);
        map.put(30, u3);

        list.add(u1);
        list.add(u2);
        list.add(u3);



        Collections.sort(list);

        System.out.println(getMyRank(22));
        System.out.println(getTopRank());
    }

    /**
     * Update User Score
     * @param id
     * @param score
     */
    public void updateUser(int id, int score){
        User user = map.get(id);
        user.setScore(score);
        Collections.sort(list);
    }

    /**
     * Get 21 User include me
     * @param id
     * @return
     */
    public List<User> getMyRank(int id){
        User user = map.get(id);
        int i=0;
        while(true){
            User target = list.get(i);
            if(target.equals(user))
                break;
            i++;
        }
        int start = i - 10;
        int end = i + 11;
        if(i < 10)
            start = 0;
        if(list.size() - 10 < i)
            end = list.size();
        return list.subList(start, end);
    }

    /**
     * Top Ranking
     * @return List(1,10)
     */

    public List<User> getTopRank(){
        return list.subList(0,10);
    }
}
