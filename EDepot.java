//Daryl Pham and Brandon Starler

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.text.*;

/* 
java -classpath ojdbc14.jar:. EDepot
*/

public class EDepot{
    
    static Connection conn;
    static Boolean isManager = false;
    static int Orders = 0;

public static void main(String[] args) throws SQLException{

 
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	

// 1. Load the Oracle JDBC driver for this program
    try {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());     
        String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
        String strUsername = "d_s_p";
        String strPassword = "5185566";
        conn = DriverManager.getConnection(strConn,strUsername,strPassword);
    }catch(Exception e){ 
        e.printStackTrace(); 
        conn.close();
        System.exit(1);
    }
	
	Start(conn, in);
	
}

//-------------------------------------------------------------------------------------------------------------------//
//      START
//-------------------------------------------------------------------------------------------------------------------//
public static void Start(Connection conn, BufferedReader in) throws SQLException{

	System.out.println("\nWelcome to the E-Depot Management System!\n-----------------------------\n");
	while(true){
	System.out.print("(1) Check Stock Levels\n(2) Process Orders\n(3) Exit\n\n>> ");
	String choose = getMyLine(in);
	if(choose.equals("1")){
		Check(conn, in);
	}else if(choose.equals("2")){
		Process(conn, in);
	}else if(choose.equals("3")){
		System.out.println("Exiting System...\n");
         conn.close();
		System.exit(1);
	}else{
		continue;
	}
	
	}
}

//-------------------------------------------------------------------------------------------------------------------//
//      READ LINE
//-------------------------------------------------------------------------------------------------------------------//
public static String getMyLine(BufferedReader in){
    String retval = null;
    try{
         retval = in.readLine();
    }catch(IOException e){
        System.out.println("Error: Could not get input");
        System.exit(1);
    }
    return retval;
}

