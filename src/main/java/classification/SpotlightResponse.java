/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

import classification.Resource;
import java.util.List;

/**
 *
 * @author andreadisst
 */
public class SpotlightResponse {
    
    private String text;
    private String confidence;
    private String support;
    private String types;
    private String sparql;
    private String policy;
    private List<Resource> Resources;
    
    public List<Resource> getResources(){
        return Resources;
    }
}
