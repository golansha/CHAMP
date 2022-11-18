package sat.EfficientImplementation;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GainLevels implements Iterable<Integer>{
    private final int MAX_NUM_OF_LEVELS = 5000;
    public int maxLevel = 0;
    double levelToGainFactor;
    RandomElementExtractionSet<Integer>[] levels = new RandomElementExtractionSet[MAX_NUM_OF_LEVELS];
    public GainLevels(int maximalClauseLength){
        for (int i = 0; i<MAX_NUM_OF_LEVELS; i++){
            levels[i] = new RandomElementExtractionSet();
        }
        levelToGainFactor = Math.pow(2,maximalClauseLength);
    }
    public Stream<Integer> stream(){
        return null;
    }
    public boolean isEmpty(){
        return levels[maxLevel].isEmpty();
    }
    public Integer pollFirst(){
        Integer t = levels[maxLevel].removeRandomElement();
        while (maxLevel>0 && levels[maxLevel].isEmpty()) maxLevel--;
        return t;
    }

    private int getLevel(double gain){
        return (int)(gain*levelToGainFactor);
    }

    public void remove(int varIndex, double gain) {
        levels[getLevel(gain)].remove(varIndex);
        while (maxLevel>0 && levels[maxLevel].isEmpty()) maxLevel--;
    }

    public void add(Integer varIndex, double gain) {
        int level = getLevel(gain);
        levels[level].add(varIndex);
        maxLevel = Math.max(maxLevel,level);
    }

    @Override
    public Iterator<Integer> iterator() {
        return null;
    }

    public Integer first() {
        return null;
    }

    public String toString(){
        return "maxGain ="+maxLevel/levelToGainFactor+"; "+ IntStream.range(0,levels.length)
            .filter(i -> !levels[i].isEmpty()).mapToObj(i->i/levelToGainFactor+"->"+levels[i]).collect(Collectors.toList()).toString();
    }
    public void clear(){
        for (int i = 0; i <= maxLevel; i++) {
            levels[i].clear();
        }
        maxLevel=0;
    }
}
