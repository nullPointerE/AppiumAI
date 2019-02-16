package map;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class VertexRW {
    public static List<Vertex> intToVertex; // index -> vertex, vertex -> index
    public static int[][] graph;


    public void writeToFile() {
        File file = new File("vertex.txt");
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            for (Vertex temp : intToVertex) {
                objectOutputStream.writeObject(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile() {
        intToVertex = new LinkedList<>();
        int count = 0;
        File file = new File("vertex.txt");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            Vertex temp;
            while ((temp = (Vertex) objectInputStream.readObject()) != null) {
                intToVertex.add(temp);
                temp.index = count++;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        int size = intToVertex.size();
        Distance.INF = (size - 1) * 1000;
        graph = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                graph[i][j] = Distance.INF;
            }
        }
        for (Vertex temp : intToVertex) {
            int index = temp.index;
            List<Integer> children = temp.adjlink;
            for (int child : children) {
                graph[index][intToVertex.get(child).index] = 1;
            }
        }
        Distance.floydWarshall(graph);
    }
}
