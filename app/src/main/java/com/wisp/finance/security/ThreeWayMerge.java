package com.wisp.finance.security;
import java.util.*;
/** Pure three-way record merge. Missing keys represent deletions; values are canonical records. */
public final class ThreeWayMerge {
    public static final int FAIL=0, LOCAL=1, REMOTE=2;
    public static final class Conflict extends Exception {}
    public static Map<String,String> merge(Map<String,String> local,Map<String,String> remote,Map<String,String> base,int policy) throws Conflict {
        if(policy<0||policy>2) throw new IllegalArgumentException();
        Set<String> ids=new TreeSet<>();ids.addAll(local.keySet());ids.addAll(remote.keySet());if(base!=null)ids.addAll(base.keySet());
        Map<String,String> result=new LinkedHashMap<>();
        for(String id:ids) {
            String l=local.get(id),r=remote.get(id),b=base==null?null:base.get(id),v;
            if(Objects.equals(l,r))v=l;
            else if(base==null&&l==null)v=r;
            else if(base==null&&r==null)v=l;
            else if(base!=null&&Objects.equals(l,b))v=r;
            else if(base!=null&&Objects.equals(r,b))v=l;
            else if(policy==LOCAL)v=l;
            else if(policy==REMOTE)v=r;
            else throw new Conflict();
            if(v!=null)result.put(id,v);
        }
        return result;
    }
}
