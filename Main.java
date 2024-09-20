import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;


public class Main {
    private static Map<String, Integer> labelTable = new HashMap<>();
    private static List<String> lines = new ArrayList<>();
    private static Map<Integer, Integer> memory = new HashMap<>(); // 模拟内存
    private static int currentLocation; // 当前指令位置
    private static int cc=0;
    private static int []register_general=new int[4];
    private static int []register_index=new int[4];
    private static int []data=new int[100000];
    private static List<String> output=new ArrayList<>();
    private static int r,x,address,I,ea;
    private static int count,LR,AL;
    private static final Map<String, String> opcodes = new HashMap<>() {{
        put("HLT", "000000");
        put("LDR", "000001");
        put("STR", "000010");
        put("LDA", "000011");
        put("LDX", "101001");
        put("STX", "101010");
        put("JZ",  "001010");
        put("JNE",  "001011");
        put("JCC",  "001100");
        put("JMA",  "001101");
        put("JSR",  "001110");
        put("RFS",  "001111");
        put("SOB",  "010000");
        put("JGE",  "010001");

	put("TRR", "100111");
        put("AND", "110000");
        put("ORR", "110001"); 
        put("NOT", "110010");
        put("SRC", "011111");
        put("RRC", "011100");

    }};
    public static void main(String[] args) {
        String inputFile = "input.asm"; // 汇编源文件
        String outputFile = "output.txt"; // 输出文件
        firstPass(inputFile);
        secondPass();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for(String s:output) {
                writer.write(s);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void firstPass(String inputFile) {
        currentLocation = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String label = parts[0].trim();
                    line = parts[1].trim();
                    // 将标签和对应的行号存入符号表
                    labelTable.put(label, currentLocation);
                }
                lines.add(line.trim());
                currentLocation++;
                if(line.startsWith("LOC")){
                    String[] parts = line.split("\\s+");
                    int address = Integer.parseInt(parts[1]);
                    currentLocation=address;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void secondPass() {
        currentLocation=0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim(); // 从 List 中获取行
            processLine(line);
        }
    }

    private static void processLine(String line) {
        line = line.trim();
        String[] parts = line.split("\\s+");
        if(line.startsWith("HLT")){
            encodeInstruction(parts[0], 0, 0, 0, 0);
        }else if(line.startsWith("LOC")){
            int address = Integer.parseInt(parts[1]);
            currentLocation=address;
        }else if(line.startsWith("Data")){
            String data_label=parts[1];
            int dat;
            if (labelTable.containsKey(data_label))
                dat = labelTable.get(data_label);
            else dat = Integer.parseInt(data_label);
            data[currentLocation]=dat;
            encodeInstruction(parts[0],0,0,dat,0);
        }else if(line.startsWith("LDR")){
            r_x_addI_update(parts[1]);
            register_general[r]=data[ea];
            encodeInstruction(parts[0], r, x, address, I);
        }else if(line.startsWith("STR")){
            r_x_addI_update(parts[1]);
            data[ea]=register_general[r];
            encodeInstruction(parts[0], r, x, address, I);
        }else if(line.startsWith("LDA")){
            r_x_addI_update(parts[1]);
            register_general[r]=ea;
            encodeInstruction(parts[0], r, x, address, I);
        }else if (line.startsWith("LDX")){
            x_addI_update(parts[1]);
            register_index[x]=data[ea];
            encodeInstruction(parts[0],r,x,address,I);
        }else if (line.startsWith("STX")){
            x_addI_update(parts[1]);
            register_index[x]=data[ea];
            encodeInstruction(parts[0],r,x,address,I);
        }else if(line.startsWith("JZ")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            if(register_general[r]==0) currentLocation=ea;
        }else if(line.startsWith("JNE")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            if(register_general[r]!=0) currentLocation=ea;
        }else if(line.startsWith("JCC")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            if(cc==r) currentLocation=ea;
        }else if(line.startsWith("JMA")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            currentLocation=ea;
        }else if(line.startsWith("JSR")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            register_general[3]=currentLocation;
            currentLocation=ea;
        }else if(line.startsWith("RFS")){
            address= parts.length==2?Integer.parseInt(parts[1]):0;
            encodeInstruction(parts[0], 0, 0, address,0);
            currentLocation=register_general[3];
            register_general[0]=address;
        }else if(line.startsWith("SOB")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            register_general[r]=register_general[r]-1;
            if(register_general[r]>0) currentLocation=ea;
        }else if(line.startsWith("JGE")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            if(register_general[r]>=0) currentLocation=ea;
        }else if(line.startsWith("TRR")){
	    r_xy_addI_update(parts[1]);
	    if (register_general[r] == register_general[x]) {
               cc = 1; // Set condition code if equal
            } else {
               cc = 0;
        }
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("AND")) {
            r_xy_addI_update(parts[1]);
            register_general[r] = register_general[r] & register_general[x];
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("ORR")) {
            r_xy_addI_update(parts[1]);
            register_general[r] = register_general[r] | register_general[x];
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("NOT")) { 
            r_x_addI_update(parts[1]);
            encodeInstruction("NOT", r, 0, 0, 0); 
        }else if(line.startsWith("SRC")){
	    LR_AL_update(parts[1]);
	    encode_Shift_Rotate("SRC", r, count, LR, AL);
        }else if(line.startsWith("RRC")){
	    LR_AL_update(parts[1]);
	    encode_Shift_Rotate("SRC", r, count, LR, AL);
        }

	             


    }
    public static void r_x_addI_update(String part){
        String[] registersAndAddress = part.split(",");
        r = Integer.parseInt(registersAndAddress[0]);
        x = Integer.parseInt(registersAndAddress[1]);
        address = Integer.parseInt(registersAndAddress[2]);
        boolean hasIndex = registersAndAddress.length > 3 && registersAndAddress[3].equals("1");
        I = hasIndex ? 1 : 0;
        ea=effective_Address(x,address,I);
    }
    public static void x_addI_update(String part){
        String[] registersAndAddress = part.split(",");
        r=0;
        x = Integer.parseInt(registersAndAddress[0]);
        address = Integer.parseInt(registersAndAddress[1]);
        boolean hasIndex = registersAndAddress.length > 2 && registersAndAddress[2].equals("1");
        I = hasIndex ? 1 : 0;
        ea=effective_Address(x,address,I);
    }
    public static void r_xy_addI_update(String part){
       String[] registers = part.split(",");
       int r = Integer.parseInt(registers[0]);
       int x = Integer.parseInt(registers[1]);
    }
	
    
    public static int effective_Address(int x,int address,int i){
        if(i==0)return register_index[x]+address;
        else return data[register_index[x]+address];
    }
   
    public static void LR_AL_update(String part){
	String[] registersAndAddress = part.split(",");
        int r = Integer.parseInt(registersAndAddress[0]);
        int x = Integer.parseInt(registersAndAddress[1]);

	int shiftCount = Integer.parseInt(registersAndAddress[2]);
        int LR = Integer.parseInt(registersAndAddress[3]);
        int AL = Integer.parseInt(registersAndAddress[4]);
    }


    private static String encodeInstruction(String instruction, int r, int x, int address, int I) {
        String opcode = opcodes.getOrDefault(instruction, "000000");
        String rBits = String.format("%2s", Integer.toBinaryString(r)).replace(' ', '0'); // 寄存器r的二进制表示
        String ixBits = String.format("%2s", Integer.toBinaryString(x)).replace(' ', '0'); // 索引寄存器的二进制表示
        String IBit = Integer.toBinaryString(I); // 索引使能位
        String addressBits = String.format("%5s", Integer.toBinaryString(address)).replace(' ', '0'); // 地址的二进制表示

	if (instruction.equals("NOT")) {
            ixBits = "00";
            IBit = "0";
            addressBits = "00000";
        }

        String binaryInstruction = opcode + rBits + ixBits + IBit + addressBits;

        int decimalValue = Integer.parseInt(binaryInstruction, 2);
        String formattedOctal = String.format("%06o", decimalValue);
        //        System.out.println("Binary Instruction: " + binaryInstruction);
//        System.out.println("Encoded Instruction: "+ formattedOctal);
        String loc = String.format("%06o", currentLocation++);
        System.out.println(loc+" "+formattedOctal);
        output.add(loc+" "+formattedOctal);
        return formattedOctal;
    }
    private static String encode_Shift_Rotate(String instruction, int r, int count, int LR, int AL) {
        String opcode = opcodes.getOrDefault(instruction, "000000");
        if (opcode.equals("000000")) {
            throw new IllegalArgumentException("Invalid instruction: " + instruction);
        }
        String rBits = String.format("%2s", Integer.toBinaryString(r)).replace(' ', '0'); // 寄存器r的二进制表示
        String alBits = String.valueOf(AL);
        String lrBits = String.valueOf(LR);
        String countBits = String.format("%6s", Integer.toBinaryString(count)).replace(' ', '0');
        String binaryInstruction = opcode + rBits + alBits + lrBits + countBits;

        int decimalValue = Integer.parseInt(binaryInstruction, 2);
        String formattedOctal = String.format("%06o", decimalValue);
        String loc = String.format("%06o", currentLocation++);
        System.out.println(loc+" "+formattedOctal);
        output.add(loc+" "+formattedOctal);
        return formattedOctal;
    }
}
