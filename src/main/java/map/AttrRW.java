package map;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AttrRW {
    public static Map<String, Integer> idToIndex;
    public static Map<String, Integer> textToIndex;
    public static List<Attributes> allAttributes;

    public void writeToFile() {
        File file = new File("attribute.txt");
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            for (Attributes temp : allAttributes) {
                objectOutputStream.writeObject(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile() {
        idToIndex=new HashMap <>();
        textToIndex=new HashMap <>();
        allAttributes = new LinkedList<>();
        int count = 0;
        File file = new File("attribute.txt");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            Attributes temp;
            while ((temp = (Attributes) objectInputStream.readObject()) != null) {
                allAttributes.add(temp);
                temp.index = count++;
                idToIndex.put(temp.ID, temp.index);
                textToIndex.put(temp.text,temp.index);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
