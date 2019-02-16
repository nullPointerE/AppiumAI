package map;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 顶点表元素
 */
public class Vertex<T> implements Serializable {
    public int weight;
    public int index;
    public String branch;
    public int depth;

    public List<Integer> adjlink;// 操作该symbol跳转的地方

    public Vertex() {
        adjlink = new LinkedList<>();
    }

    public Vertex getNextClostVertex() {
        return null;
    }
}