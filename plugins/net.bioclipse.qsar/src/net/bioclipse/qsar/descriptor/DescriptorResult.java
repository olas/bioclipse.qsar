package net.bioclipse.qsar.descriptor;

/**
 * Base implementation of a descriptor result.
 * @author ola
 *
 */
public class DescriptorResult implements IDescriptorResult{

	String descriptorId;
	String[] labels;
	Float[] values;

	String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	//Constructor
	public DescriptorResult() {
	}


	public String getDescriptorId() {
		return descriptorId;
	}


	public void setDescriptorId(String descriptorId) {
		this.descriptorId = descriptorId;
	}


	public String[] getLabels() {
		return labels;
	}


	public void setLabels(String[] labels) {
		this.labels = labels;
	}


	public Float[] getValues() {
		return values;
	}


	public void setValues(Float[] values) {
		this.values = values;
	}
	
	@Override
    public String toString() {
	    if (labels==null || labels.length<=0) return "null";
	    
	    String ret="DescriptorResult for descriptor=" + descriptorId +": ";
	    for (int i=0; i< labels.length;i++){
	        ret=ret+labels[i] + "=" + values[i];
	    }

	    return ret;
    }

}
