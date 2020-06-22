package evaluation.datastructure;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

import utils.ImTool;
import utils.LabelMatrix;

/**
 * 
 * Distance map for segment of references.
 *
 */
public class DistanceMap {
	
	/**
	 * Generating a crop containing only a segment 's' and then compute from it a distance map.
	 * @param s : the segments of interest.
	 * @param alpha : >0 allow controlling the degree of uncertainty.
	 * @param savePath : it not null, this is the folder path that will contain the saved distance map and mu_map images.
	 * @return : a matrix containing the real valor of 'sigma_s(x)' i.e. < 0 for x in the segment, 0 on the border of the segment and > 0 outside the segment.
	 * The result contains also the value of 'Mu_alpha'.
	 */
	public static double[][][] compute(SegReference s, double alpha, String savePath){
		
		/*
		 * Compute the bounding box and get the real boundingBox values.
		 */
		int realBoundingBox[] = s.computeRegionBoundingBox();	
		
		/* Bounding box width and height */
		int w = s.bbWidth;
		int h = s.bbHeight;
		
		/* Prepare the resulting matrix */
		double[][][] distanceMap = new double[s.bbWidth][s.bbHeight][2];
		
		/*
		 * Init the output containing a distance transform.
		 */
		LabelMatrix inOutMap = new LabelMatrix(w, h);
		LabelMatrix outMap = new LabelMatrix(w, h);
		
		/*
		 * Prepare some supports.
		 */
		int black = 0;
		int white = 255;
		for (int j = 0; j < h; ++j){
			for(int i = 0; i < w; ++i){
			
				Point tmpPoint = new Point(i, j);
				if(s.getBbIntegerPoints().contains(tmpPoint)) { /* foreground */
					inOutMap.setLabel(white, i, j);
					outMap.setLabel(black, i, j);
				}
				else{
					inOutMap.setLabel(black, i, j); /* background */
					outMap.setLabel(white, i, j);
				}
				
			}
		}
		
		/*
		 * Compute the intra-distance map.
		 */
		for(int y = 1; y <h-1; ++y){
			for(int x = 1; x < w-1; ++x){

				if(inOutMap.getLabel(x, y)!=black){
					for(int y1 = 0; y1 < h; ++y1){
						for(int x1 = 0; x1 < w; ++x1){

							/* Intern distance computation */
							if(inOutMap.getLabel(x1, y1)==black){

								/* Discrete distance for the inside */ 
								int d = (Math.abs(x-x1) + Math.abs(y-y1));

								if(d < inOutMap.getLabel(x, y)){
									
									inOutMap.setLabel(d, x, y);
									
									/* the 'sigma_s(x)' is negative when 'x' is inside the segment */
									distanceMap[x][y][0] = d * (-1);
									
									/* compute the membership function 'Mu_alpha' */
									distanceMap[x][y][1] = 1 / (1 + Math.exp(alpha * distanceMap[x][y][0]));
									
								}

							}

						}
					}
				}
				
			}
		}
		
		/*
		 * Compute the extra-distance map.
		 */
		for(int y = 1; y <h-1; ++y){
			for(int x = 1; x < w-1; ++x){

				if(outMap.getLabel(x, y)!=black){
					for(int y1 = 0; y1 < h; ++y1){
						for(int x1 = 0; x1 < w; ++x1){

							/* Extern distance computation */
							if(outMap.getLabel(x1, y1)==black){
								
								/* Discrete distance for the outside */
								int d = Math.abs(x-x1) + Math.abs(y-y1);

								if(d < outMap.getLabel(x, y)){
									outMap.setLabel(d, x, y);
									inOutMap.setLabel(d, x, y);
									
									/* store the value of 'sigma_s(x)' when 'x' is outside the segment 's'. */
									distanceMap[x][y][0] = d;
									
									/* compute the membership function 'Mu_alpha' */
									distanceMap[x][y][1] = 1 / (1 + Math.exp(alpha * distanceMap[x][y][0]));
									
								}
								
							}

						}
					}
				}	
				
			}
		}

		
		/* view result in windows */
		if(savePath != null){
			
			/* generate the supposed input image */
			LabelMatrix crop = new LabelMatrix(w, h); /* crop from the gt image */
//			TiffImage img = new TiffImage(s.imgGtPath); /* load the image */
//			TiffImage imgCrop = new TiffImage(w, h, img.nbBands); /* crop from the real image */
			for (int j = 0; j < h; ++j){
				for(int i = 0; i < w; ++i){
				
					Point tmpPoint = new Point(i, j);
					if(s.getBbIntegerPoints().contains(tmpPoint)) { /* paint the object */

						crop.setLabel(white, i, j); /* paint the gt crop */
						
//						for(int b=0; b<imgCrop.nbBands; ++b){ /* paint the real crop image */
//							
//							int x = s.getSegRealX(i);
//							int y = s.getSegRealY(j);
//							imgCrop.setPixel(i, j, b, img.getPixel(x, y, b));
//							
//						}
						
					}
					else{ /* paint the background */
						crop.setLabel(black, i, j);
					}
					
				}
			}
			//crop.print();
			//System.out.println(crop.getWidth() +"x"+ crop.getHeight());
			
			/* generate an image for the membership values */
			LabelMatrix muImg = new LabelMatrix(w, h);
			for (int j = 0; j < h; ++j){
				for(int i = 0; i < w; ++i){
				
					muImg.setLabel((int) (distanceMap[i][j][1] * 255), i, j);
					
				}
			}
			
			/* show images in windows */
			/*if(visu){
				
				/*Viewer2D.exec(crop, "Crop for the seg-"+ s.semanticLabel);
				Viewer2D.exec(inOutMap, "In and out distance map");
				Viewer2D.exec(outMap, "Outside distance map");
				Viewer2D.exec(muImg, "Mu map");*/
				
			/*	HashMap<Integer, Color> lut = new HashMap<Integer, Color>();
				lut.put(0, Color.black);
				lut.put(255, Color.white);
				BufferedImage cropImg = ImTool.generateRegions(crop, lut);
				ImTool.show(cropImg, 25);

			}*/
			
			/* saving */
			if(savePath != null){
				
				/* saving images for each class */
//				String pathClass = savePath +"//Class-"+ s.semanticLabel;
//				File sf = new File(pathClass);
//				if(!sf.exists())
//					new File(pathClass).mkdir();
//				ImageSave.exec(new ByteImage(crop), pathClass +"//gt_seg-"+ s.index +".png");
//				ImageSave.exec(new ByteImage(TiffConstructVisuImage.exec(imgCrop.getPiafImage(), true, false)), pathClass +"//img_seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(crop), savePath +"//seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(inOutMap), savePath +"//dist-seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(muImg), savePath +"//mu-"+ s.index +"-"+ alpha +".png");
				
				/* Saving distance crop images */
				String gtDistVisuPath = savePath +"//gt-dist";
				new File(gtDistVisuPath).mkdirs();
				BufferedImage distCropImg = ImTool.generateRegions(inOutMap, ImTool.getGrayScaleLUT());
				ImTool.equalize(distCropImg);
				ImTool.save(distCropImg, gtDistVisuPath +"//dist-"+ s.index +"-"+ alpha +".png");

				/* Saving mu crop images */
				String gtMuVisuPath = savePath +"//gt-mu";
				new File(gtMuVisuPath).mkdirs();
				BufferedImage distMuImg = ImTool.generateRegions(muImg, ImTool.getGrayScaleLUT());
				ImTool.equalize(distMuImg);
				ImTool.save(distMuImg, gtMuVisuPath +"//mu-"+ s.index +"-"+ alpha +".png");

				/* save segment's values is some files */
				try {
					
					String segSavePath = savePath + "//s-"+ s.index; 
					File f = new File(segSavePath);
					if(!f.exists() || f.isDirectory()) { 

						/* for the segment itself */
						PrintWriter segWriter = new PrintWriter(segSavePath, "UTF-8");
						segWriter.println(s.index +";"+ s.semanticLabel); /* save <index;semantic_label>  */
						segWriter.println(s.imgGtPath +";"+ s.imgGtWidth +";"+ s.imgGtHeight); /* save <imgGtPath;imgGtWidth;imgGtHeight> */
//						segWriter.println(s.boundingBox[0] +";"+ s.boundingBox[1] +";"+ s.boundingBox[2] +";"+ s.boundingBox[3]); /* save <minX;maxX;minY;maxY> for the bounding box */
						segWriter.println(realBoundingBox[0] +";"+ realBoundingBox[1] +";"+ realBoundingBox[2] +";"+ realBoundingBox[3]); /* save the real bounding box values <minX;maxX;minY;maxY> */
//						segWriter.println(s.bbWidth +";"+ s.bbHeight +";"+ s.margin); /* save <bbWidth;bbHeight;margin> */ // no need
						segWriter.println(s.bgColor); /* save <bgcolor> */
						segWriter.println(s.gtPoints.size()); /* nb points */

						/* saving gt points */
						for(Point point: s.gtPoints){
							
							segWriter.println(point.x+"_"+point.y);
							
						}
						
//						/* saving bb points */
//						for(Integer point: s.bbPoints){
//							
//							segWriter.println(point);
//							
//						}

						segWriter.close();

					}
										
					/* for distance map and mu values */
					PrintWriter distWriter = new PrintWriter(savePath +"//dist-"+ s.index+"-"+ alpha +".csv", "UTF-8"); /* file saving distance values */
					PrintWriter muWriter = new PrintWriter(savePath +"//mu-"+ s.index +"-"+ alpha +".csv", "UTF-8"); /* file saving mu values */
					for(int j=0; j<distanceMap[0].length; ++j){
						for(int i=0; i<distanceMap.length; ++i){
							
							/* distance map values */
							distWriter.print(distanceMap[i][j][0]);
							if(i < distanceMap.length - 1) distWriter.print(";");
							
							/* mu values */
							muWriter.print(distanceMap[i][j][1]);
							if(i < distanceMap.length - 1) muWriter.print(";");
							
						}
						distWriter.println();
						muWriter.println();
					}
					distWriter.close();
					muWriter.close();
										
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				System.out.println("s-"+ s.index +" files saved in \""+ savePath +"\"");
				
			}
		
//			img = null;
		}
				 
		return distanceMap;
		
	}
	
	/**
	 * Generating a crop containing only a segment 's' and then compute from it a distance map.
	 * @param s : the segments of interest.
	 * @param alpha : >0 allow controlling the degree of uncertainty.
	 * @param visu : if true, show the result in windows.
	 * @param savePath : it not null, this is the folder path that will contain the saved distance map and mu_map images.
	 * @return : a matrix containing the real valor of 'sigma_s(x)' i.e. < 0 for x in the segment, 0 on the border of the segment and > 0 outside the segment.
	 * The result contains also the value of 'Mu_alpha'.
	 */
	public static double[][][] compute(SegReference s, double alpha, boolean visu, String savePath){
		
		/*
		 * Compute the bounding box and get the real boundingBox values.
		 */
		int realBoundingBox[] = s.computeRegionBoundingBox();	
		System.out.println(realBoundingBox[0] +", "+
		                   realBoundingBox[1] +", "+
		                   realBoundingBox[2] +", "+
		                   realBoundingBox[3]);
		
		/* Bounding box width and height */
		int w = s.bbWidth;
		int h = s.bbHeight;
		
		/* Prepare the resulting matrix */
		double[][][] distanceMap = new double[s.bbWidth][s.bbHeight][2];
		
		/*
		 * Init the output containing a distance transform.
		 */
		LabelMatrix inOutMap = new LabelMatrix(w, h);
		LabelMatrix outMap = new LabelMatrix(w, h);
		
		/*
		 * Prepare some supports.
		 */
		int black = 0;
		int white = 255;
		for (int j = 0; j < h; ++j){
			for(int i = 0; i < w; ++i){
			
				Point tmpPoint = new Point(i, j);
				if(s.getBbIntegerPoints().contains(tmpPoint)) { /* foreground */
					inOutMap.setLabel(white, i, j);
					outMap.setLabel(black, i, j);
				}
				else{
					inOutMap.setLabel(black, i, j); /* background */
					outMap.setLabel(white, i, j);
				}
				
			}
		}
		
		/*
		 * Compute the intra-distance map.
		 */
		for(int y = 1; y <h-1; ++y){
			for(int x = 1; x < w-1; ++x){

				if(inOutMap.getLabel(x, y)!=black){
					for(int y1 = 0; y1 < h; ++y1){
						for(int x1 = 0; x1 < w; ++x1){

							/* Intern distance computation */
							if(inOutMap.getLabel(x1, y1)==black){

								/* Discrete distance for the inside */ 
								int d = (Math.abs(x-x1) + Math.abs(y-y1));

								if(d < inOutMap.getLabel(x, y)){
									
									inOutMap.setLabel(d, x, y);
									
									/* the 'sigma_s(x)' is negative when 'x' is inside the segment */
									distanceMap[x][y][0] = d * (-1);
									
									/* compute the membership function 'Mu_alpha' */
									distanceMap[x][y][1] = 1 / (1 + Math.exp(alpha * distanceMap[x][y][0]));
									
								}

							}

						}
					}
				}
				
			}
		}
		
		/*
		 * Compute the extra-distance map.
		 */
		for(int y = 1; y <h-1; ++y){
			for(int x = 1; x < w-1; ++x){

				if(outMap.getLabel(x, y)!=black){
					for(int y1 = 0; y1 < h; ++y1){
						for(int x1 = 0; x1 < w; ++x1){

							/* Extern distance computation */
							if(outMap.getLabel(x1, y1)==black){
								
								/* Discrete distance for the outside */
								int d = Math.abs(x-x1) + Math.abs(y-y1);

								if(d < outMap.getLabel(x, y)){
									outMap.setLabel(d, x, y);
									inOutMap.setLabel(d, x, y);
									
									/* store the value of 'sigma_s(x)' when 'x' is outside the segment 's'. */
									distanceMap[x][y][0] = d;
									
									/* compute the membership function 'Mu_alpha' */
									distanceMap[x][y][1] = 1 / (1 + Math.exp(alpha * distanceMap[x][y][0]));
									
								}
								
							}

						}
					}
				}	
				
			}
		}

		
		/* view result in windows */
		if(visu || savePath != null){
			
			/* generate the supposed input image */
			LabelMatrix crop = new LabelMatrix(w, h); /* crop from the gt image */
//			TiffImage img = new TiffImage(s.imgGtPath); /* load the image */
//			TiffImage imgCrop = new TiffImage(w, h, img.nbBands); /* crop from the real image */
			for (int j = 0; j < h; ++j){
				for(int i = 0; i < w; ++i){
				
					Point tmpPoint = new Point(i, j);
					if(s.getBbIntegerPoints().contains(tmpPoint)) { /* paint the object */

						crop.setLabel(white, i, j); /* paint the gt crop */
						
//						for(int b=0; b<imgCrop.nbBands; ++b){ /* paint the real crop image */
//							
//							int x = s.getSegRealX(i);
//							int y = s.getSegRealY(j);
//							imgCrop.setPixel(i, j, b, img.getPixel(x, y, b));
//							
//						}
						
					}
					else{ /* paint the background */
						crop.setLabel(black, i, j);
					}
					
				}
			}
			//crop.print();
			//System.out.println(crop.getWidth() +"x"+ crop.getHeight());
			
			/* generate an image for the membership values */
			LabelMatrix muImg = new LabelMatrix(w, h);
			for (int j = 0; j < h; ++j){
				for(int i = 0; i < w; ++i){
				
					muImg.setLabel((int) (distanceMap[i][j][1] * 255), i, j);
					
				}
			}
			
			/* show images in windows */
			if(visu){
				
				/*Viewer2D.exec(crop, "Crop for the seg-"+ s.semanticLabel);
				Viewer2D.exec(inOutMap, "In and out distance map");
				Viewer2D.exec(outMap, "Outside distance map");
				Viewer2D.exec(muImg, "Mu map");*/
				
				HashMap<Integer, Color> lut = new HashMap<Integer, Color>();
				lut.put(0, Color.black);
				lut.put(255, Color.white);
				BufferedImage cropImg = ImTool.generateRegions(crop, lut);
				ImTool.show(cropImg, 25);

			}
			
			/* saving */
			if(savePath != null){
				
				/* saving images for each class */
//				String pathClass = savePath +"//Class-"+ s.semanticLabel;
//				File sf = new File(pathClass);
//				if(!sf.exists())
//					new File(pathClass).mkdir();
//				ImageSave.exec(new ByteImage(crop), pathClass +"//gt_seg-"+ s.index +".png");
//				ImageSave.exec(new ByteImage(TiffConstructVisuImage.exec(imgCrop.getPiafImage(), true, false)), pathClass +"//img_seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(crop), savePath +"//seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(inOutMap), savePath +"//dist-seg-"+ s.index +".png");
				//ImageSave.exec(new ByteImage(muImg), savePath +"//mu-"+ s.index +"-"+ alpha +".png");
				
				/* save segment's values is some files */
				try {
					
					String segSavePath = savePath + "//s-"+ s.index; 
					File f = new File(segSavePath);
					if(!f.exists() || f.isDirectory()) { 

						/* for the segment itself */
						PrintWriter segWriter = new PrintWriter(segSavePath, "UTF-8");
						segWriter.println(s.index +";"+ s.semanticLabel); /* save <index;semantic_label>  */
						segWriter.println(s.imgGtPath +";"+ s.imgGtWidth +";"+ s.imgGtHeight); /* save <imgGtPath;imgGtWidth;imgGtHeight> */
//						segWriter.println(s.boundingBox[0] +";"+ s.boundingBox[1] +";"+ s.boundingBox[2] +";"+ s.boundingBox[3]); /* save <minX;maxX;minY;maxY> for the bounding box */
						segWriter.println(realBoundingBox[0] +";"+ realBoundingBox[1] +";"+ realBoundingBox[2] +";"+ realBoundingBox[3]); /* save the real bounding box values <minX;maxX;minY;maxY> */
//						segWriter.println(s.bbWidth +";"+ s.bbHeight +";"+ s.margin); /* save <bbWidth;bbHeight;margin> */ // no need
						segWriter.println(s.bgColor); /* save <bgcolor> */
						segWriter.println(s.gtPoints.size()); /* nb points */

						/* saving gt points */
						for(Point point: s.gtPoints){
							
							segWriter.println(point.x +"_"+ point.y);
							
						}
						
//						/* saving bb points */
//						for(Integer point: s.bbPoints){
//							
//							segWriter.println(point);
//							
//						}

						segWriter.close();

					}
										
					/* for distance map and mu values */
					PrintWriter distWriter = new PrintWriter(savePath +"//dist-"+ s.index+"-"+ alpha +".csv", "UTF-8"); /* file saving distance values */
					PrintWriter muWriter = new PrintWriter(savePath +"//mu-"+ s.index +"-"+ alpha +".csv", "UTF-8"); /* file saving mu values */
					for(int j=0; j<distanceMap[0].length; ++j){
						for(int i=0; i<distanceMap.length; ++i){
							
							/* distance map values */
							distWriter.print(distanceMap[i][j][0]);
							if(i < distanceMap.length - 1) distWriter.print(";");
							
							/* mu values */
							muWriter.print(distanceMap[i][j][1]);
							if(i < distanceMap.length - 1) muWriter.print(";");
							
						}
						distWriter.println();
						muWriter.println();
					}
					distWriter.close();
					muWriter.close();
										
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				System.out.println("s-"+ s.index +" files saved in \""+ savePath +"\"");
				
			}
		
//			img = null;
		}
				 
		return distanceMap;
		
	}
	
	public static void main(String args[]) {
		
//		String path = "experiments//icip-2017//tests//gt6.png";
//		DistanceMap.compute(path);
		
		String imgGtPath = "experiments//icip-2017//examples//gt.tif";
		
		double alpha = 0.9;
		int bgColor = 0; // black
		SegReference.getSegReferences(imgGtPath, bgColor, alpha, true, false, true);
	
	}

}
