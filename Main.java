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
    private static List<String> origin = new ArrayList<>();
    private static Map<Integer, Integer> memory = new HashMap<>(); // 模拟内存
    private static int currentLocation; // 当前指令位置
    private static int[] cc=new int[4];
    private static final int OVERFLOW = 0;
    private static final int UNDERFLOW = 1;
    private static final int DIVZERO = 2;
    private static final int EQUALORNOT = 3;
    private static int []register_general=new int[4];
    private static int []register_index=new int[4];
    private static int []data=new int[100000];
    private static List<String> output=new ArrayList<>();
    private static List<String> list=new ArrayList<>();
    private static String tmp;
    //under dif instruction type,r,x,address may have different meaning
    private static int r,x,address,I,ea;
    private static int count,LR,AL;
    private static final Map<String, String> opcodes = new HashMap<>() {{
        put("HLT", "000000");
        put("LDR", "000001");
        put("STR", "000010");
        put("LDA", "000011");
        put("LDX", "100001");
        put("STX", "100010");
        put("JZ",  "001000");
        put("JNE",  "001001");
        put("JCC",  "001010");
        put("JMA",  "001011");
        put("JSR",  "001100");
        put("RFS",  "001101");
        put("SOB",  "001110");
        put("JGE",  "001111");
        put("AMR",  "000100");
        put("SMR",  "000101");
        put("AIR",  "000110");
        put("SIR",  "000111");
        put("MLT", "111000");
        put("DVD", "111001");
        put("TRR", "111010");
        put("AND", "111011");
        put("ORR", "111100");
        put("NOT", "111101");
        put("SRC", "011001");
        put("RRC", "011010");

    }};
    public static void main(String[] args) {
        String inputFile = "input.asm";
        String outputFile = "load_file.txt";
        String listFile = "listing_file.txt";
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(listFile))) {
            for(String s:list) {
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
                origin.add(line);
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String label = parts[0].trim();
                    line = parts[1].trim();
                    // label and location
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
            tmp=origin.get(i).trim();
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
            if(cc[r]!=0) currentLocation=ea;
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
            int Immed= parts.length==2?Integer.parseInt(parts[1]):0;
            encodeInstruction(parts[0], 0, 0, Immed,0);
            currentLocation=register_general[3];
            register_general[0]=Immed;
        }else if(line.startsWith("SOB")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            register_general[r]=register_general[r]-1;
            if(register_general[r]>0) currentLocation=ea;
        }else if(line.startsWith("JGE")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            if(register_general[r]>=0) currentLocation=ea;
        }else if(line.startsWith("AMR")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            register_general[r]=register_general[r]+data[ea];
        }else if(line.startsWith("SMR")){
            r_x_addI_update(parts[1]);
            encodeInstruction(parts[0], r, x, address,I);
            register_general[r]=register_general[r]-data[ea];
        }else if(line.startsWith("AIR")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], x, 0, address,0);
            register_general[x]=register_general[x]+address;
        }else if(line.startsWith("SIR")){
            x_addI_update(parts[1]);
            encodeInstruction(parts[0], x, 0, address,0);
            register_general[r]=register_general[r]-address;
        }
        else if(line.startsWith("MLT")){
            r_x_update(parts[1]);
            if (!(r%2==0 && x%2==0)) {
                System.out.println("Error: rx and ry must be 0 or 2.");
                return;
            }
            int valueRx = register_general[r] & 0xFFFF;
            int valueRy = register_general[x] & 0xFFFF;
            long result = (long) valueRx * valueRy;
            int lowOrderBits = (int) (result & 0xFFFF);
            int highOrderBits = (int) ((result >> 16) & 0xFFFF);
            register_general[r] = highOrderBits;
            register_general[r + 1] = lowOrderBits;
            if (highOrderBits != 0) {
                cc[OVERFLOW]=1;
//                System.out.println("Overflow occurred.");
            }
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if(line.startsWith("DVD")){
            r_x_update(parts[1]);
            if (!(r%2==0 && x%2==0)) {
                System.out.println("Error: rx and ry must be 0 or 2.");
                return;
            }
            if (register_general[x] == 0) {
                cc[DIVZERO] = 1;  // 设置除以零标志
                System.out.println("Error: Division by zero. DIVZERO flag set.");
                return;
            }
            int quotient = register_general[r] / register_general[x];
            int remainder = register_general[r] %register_general[x];
            register_general[r] = quotient;
            register_general[r + 1] = remainder;
//            System.out.println("Quotient (register[" + r + "]): " + register_general[r]);
//            System.out.println("Remainder (register[" + (r + 1) + "]): " + register_general[r + 1]);
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if(line.startsWith("TRR")){
            r_x_update(parts[1]);
            if (register_general[r] == register_general[x]) {
                cc[EQUALORNOT] = 1; // Set condition code if equal
            } else {
                cc[EQUALORNOT] = 0;
            }
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("AND")) {
            r_x_update(parts[1]);
            register_general[r] = register_general[r] & register_general[x];
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("ORR")) {
            r_x_update(parts[1]);
            register_general[r] = register_general[r] | register_general[x];
            encodeInstruction(parts[0], r, x, 0, 0);
        }else if (line.startsWith("NOT")) {
            r=Integer.parseInt(parts[1]);
            register_general[r] = ~register_general[r];
            encodeInstruction(parts[0], r, 0, 0, 0);
        }else if(line.startsWith("SRC")){
            LR_AL_update(parts[1]);
//            System.out.println(register_general[r]);
            if (LR==1)
                register_general[r] = register_general[r] << count;
            else{
                if(AL==1)register_general[r] = register_general[r] >>> count;
                else register_general[r] = register_general[r] >> count;
            }
//            System.out.println(register_general[r]);
            encode_Shift_Rotate(parts[0], r, count, LR, AL);
        }else if(line.startsWith("RRC")){
            LR_AL_update(parts[1]);
            register_general[r] &= 0xFFFF;
//            System.out.println(register_general[r]);
            if (LR==1)register_general[r]=((register_general[r] << count) | (register_general[r] >>> (16 - count))) & 0xFFFF;
            else register_general[r]=((register_general[r] >>> count) | (register_general[r] << (16 - count))) & 0xFFFF;
//            System.out.println(register_general[r]);
            encode_Shift_Rotate(parts[0], r, count, LR, AL);
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
    // support ex1:LDX x, address[,I] ex2: AIR r, immed
    public static void x_addI_update(String part){
        String[] registersAndAddress = part.split(",");
        r=0;
        x = Integer.parseInt(registersAndAddress[0]);
        address = Integer.parseInt(registersAndAddress[1]);
        boolean hasIndex = registersAndAddress.length > 2 && registersAndAddress[2].equals("1");
        I = hasIndex ? 1 : 0;
        ea=effective_Address(x,address,I);
    }
    public static void r_x_update(String part){
        String[] registers = part.split(",");
        r = Integer.parseInt(registers[0]);
        x = Integer.parseInt(registers[1]);
    }

    public static void LR_AL_update(String part){
        String[] registersAndAddress = part.split(",");
        r = Integer.parseInt(registersAndAddress[0]);
        count = Integer.parseInt(registersAndAddress[1]);
        AL = Integer.parseInt(registersAndAddress[2]);
        LR = Integer.parseInt(registersAndAddress[3]);
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

        if (instruction.equals("NOT")) {
            ixBits = "00";
            IBit = "0";
            addressBits = "00000";
        }

        String binaryInstruction = opcode + rBits + ixBits + IBit + addressBits;
//        System.out.println(binaryInstruction);
        int decimalValue = Integer.parseInt(binaryInstruction, 2);
        String formattedOctal = String.format("%06o", decimalValue);
        String loc = String.format("%06o", currentLocation++);
        System.out.println(loc+" "+formattedOctal);
        list.add(loc+" "+formattedOctal+" "+tmp);
        output.add(loc+" "+formattedOctal);
        return formattedOctal;
    }
    private static String encode_Shift_Rotate(String instruction, int r, int count, int LR, int AL) {
        String opcode = opcodes.getOrDefault(instruction, "000000");
        String rBits = String.format("%2s", Integer.toBinaryString(r)).replace(' ', '0'); // 寄存器r的二进制表示
        String alBits = String.valueOf(AL);
        String lrBits = String.valueOf(LR);
        String countBits = String.format("%6s", Integer.toBinaryString(count)).replace(' ', '0');
        String binaryInstruction = opcode + rBits + alBits + lrBits +"00"+ countBits;
//        System.out.println(binaryInstruction);
        int decimalValue = Integer.parseInt(binaryInstruction, 2);
        String formattedOctal = String.format("%06o", decimalValue);
        String loc = String.format("%06o", currentLocation++);
        System.out.println(loc+" "+formattedOctal);
        list.add(loc+" "+formattedOctal+" "+tmp);
        output.add(loc+" "+formattedOctal);
        return formattedOctal;
    }
}