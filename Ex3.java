import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;

public class Ex3 {

	//	public static double[][] dataset; // the original dataset is loaded in here by original order, while last slot in every line is '1' for "Iris-setosa" and '2' for "Iris-versicolor".
	public static Attribute [] dataset;
	public static Attribute [] train; //this is the train part of the split dataset.
	public static Vector<Attribute> test;
	public static int Errors=0;
	public static double radius; //the radius of search for neighbors.
	//	public static int sampleSize;
	public static Attribute [] knn = null;

	public static void initKNearestNeighbors(String fn) {
		/**
		 * Main initialization function to start the system with all radius points extension.
		 * as well as 100 rounds repetition.
		 * and finally printing of relevant data, as required and logically needed.
		 */

		double [][] avgArr = new double[36][4];
		int countAVG = 0;
		loadDataset(fn); // init loading of dataset with filename fn.
		for(int p=1; p<=3; p++)
			for (int k=1; k<=5; k+=2)
				for (int ss=20; ss<=50; ss+=10)
				{
					int [] errors = new int[100];
					for (int index=0; index<100; index++)
					{
						randomDivision(ss);
						neighborsSearch(ss, k, p);
						errors[index]=Errors;
						Errors=0;
						for (int indexB=0; indexB<100; indexB++) {
							dataset[indexB].isUsed=false;
							//System.out.println("dataset[index].isUsed="+dataset[index].isUsed);
						}
					}
					avgArr[countAVG][0] = CalcAverage(errors);
					avgArr[countAVG][1] = k;
					avgArr[countAVG][2] = ss;
					avgArr[countAVG][3] = p;
					
					if(p == 3) {
						System.out.println("The average error for p=inf, k="+k+", SampleSize="+ss+", is: "+avgArr[countAVG][0]);
					}else {
						System.out.println("The average error for p="+p+", k="+k+", SampleSize="+ss+", is: "+avgArr[countAVG][0]);
					}
					countAVG++;
				}
		double minAvg = avgArr[0][0];
		int minDex = 0;
		for (int i = 0; i < avgArr.length; i++) {
			if(avgArr[i][0] < minAvg) {
				minAvg = avgArr[i][0];
				minDex = i;
			}
		}
		
		if(avgArr[minDex][3] == 3) {
			System.out.println("The Minimum Avarage Error (k,s,p): ("+avgArr[minDex][1]+","+avgArr[minDex][2]+",inf)");
		}else {
			System.out.println("The Minimum Avarage Error (k,s,p): ("+avgArr[minDex][1]+","+avgArr[minDex][2]+","+avgArr[minDex][3]+")");
		}
		System.out.println("There isn't an overfitting");
	}
	public static double CalcAverage(int[] arr) {
		double sum=0;
		for (int i=0; i<arr.length; i++)
			sum+=arr[i];

		double arrSize = arr.length;
		return (sum/arrSize);
	}
	public static void loadDataset(String fileName) {
		System.out.println("Loading the IRIS Dataset");
		/**
		 * Here We Load the dataset we would like to work with.
		 * Access to it would be global, but this function would load it.
		 */

		dataset = new Attribute[100];
		String [] line = new String[5];

		try {
			File f = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String readline = "";

			int i = 0;
			boolean isVersico = false;
			while((readline = br.readLine()) != null){
				line = readline.split(",");

				if(line[4].matches("Iris-versicolor")) {
					isVersico = true;
				}else{
					isVersico = false;
				}

				dataset[i] = new Attribute(Double.parseDouble(line[0]), Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]), isVersico);
				//dataset[i].isUsed=false;
				i++;
			}
			br.close();
		}catch (Exception e) {
			// TODO: handle exception
			System.err.println(" - ERROR READING FILE - "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void randomDivision(int sampleSize) {
		/**
		 * working with the data-set and randomly distributing it
		 * to two parts, half for training and half for testing. 
		 */

		train = new Attribute[sampleSize];
		test = new Vector<Attribute>();
		Random rn = new Random();
		boolean enoughVersico=false;

		int halfway = sampleSize/2;
		int count = 0;
		int randChoice;
		//randomizing train samples:
		while(count < sampleSize) {
			if(!enoughVersico) {
				randChoice = rn.nextInt(dataset.length/2);
				if (!dataset[randChoice].isUsed) {
					train[count] = dataset[randChoice];
					count++;
					dataset[randChoice].isUsed = true;
					if(count==halfway) {
						enoughVersico = true;
					}
				}
			}else {
				randChoice = rn.nextInt(dataset.length/2) + (dataset.length/2);
				if (!dataset[randChoice].isUsed) {
					train[count] = dataset[randChoice];
					count++;
					dataset[randChoice].isUsed = true;
				}
			}
		}

		//assemble the leftovers as test:
		for(int i=0; i<dataset.length; i++) {
			if(!dataset[i].isUsed) {
				//				leftovers.add(dataset[i]); // PRETTY SURE NOT NEEDED. CHECK.
				test.add(dataset[i]);
			}
		}
	}

	public static void neighborsSearch(int sampleSize, int k, int p) {
		/**
		 * Searches for neighbors in ranges extending and classifies those neighbors.
		 */


		for (int i = 0; i < test.size(); i++) {
			int count = 0;
			knn = new Attribute[sampleSize];
			for (int j = 0; j < train.length; j++) {

				switch (p) {
				case 1:{
					train[j].distance = pOne(test.elementAt(i), train[j]);
					break;
				}
				case 2:{
					train[j].distance = pTwo(test.elementAt(i), train[j]);
					break;
				}
				case 3:{
					train[j].distance = pThree(test.elementAt(i), train[j]);
				}
				}
				knn[count] = train[j];
				count++;
			}

			Attribute [] min = new Attribute[k];
			int minCount = 0;
			min[0] = knn[0];
			int start = 0;
			int tempIndex = 0;

			switch(k) {

			case 1:{
				for(int m = 0; m<knn.length;m++) {
					if(knn[m].distance < min[0].distance) {
						min[0] = knn[m];
					}
				}

				if(isVersico(min)) {
					if((test.elementAt(i).classification == false)) {
						updateError();
					}
				}else {
					if((test.elementAt(i).classification == true)) {
						updateError();
					}
				}
				break;
			}
			case 3:{
				for (int m = 0; m < k; m++) {
					Attribute tempMin = knn[m];
					for (int l = start; l < knn.length; l++) {
						if(knn[l].distance < tempMin.distance) {
							tempMin = knn[l];
							tempIndex = l;
						}
					}
					swap(tempIndex, start);
					start++;
					min[m] = tempMin;
				}

				if(isVersico(min)) {
					if((test.elementAt(i).classification == false)) {
						updateError();
					}
				}else {
					if((test.elementAt(i).classification == true)) {
						updateError();
					}
				}
				break;
			}
			case 5:{
				for (int m = 0; m < k; m++) {
					Attribute tempMin = knn[m];
					for (int l = start; l < knn.length; l++) {
						if(knn[l].distance < tempMin.distance) {
							tempMin = knn[l];
							tempIndex = l;
						}
					}
					swap(tempIndex, start);
					start++;
					min[m] = tempMin;
				}

				if(isVersico(min)) {
					if((test.elementAt(i).classification == false)) {
						updateError();
					}
				}else {
					if((test.elementAt(i).classification == true)) {
						updateError();
					}
				}
			}
			}
		}


		// TODO check most

	}
	public static void swap(int l, int start) {
		Attribute temp = knn[start];
		knn[start]=knn[l];
		knn[l]=temp;
	}

	public static boolean isVersico(Attribute [] nearest) {
		int countVirginica = 0, countVersico = 0;
		for (int i = 0; i < nearest.length; i++) {
			if(nearest[i].classification == false) {
				countVersico++;
			}else {
				countVirginica++;
			}
		}

		if(countVirginica > countVersico) {
			return true;
		}else {
			return false;
		}
	}

	public static double pOne(Attribute a, Attribute b) {
		return Math.abs(a.petLength-b.petLength)+Math.abs(a.petWidth-b.petWidth)+Math.abs(a.sepLength-b.sepLength)+Math.abs(a.sepWidth-b.sepWidth);
	}

	public static double pTwo(Attribute a, Attribute b) {
		return Math.sqrt(Math.pow(a.petLength-b.petLength, 2)+Math.pow(a.petWidth-b.petWidth, 2)+Math.pow(a.sepLength-b.sepLength, 2)+Math.pow(a.sepWidth-b.sepWidth, 2));
	}

	public static double pThree(Attribute a, Attribute b) {
		double [] t = new double[4];
		t[0] = Math.abs(a.petLength-b.petLength);
		t[1] = Math.abs(a.petWidth-b.petWidth);
		t[2] = Math.abs(a.sepLength-b.sepLength);
		t[3] = Math.abs(a.petWidth-b.petWidth);

		Arrays.sort(t);

		return t[3];
	}

	public static void updateError() {
		Errors++;
	}

	public static void main(String[] args) {
		initKNearestNeighbors("iris.txt");

	}

	public static void print() {
		for (int i = 0; i < dataset.length; i++) {
			System.out.println(dataset[i].sepLength+" "+dataset[i].sepWidth+" "+dataset[i].petLength+" "+dataset[i].petWidth+" "+dataset[i].classification+" ");
		}
	}
}

class Attribute{
	public double sepLength;
	public double sepWidth;
	public double petLength;
	public double petWidth;
	public boolean classification;
	public boolean isUsed;
	public double distance;

	public Attribute(double sl, double sw, double pl, double pw, boolean cls) {
		sepLength = sl;
		sepWidth = sw;
		petLength = pl;
		petWidth = pw;
		classification = cls;
		isUsed = false;
		distance = 0;

	}



}
