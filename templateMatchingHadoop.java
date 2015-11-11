import hipi.image.FloatImage;
import hipi.image.ImageHeader;
import hipi.imagebundle.mapreduce.ImageBundleInputFormat;
import hipi.imagebundle.HipiImageBundle;
import hipi.imagebundle.AbstractImageBundle;
import hipi.image.ImageHeader.ImageType;
import hipi.image.io.JPEGImageUtil;


import java.io.File;
import java.io.FileInputStream;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class templateMatchingHadoop extends Configured implements Tool {
	// Set HDFS Default Folder
	private static final String HDFS_PATH = "/user/ubuntu";
	public static class FaceCountMapper extends Mapper<ImageHeader, FloatImage, IntWritable, Text> {
		

		// Convert HIPI FloatImage to OpenCV Mat
		public Mat convertFloatImageToOpenCVMat(FloatImage floatImage, int type) {
			int w = floatImage.getWidth();
			int h = floatImage.getHeight();

			float[] valData = floatImage.getData();

			// Initialize 3 element array to hold RGB pixel average
			double[] rgb = {0.0,0.0,0.0};

			Mat mat = new Mat(h, w, type);

			// Traverse image pixel data in raster-scan order and update running average
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					rgb[0] = (double) valData[(j*w+i)*3+0] * 255.0; // R
					rgb[1] = (double) valData[(j*w+i)*3+1] * 255.0; // G
					rgb[2] = (double) valData[(j*w+i)*3+2] * 255.0; // B
					mat.put(j, i, rgb);
				}
			}
			return mat;
		}
		
		/* First, find the car number plate */
		public String dectectCarNumber(Mat file_name){
			Mat image_src = file_name;
			Mat image = imageResize(image_src, 840);
			Mat imageBlurr = imagePreProcess(image);
			Mat imageA = imageBinary(imageBlurr);

			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

			contours = removeSmallPiece(contours, image.width(), 0.9, image.height(), 0);

			// Detect Car number plate
			Mat car_num_board = null;
			int max_contours = 0;
			for(int i=0; i< contours.size();i++){
				Rect rect = Imgproc.boundingRect(contours.get(i));

				double ratio = (double) rect.width / (double) rect.height;

				// Resize car# board for accuracy
				Mat t_num_board = new Mat(rect.height, rect.width, Core.DEPTH_MASK_ALL);
				t_num_board = imageA.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
				
				Mat num_board = imageResize(t_num_board, 600);
				if (hasChild (num_board, 6)) {
					car_num_board = image.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
					break;
				}

				List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
				Imgproc.findContours(num_board, contours2, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

				if(max_contours < contours2.size()){
					if(ratio > 1.5){ // Car board's width will larger than height
						// Car board has 6 number, so there will be more than 4 rectangle in the contours.
						int max_cnt = getSimiliarSizeRect(contours2);
						if(max_cnt >= 4){
							max_contours = contours2.size();
							car_num_board = image.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
						}
					}
				}
				num_board.release();
				t_num_board.release();
			}

			String result = "";
			if(car_num_board == null){
				//result = "ERROR_CAN_NOT_DETECT_CAR_NUMBER_BOARD";
				/* if car plate cannot be found, then just pass original image */
				result = startRecognization(image);
			} else {
				result = startRecognization(car_num_board);
			}
			image_src.release();
			imageBlurr.release();
			image.release();
			imageA.release();
			car_num_board.release();
			result = result;
			return result;
		}

		/* Start detection of number in car number plate */
		public String startRecognization(Mat car_num_board){
			String car_number = "";
			Mat image = car_num_board;
			Mat imageBlurr = imagePreProcess(image);
			Mat imageA = imageBinary(imageBlurr);

			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

			contours = removeSmallPiece(contours, image.width(), 0.5, image.height(), 0.4);

			List<Integer> test = new ArrayList<Integer>();

			/* remove children of rectangle */
			for(int i=0; i< contours.size();i++){
				Rect rect = Imgproc.boundingRect(contours.get(i));
				int rect_x1 = rect.x;
				int rect_y1 = rect.y;
				int rect_x2 = rect.x + rect.width;
				int rect_y2 = rect.y + rect.height;
				for (int j = contours.size() - 1; j >= 0; j--){
					Rect rect2 = Imgproc.boundingRect(contours.get(j));
					int rect2_x1 = rect2.x;
					int rect2_y1 = rect2.y;
					int rect2_x2 = rect2.x + rect2.width;
					int rect2_y2 = rect2.y + rect2.height;

					if((rect_x1 < rect2_x1) && (rect_x2 > rect2_x2) && (rect_y1 < rect2_y1) && (rect_y2 > rect2_y2)){
						if(!test.contains(j)) test.add(j);
					}
				}
			}

			/* All number has similar size, so if rectangle do not have similar size, then remove it */
			int s_height = getSimiliarSizeRectValue(contours, test, 0); 
			int s_width = getSimiliarSizeRectValue(contours, test, 1); 
			int min_val_w = s_width * 50 / 100;
			int max_val_w = s_width * 150 / 100;
			int min_val_h = s_height * 80 / 100;
			int max_val_h = s_height * 120 / 100;
			for(int i = 0; i < contours.size(); i++){
				if(!test.contains(i)) {
					Rect rect = Imgproc.boundingRect(contours.get(i));

					int apprx_height = rect.height/10 * 10;
					int apprx_width = rect.width / 10 * 10;

					if(max_val_h < apprx_height || min_val_h > apprx_height){
						if(!test.contains(i)) test.add(i);
					} else if (max_val_w < apprx_width || min_val_w > apprx_width) {
						if(!test.contains(i)) test.add(i);
					}
				}
			}

			/* Sorting */
			for(int i = 0; i < contours.size(); i++){
				for (int j = 0; j < contours.size(); j++){
					boolean isContinued = true;
					if(test.contains(i)) {
						isContinued = false;
					} else if(test.contains(j)) {
						isContinued = false;
					}
					if(isContinued){
						MatOfPoint p1 = contours.get(i);
						Rect rect_i = Imgproc.boundingRect(p1);
						MatOfPoint p2 = contours.get(j);
						Rect rect_j = Imgproc.boundingRect(p2);

						if(rect_i.x > rect_j.x){
							contours.remove(i);
							contours.add(i, p2);

							contours.remove(j);
							contours.add(j, p1);
						}
					}
				}
			}

			// Change number image JPEG to HIB File
			List<Mat> num_list = new ArrayList<Mat>();	
			for (int i = 0; i < 10; i++){
				try {
					FileInputStream file = new FileInputStream("./" + i + ".jpg");
					//FileInputStream file = new FileInputStream(HDFS_PATH + "/num/" + i + ".jpg");
					FloatImage nimage = JPEGImageUtil.getInstance().decodeImage(file);

					Mat number_matrix = this.convertFloatImageToOpenCVMat(nimage, CvType.CV_32F);
					num_list.add(number_matrix);
					number_matrix.release();
					file.close();
				} catch (Exception e){
					System.err.println(e.toString());
				}
			}

			/* template matching */
			int k = 0;
			/* To select number when number of detecting number is more than 7 */
			int candidate_size = contours.size() - test.size();
			List candidate = null;
			if (candidate_size > 6) candidate = new ArrayList();
			for(int i=0; i< contours.size();i++){
				if (true){
					Rect rect = Imgproc.boundingRect(contours.get(i));
					boolean isContinued = true;

					if(test.contains(i)) {
						isContinued = false;
					}
					if(isContinued) {
						if (true){
							Core.rectangle(image, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
							int t_width = rect.width;
							int t_height = rect.height;
							Mat t_mat = new Mat(t_height, t_width, Core.DEPTH_MASK_ALL);

							t_mat = imageBlurr.submat(rect.y, rect.y + t_height, rect.x, rect.x + t_width);

							int result_num = -1;
							double max = 0;
							double min = 100000000;
					 		
							for (int n = 0; n < 10; n++){
								//Mat num = Highgui.imread(HDFS_PATH + "/num/" + n + ".jpg", CvType.CV_32F);
								Mat num = Highgui.imread("./" + n + ".jpg", CvType.CV_32F);
								Mat result = new Mat(t_mat.size(), CvType.CV_64FC1);
								Imgproc.resize(num, num, t_mat.size());
								Imgproc.cvtColor(num, num, Imgproc.COLOR_BGR2GRAY);

								int match_method = Imgproc.TM_CCOEFF;
								Imgproc.matchTemplate(t_mat, num, result, match_method);

								// / Localizing the best match with minMaxLoc
								MinMaxLocResult mmr = Core.minMaxLoc(result);

								if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
									if(min > mmr.minVal){
										min = mmr.minVal;
										result_num = n;
									}
								} else {
									if(max < mmr.maxVal){
										max = mmr.maxVal;
										result_num = n;
									}
								}
								num.release();
								result.release();
							}

							if (max > 1){

								if(result_num > -1){
									if (candidate_size > 6) {
										candidate.add(max);
									}
									car_number = result_num + car_number;
								}
								t_mat.release();
							}
						}
					}
				}
			}
			/* Cut string, if car_number size is more than 6 */
			if (car_number.length() > 6) {
				while (car_number.length() > 6) {
					int idx = 0;
					double min = Double.MAX_VALUE;
					for (int i = candidate.size() - 1, i >= 0; i--) {
						if (min > (double) candidate.get(i)) {
							min = (double) candidate.get(i);
							idx = i;
						}
					}
					candidate.remove(idx);
					car_number = car_number.substring(0, car_number.length() - idx - 1) + car_number.substring(car_number.length() - idx, car_number.length());
				}
			}

			if(car_number.length() == 6){
				car_number = car_number.substring(0, 2) + "_" + car_number.substring(2, 6);
			} else if(car_number.length() == 7){
				car_number = car_number.substring(0, 2) + "_" + car_number.substring(3, 7);
			} else if(car_number.length() < 3){
				car_number = "FINDING_NUMBER_IS_SMALLER_THAN_3 (" + car_number + ")";
			}
			image.release();
			imageBlurr.release();
			imageA.release();
			//System.out.println(car_number);
			return car_number;
		}

		/* Util 
		* 
		*/

		private boolean hasChild (Mat image_src, int size) {
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(image_src, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

			contours = removeSmallPiece(contours, image_src.width(), 0.5, image_src.height(), 0.4);
			if (contours.size() >= size) return true;
			return false;
		}

		private Mat imageResize (Mat image_src, int standard) {
			Mat image;

			int width = 0;
			int height = 0;
			if(image_src.width() > image_src.height()){
				width = standard;
				height = standard * image_src.height() / image_src.width();
			} else {
				height = standard;
				width = standard * image_src.width() / image_src.height();
			}
			image = new Mat (height, width, Core.DEPTH_MASK_ALL);
			Imgproc.resize (image_src, image, image.size());

			return image;
		}

		public Mat imagePreProcess(Mat src){
			Mat imageHSV = new Mat(src.size(), Core.DEPTH_MASK_8U);
			Mat imageCANNY = new Mat(src.size(), Core.DEPTH_MASK_8U);
			Mat imageBlurr = new Mat(src.size(), Core.DEPTH_MASK_8U);

			Imgproc.cvtColor(src, imageHSV, Imgproc.COLOR_BGR2GRAY);
			Imgproc.Canny(imageHSV, imageCANNY, 100, 200);

			Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);


			double zero[] = {0};
			for (int i = 0; i < imageBlurr.width(); i ++){
				for (int j = 0 ; j < imageBlurr.height(); j++){
					double tmp = imageCANNY.get(j, i)[0];
					if(tmp == 255){
						imageBlurr.put(j, i, zero);
					}
				}
			}
			Imgproc.threshold(imageBlurr, imageBlurr, 80, 255, Imgproc.THRESH_BINARY);
			Imgproc.erode(imageBlurr, imageBlurr, new Mat());
			Imgproc.dilate(imageBlurr, imageBlurr, new Mat());

			imageHSV.release();
			imageCANNY.release();
			return imageBlurr;
		}

		public Mat imageBinary(Mat src){
			Mat imageA = new Mat(src.size(), Core.DEPTH_MASK_ALL);
			Imgproc.adaptiveThreshold(src, imageA, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);

			return imageA;
		}

		public List<MatOfPoint> removeSmallPiece(List<MatOfPoint> contours, int max_width, double width_threshold, int min_height, double height_threshold){
			max_width = (int) (max_width * width_threshold);
			min_height = (int) (min_height * height_threshold);
			// Delete too small size contours or too larget size contours
			for(int i = contours.size() - 1; i >= 0; i--){
				Rect rect = Imgproc.boundingRect(contours.get(i));

				int width = rect.width;
				int height = rect.height;

				if (Imgproc.contourArea(contours.get(i)) <= 50){
					contours.remove(i);
				} 
				else if(width >= max_width) contours.remove(i);
				else if(height <= 28){
					contours.remove(i);
				} else if (width <= 20) {
					contours.remove(i);
				} else if (height < min_height){
					contours.remove(i);
				}
				//else if(width / height > 2) contours.remove(i);
/*
				// remove contours has too large width
				if(width > 750) contours.remove(i);
				// remove contours has too small height
				else if(height < min_height) contours.remove(i); */
			}

			return contours;
		}

		public int getSimiliarSizeRect(List<MatOfPoint> contours){
			Set<Integer> key_list = new HashSet<Integer>();
			Map<Integer, Integer> height_cnt = new HashMap<Integer, Integer>();
			for(int j = 0; j < contours.size(); j++){
				Rect rect3 = Imgproc.boundingRect(contours.get(j));

				int apprx_height = rect3.height/10 * 10;
				int cnt = height_cnt.get(apprx_height) == null ? 0 : height_cnt.get(apprx_height);
				height_cnt.put(apprx_height, cnt + 1);
				key_list.add(apprx_height);
			}

			int max_cnt = 0;
			for(int j : key_list){
				if(max_cnt < height_cnt.get(j)){
					max_cnt = height_cnt.get(j);
				}
			}
			return max_cnt;
		}

		public int getSimiliarSizeRectValue(List<MatOfPoint> contours, List exclude_list, int type){
			Set<Integer> key_list = new HashSet<Integer>();
			Map<Integer, Integer> value_cnt = new HashMap<Integer, Integer>();
			for(int i = 0; i < contours.size(); i++){
				if(!exclude_list.contains(i)) {
					Rect rect = Imgproc.boundingRect(contours.get(i));

					int apprx = 0;
					if(type == 0) apprx = rect.height/10 * 10;
					else if(type == 1) apprx = rect.width / 10 * 10;
					int cnt = value_cnt.get(apprx) == null ? 0 : value_cnt.get(apprx);
					value_cnt.put(apprx, cnt + 1);
					key_list.add(apprx);
				}
			}

			int max_cnt = 0;
			int similarValue = 0;
			for(int i : key_list){
				if(max_cnt < value_cnt.get(i)){
					max_cnt = value_cnt.get(i);
					similarValue = i;
				}
			}
			return similarValue;
		}

		public void setup(Context context) throws IOException, InterruptedException {
			// Load OpenCV native library
			try {
				// System.load("/usr/lib/libopencv_java2411.so");
				// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				System.load((new File("/usr/lib/libopencv_java2411.so")).getAbsolutePath());
			} catch (UnsatisfiedLinkError e) {
				System.err.println("Native code library failed to load.\n" + e + "\n" + Core.NATIVE_LIBRARY_NAME);
				System.err.println("#" + System.getProperty("java.library.path"));
				System.exit(1);
			}

			// Load cached cascade file for front face detection and create CascadeClassifier
			if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
				URI mappingFileUri = context.getCacheFiles()[0];

				if (mappingFileUri != null) {

				} else {
					System.out.println(">>>>>> NO MAPPING FILE");
				}
			} else {
				System.out.println(">>>>>> NO CACHE FILES AT ALL");
			}

			super.setup(context);
		} // setup()


		public void map(ImageHeader key, FloatImage value, Context context) throws IOException, InterruptedException {
			// Verify that image was properly decoded, is of sufficient size, and has three color channels (RGB)
			String car_num = "CAN_NOT_FIND";
			if (value != null && value.getWidth() > 1 && value.getHeight() > 1 && value.getBands() == 3) {
				Mat cvImage = this.convertFloatImageToOpenCVMat(value, CvType.CV_8UC3);
				try {
					car_num = this.dectectCarNumber(cvImage);
					System.out.println("Result::" + car_num);
				} catch(Exception e){
					System.err.println("ERROR");
					e.printStackTrace();
				}
				cvImage.release();
				System.gc();
				// Emit record to reducer
				context.write(new IntWritable(1), new Text(car_num));
			} // If (value != null...
		} // map()
	}

	public static class FaceCountReducer extends Reducer<IntWritable, ObjectWritable, IntWritable, Text> {
		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			// Initialize a counter and iterate over IntWritable/FloatImage records from mapper
			String result = "";
			int images = 0;
			int num = 1;
			for (Text val : values) {
				result += "\\n" + "" + val.toString();
				images++;
				num++;
			}

			// Emit output of job which will be written to HDFS
			context.write(new IntWritable(images), new Text(result));
		} // reduce()
	}

	public int run(String[] args) throws Exception {
		// Check input arguments
		if (args.length != 2) {
			System.out.println("Usage: firstprog <input HIB> <output directory>");
			System.exit(0);
		}

		// Initialize and configure MapReduce job
		Job job = Job.getInstance();
		// Set input format class which parses the input HIB and spawns map tasks
		job.setInputFormatClass(ImageBundleInputFormat.class);
		// Set the driver, mapper, and reducer classes which express the computation
		job.setJarByClass(templateMatchingHadoop.class);
		job.setMapperClass(FaceCountMapper.class);
		job.setReducerClass(FaceCountReducer.class);
		// Set the types for the key/value pairs passed to/from map and reduce layers
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		// Set the input and output paths on the HDFS
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// add cascade file
		for (int n = 0; n < 10; n++){
			job.addCacheFile(new URI(HDFS_PATH + "/num/" + n + ".jpg#" + n + ".jpg"));
		}
		job.addCacheFile(new URI(HDFS_PATH+"/lib/libopencv_java2411.so#libopencv_java2411.so"));

		// Execute the MapReduce job and block until it complets
		boolean success = job.waitForCompletion(true);

		// Return success or failure
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new templateMatchingHadoop(), args);
		System.exit(0);
	}
}
