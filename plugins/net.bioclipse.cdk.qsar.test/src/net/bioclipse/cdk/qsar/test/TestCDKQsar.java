package net.bioclipse.cdk.qsar.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.SmilesMolecule;
import net.bioclipse.qsar.QSARConstants;
import net.bioclipse.qsar.business.IQsarManager;
import net.bioclipse.qsar.business.QsarManager;
import net.bioclipse.qsar.descriptor.IDescriptorResult;
import net.bioclipse.qsar.descriptor.model.Descriptor;
import net.bioclipse.qsar.descriptor.model.DescriptorImpl;
import net.bioclipse.qsar.descriptor.model.DescriptorInstance;
import net.bioclipse.qsar.descriptor.model.DescriptorParameter;
import net.bioclipse.qsar.descriptor.model.DescriptorProvider;
import net.bioclipse.qsar.init.Activator;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Test;

public class TestCDKQsar {

	IQsarManager qsar;
	private String cdkProviderID="net.bioclipse.cdk.descriptorprovider";
	private String cdkProviderName="Chemistry Development Kit";
	
	public TestCDKQsar() {
		
		//Unnecessary to use OSGI.
		qsar=new QsarManager();
	}
	
	


	@Test
	public void testGetProviders(){

		//Get provider by ID
		DescriptorProvider provider=qsar.getProviderByID(cdkProviderID);
		assertNotNull(provider);
		
		assertEquals(cdkProviderID, provider.getId());
		assertEquals("Chemistry Development Kit", provider.getVendor());
		assertEquals("Chemistry Development Kit", provider.getName());
		assertEquals("http://cdk.sourceforge.net", provider.getNamesapce());
		assertEquals("CDK", provider.getShortName());
		assertEquals("1.1.0.v20080808", provider.getVersion());

		//Get provider classes
		List<DescriptorProvider> lstFull = qsar.getFullProviders();
		assertNotNull(lstFull);
		assertTrue(lstFull.contains(provider));

	}

	@Test
	public void testGetDescriptors(){

		//Matches plugin.xml, just test some classes
     	String xlogpID="org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor";
		String bpolID="org.openscience.cdk.qsar.descriptors.molecular.BPolDescriptor";

		//Get provider by ID
		DescriptorProvider provider=qsar.getProviderByID(cdkProviderID);
		assertNotNull(provider);

		List<String> descIDs=qsar.getDescriptorImplsByProvider(cdkProviderID);
		
		List<DescriptorImpl> descs=qsar.getFullDescriptorImpls(provider);
		
		//Check list of IDs and list of classes equal size
		assertEquals(descIDs.size(), descs.size());
		
		assertTrue(descIDs.contains(xlogpID));
		assertTrue(descIDs.contains(bpolID));
		
	}

	@Test
		public void testGetDescriptorsByID(){
		//Matches plugin.xml
		String bpolID="org.openscience.cdk.qsar.descriptors.molecular.BPolDescriptor";

		//Get decriptor by hardcoded id
		DescriptorImpl desc=qsar.getDescriptorImplByID(bpolID);
		assertNotNull(desc);
		assertNull(desc.getParameters());
		assertFalse(desc.isRequires3D());
		assertEquals(cdkProviderID, desc.getProvider().getId());
		assertNotNull(desc.getDescription());
		assertNotNull(desc.getDefinition());
	}

