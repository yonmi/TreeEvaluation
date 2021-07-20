/****************************************************************************
* Copyright AGAT-Team (2014)						       
* 									    
* Contributors:								
* J.F. Randrianasoa							    
* K. Kurtz								    
* E. Desjardin								    
* N. Passat								    
* 									    
* This software is a computer program whose purpose is to [describe	    
* functionalities and technical features of your software].		    
* 									    
* This software is governed by the CeCILL-B license under French law and    
* abiding by the rules of distribution of free software.  You can  use,     
* modify and/ or redistribute the software under the terms of the CeCILL-B  
* license as circulated by CEA, CNRS and INRIA at the following URL	    
* "http://www.cecill.info". 						    
* 									    
* As a counterpart to the access to the source code and  rights to copy,    
* modify and redistribute granted by the license, users are provided only   
* with a limited warranty  and the software's author,  the holder of the    
* economic rights,  and the successive licensors  have only  limited	    
* liability. 								    
* 									    
* In this respect, the user's attention is drawn to the risks associated    
* with loading,  using,  modifying and/or developing or reproducing the     
* software by the user in light of its specific status of free software,    
* that may mean  that it is complicated to manipulate,  and  that  also	   
* therefore means  that it is reserved for developers  and  experienced     
* professionals having in-depth computer knowledge. Users are therefore     
* encouraged to load and test the software's suitability as regards their   
* requirements in conditions enabling the security of their systems and/or  
* data to be ensured and,  more generally, to use and operate it in the     
* same conditions as regards security. 					    
*								            
* The fact that you are presently reading this means that you have had	    
* knowledge of the CeCILL-B license and that you accept its terms.          
* 									   		
* The full license is in the file LICENSE, distributed with this software.  
*****************************************************************************/

package experiences.pr2020;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import datastructure.CutResult;
import datastructure.Node;
import datastructure.Tree;
import standard.sequential.BPT;
import utils.CutBPT;
import utils.ImTool;
import utils.TreeVisu;

public class CuttingTreeExample {

	public static void main(String[] args) {

		String dataName = "weizmann1obj";
		//String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		
		int nbImages = 1;
		int iTest = 3083;
		//int iTest = 4730;

		
		String orandomName = "orandom";
		String ominmaxName = "ominmax";
		String omseName = "omse";
		String owsdmName = "owsdm";
		String ocolcontminmaxName = "ocolcontminmax";
		String ocolcontmseName = "ocolcontmse";
		String ocolcontwsdmName = "ocolcontwsdm";
		String omselabName ="omselab";
		
		Set<String> metricsName = new HashSet<String>();
		metricsName.add(orandomName);
		metricsName.add(ominmaxName);
		metricsName.add(omseName);
		metricsName.add(owsdmName);
		metricsName.add(ocolcontminmaxName);
		metricsName.add(ocolcontmseName);
		metricsName.add(ocolcontwsdmName);
		metricsName.add(omselabName);
		
		boolean success = false;
		try {
			
			System.out.println("[Step1] Starging BPT loadings!");
			for(int i = 0; i < nbImages; ++i) {
				
				String imgName = "img_"+ iTest +"_modif.png";

				System.out.print((i+1) +"/"+ nbImages +" "+ imgName +" ... ");
				for(String nameOfMetric: metricsName) {
					
					String bptName = "img_"+ iTest +"_modif";
					String bptDir = mainDir +"//bpt//"+ nameOfMetric;
					String bptPath = bptDir +"//"+ bptName +".h5";
					String cutDirParent = mainDir +"//cut";
					new File(cutDirParent).mkdir();
					String cutDir = cutDirParent +"//"+ nameOfMetric;
					new File(cutDir).mkdir();
					cutDir = cutDir +"//"+ bptName;
					new File(cutDir).mkdir();
					
					// Regrow the tree
					Tree bpt = new BPT(bptPath);
					
					// Drawing the tree in a file
					Node root = bpt.getRoot();
					String savePath = bptDir +"//"+ bptName +".txt";
					TreeVisu.display(root, savePath);
					
					// Cutting the tree
					int starting = 50;
					int ending = 0;
					int step = 1;
					CutResult cutResult = CutBPT.execute(bpt, starting, ending, step);
					for(Entry<Integer, BufferedImage> entry: cutResult.regionImages.entrySet()) {
						
						int numberOfRegions = entry.getKey();
						BufferedImage partition = entry.getValue();
						
						String cutName = cutDir +"//"+ numberOfRegions +"_"+ imgName;
						ImTool.save(partition, cutName);
					}
					
					System.out.print("|"+ nameOfMetric +" ");
				}		
				
				System.out.println("| Ok");
			}
			
			success = true;
			
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
		if(success)	System.out.println("[Step1] All BPT cuts finished!");
	}

}