//-------------------------------------------------------------------------------------------------------------------//
//      Check
//-------------------------------------------------------------------------------------------------------------------//
public static void Check(Connection conn, BufferedReader in) throws SQLException{
	String StockNum = "";
	Statement stmt = conn.createStatement();
	System.out.print("\nSelect the Stock Number of the Item you wish to check from below: \n");
    ResultSet rs = stmt.executeQuery("select distinct stocknum from inventory");
    while(rs.next()){
        System.out.println("\t" + rs.getString(1));
    }
    System.out.print(">> ");
	StockNum = getMyLine(in);
	System.out.print("\n-----------------------------\n\n");
	
	rs = stmt.executeQuery("select Quantity, Min, Max from Inventory where StockNum = '" + StockNum + "'");
	if(!rs.isBeforeFirst()){
		System.out.println(StockNum + " is not in the inventory.");
		System.out.print("\n-----------------------------\n\n");
	}else{
		rs.next();
		int Quantity = rs.getInt(1);
		int Min = rs.getInt(2);
		int Max = rs.getInt(3);
		System.out.println("Item " + StockNum +":");
		System.out.println("\tQnt: " + Quantity);
		System.out.println("\tItem is " + (Max-Quantity) + " under Max level.");
		System.out.println("\tItem is " + (Quantity-Min) + " over Min level.");
		System.out.print("\n-----------------------------\n\n");
	}
	rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      Process
//-------------------------------------------------------------------------------------------------------------------//
public static void Process(Connection conn, BufferedReader in) throws SQLException{
	String OrderID = "";
	String choose = "";
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select distinct EOrderID from EMartOrder where Processed = 0");
	if(!rs.isBeforeFirst()){
		System.out.println("\nAll orders have already been filled!");
		System.out.print("\n-----------------------------\n\n");
	}else{
		while(true){
			rs = stmt.executeQuery("select distinct EOrderID from EMartOrder where Processed = 0");
			System.out.println("\nOrders to be processed: ");
			while(rs.next()){
				System.out.println("Order Number: " + rs.getString(1));
			}
			System.out.print("\n-----------------------------\n\n");
			System.out.print("Select an Order Number from above to view and process: ");
			OrderID = getMyLine(in);
			rs = stmt.executeQuery("select StockNum, Quantity from EMartOrder where EOrderID = '" + OrderID + "' AND Processed = 0");
			if(!rs.isBeforeFirst()){
				System.out.println("Error: Order Number is not a valid number or order already processed.");
				System.out.print("\n-----------------------------\n\n");
				continue;
			}
			System.out.println("\nOrder contains: ");
			while(rs.next()){
				System.out.println("\tItem: " + rs.getString(1) + "\t\tQnt: " + rs.getInt(2));
			}
			while(true){
				System.out.print("\n-----------------------------\n\n");
				System.out.print("Fill this order? y/n\n>> ");
				choose = getMyLine(in);
				if(choose.equals("Y") || choose.equals("y")){
					rs = stmt.executeQuery("select StockNum, Quantity from EMartOrder where EOrderID = '" + OrderID + "'");
					while(rs.next()){
						Statement stmt2 = conn.createStatement();
						String name = rs.getString(1);
						//System.out.println("Here at beginning");
						ResultSet rs2 = stmt2.executeQuery("select quantity from inventory where StockNum = '" + name + "'");
						rs2.next();
						int NewQnt = rs2.getInt(1) - rs.getInt(2);
						//System.out.println("Here at middle");
						rs2 = stmt2.executeQuery("update inventory set quantity = " + NewQnt + " where stocknum = '" + name + "'");
						rs2.close();
					}
				}else if(choose.equals("N") || choose.equals("n")){
					System.out.println("Cancelling process...");
					System.out.print("\n-----------------------------\n\n");
					rs.close();
					return;
				}else{
					System.out.println("Invalid choice.");
					rs.close();
					continue;
				}
				//System.out.println("Here before end");
				rs = stmt.executeQuery("update EMartOrder set Processed = 1 where EOrderID = '" + OrderID + "'");
				break;
			}
			break;
		}
	}
	System.out.println("\nOrder "+ OrderID + " has been filled!\n");
	System.out.print("\n-----------------------------\n\n");
	Replenish(conn,in);
	rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      Replenish
//-------------------------------------------------------------------------------------------------------------------//
public static void Replenish(Connection conn, BufferedReader in) throws SQLException{
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select Manufacturer from Inventory where Quantity < Min group by manufacturer having count(*) >= 3");
	if(rs.isBeforeFirst()){
		while(rs.next()){
			String Manufacturer = rs.getString(1);
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select StockNum, Quantity, Max, ModelNum from inventory where manufacturer = '" + Manufacturer + "' AND quantity < max");
			while(rs2.next()){
				String item = rs2.getString(1);
				int difference = rs2.getInt(3) - rs2.getInt(2);
				int NoticeID = 1;
				Statement stmt3 = conn.createStatement();
				ResultSet rs3 = stmt3.executeQuery("select NoticeID from Manufacturer");
				while(rs3.next()){
					if(NoticeID < Integer.parseInt(rs3.getString(1))){
						NoticeID = Integer.parseInt(rs3.getString(1));
					}
				}
				NoticeID = NoticeID + 1;
				rs3 = stmt3.executeQuery("insert into manufacturer values('" + NoticeID + "', '" + Manufacturer + "')");
				rs3 = stmt3.executeQuery("insert into ShipItems values('" + NoticeID + "', '" + Manufacturer + "', '" + rs2.getString(4) + "', " + difference + ")"); 
				rs3 = stmt3.executeQuery("update inventory set Quantity = " + rs2.getInt(3) + " where stocknum = '" + item + "'");
				rs3.close();
				}
			rs2.close();
		}
	}else{
		rs = stmt.executeQuery("select StockNum, Quantity, Max, ModelNum, Manufacturer from inventory where quantity < min");
		while(rs.next()){
			String Manufacturer = rs.getString(5);
			String ModelNum = rs.getString(4);
			String item = rs.getString(1);
			int difference = rs.getInt(3) - rs.getInt(2);
			int NoticeID = 1;
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select NoticeID from Manufacturer");
			while(rs2.next()){
				if(NoticeID < Integer.parseInt(rs2.getString(1))){
					NoticeID = Integer.parseInt(rs2.getString(1));
				}
			}
			NoticeID = NoticeID + 1;
			rs2 = stmt2.executeQuery("insert into manufacturer values('" + NoticeID + "', '" + Manufacturer + "')");
			rs2 = stmt2.executeQuery("insert into ShipItems values('" + NoticeID + "', '" + Manufacturer + "', '" + rs.getString(4) + "', " + difference + ")"); 
			rs2 = stmt2.executeQuery("update inventory set Quantity = " + rs.getInt(3) + " where stocknum = '" + item + "'");
			rs2.close();
		}
	}
	}
	


}