	@Test
	public void testGetDescriptorsByIDWithParameters(){
		//Matches plugin.xml
     	String xlogpID="org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor";

		//Get decriptor by hardcoded id with parameters
		DescriptorImpl desc=qsar.getDescriptorImplByID(xlogpID);
		assertNotNull(desc);
		assertNotNull(desc.getParameters());
		assertNotNull(desc.getDescription());
		assertNotNull(desc.getDefinition());

		List<String> paramKeys=new ArrayList<String>();
		List<String> paramVals=new ArrayList<String>();
		List<String> paramDesc=new ArrayList<String>();
		for (DescriptorParameter param: desc.getParameters()){
			System.out.println("Param: " + param.getKey() + " = " + param.getDefaultvalue() + " ; " + param.getDescription());
			paramKeys.add(param.getKey());
			paramVals.add(param.getDefaultvalue());
			paramDesc.add(param.getDescription());
		}
		
		assertEquals(paramKeys.get(0), "checkAromaticity");
		assertEquals(paramKeys.get(1), "salicylFlag");
		
		assertEquals(paramVals.get(0), "false");
		assertEquals(paramVals.get(1), "true");

		assertNotNull(paramDesc.get(0));
		assertNotNull(paramDesc.get(1));

	}
	
	
	@Test
	public void testGetDescriptorImplNotInOntology(){

		System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");
		System.out.println("Impl not in onology:");
		System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");
		for (DescriptorImpl impl : qsar.getFullDescriptorImpls()){
			if (qsar.getDescriptors().contains(impl.getDefinition())){
				//All is well
			}
			else{
				System.out.println("=.= Descriptor impl: " + impl.getName() + " with def: " + impl.getDefinition());
				
			}
			
		}
		System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");
		
		
	}


	@Test
	public void testGetPrefferedImplByDescriptorID(){

		String descriptorID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#chiChain";

		IEclipsePreferences prefs = new DefaultScope().getNode(Activator.PLUGIN_ID);
		assertNotNull(prefs);
		String prefsString=prefs.get(QSARConstants.QSAR_PROVIDERS_ORDER_PREFERENCE, null);
		assertNotNull(prefsString);

		System.out.println("Got prefs string: " + prefsString);
		assertTrue(prefsString.contains(cdkProviderName));
		
		DescriptorImpl impl=qsar.getPreferredImpl(descriptorID);
		assertNotNull(impl);
		System.out.println("pref impl: " + impl.getId());
//		assertEquals("net.bioclipse.qsar.test.descriptor2", impl.getId());
		System.out.println("wee");

	}
	
	@Test
	public void testCalculateBpolFromSmiles() throws BioclipseException{

		IMolecule mol=new SmilesMolecule("C1CNCCC1CC(COC)CCNC");
		String bpolID="org.openscience.cdk.qsar.descriptors.molecular.BPolDescriptor";
		
		IDescriptorResult dres = qsar.calculate(mol, bpolID);

		//We know only one result as we only asked for one descriptor
		assertNotNull(dres);
		assertNull(dres.getErrorMessage());
		assertEquals(bpolID, dres.getDescriptorId());

		System.out.println("Mol: " + mol.getSmiles() + 
				" ; Desc: " + dres.getDescriptorId() +": ");
		for (int i=0; i<dres.getValues().length;i++){
			System.out.println("    " + dres.getLabels()[i] 
			                                     + "=" + dres.getValues()[i] ); 
		}
		
		assertEquals("bpol", dres.getLabels()[0]);
		assertEquals(4.556, dres.getValues()[0]);
		
		
	}

	@Test
	public void testCalculateXlogPFromSmiles() throws BioclipseException{

		IMolecule mol=new SmilesMolecule("C1CNCCC1CC(COC)CCNC");
     	String xlogpID="org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor";
		
		IDescriptorResult dres1=qsar.calculate(mol, xlogpID);
		assertNotNull(dres1);
		assertNull(dres1.getErrorMessage());
		assertEquals(xlogpID, dres1.getDescriptorId());

		System.out.println("Mol: " + mol.getSmiles() + 
				" ; Desc: " + dres1.getDescriptorId() +": ");
		for (int i=0; i<dres1.getValues().length;i++){
			System.out.println("    " + dres1.getLabels()[i] 
			                                     + "=" + dres1.getValues()[i] ); 
		}
		
		assertEquals("XLogP", dres1.getLabels()[0]);
		assertEquals(0.184, dres1.getValues()[0]);
		
		
	}

