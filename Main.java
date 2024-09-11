import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Map<Integer, Integer> memory = new HashMap<>(); // 模拟内存
    private static int currentLocation = 6; // 当前指令位置
    private static int []register_general=new int[4];
    private static int []register_index=new int[4];
    private static int []data=new int[100000];
    public static void main(String[] args) {
        String outputFile = "output.txt"; // 输出文件

        // 处理汇编文件
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void processLine(String line) {
        line = line.trim();         // needs to trim with one or more white spaces
        //System.out.println(line);
        String[] parts = line.split(" ");
        if (line.startsWith("LDX")){
            String[] registersAndAddress = parts[1].split(",");
            int x = Integer.parseInt(registersAndAddress[0]);
            int address = Integer.parseInt(registersAndAddress[1]);
            register_index[x]=data[address];
            encodeInstruction("LDX",0,x,address,0);
        }else if(line.startsWith("Data")){
            int address = Integer.parseInt(parts[1]);
            data[currentLocation]=address;
            encodeInstruction("Data",0,0,address,0);
        }else if(line.startsWith("LDR")){
            String[] registersAndAddress = parts[1].split(",");
            int r = Integer.parseInt(registersAndAddress[0]);
            int x = Integer.parseInt(registersAndAddress[1]);
            int address = Integer.parseInt(registersAndAddress[2]);
            boolean hasIndex = registersAndAddress.length > 2 && registersAndAddress[2].equals("1");
            int I = hasIndex ? 1 : 0;
            encodeInstruction("LDR", r, x, address, I);
        }else if(line.startsWith("LDA")){
            String[] registersAndAddress = parts[1].split(",");
            int r = Integer.parseInt(registersAndAddress[0]);
            int x = Integer.parseInt(registersAndAddress[1]);
            int address = Integer.parseInt(registersAndAddress[2]);
            boolean hasIndex = registersAndAddress.length > 2 && registersAndAddress[2].equals("1");
            int I = hasIndex ? 1 : 0;
            encodeInstruction("LDA", r, x, address, I);
        }

    }

    private static String encodeInstruction(String instruction, int r, int x, int address, int I) {
        String opcode="";
        if(instruction.equals("LDX"))opcode="101001";
        else if(instruction.equals("Data")){}
        else if(instruction.equals("LDR")){opcode="000001";}
        else if(instruction.equals("LDA")){opcode="000011";}
        String rBits = String.format("%2s", Integer.toBinaryString(r)).replace(' ', '0'); // 寄存器r的二进制表示
        String ixBits = String.format("%2s", Integer.toBinaryString(x)).replace(' ', '0'); // 索引寄存器的二进制表示
        String IBit = Integer.toBinaryString(I); // 索引使能位
        String addressBits = String.format("%5s", Integer.toBinaryString(address)).replace(' ', '0'); // 地址的二进制表示

        String binaryInstruction = opcode + rBits + ixBits + IBit + addressBits;
//        System.out.println("Binary Instruction: " + binaryInstruction);
        int decimalValue = Integer.parseInt(binaryInstruction, 2);
        String octalValue = Integer.toOctalString(decimalValue);
        String formattedOctal = String.format("%06o", decimalValue);
//        System.out.println("Encoded Instruction: "+ formattedOctal);
        System.out.println(formattedOctal);
        return formattedOctal;
    }
}
