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
