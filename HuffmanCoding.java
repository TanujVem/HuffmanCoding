package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        sortedCharFreqList = new ArrayList();
        StdIn.setFile(fileName);
        int[] freq = new int[128];
        char ptr;
        double length = 0;
        while(StdIn.hasNextChar()){
            ptr = StdIn.readChar();
            freq[ptr]++;
            length++;
        }
        int i  = 0;
        int y = 0;
        while(i<freq.length){
            if(freq[i] != 0){
                double frq = ((double)(freq[i]))/length;
                CharFreq x = new CharFreq((char)i, frq);
                sortedCharFreqList.add(x);
                y = i;
            }
            i++;
        }
        if(sortedCharFreqList.size() == 1){
            sortedCharFreqList.add(new CharFreq((char)(y+1),0.0));
        }
        Collections.sort(sortedCharFreqList);
    }                           

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
    TreeNode smallest;
    TreeNode smallest2;
    Queue<TreeNode> source = new Queue<TreeNode>();
    for(int i = 0; i<sortedCharFreqList.size(); i++){
        source.enqueue(new TreeNode(sortedCharFreqList.get(i),null,null));
    }
    Queue<TreeNode> target = new Queue<TreeNode>();
    Queue<TreeNode> dequed = new Queue<TreeNode>();
    TreeNode y = null;
    while(!source.isEmpty() || target.size()!=1){
        while(dequed.size()<2){
            if(target.isEmpty()){
                dequed.enqueue(source.dequeue());
            }
            else{
                if(!source.isEmpty()){
                    if(source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()){
                        dequed.enqueue(source.dequeue());
                    }
                    else if(target.peek().getData().getProbOcc() < source.peek().getData().getProbOcc()){
                        dequed.enqueue(target.dequeue());
                    }
                }
                else if(source.isEmpty()){
                    dequed.enqueue(target.dequeue());
                }
                
            }

        }
        if(dequed.isEmpty()){
            smallest = null;
        }
        else{
            smallest = dequed.dequeue();
        }
        if(dequed.isEmpty()){
            smallest2 = null;
        }
        else{
            smallest2 = dequed.dequeue();
        }
        double ProbOcc;
        double ProbOcc2;
        if(smallest!= null){
            ProbOcc = smallest.getData().getProbOcc();
        }
        else{
            ProbOcc = 0;
        }
        if(smallest2!= null){
            ProbOcc2 = smallest2.getData().getProbOcc();
        }
        else{
            ProbOcc2 = 0;
        }
        y = new TreeNode(new CharFreq(null,ProbOcc+ProbOcc2),smallest,smallest2);
        target.enqueue(y);
    }
    huffmanRoot = y;   
}
    
	
    

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];
        TreeNode ptr = huffmanRoot;
        recursiveEncode(ptr, "");
    }

    public void recursiveEncode(TreeNode ptr, String cod){
        if(ptr.getLeft()!= null){
            recursiveEncode(ptr.getLeft(), cod+"0");
        }
        if(ptr.getRight()!= null){
            recursiveEncode(ptr.getRight(), cod+"1");
        }
        if(ptr.getLeft() == null & ptr.getRight() == null){
            if(ptr.getData().getProbOcc()!=0.0){
            encodings[ptr.getData().getCharacter()] = cod;
            }
        }
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String code = "";
        while(StdIn.hasNextChar()){
            code+=encodings[StdIn.readChar()];
        }
        writeBitString(encodedFile, code);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        
        String code = readBitString(encodedFile);
        TreeNode huffmanptr = huffmanRoot;
        for(int i = 0; i<code.length(); i++){
            if(huffmanptr.getData().getCharacter() != null){
                StdOut.print(huffmanptr.getData().getCharacter());
                huffmanptr = huffmanRoot;
            }
            if(code.charAt(i)=='0'){
                huffmanptr = huffmanptr.getLeft();
            }
           if(code.charAt(i)=='1'){
                huffmanptr = huffmanptr.getRight();
            }
            if(i == code.length()-1){
                StdOut.print(huffmanptr.getData().getCharacter());
            }
            
        }
       
        }


    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
