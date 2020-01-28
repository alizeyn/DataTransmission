package ir.alizeyn.datatransmission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class Opration {

    public static byte[] toByteArray(String input){
        for (int i = 0; i < input.length()%8; i++) {
            input += '0';
        }

        byte[] byteArray = new byte[input.length()/8];
        String substring = "";
        for(int i=0; i<input.length(); i+=8) {
            substring = input.substring(i,i+8);
            byteArray[i/8] = (byte) Integer.parseInt(substring,2);
        }
        return byteArray;
    }
    public static int[] toIntArray(String input){
        int[] intArray = new int[input.length()];
        for(int i=0; i<input.length(); i+=1) {
            intArray[i] = Integer.parseInt(input.substring(i, i+1));
        }
        return intArray;
    }
    public static String toString(int[] input){
        String output = "" ;
        for (int i = 0; i < input.length; i++) {
            output += String.valueOf(input[i]);
        }
        return output;
    }
    public static void writeFile(byte[] data) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File("./des.txt"));
            os.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 1. Coding Functions
    public static String enocdeByHDB3(String input){
        int previusePulseSign = -1;
        int pulseCounter = 0;
        String output = "";
        int index = 0;
        System.out.println(input.length());
        while (index < input.length()){
            if (input.charAt(index) == '1') {
                if (previusePulseSign > 0)
                    output += "101";
                else
                    output += "111";
                previusePulseSign *= -1;
                pulseCounter+=1;
            }
            if (input.charAt(index) == '0')
                if (index+4 < input.length() && input.substring(index,index+4).equals("0000")) {
                    if (pulseCounter%2 == 0) {
                        if (previusePulseSign > 0)
                            output += "10100101";
                        else
                            output += "11100111";
                        previusePulseSign *= -1;
                    }
                    else
                    if (previusePulseSign > 0)
                        output += "000111";
                    else
                        output += "000101";
                    pulseCounter = 0;
                    index += 4;
                    continue;
                }
                else
                    output += "0";
            index++;
        }
        return output;

    }
    public static String encodeByB8ZS(String input){
        int previusePulseSign = -1;
        String output = "";
        int index = 0;
        while (index < input.length()){
            if (input.charAt(index) == '1') {
                if (previusePulseSign > 0)
                    output += "101";
                else
                    output += "111";
                previusePulseSign *= -1;
            }
            if (input.charAt(index) == '0')
                if (index+8 < input.length() && input.substring(index,index+8).equals("00000000")) {
                    if (previusePulseSign > 0)
                        output += "0001111010101111";
                    else
                        output += "0001011110111101";
                    index += 8;
                    continue;
                }
                else
                    output += "0";
            index++;
        }
        return output;
    }
    public static String decodeFromHDB3(String input){
        int previusePulseSign = -1;
        String output = "";
        int index = 0;

        while (index < input.length()) {
            if (input.charAt(index) == '0')
                output += '0';
            if (input.charAt(index) == '1'){
                if ((input.substring(index,index+3).equals("111") && previusePulseSign > 0) || (input.substring(index,index+3).equals("101") && previusePulseSign < 0)){
                    output = output.substring(0,output.length()-3) + '0' + output.substring(output.length()-2);
                    output += '0';
                }
                else {
                    output += '1';
                    previusePulseSign *= -1;
                }

                index +=3;
                continue;
            }
            index++;
        }

        return output;
    }
    public static String decodeFromB8ZS(String input){
        int previusePulseSign = -1;
        String output = "";
        int index = 0;

        while (index < input.length()) {
            if (input.charAt(index) == '0')
                output += '0';
            if (input.charAt(index) == '1'){
                if (index+13 < input.length() && ((input.substring(index,index+3).equals("111") && previusePulseSign > 0) || (input.substring(index,index+3).equals("101") && previusePulseSign < 0))){
                    index += 13;
                    output += "0000000";
                }
                else {
                    index += 3;
                    output += '1';
                    previusePulseSign *= -1;
                }
                continue;
            }
            index++;
        }
        return output;
    }

    // 2. Error Detection Functions
    // use to generate CRC32 bit value , add this to begining of data must save in file
    public static String getCRC32Value(String input){
        byte[] byteArray = toByteArray(input);
        CRC32 CRCGenerator = new CRC32();
        CRCGenerator.update(byteArray);
        return String.format("%32s", Long.toBinaryString(CRCGenerator.getValue())).replace(' ', '0');
    }
    // the data you saved, with 32 bit CRC32 in first , check for error
    public static boolean verifyCRC32Value (String input){
        String CRC32Received = input.substring(0, 32);
        String data = input.substring(32);
        return (getCRC32Value(data).equals(CRC32Received));
    }
    // remove CRC32 bit from the begining of data
    public static String removeCRC32Value(String input) {
        return input.substring(32);
    }

    // use to encode hamming
    public static String encodeHamming(String input){
        String output = "";
        for (int i = 0; i < input.length(); i+=2) {
            int a[] = toIntArray(input.substring(i, i+2));
            int b[] = generateCode(a);
            output += toString(b);
        }
        return output;

    }

    // use to decode hamming + auto correction ;)
    public static String correctHammingOrginalData(String input){
        String output = "";
        char[] temp;
        for (int i = 0; i < input.length(); i+=5) {
            int a[] = toIntArray(input.substring(i, i+5));
            temp = receive(a).toCharArray();
            output += Character.toString(temp[1]) + Character.toString(temp[0]);
        }
        return output;
    }

    // hamming helper functions
    public static int[] generateCode(int a[]) {
        int b[];
        int i=0, parity_count=0 ,j=0, k=0;
        while(i < a.length) {
            if(Math.pow(2,parity_count) == i+parity_count + 1) {
                parity_count++;
            }
            else {
                i++;
            }
        }
        b = new int[a.length + parity_count];
        for(i=1 ; i <= b.length ; i++) {
            if(Math.pow(2, j) == i) {
                b[i-1] = 2;
                j++;
            }
            else {
                b[k+j] = a[k++];
            }
        }
        for(i=0 ; i < parity_count ; i++) {
            b[((int) Math.pow(2, i))-1] = getParity(b, i);
        }
        return b;
    }
    public static int getParity(int b[], int power) {
        int parity = 0;
        for(int i=0 ; i < b.length ; i++) {
            if(b[i] != 2) {
                // If 'i' doesn't contain an unset value,
                // We will save that index value in k, increase it by 1,
                // Then we convert it into binary:

                int k = i+1;
                String s = Integer.toBinaryString(k);

                //Nw if the bit at the 2^(power) location of the binary value of index is 1
                //Then we need to check the value stored at that location.
                //Checking if that value is 1 or 0, we will calculate the parity value.

                int x = ((Integer.parseInt(s))/((int) Math.pow(10, power)))%10;
                if(x == 1) {
                    if(b[i] == 1) {
                        parity = (parity+1)%2;
                    }
                }
            }
        }
        return parity;
    }
    public static String receive(int a[]) {
        int power;
        int parity_count = 3;
        // int parity[] = new int[parity_count];
        // String syndrome = new String();
        // for(power=0 ; power < parity_count ; power++) {
        // 	for(int i=0 ; i < a.length ; i++) {
        // 		int k = i+1;
        // 		String s = Integer.toBinaryString(k);
        // 		int bit = ((Integer.parseInt(s))/((int) Math.pow(10, power)))%10;
        // 		if(bit == 1) {
        // 			if(a[i] == 1) {
        // 				parity[power] = (parity[power]+1)%2;
        // 			}
        // 		}
        // 	}
        // 	syndrome = parity[power] + syndrome;
        // }
        // int error_location = Integer.parseInt(syndrome, 2);
        // if(error_location != 0) {
        // 	a[error_location-1] = (a[error_location-1]+1)%2;
        // }
        String output = "";
        power = parity_count-1;
        for(int i=a.length ; i > 0 ; i--) {
            if(Math.pow(2, power) != i) {
                output+= String.valueOf(a[i-1]);
            }
            else {
                power--;
            }
        }
        return output;
    }
    public static String encodeASK(String input){
        String output = "2";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '0')
                output += "32123212";
            else
                output += "42024202";
        }

        return output;
    }

    public static String encodeFSK (String input){
        String output = "|";

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '0')
                output += "121012101|";
            else
                output += "12101210121012101|";
        }

        return output;
    }

    public static String decodeFSK (String input){
        String output = "";
        int i = 0;
        while (i < input.length()-1){

            if (input.charAt(i+10) == '|'){
                output += "0";
                i+= 10;
            }
            else{
                output += "1";
                i += 18;
            }
        }
        return output;
    }



    public static String decodeASK (String input) {
        String output = "";
        input = input.replace("2", "");
        for (int i = 0; i < input.length(); i+=4) {
            if (input.charAt(i) == '3')
                output += "0";
            else
                output += "1";
        }
        return output;
    }


}
