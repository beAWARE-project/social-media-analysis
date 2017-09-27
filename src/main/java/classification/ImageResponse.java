/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

/**
 *
 * @author andreadisst
 */
public class ImageResponse {
    
    String dcnnFeature = "";
    boolean relevancy = false;
    
    public ImageResponse(){}
    
    public ImageResponse(String dcnnFeature, boolean relevancy){
        this.dcnnFeature = dcnnFeature;
        this.relevancy = relevancy;
    }
    
    public String getDcnnFeature(){
        return dcnnFeature;
    }
    
    public boolean getRelevancy(){
        return relevancy;
    }
    
}
