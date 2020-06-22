package evaluation.datastructure;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import datastructure.Node;
import ui.ImFrame;
import utils.ImTool;
import utils.LabelMatrix;
import utils.Log;
import utils.SegmentByConnexityRaw;

/**
 * 
 * Segment of reference from a ground truth GT.
 *
 */
public class SegReference{

	/**
	 * Pixels of the segment according to its bounding box.
	 */
	public ArrayList<Point> bbPoints;

	/**
	 * Height of the bounding box.
	 */
	public int bbHeight;

	/**
	 * Width of the bounding box.
	 */
	public int bbWidth;

	/**
	 *  Best matching node.
	 */
	public Node bestN = null;

	/**
	 * Best score of similarity associated with the best matched node 'lamda(S_i)'.
	 */
	public double bestSimilarityScore = 0.0;

	/**
	 * Background color of the GT image.
	 */
	public int bgColor;

	/**
	 * Bounding box including a margin.\n
	 * 
	 * <ul>
	 * <li>boundingBox[0] is the cell coordinate of the upper-left point
	 * <li>boundingBox[1] is the cell coordinate of the lower-right point
	 * <li>boundingBox[2] is the row coordinate of the upper-left point
	 * <li>boundingBox[3] is the row coordinate of the lower-right point
	 * </ul>
	 */
	public int[] boundingBox = new int[4];
	
	/**
	 * Path where the extracted GT informations are stored.
	 */
	public String gtPath;

	/**
	 * Pixels of a segment contained in a GT image.
	 */
	public ArrayList<Point> gtPoints;

	/**
	 * Path of the GT image associated to the segment.
	 */
	public String imgGtPath;

	/**
	 * Height of the crop image containing only the segment.
	 */
	public int imgGtHeight;

	/**
	 * Width of the GT image containing only the segment.
	 */
	public int imgGtWidth;

	/**
	 * Index of the segment.
	 */
	public int index;

	/**
	 * value[0] -> Number of segments in the class. <br>
	 * value[1] -> Size of all segments in the class. <br>
	 * value[2] -> Score f1 <br>
	 * value[3] -> Score f2 <br>
	 * Only the first segment of a list should contain this value.
	 */
	public TreeMap<Integer, Double[]> listOfAllSemanticLabels;

	/**
	 * Number of pixels that will be added around the bounding box when computing a crop of the segment.
	 */
	public  int margin;

	/**
	 * A matrix containing the real valor of 'sigma_s(x)' i.e. < 0 for x in the segment, 0 on the border of the segment and > 0 outside the segment and contains also the value of 'Mu_alpha'.
	 * muMap[x][y][0] -> distance value <br>
	 * muMap[x][y][1] -> mu value
	 */
	public double[][][] muMap;

	public static int nbSegs = 0;
	
	/**
	 * Coordinates of the real bounding box without margins.
	 */
	public int[] realBoundingBox;

	/**
	 * Thematic label.
	 */
	public int semanticLabel;

	/**
	 * If true. the number of segments of each class and the total size of each class are computed.
	 * Only the first segment of the list should contain this value.
	 */
	public boolean sizeAndNbSet;



	/**
	 * Prepare a segment of reference.
	 * @param index : identification of the segment.
	 * @param points ~ pixels contained in the segment of reference.
	 * @param semanticLabel : thematic label.
	 * @param bgColor : background color of the GT image.
	 */
	public SegReference(ArrayList<Point> points, int semanticLabel, int bgColor) {
	
		this.index = ++SegReference.nbSegs;
		this.gtPoints = points;
		this.semanticLabel = semanticLabel;
		this.bgColor = bgColor;
	}

	/**
	 * Prepare a segment of reference.
	 * @param imgGtPath : Path of an image containing only the segment having a white background.
	 * @param semanticLabel : thematic label
	 * @param bgColor : background color of the GT image.
	 * @param computeBB : if 'true', compute the bounding box and also fill a list of points having coordinates according to the bounding box.
	 */
	public SegReference(String imgGtPath, int semanticLabel, int bgColor, boolean computeBB){
	
		this.index = ++SegReference.nbSegs;
	
		/* Remember the gt crop image path. */
		this.imgGtPath = imgGtPath;
	
		/*
		 * Load the GT image.
		 */
		BufferedImage img = ImTool.read(this.imgGtPath);
		this.imgGtWidth= img.getWidth();
		this.imgGtHeight= img.getHeight();
	
		/*
		 * Set the background color of the initial GT image containing the segments.
		 */
		//		this.initBgColor(bgColor);
		this.bgColor = bgColor;
	
		/*
		 * Get the list of integer points.
		 */
		this.gtPoints = new ArrayList<Point>();
		for(int x=0;x<this.imgGtWidth;x++) {
			for(int y=0;y<this.imgGtHeight;y++) {
	
				if(img.getRGB(x, y) != this.bgColor) {

					Point p = new Point(x, y);
					this.gtPoints.add(p);
				}
			}
		}
	
		/*
		 * Store a semantic label. 
		 */
		this.semanticLabel = semanticLabel;
	
		if(computeBB){
	
			/*
			 * Compute the bounding box.
			 */
			this.computeRegionBoundingBox();
		}
	}

