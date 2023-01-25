
import java.io.File;
import java.util.Scanner;


public class sim {

// smith predictor
  
    static void smith(int n, String filename) throws Exception{
        Scanner scanner = new Scanner(new File(filename));
        int total = 0, miss = 0;
        int counter_value = 1 << (n-1), counter = counter_value, mod = 1 << n;
        String predict = "";
      //read full file
        while(scanner.hasNextLine())
        {
            //get each line
            String s = scanner.nextLine();
          // split to get the hex
            String hex = s.split(" ")[0];
          // get the taken or not taken
          
            String taken = s.split(" ")[1];
            total++;
          // make prediction based on the smith bit size
            if (counter_value >= counter) 
              predict = "t";
            else predict = "n";
            // count misses and hits
            if (taken.equals("t")){ 
              if (counter_value<mod-1) 
                counter_value++;
            }
            else { 
              if (counter_value>0) 
                counter_value--;
                 }
            
            if (!predict.equals(taken)) miss++;
        }
      //print per test
        System.out.println("number of predictions:\t\t" + total);
        System.out.println("number of mispredictions:\t" + miss);
        System.out.println("misprediction rate:\t\t" + String.format("%.2f", Math.round(miss/(total/10000.0))/100.0) + "%");
        System.out.println("FINAL COUNTER CONTENT:\t\t" + counter_value);
    }
  // bimodel 
    static void bimod(int m, String filename) throws Exception{
        int len = 1 << m;
        int table[] = new int[len];
        for (int i=0;i<len;i++) table[i] = 4;
        Scanner scanner = new Scanner(new File(filename));
        int total = 0, miss = 0;
        int mod = 8;
        String predict = "";
        while(scanner.hasNextLine())
        {
          // get hex and taken not taken values
            String s = scanner.nextLine();
            String hex = s.split(" ")[0];
            String taken = s.split(" ")[1];
            String binary = Integer.toBinaryString(Integer.parseInt(hex, 16));
          // get index
            int index = Integer.parseInt(binary.substring(binary.length()-m-2, binary.length()-2), 2);
            total++;
        // get prediction based on counter value table index
            int counter_value = table[index];
            if (counter_value >= 4) predict = "t";
            else predict = "n";
            // update 
            if (taken.equals("t")){ if (counter_value<7) counter_value++;}
            else { if (counter_value>0) counter_value--;}
            // get miss rate
            table[index] = counter_value;
            if (!predict.equals(taken)) miss++;
        }
      // print whats needed in validation file
        System.out.println("number of predictions:\t\t" + total);
        System.out.println("number of mispredictions:\t" + miss);
        System.out.println("misprediction rate:\t\t" + String.format("%.2f", Math.round(miss/(total/10000.0))/100.0) + "%");
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0;i<len;i++)
            System.out.println("" + i + "\t" + table[i]);
        
    }
  // gshare predictor:
    static void gshare(int m, int n, String filename) throws Exception{
        int len = 1 << m;
        int table[] = new int[len];
        for (int i=0;i<len;i++) table[i] = 4;
        int global_reg = 0;
        Scanner scanner = new Scanner(new File(filename));
        int total = 0, miss = 0;
        int mod = 8;
        String predict = "";
        while(scanner.hasNextLine())
        {
        // read each line 
            String s = scanner.nextLine();
          //split for hex and taken
            String hex = s.split(" ")[0];
            String taken = s.split(" ")[1];
            String binary = Integer.toBinaryString(Integer.parseInt(hex, 16));
            binary = binary.substring(binary.length()-m-2, binary.length()-2);
          // update index
            int index = Integer.parseInt(binary.substring(m-n), 2) ^ global_reg;
            if (m > n){
                index |= Integer.parseInt(binary.substring(0, m-n), 2) << n;
            }
          // make prediction 
            total++;
            int counter_value = table[index];
            if (counter_value >= 4) predict = "t";
            else predict = "n";
            
            global_reg = global_reg >> 1;
            // updated miss rate and correct rate
            if (taken.equals("t")){if (counter_value<7) counter_value++;global_reg |= (1 << (n-1));}
            else { 
              if (counter_value>0) 
                counter_value--;
            }
            
            table[index] = counter_value;
            if (!predict.equals(taken)){ 
              miss++;
            }
            
        }
        System.out.println("number of predictions:\t\t" + total);
        System.out.println("number of mispredictions:\t" + miss);
        System.out.println("misprediction rate:\t\t" + String.format("%.2f", Math.round(miss/(total/10000.0))/100.0) + "%");
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0;i<len;i++)
            System.out.println("" + i + "\t" + table[i]);
        
    }
  // hybrid predictor 
    static void hybrid(int k, int m1, int n, int m2, String filename) throws Exception{
      // check valid k,  m1, m2
        int len1 = 1 << k, len2 = 1 << m1, len3 = 1 << m2;
      // indiate the tables we will need along with variables
        int chooser_table[] = new int[len1], gshare_table[] = new int[len2], bimodal_table[] = new int[len3];
        for (int i=0;i<len1;i++)chooser_table[i]=1;
        for (int i=0;i<len2;i++)gshare_table[i]=4;
        for (int i=0;i<len3;i++)bimodal_table[i]=4;
        int global_reg = 0;
        Scanner scanner = new Scanner(new File(filename));
        int total = 0, miss = 0;
        int mod = 8;
        String predict = "";
        while(scanner.hasNextLine())
        {
          // scan the file
            String s = scanner.nextLine();
          // get hex
            String hex = s.split(" ")[0];
          // get taken valyes
            String taken = s.split(" ")[1];
            String binary = Integer.toBinaryString(Integer.parseInt(hex, 16));
            total++;
            
            String binary_gshare = binary.substring(binary.length()-m1-2, binary.length()-2);
            int index_gshare = Integer.parseInt(binary_gshare.substring(m1-n), 2) ^ global_reg;
            if (m1 > n){
                index_gshare |= Integer.parseInt(binary_gshare.substring(0, m1-n), 2) << n;
            }
          // check counter value against gshare table index
            int counter_value_gshare = gshare_table[index_gshare];
            String predict_gshare="";
          // if counter value is equal or higher than 4 predict taken
            if (counter_value_gshare >= 4) predict_gshare = "t";
              // otherwise not taken
            else predict_gshare = "n";
          // shiftglobal
            global_reg = global_reg >> 1;

          // repeat for bimodal
            String binary_bimodal = binary.substring(binary.length()-m2-2, binary.length()-2);
            int index_bimodal = Integer.parseInt(binary_bimodal, 2);
            int counter_value_bimodal = bimodal_table[index_bimodal];
            String predict_bimodal="";
            if (counter_value_bimodal >= 4) predict_bimodal = "t";
            else predict_bimodal = "n";
            
            int index_hybrid = Integer.parseInt(binary.substring(binary.length()-k-2, binary.length()-2), 2);
            int counter_value_hybrid = chooser_table[index_hybrid];
            if(counter_value_hybrid >= 2){ 
                predict = predict_gshare;
                if (taken.equals("t")) { if (counter_value_gshare<7) counter_value_gshare++;}
                else { if (counter_value_gshare>0) counter_value_gshare--;}
                gshare_table[index_gshare] = counter_value_gshare;
            } else {
                predict = predict_bimodal;
              // if taken was true , if counter is less than 7 we will move one ahead
                if (taken.equals("t")) { if (counter_value_bimodal<7) counter_value_bimodal++;}
                  // otherwise we as long as we are above zero we will move one back
                else { if (counter_value_bimodal>0) counter_value_bimodal--;}
                bimodal_table[index_bimodal] = counter_value_bimodal;
            }
            
            if (taken.equals("t")){ global_reg |= (1 << (n-1));}
            // if the branch that was taken is from what we predicted in gshare and not what we predicted in bimodel
          // then check if the counter value in hybrid is less than 3 then move one ahead
            if (taken.equals(predict_gshare) && !taken.equals(predict_bimodal)) { if (counter_value_hybrid<3) counter_value_hybrid++;}
              // otherwise if prediction is bimodal is our taken and taken is not eqial to prediction in gshare 
              // if counter value in hyprid is postive , then deduct one , go one step back
            else if (taken.equals(predict_bimodal) && !taken.equals(predict_gshare)) { if (counter_value_hybrid > 0) counter_value_hybrid--;}

          // update the chooser table with new counter value
            chooser_table[index_hybrid] = counter_value_hybrid;
            // if we predicred wrong just add one to miss
            if (!predict.equals(taken)){miss++;}
            
        }
      // printing to match output shown in the sample validations
        System.out.println("number of predictions:\t\t" + total);
        System.out.println("number of mispredictions:\t" + miss);
        System.out.println("misprediction rate:\t\t" + String.format("%.2f", Math.round(miss/(total/10000.0))/100.0) + "%");
        System.out.println("FINAL CHOOSER CONTENTS");
        for (int i=0;i<len1;i++) System.out.println("" + i + "\t" + chooser_table[i]);
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i=0;i<len2;i++) System.out.println("" + i + "\t" + gshare_table[i]);
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i=0;i<len3;i++) System.out.println("" + i + "\t" + bimodal_table[i]);
    }
    public static void main(String[] args) throws Exception{
      //print output 
        System.out.println("COMMAND");
        String str = "./sim";
        for (int i=0;i<args.length;i++) str += " " + args[i];
        System.out.println(str);
        System.out.println("OUTPUT");
      // pick with predictor I will need
        switch(args[0].toLowerCase()){
            case "smith":
                smith(Integer.parseInt(args[1]), args[2]);
                break;
            case "bimodal":
                bimod(Integer.parseInt(args[1]), args[2]);
                break;
            case "gshare":
                gshare(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                break;
            case "hybrid":
                hybrid(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
                break;
        }
    }
}
