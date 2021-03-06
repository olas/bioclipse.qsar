/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.qsar.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.SMILESMolecule;
import net.bioclipse.qsar.DescriptorType;
import net.bioclipse.qsar.QSARConstants;
import net.bioclipse.qsar.business.IQsarManager;
import net.bioclipse.qsar.business.QsarManager;
import net.bioclipse.qsar.descriptor.IDescriptorResult;
import net.bioclipse.qsar.descriptor.model.Descriptor;
import net.bioclipse.qsar.descriptor.model.DescriptorCalculationResult;
import net.bioclipse.qsar.descriptor.model.DescriptorImpl;
import net.bioclipse.qsar.descriptor.model.DescriptorProvider;
import net.bioclipse.qsar.init.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class PluginTestCDKRESTQsar {

    IQsarManager qsar;
    private String cdkRestProviderID="net.bioclipse.cdk.rest.descriptorprovider";
    private static final String CDK_REST_SHORTNAME="CDK REST";

    String bpolID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#bpol";
    String xlogpID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#xlogP";
    String chiChainID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#chiChain";
    String bcutID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#BCUT";
    String atomCountlID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#atomCount";
    String rotBondsCntID="http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#rotatableBondsCount";

    public PluginTestCDKRESTQsar() {
        qsar=Activator.getDefault().getJavaQsarManager();
    }


    @Test
    public void testGetDescriptors() throws BioclipseException{

        //Matches plugin.xml, just test some classes

        //Get provider by ID
        DescriptorProvider provider=qsar.getProviderByID(cdkRestProviderID);
        assertNotNull(provider);

        List<String> descImplIDs=qsar.getDescriptorImplsByProvider(cdkRestProviderID);

        List<DescriptorImpl> descs=qsar.getFullDescriptorImpls(provider);

        //Check list of IDs and list of classes equal size
        assertEquals(descImplIDs.size(), descs.size());

        List<String> descIDs=new ArrayList<String>();
        for (DescriptorImpl impl : descs){
            descIDs.add(impl.getDefinition());
        }

        assertTrue(descIDs.contains(xlogpID));
        assertTrue(descIDs.contains(bpolID));
        
    }

    @Test
    public void testGetDescriptorsByID() throws BioclipseException{

        //Get decriptor by hardcoded id
        DescriptorImpl desc=qsar.getDescriptorImpl(bpolID, cdkRestProviderID);
        assertNotNull(desc);
        assertNull(desc.getParameters());
        assertFalse(desc.isRequires3D());
        assertEquals(cdkRestProviderID, desc.getProvider().getId());
        assertNotNull(desc.getDescription());
        assertNotNull(desc.getDefinition());
    }


    @Test
    public void testGetDescriptorImplNotInOntology(){

        System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");
        System.out.println("Impl not in onology:");
        System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");
        boolean toFail=false;
        for (DescriptorImpl impl : qsar.getFullDescriptorImpls()){
            if (qsar.getDescriptorIDs().contains(impl.getDefinition())){
                //All is well
            }
            else{
                System.out.println("=.= Descriptor impl: " + impl.getName() + " with def: " + impl.getDefinition());
                toFail=true;
            }

        }
        System.out.println("=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.=.");

        assertFalse( "Not all CDK impl are present in ontology, see list on stdout"
                     , toFail );

    }


    @Test
    public void testGetPrefferedImplByDescriptorID(){


        IEclipsePreferences prefs = new DefaultScope().getNode(Activator.PLUGIN_ID);
        assertNotNull(prefs);
        String prefsString=prefs.get(QSARConstants.QSAR_PROVIDERS_ORDER_PREFERENCE, null);
        assertNotNull(prefsString);

        System.out.println("Got prefs string: " + prefsString);

        DescriptorImpl impl=qsar.getPreferredImpl(chiChainID);
        assertNotNull(impl);
        System.out.println("pref impl: " + impl.getId());
        //		assertEquals("net.bioclipse.qsar.test.descriptor2", impl.getId());
        System.out.println("wee");

    }

    @Test
    public void testCalculateBpolFromSmiles() throws BioclipseException{

        IMolecule mol=new SMILESMolecule("C1CNCCC1CC(COC)CCNC");

//        DescriptorImpl desc=qsar.getDescriptorImpl(bpolID, cdkRestProviderID);
        IDescriptorResult dres = qsar.calculate(mol, bpolID, CDK_REST_SHORTNAME);

        assertNull(dres.getErrorMessage());
        assertEquals(bpolID, dres.getDescriptor().getOntologyid());

        System.out.println("Mol: " +
        		mol.toSMILES() + 
                           " ; Desc: " + dres.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres.getValues().length;i++){
            System.out.println("    " + dres.getLabels()[i] 
                                                         + "=" + dres.getValues()[i] ); 
        }

        assertEquals("bpol", dres.getLabels()[0]);
        assertEquals(new Float(31.659382), dres.getValues()[0]);


    }

    @Test
    public void testCalculateXlogPFromSMILES() throws BioclipseException{

        IMolecule mol=new SMILESMolecule("C1CNCCC1CC(COC)CCNC");

        IDescriptorResult dres1 =qsar.calculate(mol, xlogpID, CDK_REST_SHORTNAME);
        
        assertNotNull(dres1);
        assertNull(dres1.getErrorMessage());
        assertEquals(xlogpID, dres1.getDescriptor().getOntologyid());

        System.out.println("Mol: " +
                           mol.toSMILES() + 
                           " ; Desc: " + dres1.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres1.getValues().length;i++){
            System.out.println("    " + dres1.getLabels()[i] 
                                                          + "=" + dres1.getValues()[i] ); 
        }

        assertEquals("XLogP", dres1.getLabels()[0]);
        assertEquals(new Float(0.184), dres1.getValues()[0]);


    }

    @Test
    public void testCalculateXlogPFromCML() throws BioclipseException, FileNotFoundException, IOException, CoreException{

        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

        Bundle bun=Platform.getBundle(net.bioclipse.cdk.qsar.test.Activator.PLUGIN_ID);
        URL url=FileLocator.find(bun, new Path("src/testFiles/0037.cml"), null);
        String str=FileLocator.toFileURL(url).getFile();

        ICDKMolecule mol = cdk.loadMolecule( new MockIFile(str) );

        IDescriptorResult dres1=qsar.calculate(mol, xlogpID, CDK_REST_SHORTNAME);
        IDescriptorResult dres2=qsar.calculate(mol, xlogpID, "CDK");
        
        assertNotNull(dres1);
        assertNull(dres1.getErrorMessage(),dres1.getErrorMessage());
        assertEquals(xlogpID, dres1.getDescriptor().getOntologyid());
        assertNotNull(dres2);
        assertNull(dres2.getErrorMessage(),dres1.getErrorMessage());
        assertEquals(xlogpID, dres2.getDescriptor().getOntologyid());
        
        assertEquals("CDK and CDK REST gives different labels", dres2.getLabels()[0], dres1.getLabels()[0] );
        assertEquals("CDK and CDK REST gives different values", dres2.getValues()[0], dres1.getValues()[0] );

    }


    @Test
    public void testCalculateBpolFromSMILES() throws BioclipseException{

        IMolecule mol=new SMILESMolecule("C1CNCCC1CC(COC)CCNC");

        IDescriptorResult dres1 = qsar.calculate(mol, bpolID, CDK_REST_SHORTNAME);

        assertNotNull(dres1);
        assertNull(dres1.getErrorMessage());
        assertEquals(bpolID, dres1.getDescriptor().getOntologyid());
        assertEquals(cdkRestProviderID, dres1.getDescriptor().getProvider());

        System.out.println("Mol: " + 
                           mol.toSMILES() + 
                           " ; Desc: " + dres1.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres1.getValues().length;i++){
            System.out.println("    " + dres1.getLabels()[i] 
                                                          + "=" + dres1.getValues()[i] ); 
        }

        assertEquals(6, dres1.getValues().length);
        assertEquals(6, dres1.getLabels().length);

        assertEquals("BCUTw-1l", dres1.getLabels()[0]);
        assertEquals(new Float(11.993387), dres1.getValues()[0]);

        assertEquals("BCUTw-1h", dres1.getLabels()[1]);
        assertEquals(new Float(15.994919), dres1.getValues()[1]);
        assertEquals("BCUTc-1l", dres1.getLabels()[2]);
        assertEquals(new Float(0.89), dres1.getValues()[2]);
        assertEquals("BCUTc-1h", dres1.getLabels()[3]);
        assertEquals(new Float(1.1102645), dres1.getValues()[3]);
        assertEquals("BCUTp-1l", dres1.getLabels()[4]);
        assertEquals(new Float(4.6727624), dres1.getValues()[4]);
        assertEquals("BCUTp-1h", dres1.getLabels()[5]);
        assertEquals(new Float(9.596294), dres1.getValues()[5]);

    }

    @Test
    public void testCalculateMultipleMolMultipleDescriptor() throws BioclipseException{

        IMolecule mol1=new SMILESMolecule("C1CNCCC1CC(COC)CCNC");
        IMolecule mol2=new SMILESMolecule("C1CCCCC1CC(CC)CCCCCOCCCN");

        List<IMolecule> mols=new ArrayList<IMolecule>();
        List<String> descs=new ArrayList<String>();

        mols.add(mol1);
        mols.add(mol2);
        descs.add(bpolID);
        descs.add(xlogpID);

        DescriptorCalculationResult calres = qsar.calculate(mols, descs, CDK_REST_SHORTNAME);
        
        Map<IMolecule, List<IDescriptorResult>> res = calres.getResultMap();
        
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

        System.out.println("Mol: " +
        		mol1.toSMILES() + 
                           " ; Desc: " + dres1.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres1.getValues().length;i++){
            System.out.println("    " + dres1.getLabels()[i] 
                                                          + "=" + dres1.getValues()[i] ); 
        }

        System.out.println("Mol: " +
                           mol1.toSMILES() + 
                           " ; Desc: " + dres11.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres11.getValues().length;i++){
            System.out.println("    " + dres11.getLabels()[i] 
                                                           + "=" + dres11.getValues()[i] ); 
        }

        System.out.println("Mol: " +
                           mol2.toSMILES() + 
                           " ; Desc: " + dres2.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres2.getValues().length;i++){
            System.out.println("    " + dres2.getLabels()[i] 
                                                          + "=" + dres2.getValues()[i] ); 
        }

        System.out.println("Mol: " +
                           mol2.toSMILES() + 
                           " ; Desc: " + dres22.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres22.getValues().length;i++){
            System.out.println("    " + dres22.getLabels()[i] 
                                                           + "=" + dres22.getValues()[i] ); 
        }

        assertEquals("bpol", dres1.getLabels()[0]);
        assertEquals(new Float(31.659382), dres1.getValues()[0]);
        assertEquals("XLogP", dres11.getLabels()[0]);
        assertEquals(new Float(0.184), dres11.getValues()[0]);

        assertEquals("bpol", dres2.getLabels()[0]);
        assertEquals(new Float(41.70466), dres2.getValues()[0]);
        assertEquals("XLogP", dres22.getLabels()[0]);
        assertEquals(new Float(6.749), dres22.getValues()[0]);

    }



    @Test
    public void testCalculateMolDescMap() throws BioclipseException{

        IMolecule mol1=new SMILESMolecule("C1CCCCC1CC(CC)CC");
        IMolecule mol2=new SMILESMolecule("C1CCCCC1CC(CC)CCCCCO");

        Descriptor desc1=qsar.getDescriptorByID( bpolID );
        Descriptor desc2=qsar.getDescriptorByID( xlogpID );
        DescriptorImpl impl1 = qsar.getPreferredImpl( bpolID );
        DescriptorImpl impl2 = qsar.getPreferredImpl( xlogpID );

        DescriptorType dtype1 = qsar.createDescriptorType( null, null, desc1, impl1, null );
        DescriptorType dtype2 = qsar.createDescriptorType( null, null, desc2, impl2, null );

        Map<IMolecule, List<DescriptorType>> moldescmap=new HashMap<IMolecule, List<DescriptorType>>();
        List<DescriptorType> list1=new ArrayList<DescriptorType>();
        list1.add( dtype1 );
        list1.add( dtype2 );
        moldescmap.put( mol1, list1 );
        List<DescriptorType> list2=new ArrayList<DescriptorType>();
        list2.add( dtype2 );
        moldescmap.put( mol2, list2 );

        Map<? extends IMolecule, List<IDescriptorResult>> res = qsar.doCalculation( moldescmap, new NullProgressMonitor() );
        assertNotNull("QSAR calculation returned NULL", res);

        assertNotNull(res);

        List<IDescriptorResult> res1=res.get(mol1);
        List<IDescriptorResult> res2=res.get(mol2);

        assertEquals(2, res1.size());
        assertEquals(1, res2.size());

        IDescriptorResult dres1=res1.get(0);
        IDescriptorResult dres11=res1.get(1);
        IDescriptorResult dres2=res2.get(0);

        assertNull(dres1.getErrorMessage());
        assertNull(dres11.getErrorMessage());
        assertNull(dres2.getErrorMessage());

        System.out.println("Mol: " +
                           mol1.toSMILES() + 
                           " ; Desc: " + dres1.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres1.getValues().length;i++){
            System.out.println("    " + dres1.getLabels()[i] 
                                                          + "=" + dres1.getValues()[i] ); 
        }

        System.out.println("Mol: " +
                           mol1.toSMILES() + 
                           " ; Desc: " + dres11.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres11.getValues().length;i++){
            System.out.println("    " + dres11.getLabels()[i] 
                                                           + "=" + dres11.getValues()[i] ); 
        }

        System.out.println("Mol: " +
                           mol2.toSMILES() + 
                           " ; Desc: " + dres2.getDescriptor().getOntologyid() +": ");
        for (int i=0; i<dres2.getValues().length;i++){
            System.out.println("    " + dres2.getLabels()[i] 
                                                          + "=" + dres2.getValues()[i] ); 
        }


        assertEquals("bpol", dres1.getLabels()[0]);
        assertEquals(new Float(26.236967), dres1.getValues()[0]);
        assertEquals("XLogP", dres11.getLabels()[0]);
        assertEquals(new Float(6.706), dres11.getValues()[0]);

        assertEquals("XLogP", dres2.getLabels()[0]);
        assertEquals(new Float(6.648), dres2.getValues()[0]);

    }


}
