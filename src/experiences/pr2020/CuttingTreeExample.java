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
