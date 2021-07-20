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

import org.apache.commons.io.FilenameUtils;

import datastructure.Tree;
import metric.bricks.Metric;
import metric.bricks.Metric.TypeOfMetric;
import metric.bricks.MetricFactory;
import metric.color.Ominmax;
import metric.color.Omse;
import metric.color.Omselab;
import metric.color.Owsdm;
import metric.others.Orandom;
import standard.sequential.BPT;
import standard.sequential.BPT.TypeOfConnectivity;
import utils.ImTool;
import utils.SaveBPT;

public class Step1_BptCreation {

	public static void main(String args[]) {
		
		//String dataName = "weizmann1obj";
		String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		String imgDir = mainDir +"//img";
		String presegDir = mainDir +"//slic";

		boolean success = false;
		try {
			
			System.out.println("[Step1] Starging BPT creations!");
			
			File folder = new File(imgDir);
			File[] listOfFiles = folder.listFiles();
			int nbImages = listOfFiles.length;

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					
					File file = listOfFiles[i];

					String imgName = file.getName();
					String imgPath = file.getPath();
					String presegName = FilenameUtils.removeExtension(imgName) +".tif";
					String presegPath = presegDir +"//"+ presegName;
					
					System.out.print((i+1) +"/"+ nbImages +" "+ imgName +" ... ");
					createBPT(mainDir, imgName, imgPath, presegName, presegPath);
				}
			}
			
			success = true;
			
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
		if(success)	System.out.println("[Step1] All BPT creation finished!");
	}
	
	public static void createBPT(String mainDir, String imgName, String imgPath, String presegName, String presegPath) {
		
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
		
		BufferedImage img = ImTool.read(imgPath);
		BufferedImage presegImg = ImTool.read(presegPath);
		
		for(String nameOfMetric: metricsName) {
			
			Metric metric = null;
			
			if(nameOfMetric == orandomName)	metric = new Orandom(img);
			else if(nameOfMetric == ominmaxName) metric = new Ominmax(img);
			else if(nameOfMetric == omseName) metric = new Omse(img);
			else if(nameOfMetric == owsdmName) metric = new Owsdm(img);
			else if(nameOfMetric == ocolcontminmaxName) metric = MetricFactory.initMetric(TypeOfMetric.OCOL_CONT_MIN_MAX, img); 
			else if(nameOfMetric == ocolcontmseName) metric = MetricFactory.initMetric(TypeOfMetric.OCOL_CONT_MSE, img);
			else if(nameOfMetric == ocolcontwsdmName) metric = MetricFactory.initMetric(TypeOfMetric.OCOL_CONT_WSDM, img);
			else if(nameOfMetric == omselabName) metric = new Omselab(img);
			
			String bptName = FilenameUtils.removeExtension(imgName);
			String bptDirParent = mainDir +"//bpt";
			new File(bptDirParent).mkdir();
			String bptDir = bptDirParent +"//"+ nameOfMetric;
			new File(bptDir).mkdir();
			
			Tree bpt = new BPT(img, TypeOfConnectivity.CN8);
			bpt.setPreSegImage(presegImg);
			bpt.setMetric(metric);
			bpt.setName(bptName);
			bpt.setDirectory(bptDir);
			bpt.grow();
			SaveBPT.toHDF5(bpt);
			
			System.out.print("|"+ nameOfMetric +" ");
		}		
		
		System.out.println("| Ok");
		System.gc();
	}
}
