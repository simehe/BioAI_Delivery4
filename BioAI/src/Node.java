import java.util.ArrayList;

public class Node {
    Integer id;
    Integer op_time;
    ArrayList<Node> possList;
    public Node(Integer id, Integer op_time){
        this.id = id;
        this.op_time = op_time;
        this.possList= null;
    }
    public void updatePossList(ArrayList<Node> list){
        this.possList=list;
    }
}