	/**
	 * Create a segment of reference having a specified label.
	 * @param semanticLabel : thematic label.
	 * @param gtWidth : width of the GT image.
	 * @param gtHeight : height of the GT image.
	 * @param gtImgPath : path of the GT image.
	 * @param alpha : >0 allow controlling the degree of uncertainty. 
	 */
	public SegReference(int semanticLabel, int gtWidth, int gtHeight, String gtImgPath, double alpha){
	
		this.index = ++SegReference.nbSegs;
		this.semanticLabel = semanticLabel;
		this.imgGtWidth = gtWidth;
		this.imgGtHeight = gtHeight;
		this.imgGtPath = gtImgPath;
	
		/* Pixel margin around the real bounding box */
		if(alpha != 0) {
			
			this.margin = (int)(Math.log((1 / 0.001) - 1) / alpha); /* in px */
			
		}else {
			this.margin = 0;
		}
	
		this.gtPoints = new ArrayList<Point>();
	
	}
	
	/**
	 * Create a segment of reference having a specified label.
	 * @param semanticLabel : thematic label.
	 * @param gtWidth : width of the GT image.
	 * @param gtHeight : height of the GT image.
	 * @param gtImgPath : path of the GT image.
	 * @param bgColor : background color of the GT image.
	 * @param alpha : >0 allow controlling the degree of uncertainty. 
	 */
	public SegReference(int semanticLabel, int gtWidth, int gtHeight, String gtImgPath, int bgColor, double alpha){
	
		this.index = ++SegReference.nbSegs;
		this.semanticLabel = semanticLabel;
		this.imgGtWidth = gtWidth;
		this.imgGtHeight = gtHeight;
		this.imgGtPath = gtImgPath;
		this.bgColor = bgColor;
	
		/* Pixel margin around the real bounding box */
		if(alpha != 0) {
			
			this.margin = (int)(Math.log((1 / 0.001) - 1) / alpha); /* in px */
			
		}else {
			this.margin = 0;
		}
	
		this.gtPoints = new ArrayList<Point>();
	}

	/**
	 * Add a pixel from a GT image.
	 * @param point ~ pixel to add.
	 */
	public void addGtPoint(Point point){

		this.gtPoints.add(point);	
	}

	/**
	 * Compute the bounding box BB of the segment and translate the pixels' location according to this BB.
	 * @return the real bounding box values.
	 */
	public int[] computeRegionBoundingBox() {

		this.boundingBox = new int[4];
		this.realBoundingBox = new int[4];

		/* Compute a bounding box */
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		for(Point p : this.gtPoints) {	

			int x= p.x;
			int y= p.y;

			if(x < minX)
				minX = x;
			if(x > maxX)
				maxX = x;
			if(y < minY)
				minY = y;
			if(y > maxY)
				maxY = y;
		}

		/* Remember the crop bounding box */
		int minXMargin = minX - this.margin;
		int maxXMargin = maxX + this.margin;
		int minYMargin = minY - this.margin;
		int maxYMargin = maxY + this.margin;

		if(minXMargin >= 0) this.boundingBox[0] = minXMargin;
		else this.boundingBox[0] = 0;
		if(maxXMargin <= this.imgGtWidth) this.boundingBox[1] = maxXMargin;
		else this.boundingBox[1] = this.imgGtWidth;
		if(minYMargin >= 0) this.boundingBox[2] = minYMargin;
		else this.boundingBox[2] = 0;
		if(maxYMargin <= this.imgGtHeight) this.boundingBox[3] = maxYMargin;
		else this.boundingBox[3] = this.imgGtHeight;

		/* real bounding box values */
		this.realBoundingBox[0] = minX;
		this.realBoundingBox[1] = maxX;
		this.realBoundingBox[2] = minY;
		this.realBoundingBox[3] = maxY;

		/* Compute the width and the height of the bounding box */
		this.bbWidth = this.boundingBox[1] - this.boundingBox[0] + 1;
		this.bbHeight = this.boundingBox[3] - this.boundingBox[2] + 1;

		if(this.bbWidth <= 0) this.bbWidth = 1;
		if(this.bbHeight <= 0) this.bbHeight = 1;

		/* Update the pixel location values according to the bounding box */
		this.gtToBB();

		return realBoundingBox;
	}
	
