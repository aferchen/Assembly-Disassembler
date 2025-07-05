package cs321as2;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Assignment2 {

	public static HashMap<Integer, String> opcode = new HashMap<>();
	public static HashMap<Integer, String> cond = new HashMap<>();
	private static int count = 0;
	public static void main(String[] args) {
		
		initializeOpcodes();
        initializeConditions();
        if (args.length < 1) {
            System.err.println("Not right path");
            return;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(args[0]));

            if (fileBytes.length % 4 != 0) {
                System.err.println("File size not a multiple of 4 bytes. Extra bytes will be ignored.");
            }

            for (int i = 0; i + 3 < fileBytes.length; i += 4) {
            	int b1 = (fileBytes[i]     & 0xFF) << 24; // Most significant byte
            	int b2 = (fileBytes[i + 1] & 0xFF) << 16;
            	int b3 = (fileBytes[i + 2] & 0xFF) << 8;
            	int b4 = (fileBytes[i + 3] & 0xFF);       // Least significant byte

            	int instruction = b1 | b2 | b3 | b4;

                disassemble(instruction);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
	private static void initializeOpcodes() {
		opcode.put(0b10001011000, "ADD");
        opcode.put(0b1001000100, "ADDI");
        opcode.put(0b10001010000, "AND");
        opcode.put(0b1001001000, "ANDI");
        opcode.put(0b000101, "B");
        opcode.put(0b01010100, "B.");
		opcode.put(0b100101, "BL");
        opcode.put(0b11010110000, "BR");
        opcode.put(0b10110101, "CBNZ");
        opcode.put(0b10110100, "CBZ");
        opcode.put(0b11001010000, "EOR");
        opcode.put(0b1101001000, "EORI");
        opcode.put(0b11111000010, "LDUR");
        opcode.put(0b11010011011, "LSL");
        opcode.put(0b11010011010, "LSR");
        opcode.put(0b10101010000, "ORR");
        opcode.put(0b1011001000, "ORRI");
        opcode.put(0b11111000000, "STUR");
        opcode.put(0b11001011000, "SUB");
        opcode.put(0b1101000100, "SUBI");
        opcode.put(0b1111000100, "SUBIS");
        opcode.put(0b11101011000, "SUBS");
        opcode.put(0b10011011000, "MUL");
        opcode.put(0b11111111101, "PRNT");
        opcode.put(0b11111111100, "PRNL");
        opcode.put(0b11111111110, "DUMP");
        opcode.put(0b11111111111, "HALT");
	}
	private static void initializeConditions() {
		cond.put(0x0, "EQ"); //0
        cond.put(0x1, "NE"); //1
        cond.put(0x2, "HS"); //2
        cond.put(0x3, "LO"); //3
        cond.put(0x4, "MI"); //4
        cond.put(0x5, "PL"); //5
        cond.put(0x6, "VS"); //6
        cond.put(0x7, "VC"); //7
        cond.put(0x8, "HI"); //8
        cond.put(0x9, "LS"); //9
        cond.put(0xa, "GE"); //a
        cond.put(0xb, "LT"); //b
        cond.put(0xc, "GT"); //c
        cond.put(0xd, "LE"); //d
	}
	private static void disassemble(int instruction) {
	    int R_D_opcode = (instruction >> 21) & 0x7FF;
	    int I_opcode = (instruction >> 22) & 0x3FF;
	    int CB_opcode = (instruction >> 24) & 0xFF;
	    int B_opcode = (instruction >> 26) & 0x3F;
	    String s = "";

	    if (opcode.containsKey(R_D_opcode)) {
	        s += opcode.get(R_D_opcode);

	        if (isRTypeInstruction(R_D_opcode)) {
	            int Rd_num = instruction & 0x1F;
	            int Rn_num = (instruction >> 5) & 0x1F;
	            int Rm_num = (instruction >> 16) & 0x1F;

	            String Rd = registerName(Rd_num);
	            String Rn = registerName(Rn_num);
	            String Rm = registerName(Rm_num);

	            s += " " + Rd + ", " + Rn + ", " + Rm;
	        }
	        //LSL or LSR
	        else if (isShiftInstruction(R_D_opcode)) {
	            int Rd_num = instruction & 0x1F;
	            int Rn_num = (instruction >> 5) & 0x1F;
	            int shamt = (instruction >> 10) & 0x3F;

	            if (shamt >= 32) {
	                shamt -= 64;
	            }

	            String Rd = registerName(Rd_num);
	            String Rn = registerName(Rn_num);

	            s += " " + Rd + ", " + Rn + ", #" + shamt;
	        }
	        //PRNT 
	        else if (R_D_opcode == 0b11111111101) {
	            int Rd_num = instruction & 0x1F;
	            String Rd = registerName(Rd_num);

	            s += " " + Rd;
	        }
	        //LDUR or STUR
	        else if (isLoadStoreInstruction(R_D_opcode)) {
	            int Rt_num = instruction & 0x1F;
	            int Rn_num = (instruction >> 5) & 0x1F;

	            String Rt = registerName(Rt_num);
	            String Rn = registerName(Rn_num);

	            int DTAddr = (instruction >> 12) & 0x1FF;
	            if (DTAddr >= 256) {
	                DTAddr -= 512;
	            }

	            s += " " + Rt + ", [" + Rn + ", #" + DTAddr + "]";
	        }
	    }
	    //I
	    else if (opcode.containsKey(I_opcode)) {
	        s += opcode.get(I_opcode);

	        int Rd_num = instruction & 0x1F;
	        int Rn_num = (instruction >> 5) & 0x1F;
	        int Immediate = (instruction >> 10) & 0xFFF;

	        if (Immediate >= 2048) {
	        	Immediate -= 4096;
	        }

	        String Rd = registerName(Rd_num);
	        String Rn = registerName(Rn_num);

	        s += " " + Rd + ", " + Rn + ", #" + Immediate;
	    }
	    //B.cond
	    else if (opcode.containsKey(CB_opcode)) {
	        s += opcode.get(CB_opcode);
	        if (CB_opcode == 0b01010100) {
	            int cond_code = instruction & 0x1F;
	            String condString = cond.get(cond_code);
	            if (condString != null) {
	                s += condString;
	            } else {
	                s += "UNKNOWN_COND";
	            }
	        }

	        int BranchAddr = (instruction >> 5) & 0x7FFFF;
	        if (BranchAddr >= 262144) {
	            BranchAddr -= 524288;
	        }

	        s += " L" + (count + BranchAddr);
	    }

	    //B

	    else if (opcode.containsKey(B_opcode)) {
	        int BranchAddr = instruction & 0x3FFFFFF;
	        if (BranchAddr >= 33554432) {
	            BranchAddr -= 67108864;
	        }

	        s += opcode.get(B_opcode) + " L" + (count + BranchAddr);
	    }

	    // UHOH

	    else {
	        System.out.println("Opcode not found --> Error with program");
	    }


	    //Print
	    System.out.println("L" + count + ": " + s);
	    count++;
	}
        
        
        
            //B Opcodes
            //B 0b000101
            //BL 0b100101
            
            //I Opcodes
            //ORRI 0b1011001000
            //EORRI 0b1101001000
            //ADDI 0b1001000100
            //ANDI 0b1001001000
            //ADDIS 0b1011000100
            //SUBI 0b1101000100
            //SUBIS 0b1111000100
            //ANDIS 0b1011000100
            
            //CB Opcodes
            //CBZ
            //CBNZ
            
            //D Opcodes
            //STURB 0b00111000000
            //LDURB 0b00111000010
            //STURH 0b01111000000
            //LDURH 0b01111000010
            //STURW 0b10111000000
            //LDURSW 0b10111000100
            //STXR 
            //LDXR
            //STUR 0b11111000000
            //LDUR 0b11111000010
	private static String registerName(int regNum) {
	    switch (regNum) {
	        case 28: return "SP";
	        case 29: return "FP";
	        case 30: return "LR";
	        case 31: return "XZR";
	        default: return "X" + regNum;
	    }
	}
	//R Opcodes
    //ADD 0b10001011000
    //AND 0b10001010000
    //EOR 0b11001010000
    //ORR 0b10101010000
    //SUB 0b11001011000
    //SUBS 0b11101011000
    //MUL 0b10011011000
    //BR 0b11010110000
	private static boolean isRTypeInstruction(int opcode) {
	    return opcode == 0b10001011000 ||
	           opcode == 0b10001010000 ||
	           opcode == 0b11001010000 ||
	           opcode == 0b10101010000 ||
	           opcode == 0b11001011000 ||
	           opcode == 0b11101011000 ||
	           opcode == 0b10011011000;
	}
	
	// LSR 0b11010011011
	// LSL 0b11010011010
	private static boolean isShiftInstruction(int opcode) {
	    return opcode == 0b11010011011 || opcode == 0b11010011010;
	}
	
    //STUR 0b11111000000
    //LDUR 0b11111000010
	private static boolean isLoadStoreInstruction(int opcode) {
        return opcode == 0b11111000010 || opcode == 0b11111000000;
    }

}
