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
    private static Map<Integer, Integer> memory = new HashMap<>(); // 模拟内存
    private static int currentLocation = 0; // 当前指令位置
    private static int cc=0;
    private static int []register_general=new int[4];
    private static int []register_index=new int[4];
    private static int []data=new int[100000];
    private static List<String> output=new ArrayList<>();
    private static int r,x,address,I,ea;
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

        put("SRC",  "011111");

    }};
    public static void main(String[] args) {
        String inputFile = "input.asm"; // 汇编源文件
        String outputFile = "output.txt"; // 输出文件

        // 处理汇编文件
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for(String s:output) {
                writer.write(s);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void processLine(String line) {
        line = line.trim();
        String[] parts = line.split(" ");
        if(line.startsWith("HLT")){
            encodeInstruction(parts[0], 0, 0, 0, 0);
        }else if(line.startsWith("LOC")){
            int address = Integer.parseInt(parts[1]);
            currentLocation=address;
        }else if(line.startsWith("Data")){
            int address = Integer.parseInt(parts[1]);
            data[currentLocation]=address;
            encodeInstruction(parts[0],0,0,address,0);
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
            encodeInstruction(parts[0],r,x,address,0);
        }else if (line.startsWith("STX")){
            x_addI_update(parts[1]);
            register_index[x]=data[ea];
            encodeInstruction(parts[0],r,x,address,0);
        }else if(line.startsWith("JZ")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,0);
            if(register_general[r]==0) currentLocation=ea;
        }else if(line.startsWith("JNE")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,0);
            if(register_general[r]!=0) currentLocation=ea;
        }else if(line.startsWith("JCC")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,0);
            if(cc==r) currentLocation=ea;
        }else if(line.startsWith("JMA")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,0);
            currentLocation=ea;
        }else if(line.startsWith("JSR")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,0);
            register_general[3]=currentLocation;
            currentLocation=ea;
        }else if(line.startsWith("RFS")){

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
    public static int effective_Address(int x,int address,int i){
        if(i==0)return register_index[x]+address;
        else return data[register_index[x]+address];
    }


    private static String encodeInstruction(String instruction, int r, int x, int address, int I) {
        String opcode = opcodes.getOrDefault(instruction, "000000");
        String rBits = String.format("%2s", Integer.toBinaryString(r)).replace(' ', '0'); // 寄存器r的二进制表示
        String ixBits = String.format("%2s", Integer.toBinaryString(x)).replace(' ', '0'); // 索引寄存器的二进制表示
        String IBit = Integer.toBinaryString(I); // 索引使能位
        String addressBits = String.format("%5s", Integer.toBinaryString(address)).replace(' ', '0'); // 地址的二进制表示

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