	@Test
	public void testCalculateBCUTFromSmiles() throws BioclipseException{

		IMolecule mol=new SmilesMolecule("C1CNCCC1CC(COC)CCNC");
     	String bcutID="org.openscience.cdk.qsar.descriptors.molecular.BCUTDescriptor";
		
		IDescriptorResult dres1=qsar.calculate(mol, bcutID);
		assertNotNull(dres1);
		assertNull(dres1.getErrorMessage());
		assertEquals(bcutID, dres1.getDescriptorId());

		System.out.println("Mol: " + mol.getSmiles() + 
				" ; Desc: " + dres1.getDescriptorId() +": ");
		for (int i=0; i<dres1.getValues().length;i++){
			System.out.println("    " + dres1.getLabels()[i] 
			                                     + "=" + dres1.getValues()[i] ); 
		}

		assertEquals(6, dres1.getValues().length);
		assertEquals(6, dres1.getLabels().length);
		
		assertEquals("BCUTw-1l", dres1.getLabels()[0]);
		assertEquals(11.993387, dres1.getValues()[0]);

		assertEquals("BCUTw-1h", dres1.getLabels()[1]);
		assertEquals(15.994919, dres1.getValues()[1]);
		assertEquals("BCUTc-1l", dres1.getLabels()[2]);
		assertEquals(0.89, dres1.getValues()[2]);
		assertEquals("BCUTc-1h", dres1.getLabels()[3]);
		assertEquals(1.1102645, dres1.getValues()[3]);
		assertEquals("BCUTp-1l", dres1.getLabels()[4]);
		assertEquals(4.6727624, dres1.getValues()[4]);
		assertEquals("BCUTp-1h", dres1.getLabels()[5]);
		assertEquals(9.596294, dres1.getValues()[5]);
		
	}

	@Test
	public void testCalculateMultipleMolMultipleDescriptor() throws BioclipseException{
		
		IMolecule mol1=new SmilesMolecule("C1CNCCC1CC(COC)CCNC");
		IMolecule mol2=new SmilesMolecule("C1CCCCC1CC(CC)CCCCCOCCCN");
		String bpolID="org.openscience.cdk.qsar.descriptors.molecular.BPolDescriptor";
     	String xlogpID="org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor";
		
		List<IMolecule> mols=new ArrayList<IMolecule>();
		List<String> descs=new ArrayList<String>();
		
		mols.add(mol1);
		mols.add(mol2);
		descs.add(bpolID);
		descs.add(xlogpID);
		
		Map<IMolecule, List<IDescriptorResult>> res = qsar.calculateNoParams(mols, descs);
		assertNotNull(res);
		
		List<IDescriptorResult> res1=res.get(mol1);
		List<IDescriptorResult> res2=res.get(mol2);

		assertEquals(2, res1.size());
		assertEquals(2, res2.size());

		IDescriptorResult dres1=res1.get(0);
		IDescriptorResult dres11=res1.get(1);
		IDescriptorResult dres2=res2.get(0);
		IDescriptorResult dres22=res2.get(1);

		assertNull(dres1.getErrorMessage());
		assertNull(dres11.getErrorMessage());
		assertNull(dres2.getErrorMessage());
		assertNull(dres22.getErrorMessage());

		System.out.println("Mol: " + mol1.getSmiles() + 
				" ; Desc: " + dres1.getDescriptorId() +": ");
		for (int i=0; i<dres1.getValues().length;i++){
			System.out.println("    " + dres1.getLabels()[i] 
			                                     + "=" + dres1.getValues()[i] ); 
		}
		
		System.out.println("Mol: " + mol1.getSmiles() + 
				" ; Desc: " + dres11.getDescriptorId() +": ");
		for (int i=0; i<dres11.getValues().length;i++){
			System.out.println("    " + dres11.getLabels()[i] 
			                                     + "=" + dres11.getValues()[i] ); 
		}

		System.out.println("Mol: " + mol2.getSmiles() + 
				" ; Desc: " + dres2.getDescriptorId() +": ");
		for (int i=0; i<dres2.getValues().length;i++){
			System.out.println("    " + dres2.getLabels()[i] 
			                                     + "=" + dres2.getValues()[i] ); 
		}
		
		System.out.println("Mol: " + mol2.getSmiles() + 
				" ; Desc: " + dres22.getDescriptorId() +": ");
		for (int i=0; i<dres22.getValues().length;i++){
			System.out.println("    " + dres22.getLabels()[i] 
			                                     + "=" + dres22.getValues()[i] ); 
		}

		assertEquals("bpol", dres1.getLabels()[0]);
		assertEquals(4.556, dres1.getValues()[0]);
		assertEquals("XLogP", dres11.getLabels()[0]);
		assertEquals(0.184, dres11.getValues()[0]);

		assertEquals("bpol", dres2.getLabels()[0]);
		assertEquals(2.576, dres2.getValues()[0]);
		assertEquals("XLogP", dres22.getLabels()[0]);
		assertEquals(0.04, dres22.getValues()[0]);
		
	}

	
	
