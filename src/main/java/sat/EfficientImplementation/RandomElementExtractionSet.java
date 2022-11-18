package sat.EfficientImplementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static sat.common.Common.random;

public class RandomElementExtractionSet<A> {
    ArrayList<A> contents = new ArrayList();
    HashMap<A,Integer> indices = new HashMap<A,Integer>();

    public Collection<A> getContent(){
        return contents;
    }
    //selects random element in constant time
    public A randomElement() {
        int location = random.nextInt(contents.size());
        return contents.get(location);
    }

    //adds new element in constant time
    public void add(A a) {
        indices.put(a,contents.size());
        contents.add(a);
    }

    //removes element in constant time
    public void remove(A a) {
        if (!indices.containsKey(a)){
            return;
        }
        int index = indices.get(a);
        if (index != contents.size()-1) {
            contents.set(index, contents.get(contents.size() - 1));
            indices.put(contents.get(index), index);
        }
        contents.remove(contents.size()-1);
        indices.remove(a);
    }

    public A removeRandomElement(){
        A a = randomElement();
        remove(a);
        return a;
    }

    public int size(){
        return contents.size();
    }

    public boolean contains(A a){
        return indices.containsKey(a);
    }

    @Override
    public String toString() {
        return contents.toString();
    }

    public boolean isEmpty(){
        return size()==0;
    }

    public void clear(){
        indices.clear();
        contents.clear();
    }
}
