import com.wisp.finance.security.ThreeWayMerge;
import java.util.*;
public class ThreeWayMergeTest {
    static Map<String,String> map(String... pairs){Map<String,String> m=new HashMap<>();for(int i=0;i<pairs.length;i+=2)m.put(pairs[i],pairs[i+1]);return m;}
    static void check(boolean b){if(!b)throw new AssertionError();}
    public static void main(String[] args)throws Exception {
        Map<String,String> base=map("a","100","b","200"),local=map("a","110","b","200"),remote=map("a","100","b","220");
        check(ThreeWayMerge.merge(local,remote,base,0).equals(map("a","110","b","220")));
        check(ThreeWayMerge.merge(map("a","100"),base,base,0).equals(map("a","100")));
        check(ThreeWayMerge.merge(base,map("a","100"),base,0).equals(map("a","100")));
        check(ThreeWayMerge.merge(map("a","100"),map("c","300"),null,0).equals(map("a","100","c","300")));
        for(int scenario=0;scenario<3;scenario++) {
            Map<String,String> r=scenario==0?map("a","120","b","200"):scenario==1?map("b","200"):map("a","99","b","200");
            try{ThreeWayMerge.merge(local,r,scenario==2?null:base,0);throw new AssertionError("Silent conflict");}catch(ThreeWayMerge.Conflict expected){}
        }
        check(ThreeWayMerge.merge(local,map("a","120","b","200"),base,1).equals(local));
        check(ThreeWayMerge.merge(local,map("a","120","b","200"),base,2).equals(map("a","120","b","200")));
        check(base.equals(map("a","100","b","200")));
        System.out.println("PASS: independent edits, first-device union, both deletion directions, edit/edit and delete/edit conflicts, explicit resolution, immutable inputs.");
    }
}