	@Test
	public void testCalculateAtomCountWithDefaultParams() throws BioclipseException{

		//Calculate C and N from this SMILES mol
		IMolecule mol=new SmilesMolecule("C1CNCCC1CC(COC)CCNC");

		String descrImplID="org.openscience.cdk.qsar.descriptors.molecular.AtomCountDescriptor";
		
		IDescriptorResult dres1=qsar.calculate(mol, descrImplID);
		assertNotNull(dres1);
		assertNull(dres1.getErrorMessage());
		assertEquals(descrImplID, dres1.getDescriptorId());
		
		assertEquals(1, dres1.getValues().length);
		
		System.out.println("Mol with default param C: " + mol.getSmiles() + 
				" ; Desc: " + dres1.getDescriptorId() +": " + dres1.getValues()[0] );

		
	}

	@Test
	public void testCalculateAtomCOuntWithParams() throws BioclipseException{

		//Calculate C and N from this SMILES mol
		IMolecule mol=new SmilesMolecule("C1CNCCC1CC(COC)CCNCCN");

		String descrImplID="org.openscience.cdk.qsar.descriptors.molecular.AtomCountDescriptor";

		DescriptorImpl impl=qsar.getDescriptorImplByID(descrImplID);
		assertEquals(1, impl.getParameters().size());

		//Work on a new instance
		DescriptorParameter newParam=impl.getParameters().get(0).clone();
		newParam.setValue("N");

		DescriptorParameter newParam2=impl.getParameters().get(0).clone();
		newParam2.setValue("C");

		
		List<DescriptorParameter> params=new ArrayList<DescriptorParameter>();
		params.add(newParam);

		List<DescriptorParameter> params2=new ArrayList<DescriptorParameter>();
		params2.add(newParam2);


		List<DescriptorInstance> descriptorInstances=new ArrayList<DescriptorInstance>();
		
		Descriptor descriptor=qsar.getDescriptorByID(impl.getDefinition());

		DescriptorInstance descInst1=new DescriptorInstance(descriptor,impl,params);
		DescriptorInstance descInst2=new DescriptorInstance(descriptor,impl,params2);
		
		descriptorInstances.add(descInst1);
		descriptorInstances.add(descInst2);
		
		List<IDescriptorResult> resList = qsar.calculate(mol, descriptorInstances);

		//We know only one result as we only asked for one descriptor
		assertEquals(2, resList.size());

		IDescriptorResult dres1=resList.get(0);
		assertNotNull(dres1);
		assertNull(dres1.getErrorMessage());
		assertEquals(descrImplID, dres1.getDescriptorId());
		assertEquals(1, dres1.getValues().length);
		
		System.out.println("Mol with param N: " + mol.getSmiles() + 
				" ; Desc: " + dres1.getDescriptorId() +": " + dres1.getValues()[0] );

		IDescriptorResult dres2=resList.get(1);
		assertNotNull(dres2);
		assertNull(dres2.getErrorMessage());
		assertEquals(descrImplID, dres2.getDescriptorId());
		assertEquals(1, dres2.getValues().length);
		
		System.out.println("Mol with param C: " + mol.getSmiles() + 
				" ; Desc: " + dres2.getDescriptorId() +": " + dres2.getValues()[0] );

		assertEquals(3, dres1.getValues()[0]);

		assertEquals(13, dres2.getValues()[0]);
		

		
	}
	
	
	

}
