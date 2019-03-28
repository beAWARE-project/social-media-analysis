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
public class VerificationResponse {
    
    boolean predicted_value = false;
    Double confidence_value = 0.0;
    
    public VerificationResponse(){}
    
    public VerificationResponse(boolean predicted_value, Double confidence_value){
        this.predicted_value = predicted_value;
        this.confidence_value = confidence_value;
    }
    
    public boolean getPredictedValue(){
        return predicted_value;
    }
    
    public Double getConfidenceValue(){
        return confidence_value;
    }
    
}
