/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author andreadisst
 */
public class Jaccard {
    
    public static double getJaccardSimilarity(String[] setA, String[] setB){
        Set intersection = new HashSet();
        Set union = new HashSet();
        for(String itemA : setA){
            for(String itemB : setB){
                if(!itemA.equals(""))
                    union.add(itemA.toLowerCase());
                if(!itemB.equals(""))
                    union.add(itemB.toLowerCase());
                if(!itemA.equals("") && !itemB.equals("") &&itemA.equalsIgnoreCase(itemB))
                    intersection.add(itemA.toLowerCase());
            }
        }
        return (intersection.isEmpty() && union.isEmpty()) ? 0 : ((double) intersection.size()) / union.size();
    }
    
}
