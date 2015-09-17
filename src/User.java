/**
 * Created by lk on 2015. 9. 18..
 */
public class User implements Comparable {

    private String name;    // 이름
    private int age;        // 나이
    private int score;      // 점수

    public User(String name, int age, int score) {
        this.name = name;
        this.age = age;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Object o) {
        User other = (User)o;
        if(this.score > other.getScore())
            return 1;
        else if(this.score < other.getScore())
            return -1;
        else
            return 0;
    }
}
