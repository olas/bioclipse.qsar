package net.bioclipse.cdk.qsar.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IDescriptor;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.BooleanResult;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IntegerArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.qsar.business.IQsarManager;
import net.bioclipse.qsar.business.QsarManager;
import net.bioclipse.qsar.descriptor.DescriptorResult;
import net.bioclipse.qsar.descriptor.IDescriptorCalculator;
import net.bioclipse.qsar.descriptor.IDescriptorResult;
import net.bioclipse.qsar.descriptor.model.Descriptor;
import net.bioclipse.qsar.descriptor.model.DescriptorImpl;
import net.bioclipse.qsar.descriptor.model.DescriptorInstance;
import net.bioclipse.qsar.descriptor.model.DescriptorParameter;

public class CDKDescriptorCalculator implements IDescriptorCalculator {

    private static final Logger logger = Logger.getLogger(QsarManager.class);

    ICDKManager cdk;
	private DescriptorEngine engine = null;
	private Map<String, IMolecularDescriptor> descriptorInstances;
	IQsarManager qsar;
   
    public CDKDescriptorCalculator() {
		cdk=Activator.getDefault().getCDKManager();
		qsar=net.bioclipse.qsar.init.Activator.getDefault().getQsarManager();
	}
    
	public Map<String, IMolecularDescriptor> getDescriptorMap() {
		if (descriptorInstances == null){ 
			getEngine();
			descriptorInstances = new HashMap<String, IMolecularDescriptor>();
			
			List<IDescriptor> lst = engine.getDescriptorInstances();
			
			for (IDescriptor descriptor : lst) {
				IMolecularDescriptor mdesc=(IMolecularDescriptor)descriptor;
				descriptorInstances.put(mdesc.getClass().getName(), mdesc);
			}
		}

		return descriptorInstances;
	}

	private void getEngine() {
		if (engine == null) {
			List<String> descriptors = new ArrayList<String>();
			
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ZagrebIndexDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.WienerNumbersDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.WHIMDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.WeightedPathDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.VAdjMaDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.TPSADescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.PetitjeanShapeIndexDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.PetitjeanNumberDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.MomentOfInertiaDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.MDEDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.LongestAliphaticChainDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.LengthOverBreadthDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.LargestPiSystemDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.LargestChainDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.KierHallSmartsDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.KappaShapeIndicesDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.GravitationalIndexDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.FragmentComplexityDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.EccentricConnectivityIndexDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.CPSADescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ChiPathDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ChiPathClusterDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ChiClusterDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ChiChainDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.CarbonTypesDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.BPolDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.BondCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.BCUTDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AutocorrelationDescriptorPolarizability");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AutocorrelationDescriptorMass");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AutocorrelationDescriptorCharge");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AtomCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AromaticBondsCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.AromaticAtomsCountDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.APolDescriptor");
			descriptors.add("org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor");