	/**
	 * Removes a segment from a list of segments of reference according to its RGB integer color value.
	 * @param segReferences tree map containing some segments of reference.
	 * @param color of the segment to remove.
	 */
	public static void deleteSegment(TreeMap<Integer, SegReference> segReferences, int color) {
	
		SegReference seg = segReferences.get(color);
		if(seg != null){
			
			segReferences.remove(color);
			int i = 0;
			for(Entry <Integer, SegReference> entry: segReferences.entrySet()) {
				
				SegReference sr = entry.getValue();
				sr.index = i++; 
			}
		}
		Log.println("Delete a seg. ref.", "nb. remaining segs.: "+ segReferences.size() +"\n");
	}

	/**
	 * Extracts all segments of a fat zone image and saves the valuable information in some files. 
	 * 
	 * @param gtImgPath : Path of the image GT having white background. 
	 * @param alpha : >0 allow controlling the degree of uncertainty.
	 * @param generateCrops : saving map images and the segments in a file.
	 * @return
	 */
	public static TreeMap<Integer, SegReference> extractSegmentsOfReference(String gtImgPath, double alpha, boolean generateCrops){

		Log.println("GT-EXTRACTION", "Starting");
		
		/* Load the image */
		BufferedImage gtImg = ImTool.read(gtImgPath);
		int gtWidth = gtImg.getWidth();
		int gtHeight = gtImg.getHeight();

		/* Set containing list of segments of reference */
		TreeMap<Integer, SegReference> listOfSegs = new TreeMap<Integer, SegReference>();

		/* Get all the connected regions contained in the GT image */
		SegmentByConnexityRaw sbc = new SegmentByConnexityRaw(gtImg);
		LabelMatrix inputLabelMatrix = sbc.runForFullImage();

		/* Extract the segments */
		TreeMap<Integer, Double[]> semanticLabels = new TreeMap<Integer, Double[]>();
		for(int y=0; y < gtHeight; ++y){
			for(int x=0; x < gtWidth; ++x){

				
				boolean isBlack = true;
				int nbBands = Math.min(ImTool.getNbBandsOf(gtImg), 3); 
				for(int band = 0; band < nbBands; ++band) {
					
					double val = ImTool.getPixelValue(x, y, band, gtImg);
					if(val != 0) {
						
						isBlack = false;
						break;
					}
				}
				
				if(!isBlack) { // drop the black background

					int segLabNonSem = inputLabelMatrix.getLabel(x, y); 

					int rgbVal = gtImg.getRGB(x, y);
					
					if(!listOfSegs.containsKey(segLabNonSem)){

						/* create a segment of reference object, add it to the list with its semantic label */
						int semanticLabel = rgbVal;
						listOfSegs.put(segLabNonSem, new SegReference(semanticLabel, gtWidth, gtHeight, gtImgPath, alpha));
						if(!semanticLabels.containsKey(semanticLabel)){
							Double[] val = new Double[4];
							val[0] = 0.0;
							val[1] = 0.0;
							val[2] = 0.0;
							val[3] = 0.0;
							semanticLabels.put(semanticLabel, val);
						}
					}	

					/* store the point ~ pixel in the segment of reference object */ 
					listOfSegs.get(segLabNonSem).addGtPoint(new Point(x, y));	
				}
			}
		}

		/* Use the first segment to store the information about the semantic labels */
		listOfSegs.firstEntry().getValue().listOfAllSemanticLabels = semanticLabels;

		/* Identify the correct path where the segment outputs will be stored */
		String folderPath[] = gtImgPath.split("\\\\");
		int lastIndex = folderPath.length - 1;
		String gtImgName = folderPath[lastIndex];
		String fpath[] = gtImgPath.split(gtImgName);
		String gtPath = fpath[0] +"segref_("+ gtImgName +")";

		/* Save a manifest file if it's not already existing */
		String manifPath = gtPath +"//manifest";
		File mf = new File(manifPath);
		if(!mf.exists() || mf.isDirectory()){ /* create a file and register the number of segments and the 1st alpha used */

			try {

				/* global info on the list of segments */
				new File(gtPath).mkdir();
				PrintWriter manifWriter = new PrintWriter(manifPath, "UTF-8");
				manifWriter.println(listOfSegs.size()); /* save <nb_segments> */
				int it = 0;
				for(Entry<Integer, Double[]> entry: semanticLabels.entrySet()){
					
					manifWriter.print(entry.getKey());
					if(it < semanticLabels.size() - 1)
						manifWriter.print(";");
					it++;
				}
				manifWriter.println();
				manifWriter.print(alpha);
				manifWriter.close();

			} catch (Exception e){
				e.printStackTrace();
			}

		}else{

			/* add the current alpha in the manifest file */
			try(FileWriter fw = new FileWriter(manifPath, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)){
				
				out.print(";"+ alpha); /* save alpha in the manifest file */
				out.close();
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* Compute the distance map and save files for each segment of reference */
		for(Map.Entry<Integer, SegReference> entry : listOfSegs.entrySet()){

			SegReference s = entry.getValue();
			s.muMap = DistanceMap.compute(s, alpha, gtPath);
			if(generateCrops) {
				
				String gtVisuPath = gtPath +"//gt-visu";
				new File(gtVisuPath).mkdirs();
				ImTool.save(s.toBufferedImage(), gtVisuPath +"//s-"+ s.index +".png");
			}
		}

		int nbRegions = inputLabelMatrix.getNbRegions();
		Log.println("Segment of reference", "number of segments including the background segments : "+ nbRegions);
		Log.println("Segment of reference", "number of semantic labels : "+ semanticLabels.size());

		return listOfSegs;
	}

	/**
	 * Get the set of pixels by considering their location in the bounding box.
	 * @return List of Point.
	 */
	public ArrayList<Point> getBbIntegerPoints(){

		return this.bbPoints;
	}

	/**
	 * Get the set of pixels by considering their location in the GT image.
	 * @return List of Point.
	 */
	public ArrayList<Point> getPoints(){

		return this.gtPoints;
	}

	/**
	 * From a saved file, get all segments. <br>
	 * The alpha considered for the mu maps is the first alpha mentioned in the manifest file. <br>
	 * @param path Folder containing save files. <br>
	 * @param visu if yes, show segments in windows. <br>
	 * @return a list of segments.
	 */
	public static TreeMap<Integer, SegReference> getSegReferences(String path, double alpha, boolean visu) {

		/* Set containing list of segments of reference */
		TreeMap<Integer, SegReference> listOfSegs = new TreeMap<Integer, SegReference>();

		/* get important information for the manifest  */
		try {

			/* read the manifest file and start to get information */
			String manifPath = path +"//manifest";
			BufferedReader br = new BufferedReader(new FileReader(manifPath));
			String line = br.readLine(); /* got the number of segments */
			String lineElems[];
			String sep = ";";
			int nbSegs = Integer.parseInt(line);

			line = br.readLine();/* got the list of labels <lab1;lab2;...> */
			lineElems = line.split(sep);
			TreeMap<Integer, Double[]> semanticLabels = new TreeMap<Integer, Double[]>();
			for(int i = 0; i < lineElems.length; ++i){

				Double[] val = new Double[4];
				val[0] = 0.0;
				val[1] = 0.0;
				val[2] = 0.0;
				val[3] = 0.0;
				semanticLabels.put(Integer.parseInt(lineElems[i]), val);

			}

			line = br.readLine(); /* got the alphas */
			String alphas[] = line.split(sep);

			/* parse each files corresponding to each segment */
			for(int ns = 1; ns <= nbSegs; ++ns){

				/* seg info */
				String sPath = path +"//s-"+ ns;
				BufferedReader segBr = new BufferedReader(new FileReader(sPath));
				line = segBr.readLine(); /* got <index;semantic_label> */
				lineElems = line.split(sep);
				int index = Integer.parseInt(lineElems[0]);
				int semanticLabel = Integer.parseInt(lineElems[1]); 

				line = segBr.readLine(); /* got <imgGtPath;imgGTWidth;imgGTHeight> */
				lineElems = line.split(sep);
				String gtImgPath = lineElems[0];
				int gtWidth = Integer.parseInt(lineElems[1]);
				int gtHeight = Integer.parseInt(lineElems[2]);

				line = segBr.readLine(); /* got the real bounding box values <minX;maxX;minY;maxY> */
				lineElems = line.split(sep);
				int minX = Integer.parseInt(lineElems[0]);
				int maxX = Integer.parseInt(lineElems[1]);
				int minY = Integer.parseInt(lineElems[2]);
				int maxY = Integer.parseInt(lineElems[3]);

				line = segBr.readLine(); /* got the real int <bgcolor> */
				int bgColor = Integer.parseInt(line);

				/* create the base of the segment */
				SegReference s = new SegReference(semanticLabel, gtWidth, gtHeight, gtImgPath, bgColor, alpha);
				s.index = index; /* just in case */
				s.bgColor = bgColor;

				/* fill the crop bounding box information */
				/* Remember the crop bounding box */
				if(alpha >= 0){ /* use margin */

					int minXMargin = minX - s.margin;
					int maxXMargin = maxX + s.margin;
					int minYMargin = minY - s.margin;
					int maxYMargin = maxY + s.margin;

					if(minXMargin >= 0) s.boundingBox[0] = minXMargin;
					else s.boundingBox[0] = 0;
					if(maxXMargin <= s.imgGtWidth) s.boundingBox[1] = maxXMargin;
					else s.boundingBox[1] = s.imgGtWidth;
					if(minYMargin >= 0) s.boundingBox[2] = minYMargin;
					else s.boundingBox[2] = 0;
					if(maxYMargin <= s.imgGtHeight) s.boundingBox[3] = maxYMargin;
					else s.boundingBox[3] = s.imgGtHeight;

				}else{

					s.boundingBox[0] = minX;
					s.boundingBox[1] = maxX;
					s.boundingBox[2] = minY;
					s.boundingBox[3] = maxY;

				}

				/* Compute the width and the height of the bounding box */
				s.bbWidth = s.boundingBox[1] - s.boundingBox[0] + 1;
				s.bbHeight = s.boundingBox[3] - s.boundingBox[2] + 1;
				if(s.bbWidth <= 0) s.bbWidth = 1;
				if(s.bbHeight <= 0) s.bbHeight = 1;

				/* fill the gt points */
				line = segBr.readLine(); /* got the <nbPoints> */
				int nbPoints = Integer.parseInt(line);
				for(int np = 0; np < nbPoints; ++np){

					line = segBr.readLine(); /* got the gt point value */
					String splited[] = line.split("_");
					int x = Integer.parseInt(splited[0]);
					int y = Integer.parseInt(splited[1]);
					Point p = new Point(x, y);
					s.gtPoints.add(p);
				}

				/* fill the bb points */
				s.gtToBB();

				/* add the segment in the list */
				listOfSegs.put(ns, s);

				/*
				 * In case of soft evaluation.
				 */
				/* check if the alpha exists in the manifest file */
				if(alpha >=0 ){

					int t = 0; // comme trouver
					boolean found = false;
					while(!found && t < alphas.length){

						if(Double.parseDouble(alphas[t]) == alpha){
							found = true;
						}
						t++;

					}


					if(!found){ /* compute an new mu map if the specified alpha is not found */

						s.muMap = DistanceMap.compute(s, alpha, false, path);
						s.margin = (int)(Math.log((1 / 0.001) - 1) / alpha); /* en px */

						/* add the current alpha in the manifest file */
						try(FileWriter fw = new FileWriter(manifPath, true);
								BufferedWriter bw = new BufferedWriter(fw);
								PrintWriter out = new PrintWriter(bw))
						{
							out.print(";"+ alpha); /* save alpha in the manifest file */
							out.close();
							bw.close();
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}else{

						String distancePath = path +"//dist-"+ s.index +"-"+ alpha +".csv";
						String muPath = path +"//mu-"+ s.index +"-"+ alpha +".csv";
						BufferedReader dBr = new BufferedReader(new FileReader(distancePath));
						BufferedReader mBr = new BufferedReader(new FileReader(muPath));
						String line2, lineElems2[];
						s.muMap = new double[s.bbWidth][s.bbHeight][2];
						for(int h=0; h<s.bbHeight; ++h){
							line = dBr.readLine(); /* got one row from the distance file */
							lineElems = line.split(sep);
							line2 = mBr.readLine(); /* got one row from the mu file */
							lineElems2 = line2.split(sep);
							for(int w=0; w<s.bbWidth; ++w){

								s.muMap[w][h][0] = Double.parseDouble(lineElems[w]); /* set the distance value */
								s.muMap[w][h][1] = Double.parseDouble(lineElems2[w]); /* set the mu value */

							}
						}
						dBr.close();
						mBr.close();

					}

				}

				/* visualize the crop */
				if(visu){
					/* generate the segment crop image */
					int white = 255;
					int black = 0;
					LabelMatrix crop = new LabelMatrix(s.bbWidth, s.bbHeight);
					for (int j = 0; j < s.bbHeight; ++j){
						for(int i = 0; i < s.bbWidth; ++i){

							Point tmpPoint = new Point(i, j);
							if(s.getBbIntegerPoints().contains(tmpPoint)) {
								crop.setLabel(white, i, j);
							}
							else{
								crop.setLabel(black, i, j);
							}

						}
					}

					/* show images in windows */
					BufferedImage cropImg = ImTool.generateRegions(crop, null);
					ImTool.show(cropImg, 50);

				}

				segBr.close();
			}

			/* Use the first segment to store the information about the semantic labels */
			listOfSegs.firstEntry().getValue().listOfAllSemanticLabels = semanticLabels;

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return listOfSegs;
	}

	/**
	 * TODO obsolete
	 * From an image GT, get each segments.
	 * @param gtImgPath : Path of the image GT having white background. 
	 * @param bgColor : background color of the GT image.
	 * @param alpha : >0 allow controlling the degree of uncertainty.
	 * @param computeDistanceMap : if true, compute the matrix of distance map and containing also the membership function.
	 * @param showMaps : if true, show the distance and the mu maps.
	 * @param save : saving map images and the segments in a file.
	 * @return
	 */
	public static TreeMap<Integer, SegReference> getSegReferences(String gtImgPath, int bgColor, double alpha, boolean computeDistanceMap, boolean showMaps, boolean save){

		/* Load the image */
		BufferedImage gtImg = ImTool.read(gtImgPath);

		/* GT image width and height */
		int gtWidth = gtImg.getWidth();
		int gtHeight = gtImg.getHeight();

		/* set the background color value */
		int bgC = bgColor;

		/* Set containing list of segments of reference */
		TreeMap<Integer, SegReference> listOfSegs = new TreeMap<Integer, SegReference>();

		/* Get all the segments contained in the GT image including its background */
		SegmentByConnexityRaw sbc = new SegmentByConnexityRaw(gtImg);
		LabelMatrix inputLabelMatrix = sbc.runForFullImage();
		//inputLabelMatrix.print();

		/* get all segments and drop the background */
		TreeMap<Integer, Double[]> semanticLabels = new TreeMap<Integer, Double[]>();
		for(int y=0; y < gtHeight; ++y){
			for(int x=0; x < gtWidth; ++x){

				int segLabNonSem = inputLabelMatrix.getLabel(x, y); 

				/* drop the background */
				int rgbVal = gtImg.getRGB(x, y);
				Color myColor = new Color(rgbVal);
				rgbVal = ImTool.getRGBValueFrom(myColor.getRed(), myColor.getGreen(), myColor.getBlue());
				
				System.out.println("\n\n rgbval: "+ rgbVal +", bgC: "+ bgC +"\n\n");
				if(rgbVal != bgC){

					if(!listOfSegs.containsKey(segLabNonSem)){

						/* create a segment of reference object, add it to the list with its semantic label */
						int semanticLabel = rgbVal;
						listOfSegs.put(segLabNonSem, new SegReference(semanticLabel, gtWidth, gtHeight, gtImgPath, bgColor, alpha));
						if(!semanticLabels.containsKey(semanticLabel)){
							Double[] val = new Double[4];
							val[0] = 0.0;
							val[1] = 0.0;
							val[2] = 0.0;
							val[3] = 0.0;
							semanticLabels.put(semanticLabel, val);
						}

					}	

					/* store the point ~ pixel in the segment of reference object */ 
					listOfSegs.get(segLabNonSem).addGtPoint(new Point(x, y));	

				}

			}
		}

		/* Use the first segment to store the information about the semantic labels */
		listOfSegs.firstEntry().getValue().listOfAllSemanticLabels = semanticLabels;

		/* preparing a path and a manifest file */
		String path = null;
		if(save){

			/* set a correct path */
			String folderPath[] = gtImgPath.split("//");
			int lastIndex = folderPath.length - 1;
			String gtImgName = folderPath[lastIndex];
			String fpath[] = gtImgPath.split(gtImgName);
			path = fpath[0] +"segref_("+ gtImgName +")";

			/* save a manifest file if it's not already existing */
			String manifPath = path +"//manifest";
			File mf = new File(manifPath);
			if(!mf.exists() || mf.isDirectory()){ /* create a file and register the number of segments and the 1st alpha used */

				try {

					/* global info on the list of segments */
					new File(path).mkdir();
					PrintWriter manifWriter = new PrintWriter(manifPath, "UTF-8");
					manifWriter.println(listOfSegs.size()); /* save <nb_segments> */
					int it = 0;
					for(Entry<Integer, Double[]> entry: semanticLabels.entrySet()){
						manifWriter.print(entry.getKey());
						if(it < semanticLabels.size() - 1)
							manifWriter.print(";");
						it++;
					}
					manifWriter.println();
					manifWriter.print(alpha);
					manifWriter.close();

				} catch (Exception e){
					e.printStackTrace();
				}

			}else{

				/* add the current alpha in the manifest file */
				try(FileWriter fw = new FileWriter(manifPath, true);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw))
				{
					out.print(";"+ alpha); /* save alpha in the manifest file */
					out.close();
					bw.close();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
		
		boolean visu = true;
		/* visualize the crop */
		if(visu){
			
			BufferedImage imgWithBBRect = ImTool.clone(gtImg);
			for(Map.Entry<Integer, SegReference> entry : listOfSegs.entrySet()){

				SegReference s = entry.getValue();
				System.out.println(s.index);
				int realBoundingBox[] = s.computeRegionBoundingBox();
				
				/* generate the segment crop image */
				int white = 1;
				int black = 0;
				LabelMatrix crop = new LabelMatrix(s.bbWidth, s.bbHeight);
				System.out.println("bbWidth: "+ s.bbWidth +" bbHeight: "+ s.bbHeight);
				for (int j = 0; j < s.bbHeight; ++j){
					for(int i = 0; i < s.bbWidth; ++i){

						Point tmpPoint = new Point(i, j);
						if(s.getBbIntegerPoints().contains(tmpPoint)) {
							crop.setLabel(white, i, j);
						}
						else{
							crop.setLabel(black, i, j);
						}

					}
				}

				/* show images in windows */
				HashMap<Integer, Color> lut = new HashMap<Integer, Color>();
				lut.put(0, Color.black);
				lut.put(1, Color.white);
				BufferedImage cropImg = ImTool.generateRegions(crop, lut);
				ImTool.show(cropImg, 30);

				
				/* draw bounding boxes */
				Random rand = new Random();
				int r = rand.nextInt();
				int g = rand.nextInt();
				int b = rand.nextInt();
				int color = ImTool.getRGBValueFrom(r, g, b);
				ImTool.drawRectangleOn(imgWithBBRect, 
									   realBoundingBox[0],
									   realBoundingBox[1],
									   realBoundingBox[2],
									   realBoundingBox[3],
									   color);
			}
			ImTool.show(imgWithBBRect, 30);
			
		}

		/* Compute or not the distance map */
		if(computeDistanceMap)
			for(Map.Entry<Integer, SegReference> entry : listOfSegs.entrySet()){

				SegReference s = entry.getValue();
				s.muMap = DistanceMap.compute(s, alpha, showMaps, path);

			}

		int nbRegions = inputLabelMatrix.getNbRegions();
		Log.println("Segment of reference", "number of segments including the background segments : "+ nbRegions);
		Log.println("Segment of reference", "number of semantic labels : "+ semanticLabels.size());

		return listOfSegs;

	}

	/**
	 * Convert an x coordinate of the bounding box to the real x coordinate of the input gt image.
	 * @param x coordinate in the bounding box.
	 * @return X real coordinate in the gt input image.
	 */
	public int getSegRealX(int x) {

		return this.boundingBox[0] + x;

	}

	/**
	 * Convert an y coordinate of the bounding box to the real y coordinate of the input gt image.
	 * @param y coordinate in the bounding box.
	 * @return y real coordinate in the gt input image.
	 */
	public int getSegRealY(int y) {

		return this.boundingBox[2] + y;

	}

	/**
	 * Convert the value of each point from the GT image to the bounding box;
	 */
	public void gtToBB(){

		int xdecal = this.boundingBox[0];
		int ydecal = this.boundingBox[2];

		this.bbPoints = new ArrayList<Point>();
		for(Point pixel : this.gtPoints){

			int xGt = pixel.x; // X POSITION OF THE POINT IN THE GT IMAGE.
			int yGt = pixel.y; // Y POSITION OF THE POINT IN THE GT IMAGE.
			Point newPixel = new Point((xGt - xdecal), (yGt - ydecal));
			this.bbPoints.add(newPixel);
		}
	}

	

	//	/**
	//	 * Initialize the background color picked from the GT image.
	//	 * @param bgColor
	//	 */
	//	private void initBgColor(BgColor bgColor) {
	//
	//		switch(bgColor){
	//			case WHITE:
	//				this.bgColor = 255;
	//				break;
	//			case BLACK:
	//				this.bgColor = 0;
	//				break;
	//			default :
	//				this.bgColor = 0;
	//				break;
	//		}
	//		
	//	}


	//	/**
	//	 * Possible background color of the GT image.
	//	 *
	//	 */
	//	public enum BgColor{
	//		WHITE,
	//		BLACK
	//	}

	/**
	 * Retain the best matching node 'n' and the associated similarity score lambda(S_i).
	 * @param n
	 * @param lambda
	 */
	public void setBestMatchedNode(Node n, double lambda){

		this.bestN = n;
		this.bestSimilarityScore = lambda;
	}

	/**
	 * Create an image representing the segment and show it in a window.
	 * @param percent defines how the image covers the screen
	 * 
	 * Example of values are:
	 * <li> {@link ImFrame#IMAGE_REAL_SIZE }
	 * <li> {@link ImFrame#IMAGE_DEFAULT_SIZE }
	 */
	public void show(int percent) {
		
		ImTool.show(this.toBufferedImage(), percent, "S-"+ this.index);
	}

	public static BufferedImage showBoundingBoxes(TreeMap<Integer, SegReference> segReferences, BufferedImage image, int percent, String title) {
	
		BufferedImage imgWithBBRect = ImTool.clone(image);
		for(Map.Entry<Integer, SegReference> entry : segReferences.entrySet()){
	
			SegReference s = entry.getValue();
			s.show(percent);
	
			/* draw bounding boxes */
			Random rand = new Random();
			int r = rand.nextInt();
			int g = rand.nextInt();
			int b = rand.nextInt();
			int color = ImTool.getRGBValueFrom(r, g, b);
			ImTool.drawRectangleOn(imgWithBBRect, 
					s.realBoundingBox[0],
					s.realBoundingBox[1],
					s.realBoundingBox[2],
					s.realBoundingBox[3],
					color);
		}
		ImTool.show(imgWithBBRect, percent, title);
		
		return imgWithBBRect;
	}

	public int size(){
	
		return this.gtPoints.size();
	}

	public BufferedImage toBufferedImage() {
	
		this.computeRegionBoundingBox();
		
		/* Generate the segment crop image */
		int white = 1;
		int black = 0;
		LabelMatrix crop = new LabelMatrix(this.bbWidth, this.bbHeight);
		for (int j = 0; j < this.bbHeight; ++j){
			for(int i = 0; i < this.bbWidth; ++i){
	
				Point tmpPoint = new Point(i, j);
				if(this.getBbIntegerPoints().contains(tmpPoint)) {
					crop.setLabel(white, i, j);
				}
				else{
					crop.setLabel(black, i, j);
				}
			}
		}
	
		/* Generate a buffered image from the label matrix */
		HashMap<Integer, Color> lut = new HashMap<Integer, Color>();
		lut.put(white, Color.white);		
		lut.put(black, Color.black);
		
		return ImTool.generateRegions(crop, lut);
	}

	/**
	 * From a saved file, get all segments. <br>
	 * @param path Folder containing the saved files.
	 * @param alpha
	 * @param visu
	 * @return
	 */
	//	public static TreeMap<Integer, SegReference> getSegReferences(String path, double alpha, boolean visu) {
	//				
	//		/* Set containing list of segments of reference */
	//		TreeMap<Integer, SegReference> listOfSegs = SegReference.getSegReferences(path, visu);
	//		
	//		/* fill the distance map and the mu map */
	//		String line, lineElems[];
	//		String sep = ";";
	//		try{
	//			
	//			for(Entry<Integer,SegReference> entry: listOfSegs.entrySet()){
	//
	//				SegReference s = entry.getValue();
	//
	//				String distancePath = path +"//dist-"+ s.index +"-"+ alpha +".csv";
	//				String muPath = path +"//mu-"+ s.index +"-"+ alpha +".csv";
	//				BufferedReader dBr = new BufferedReader(new FileReader(distancePath));
	//				BufferedReader mBr = new BufferedReader(new FileReader(muPath));
	//				String line2, lineElems2[];
	//				s.muMap = new double[s.bbWidth][s.bbHeight][2];
	//				for(int h=0; h<s.bbHeight; ++h){
	//					line = dBr.readLine(); /* got one row from the distance file */
	//					lineElems = line.split(sep);
	//					line2 = mBr.readLine(); /* got one row from the mu file */
	//					lineElems2 = line2.split(sep);
	//					for(int w=0; w<s.bbWidth; ++w){
	//
	//						s.muMap[w][h][0] = Double.parseDouble(lineElems[w]); /* set the distance value */
	//						s.muMap[w][h][1] = Double.parseDouble(lineElems2[w]); /* set the mu value */
	//
	//					}
	//				}
	//				dBr.close();
	//				mBr.close();
	//
	//			}
	//			
	//		}catch(Exception e){
	//			e.printStackTrace();
	//		}
	//		
	//		/* verification if the mu map for the specified alpha is not already computed and stored in a file */
	//		String manifPath = path +"//manifest";
	//		try {
	//
	//			BufferedReader br = new BufferedReader(new FileReader(manifPath));
	//			line = br.readLine(); /* got the <nb_segs> */
	//			line = br.readLine(); /* got the <lab1;lab2;...> */
	//			
	//			line = br.readLine(); /* got the list of alphas */
	//			lineElems = line.split(";");
	//			boolean found = false;
	//			int i = 0;
	//			while(!found && i < lineElems.length){
	//				
	//				if(Double.parseDouble(lineElems[i]) == alpha){
	//					found = true;
	//				}
	//				i++;
	//				
	//			}
	//
	//			br.close();
	//
	//			if(!found){ /* compute an new mu map if the specified alpha is not found */
	//
	//				for(Map.Entry<Integer, SegReference> entry : listOfSegs.entrySet()){
	//	
	//					SegReference s = entry.getValue();
	//					s.muMap = DistanceMap.compute(s, alpha, false, path);
	//					s.margin = (int)(Math.log((1 / 0.001) - 1) / alpha); /* en px */
	//	
	//				}
	//				
	//				/* add the current alpha in the manifest file */
	//				try(FileWriter fw = new FileWriter(manifPath, true);
	//						BufferedWriter bw = new BufferedWriter(fw);
	//						PrintWriter out = new PrintWriter(bw))
	//				{
	//					out.print(";"+ alpha); /* save alpha in the manifest file */
	//					out.close();
	//					bw.close();
	//					fw.close();
	//				} catch (IOException e) {
	//					e.printStackTrace();
	//				}
	//				
	//			}
	//			
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		
	//		
	//		return listOfSegs;
	//	}

}