			engine  = new DescriptorEngine(descriptors);
			engine.getDescriptorInstances();
		}
	}
    
    
    
    
    
    
    
    
    
	public Map<IMolecule, List<IDescriptorResult>> calculateDescriptor(
			List<IMolecule> molecules, List<DescriptorInstance> descriptorInstances) {
		
		Map<IMolecule, List<IDescriptorResult>> allResults=
						new HashMap<IMolecule, List<IDescriptorResult>>();
		
		for (IMolecule mol : molecules){
			try {
				ICDKMolecule cdkmol=cdk.create(mol);
				
				List<IDescriptorResult> retlist = doCalculate(cdkmol, descriptorInstances);
				
				allResults.put(mol, retlist);
				
			} catch (BioclipseException e) {
				logger.error("Unable to create CDKMolecule from Imolecule. " +
						"Skipping descriptor calculation for this mol.");
				LogUtils.debugTrace(logger, e);
			}
			
		}
		
		return allResults;
	}

	
	/**
	 * Calculate descriptors for a single molecule
	 * @param mol Input molecule
	 * @param descriptorInstances2 List<String> of descriptor IDs
	 * @return IDescriptorResult with results of the calculation
	 * @throws BioclipseException 
	 */
	private List<IDescriptorResult> doCalculate(ICDKMolecule cdkmol, List<DescriptorInstance> descriptorInstances){

		//Get atomcontainer from IMolecule
		IAtomContainer container=cdkmol.getAtomContainer();
		
		//Make sure e have a Molecule, otherwise create it
		if (!(container instanceof IMolecule)) container = container.getBuilder().newMolecule(container);

		//Store results here
		List<IDescriptorResult> results = new ArrayList<IDescriptorResult>();

		//Loop over all descriptors
		for (DescriptorInstance inst : descriptorInstances){
			String descriptorImplID=inst.getDescriptorImpl().getId();			
			logger.debug("Calculating descriptor: " + descriptorImplID + "for mol: " + cdkmol.getName());

			//This is where we store the result
			DescriptorResult res=new DescriptorResult();
			res.setDescriptorId(descriptorImplID);

			//Get descriptor by id
			IMolecularDescriptor cdkDescriptor = getDescriptorMap().get(descriptorImplID);
			if (cdkDescriptor==null){
				res.setErrorMessage("Descriptor not supported in cdk: " + descriptorImplID);
			}
			
			//We have a valid descriptorID
			else{
				
				//Do we have parameters available for this descriptorID in Map?
				if (inst.getParameters()!=null && inst.getParameters().size()>0){
					logger.debug("We have: " + inst.getParameters().size() 
							+ " params available.");
					
					//OK, get it and set it to descriptor
					List<DescriptorParameter> bcParams = inst.getParameters();
					
					//Check that the CDK descriptor accepts parameters
					if (cdkDescriptor.getParameterNames()==null && cdkDescriptor
												.getParameterNames().length<=0){
						
						logger.error("Trying to set params for descrImpl: " + 
								descriptorImplID + " but corresponding " +
							       "CDKDescriptor does not accept any params.");
						
					}
					else{
						//These are the ones we should fill
						Object[] cdkParams=cdkDescriptor.getParameters();
						
						for (int i=0; i< cdkParams.length; i++){
							String cdkName=cdkDescriptor.getParameterNames()[i];
							logger.debug("CDK descr accepts params.");
							
							for (DescriptorParameter param : bcParams){
								if (param.getKey().equals(cdkName)){
									//Match
									Object obj=cdkDescriptor.getParameterType(cdkName);
									if (obj instanceof String) {
										cdkParams[i]=param.getValue();
										logger.debug("   - Set param: " + 
												cdkName + " to " + param.getValue());
									}
								}
							}
						}
						try {
							cdkDescriptor.setParameters(cdkParams);
						} catch (CDKException e) {
							e.printStackTrace();
						}
						
					}

				}
				
				
				//Number of return values
				Float[] resultVals = new Float[cdkDescriptor.getDescriptorResultType().
				         length() > 0 ? cdkDescriptor.getDescriptorResultType().
				        		 length() : 1];
				String[] resultLabels = null;
				logger.debug("# result values: " + resultVals.length);

				//Calculate descriptor
				try {
					DescriptorValue value = ((IMolecularDescriptor) cdkDescriptor).calculate(container);
					resultLabels = value.getNames();
					org.openscience.cdk.qsar.result.IDescriptorResult result = value.getValue();
					if (result instanceof BooleanResult) {
						if (((BooleanResult)result).booleanValue()) {
							resultVals[0] = 1f;
							logger.debug("Calculated value: " + resultVals[0]);
						} else {
							resultVals[0] = 0f;
							logger.debug("Calculated value: " + resultVals[0]);
						}
					} else if (result instanceof DoubleResult) {
						resultVals[0] = (float)((DoubleResult)result).doubleValue();
						logger.debug("Calculated value: " + resultVals[0]);
					} else if (result instanceof IntegerResult) {
						resultVals[0] = (float)((IntegerResult)result).intValue();
						logger.debug("Calculated value: " + resultVals[0]);
					} else if (result instanceof IntegerArrayResult) {
						resultVals = new Float[((IntegerArrayResult)result).length()];
						for (int i=0; i<resultVals.length; i++) {
							resultVals[i] = (float)((IntegerArrayResult)result).get(i);
							logger.debug("Calculated value " + i +": " + resultVals[i]);
						}
					} else if (result instanceof DoubleArrayResult) {
						resultVals = new Float[((DoubleArrayResult)result).length()];
						for (int i=0; i<resultVals.length; i++) {
							resultVals[i] = (float)((DoubleArrayResult)result).get(i);
							logger.debug("Calculated value " + i +": " + resultVals[i]);
						}
					} else {
						logger.error("Uknown descriptor result type: " + result.getClass().getName());
					}
					
				} catch (Throwable e) {
					logger.error("Could not generate descriptor for molecule: " + 
							cdkmol.getName() + ". Reason: " + e.getMessage());
					for (int i=0; i<resultVals.length; i++) {
						resultVals[i] = Float.NaN;
					}
					//				e.printStackTrace();
				}

				//Check that we got back correct number of values
				if (resultLabels.length != resultVals.length) {
					System.out.println("WARN: #labels != #vals for " + descriptorImplID);
				}

				res.setLabels(resultLabels);
				res.setValues(resultVals);
			}

			//Add results. Could be empty with just error msg.
			results.add(res);
		}


		return results;

	}

}