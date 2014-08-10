//Daryl Pham and Brandon Starler

import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.text.*;

/* 
java -classpath ojdbc14.jar:. DataBases
*/

public class EMart{
    
    static Connection conn;
    static Boolean isManager = false;
	static String Username = "";
    static String Password = "";
    static int Orders = 0;
    static int choice;
    static String choose = "";

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
	
// 2. Login
    
    while(true){
        Boolean logged = false;
        System.out.print("-------------------\n(1) Login\n(2) Register\n(3) Exit\n\n>> ");
        choose = getMyLine(in);
        if(choose.equals("1")){
            while(true){
                if(Login(conn, in)){
                    if(isManager){
                        System.out.println("\nYou have successfully logged onto the Manager account!");
                    }else{
                        System.out.println("\nWelcome! You have successfully logged on!");
                    }
                    logged = true;
                    break;
                }else{
                    System.out.println("Invalid Username/Password.\n");
                    logged = false;
                    break;
                }
            }
            if(logged){
                break;
            }
        }else if(choose.equals("2")){
            Boolean reg = Register(conn, in);
            if(reg){
                break;
            }else{
                continue;
            }
        }else if(choose.equals("3")){
            System.out.println("\nExiting...");
            conn.close();
            System.exit(1);
        }else{
            continue;
        }
    }
// 3. Menu with list of options to perform
    if(!isManager){
        Customer(conn, in);
    }else{
        Manager(conn, in);
    }
}
    //Customer section functions
public static void Customer(Connection conn, BufferedReader in) throws SQLException{
        while(true){
            System.out.print("\n--------Menu--------\n\n(1) Search\n(2) Add\n(3) Delete\n(4) Display Cart\n(5) Checkout\n(6) Find Order\n(7) Re-run Order\n(8) Exit\n\n>> ");
            choose = getMyLine(in);
            if(choose.equals("1")){
                Search(conn, in);
            }else if(choose.equals("2")){
                add(conn,in);
            }else if(choose.equals("3")){
                del(conn,in);
            }else if(choose.equals("4")){
            	display(conn,in);
            }else if(choose.equals("5")){
                CheckMeowt(conn,in);
            }else if(choose.equals("6")){
                FindOrder(conn,in);
            }else if(choose.equals("7")){
                RerunOrder(conn,in);
            }else if(choose.equals("8")){
                System.out.println("\nThank you, come again!");
                conn.close();
                try{
                    in.close();
                }catch(IOException e){
                    System.out.println("FK");
                    System.exit(1);
                }
                conn.close();
                System.exit(1);
            }else{
                continue;
            }
        }
    }
  
    //Manager section functions  
public static void Manager(Connection conn, BufferedReader in) throws SQLException{
        while(true){
            System.out.print("\n--------Menu--------\n\n(1) Adjust Customer Status\n(2) Change Item Price\n(3) Change user privileges\n(4) Print Monthly Stmt\n(5) Delete Unnecessary Transactions\n(6) Request Stock\n(7) Exit\n\n>> ");
            choose = getMyLine(in);
            if(choose.equals("1")){
                StatusAdjust(conn, in);
            }else if(choose.equals("2")){
                ChangePrice(conn,in);
            }else if(choose.equals("3")){
                managerize(conn,in);
            }else if(choose.equals("4")){
                printStmt(conn,in);
            }else if(choose.equals("5")){
				deleteUnnecessary(conn,in);
			}else if(choose.equals("6")){
				request(conn,in);
			}else if(choose.equals("7")){
                System.out.println("Logged out.");
                conn.close();
                try{
                    in.close();
                }catch(IOException e){
                    System.out.println("FK");
                    System.exit(1);
                }
                System.exit(1);
                
            }else{
                continue;
            }
        }
    }


   



//-------------------------------------------------------------------------------------------------------------------//
//      LOGIN
//-------------------------------------------------------------------------------------------------------------------//
public static Boolean Login(Connection conn, BufferedReader in) throws SQLException{
    
    System.out.print("\nUsername: ");
    Username = getMyLine(in);
    System.out.print("Password: ");
    Password = getMyLine(in);
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select CID from Customer where CID ='" + Username + "' AND Password = '" + Password + "'");
    if(rs.isBeforeFirst()){
		rs = stmt.executeQuery("select manager from customer where CID = '" + Username + "'");
		rs.next();
		if(rs.getInt(1) == 1){
			isManager = true;
		}else{
			isManager = false;
		}
        rs.close();
        return true;
    }
    rs.close();
    return false;
}

//-------------------------------------------------------------------------------------------------------------------//
//      REGISTER
//-------------------------------------------------------------------------------------------------------------------//
public static Boolean Register(Connection conn, BufferedReader in) throws SQLException{
    String Email = null;
    String FName = null;
    String LName = null;    
    System.out.print("Enter Username: ");
    Username = getMyLine(in);
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select CID from Customer where CID='" + Username + "'");
    if(rs.isBeforeFirst()){
        System.out.println("Username is already in use.  Please select different name.");
        Register(conn, in);
        return true;
    }
    System.out.print("Enter Password: ");
    Password = getMyLine(in);
    System.out.print("Enter your First Name: ");
    FName = getMyLine(in);
    System.out.print("Enter your Last Name: ");
    LName = getMyLine(in);
    System.out.print("Enter your Email: ");
    Email = getMyLine(in);
    if(Username.equals("") || Email.equals("") || FName.equals("") || LName.equals("") || Password.equals("")){
        System.out.println("\nInvalid entry somewhere.  Back to square one.\n");
        return false;
    }
    rs = stmt.executeQuery("insert into Customer Values('" + Username + "','" + Password + "','" + Email + "','New','" + FName + "','" + LName + "',0)");
	System.out.print("Would you like to enter your address? y/n\n>> ");
	int choice;
    String choose = "";
	while(true){
		choose = getMyLine(in);
		if(!(choose.equals("Y") || choose.equals("y") || choose.equals("N") || choose.equals("n"))){
			System.out.println("Invalid input.  Maybe try again.\n");
		}else if(choose.equals("Y") || choose.equals("y")){
			Address(conn, in);
			break;
		}else{
			break;
		}
	}
  
	rs.close();
    return true;
}
        
//-------------------------------------------------------------------------------------------------------------------//
//      ADDRESS
//-------------------------------------------------------------------------------------------------------------------//    
public static void Address(Connection conn, BufferedReader in) throws SQLException{
	String HNum = "";
	String Street = "";
	String City = "";
	String Apartment = "";
	String State = "";
	String Zip = "";
	
	while(true){
	System.out.print("Enter your House Number: ");
	HNum = getMyLine(in);
	System.out.print("Enter your Street: ");
	Street = getMyLine(in);
	System.out.print("Enter your City: ");
	City = getMyLine(in);
	System.out.print("Enter your Appartment#: ");
	Apartment = getMyLine(in);
	System.out.print("Enter your State: ");
	State = getMyLine(in);
	System.out.print("Enter your Zip Code: ");
	Zip = getMyLine(in);
    if((HNum.equals("") || Street.equals("") || City.equals("") || State.equals(""))){
        System.out.print("You entered an invalid value somewhere.\nBack to the menu.\n ");
        Customer(conn, in);
	}else{
        break;
    }
    }
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select count(*) from Address where CID = '" + Username + "'");
	rs.next();
	if(rs.getInt(1) == 0){
		rs = stmt.executeQuery("insert into address values ('" + Username + "', '" + HNum + "', '" + Street + "', '" + City + "', '" + Apartment + "', '" + State + "', '" + Zip + "')");
	}else{
		rs = stmt.executeQuery("Update address set HouseNumber = '" + HNum + "' where CID = '" + Username + "'");
		rs = stmt.executeQuery("Update address set Street = '" + Street + "' where CID = '" + Username + "'");
		rs = stmt.executeQuery("Update address set City = '" + City + "' where CID = '" + Username + "'");
		rs = stmt.executeQuery("Update address set Apartment = '" + Apartment + "' where CID = '" + Username + "'");
		rs = stmt.executeQuery("Update address set State = '" + State + "' where CID = '" + Username + "'");
		rs = stmt.executeQuery("Update address set ZIP = '" + Zip + "' where CID = '" + Username + "'");
	}
	
	System.out.println("Your address has been updated!\n");
	rs.close();

}
		
//-------------------------------------------------------------------------------------------------------------------//
//      SEARCH
//-------------------------------------------------------------------------------------------------------------------//    
public static void Search(Connection conn, BufferedReader in) throws SQLException{
    String StockNum = "";
    String Manufacturer = "";
    String Category = "";
    String Attribute = "";
    String Accessory = "";
    String Value = "";
    String Search = "";
    int choice;
    String choose = "";
    
    System.out.print("\n(1) Search for items\n(2) Search for accessory of an item\n\n>> ");
    choose = getMyLine(in);
    //Search by fields
    
	if(choose.equals("1")){
		System.out.println("Enter each field for search.  Skip fields you don't want to search by.");
		System.out.print("Stock Number: ");
		StockNum = getMyLine(in);
		System.out.print("Category: ");
		Category = getMyLine(in);
		System.out.print("Manufacturer: ");
		Manufacturer = getMyLine(in);
		System.out.print("Description: ");
		Attribute = getMyLine(in);
		System.out.print("Description Value: ");
		Value = getMyLine(in);
    

        if(!StockNum.equals("")){
            if(Search == ""){
                Search = Search + "where P.StockNum = '" + StockNum + "'";
            }else{
                Search = Search + " AND P.StockNum = '" + StockNum + "'";
            }
        }
        if(!Category.equals("")){
            if(Search == ""){
                Search = Search + "where P.Category = '" + Category + "'";
            }else{
                Search = Search + " AND P.Category = '" + Category + "'";
            }
        }
        if(!Manufacturer.equals("")){
            if(Search == ""){
                Search = Search + "where P.Manufacturer = '" + Manufacturer + "'";
            }else{
                Search = Search + " AND P.Manufacturer = '" + Manufacturer + "'";
            }
        }
        if(!Attribute.equals("")){
            if(Search == ""){
                Search = Search + "where P.StockNum IN( select D.StockNum from Description D where D.Attribute = '" + Attribute + "'";
            }else{
                Search = Search + " AND P.StockNum IN( select D.StockNum from Description D where D.Attribute = '" + Attribute + "'"; ;
            }
            if(!Value.equals("")){
                if(Search == ""){
                    Search = Search + "where D.Value = '" + Value + "')";
                }else{
                    Search = Search + " AND D.Value = '" + Value + "')";
                }
            }
        }
        if(Value.equals("") ^ Attribute.equals("")){
                System.out.println("\nNeed Attribute AND Value PAIR.\n");
                return;
        }
        
        
	Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select count(*) from Products P " + Search);
	rs.next();
	int count = rs.getInt(1);
    
    
	System.out.println("\n-------------------\n" + count + " Search Result(s): ");
	if(count == 0){
		System.out.println("\nNo items found based on your search Criteria.\n-------------------");
	}else{
		rs = stmt.executeQuery("select * from Products P " + Search);
		while(rs.next()){
			System.out.println("-------------------");
			String stknm = rs.getString(1);
			System.out.println("Stock Number: " + stknm);
			System.out.println("Category: " + rs.getString(2));
			System.out.println("Manufacturer: " + rs.getString(3));
			System.out.println("Model Number: " + rs.getString(4));
			System.out.println("Warranty: " + rs.getInt(5));
			System.out.println("Price: " + rs.getDouble(6));
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select * from Description D where D.StockNum = '" + stknm + "'");
			System.out.println("\nDescription:");
			while(rs2.next()){
				System.out.println(rs2.getString(2) + ": " + rs2.getString(3));
			}
			rs2.close();
		}
		rs.close();
    }
    
    //Search for accessories
    
	}else if(choose.equals("2")){
    System.out.print("\nEnter the Stock Number of the Item you want the accessory of:\n>> ");
	String item = getMyLine(in);
	Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select * from Products P where P.StockNum IN (select A.StockNum2 from Accessory A where A.StockNum = '" + item + "')");
	System.out.println("\n-------------------\n Accessories:");
    while(rs.next()){
			System.out.println("-------------------");
			String stknm = rs.getString(1);
			System.out.println("Stock Number: " + stknm);
			System.out.println("Category: " + rs.getString(2));
			System.out.println("Manufacturer: " + rs.getString(3));
			System.out.println("Model Number: " + rs.getString(4));
			System.out.println("Warranty: " + rs.getInt(5));
			System.out.println("Price: " + rs.getDouble(6));
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select * from Description D where D.StockNum = '" + stknm + "'");
			System.out.println("\nDescription:");
			while(rs2.next()){
				System.out.println(rs2.getString(2) + ": " + rs2.getString(3));
			}
		}
		rs.close();
       
	}else{
        Search(conn,in);
        return;
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
//      MAKE 2 DECIMAL PLACE
//-------------------------------------------------------------------------------------------------------------------//
public static double makeDecimal(double d){
	double val = Math.floor(d*100);
	//System.out.println(val);
	return val/100;
}

//-------------------------------------------------------------------------------------------------------------------//   
//      CHANGE PRICE
//-------------------------------------------------------------------------------------------------------------------//
public static void ChangePrice(Connection conn, BufferedReader in) throws SQLException{
	String Stocknum = "";
	String Price = "";
	
	System.out.print("\nStock Number whose price you want to change: ");
	Stocknum = getMyLine(in);
	
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select Stocknum from Products where Stocknum='" + Stocknum + "'");
	if(!rs.isBeforeFirst()){
        System.out.println("No such Stocknum, try a bit harder.");
        ChangePrice(conn, in);
        return;
    }
	
	System.out.print("Change price to: ");
	Price = getMyLine(in);
	
	rs = stmt.executeQuery("update Products P set P.Price = '" + Price + "' where P.Stocknum =  '" + Stocknum + "'");
    rs.close();
}


//-------------------------------------------------------------------------------------------------------------------//   
//      ADJUST STATUS
//-------------------------------------------------------------------------------------------------------------------//
public static void StatusAdjust(Connection conn, BufferedReader in) throws SQLException{
	String User = "";
	String Stat = "";
	System.out.print("\nUsername to change: ");
	User = getMyLine(in);
	
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select CID from Customer where CID='" + User + "'");
	//checking if valid Username
	if(!rs.isBeforeFirst()){
        System.out.println("No such Username, try a bit harder.");
        StatusAdjust(conn, in);
        return;
    }
	System.out.print("Change status to: ");
	Stat = getMyLine(in);
	while(!(Stat.equals("New") || Stat.equals("Green") || Stat.equals("Silver") || Stat.equals("Gold"))){
		System.out.println("No such Status, try that one again.");
		System.out.print("Change status to: ");
		Stat = getMyLine(in);
	}
	
	rs = stmt.executeQuery("update Customer C set C.Status = '" + Stat + "'where C.CID = '" + User + "'");
    rs.close();
	//check if CustomerID exists
	

}


//-------------------------------------------------------------------------------------------------------------------//
//      ADD ITEM TO CART
//-------------------------------------------------------------------------------------------------------------------//
public static void add(Connection conn, BufferedReader in) throws SQLException{
    String item = "";
    int Quantity;
    int inCart;
    System.out.print("\nEnter Stock Number to add to cart: ");
    item = getMyLine(in);
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select * from products where stocknum = '" + item + "'");
    if(!rs.isBeforeFirst()){
        System.out.println("\nItem does not exist.\n");
        return;
    }
    System.out.print("Enter Quantity desired: ");
    Quantity = Integer.parseInt(getMyLine(in));
    rs = stmt.executeQuery("select * from cart where CID = '" + Username + "' AND stocknum = '" + item + "'");
    if(rs.isBeforeFirst()){
        rs.next();
        inCart = rs.getInt(3);
        rs = stmt.executeQuery("update cart set Quantity = " + (inCart+Quantity) + " where CID = '" + Username + "' AND stocknum = '" + item + "'");
        System.out.println("\nYou added " + Quantity + " more of item " + item + " to your cart.");
    }else{
        rs = stmt.executeQuery("insert into Cart Values('" + Username + "', '" + item + "', '" + Quantity + "')");
        System.out.println("\nYou added " + Quantity + " of item " + item + " to your cart.");
    }
    rs.close();
    
}

//-------------------------------------------------------------------------------------------------------------------//
//      DELETE ITEM FROM CART
//-------------------------------------------------------------------------------------------------------------------//
public static void del(Connection conn, BufferedReader in) throws SQLException{
    String item = "";
    int Quantity, inCart;
    System.out.print("\nEnter Stock Number to delete from cart: ");
    item = getMyLine(in);
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select * from cart where stocknum = '" + item + "' AND CID = '" + Username + "'");
    if(!rs.isBeforeFirst()){
        System.out.println("\nItem not in your cart.\n");
        return;
    }
    System.out.print("Enter Quantity to be removed: ");
    Quantity = Integer.parseInt(getMyLine(in));
    rs.next();
    inCart = rs.getInt(3);
    if(inCart <= Quantity){
        rs = stmt.executeQuery("delete from cart where CID = '" + Username + "' AND stocknum = '" + item + "'");
        System.out.println("\nYou removed item " + item + " from your cart.");
    }else{
        rs = stmt.executeQuery("update cart set Quantity = " + (inCart-Quantity) + " where CID = '" + Username + "' AND stocknum = '" + item + "'");
        System.out.println("\nYou removed " + Quantity + " of item " + item + " from your cart.");
    }
    rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      DISPLAY ITEM FROM CART
//-------------------------------------------------------------------------------------------------------------------//
public static void display(Connection conn, BufferedReader in) throws SQLException{

	Statement stmt = conn.createStatement();
		
	ResultSet rs = stmt.executeQuery("select Ca.Stocknum, Ca.Quantity from Cart Ca where CA.CID = '" + Username + "'");
	System.out.println("\n-------------------\nCart:\n");
    while(rs.next()){
    	System.out.print("Stock Number: " + rs.getString(1));
		System.out.println("\t\tQuantity: " + rs.getString(2));
    }
    System.out.println("-------------------");
    rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      MAKE ORDER HAPPEN PLS
//-------------------------------------------------------------------------------------------------------------------//
public static void CheckMeowt(Connection conn, BufferedReader in) throws SQLException{
    String answer = "";
    String status = "";
    int quantity = 0;
    int ordno;
    String item = "";
    double price = 0;
    double shipping = 0;
    double discount = 0;
	double total = 0;
    java.util.Date date = new java.util.Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH)+1;
    int year = cal.get(Calendar.YEAR);
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select count(*) from cart where CID = '" + Username + "'");
    rs.next();
    if(rs.getInt(1) <= 0){
        System.out.println("\nThere is nothing in your cart to checkout.\n");
        return;
    }
    rs = stmt.executeQuery("select count(*) from orders");
    rs.next();
    ordno = rs.getInt(1);
    rs = stmt.executeQuery("select status from customer where CID = '" + Username + "'");
    rs.next();
    status = rs.getString(1).trim();
    
    rs = stmt.executeQuery("select * from Cart where CID = '" + Username + "'");
    System.out.println("\n-------------------\n");
    while(rs.next()){
        item = rs.getString(2);
        quantity = rs.getInt(3);
    	System.out.print("Stock Number: " + item);
		System.out.println("\t\tQuantity: " + quantity);
        ResultSet rs3 = stmt2.executeQuery("select price from products where stocknum = '" + item + "'");
        rs3.next();
        price = price + rs3.getDouble(1) * quantity;
    }
	price = makeDecimal(price);
    System.out.println("\nSubtotal:\t\t\t$" + price);
    if(price >= 100){
        shipping = makeDecimal(0.0);
    }else{
        shipping = makeDecimal(price*.1);
    }
    if(status.equals("Gold") || status.equals("New")){
        discount = (-1)*makeDecimal(price*.1);
    }else if(status.equals("Silver")){
        discount = (-1)*makeDecimal(price*.05);
    }else{
        discount = (-1)*makeDecimal(0.0);
    }
    System.out.println("Shipping:\t\t\t$" + shipping);
    System.out.println(status + " Discount:\t\t\t$" + discount);
    System.out.println("TOTAL:\t\t\t\t$" + (price+shipping+discount));
	total = price + shipping + discount;
    System.out.println("-------------------");
    
	rs = stmt.executeQuery("select count(*) from address where CID = '" + Username + "'");
	rs.next();
	if(rs.getInt(1) == 0){
		System.out.println("\nNo address on file.  Please fill in the following: \n");
		Address(conn, in);
	}
		String choose = "";
		while(true){
			rs = stmt.executeQuery("select * from address where CID = '" + Username + "'");
			rs.next();
            String address = "\n Shipping to:\n\t" + rs.getString(2) + " " + rs.getString(3) + "\n\t" + rs.getString(4);
            if(rs.getString(5) != null){
                address = address + " " + rs.getString(5);
            }
            address = address + ", " + rs.getString(6);
             if(rs.getString(7) != null){
                address = address + " " + rs.getString(7);
            }
			System.out.println(address);
			System.out.print("Is this address correct? y/n\n>> ");
			choose = getMyLine(in);
			if(!(choose.equals("Y") || choose.equals("y") || choose.equals("N") || choose.equals("n"))){
				System.out.println("\nDo you or don't you want to ship to the correct address.");
				System.out.println("-------------------");
			}else if(choose.equals("N") || choose.equals("n")){
				Address(conn, in);
				break;
			}else{
				break;
			}
		}
    
    
    while(true){
        System.out.print("\nAre you sure you want to check out? y/n\n>> ");
        answer = getMyLine(in);
        if(answer.equals("y") || answer.equals("Y")){
            ordno = ordno + 1;
            rs = stmt.executeQuery("select * from cart where CID = '" + Username + "'");
            ResultSet rs2;
            rs2 = stmt2.executeQuery("insert into Orders Values('" + ordno + "', '" + Username + "', 0, " + month + ", " + year + ")");
            while(rs.next()){
                item = rs.getString(2);
                quantity = rs.getInt(3);
                //System.out.println(item + ", " + quantity);
                rs2 = stmt2.executeQuery("select price from products where stocknum = '" + item + "'");
                rs2.next();
                double itemprice = rs2.getDouble(1);
                rs2 = stmt2.executeQuery("insert into OrderItems Values('" + ordno + "', '" + item + "', " + quantity + ", " + itemprice + ")");                
				rs2 = stmt2.executeQuery("insert into EMartOrder Values('" + ordno + "', '" + item + "', " + quantity + ", 0)");
            }
            rs2 = stmt2.executeQuery("update orders set Price = " + total + " where OrderID = '" + ordno + "'");
            rs2 = stmt2.executeQuery("delete from cart where CID = '" + Username + "'");
            rs2.close();
            
            System.out.println("\n\nCheckout complete! Your order number is:\n" + ordno + "\nThank you for your business!\n");
			
			rs = stmt.executeQuery("select Price from (select * from orders where cid = '" + Username + "' order by orderid DESC) where rownum <= 3");
			double adjust = 0;
			while(rs.next()){
				adjust = adjust + rs.getDouble(1);
			}
			if(adjust <= 100){
				rs = stmt.executeQuery("update customer set status = 'Green' where cid = '" + Username + "'");
			}else if(adjust > 100 && adjust <= 500){
				rs = stmt.executeQuery("update customer set status = 'Silver' where cid = '" + Username + "'");
			}else if(adjust > 500){
				rs = stmt.executeQuery("update customer set status = 'Gold' where cid = '" + Username + "'");
			}else{
				rs = stmt.executeQuery("update customer set status = 'Green' where cid = '" + Username + "'");
			}
			rs.close();
            return;
        }else if(answer.equals("n") || answer.equals("N")){
            System.out.println("Cancelling Order process.\n");
            rs.close();
            return;
        }else{
            System.out.println("Invalid choice.  Please enter 'y' or 'n'.");
        }
    }
    
}

//-------------------------------------------------------------------------------------------------------------------//
//      FIND ORDER MY FRIEND
//-------------------------------------------------------------------------------------------------------------------//
public static void FindOrder(Connection conn, BufferedReader in) throws SQLException{
	String OrderID = "";
	String choice= "";
	
	System.out.print("\nOrderID of Order you wish to find: ");
	OrderID = getMyLine(in);
	
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("select * from Orders O where O.OrderID = '" + OrderID + "'");
	if(!rs.isBeforeFirst()){
        System.out.println("No such Order ID, let's get you out of here.");
        return;
    }
	System.out.println("\n-------------------\nOrder ID: " + OrderID);
	rs = stmt.executeQuery("select O.OrderID, O.Price, O.Month, O.Year, OI.StockNum, OI.Quantity from Orders O, OrderItems OI where O.OrderID = '" + 
									  OrderID + "' AND O.OrderID = OI.OrderID AND O.CID = '" + Username + "'");
	
	while(rs.next()){
		System.out.print("\nStock Number: " + rs.getString(5));
		System.out.print("\tQuantity: " + rs.getInt(6));
		System.out.print("\tTotal Price: " + rs.getDouble(2));
		System.out.print("\tMonth: " + rs.getInt(3));
		System.out.print("\tYear: " + rs.getInt(4));
	}
	System.out.println("\n-------------------\n");

	rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      RE-RUN ORDER MY FRIEND
//-------------------------------------------------------------------------------------------------------------------//
public static void RerunOrder(Connection conn, BufferedReader in) throws SQLException{
	String OrderID = "";
	String Stocknum = "";
	int Quantity;
	int inCart;
	System.out.println("Your previous orders: ");
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select OrderID from Orders where cid = '" + Username + "'");
    if(!rs.isBeforeFirst()){
        System.out.println("\nYou have no orders...buy something first maybe.\n\n");
        return;
    }
    while(rs.next()){
        System.out.println("\t" + rs.getString(1));
    }
	System.out.print("\n\nOrderID you wish to re-run: ");
	OrderID = getMyLine(in);
	
	Statement stmt2 = conn.createStatement();
	
	rs = stmt.executeQuery("select Orderid from Orders where cid = '" + Username + "' AND OrderID = '" + OrderID + "'");
	if(!rs.isBeforeFirst()){
        System.out.println("That isn't a number from the list above...?");
        return;
    }
    
	
	ResultSet rs2 = stmt.executeQuery("select OI.StockNum, OI.Quantity from OrderItems OI, Orders O where OI.OrderID = '" + OrderID + 
									"' AND O.OrderID = OI.OrderID AND O.cid = '" + Username + "'" ); 
    
	Boolean skip = false;

	while(rs2.next()) {	
		ResultSet rs4;
		skip = false;
		Stocknum = rs2.getString(1);
		Quantity = rs2.getInt(2);
		ResultSet rs3 = stmt2.executeQuery("select C.StockNum, C.Quantity from Cart C where C.cid = '" + Username + "'");
		while(rs3.next()) {
			inCart = rs3.getInt(2);
			if (rs3.getString(1).equals(Stocknum)) {
				rs4 = stmt2.executeQuery("update cart set Quantity = " + (inCart + Quantity) + " where CID = '" + Username + "' AND Stocknum = '" + Stocknum + "'");
					skip = true;
				}
		}
		//System.out.println(Stocknum);
		//System.out.println(Quantity);
		if (skip)
			continue;
		rs4 = stmt2.executeQuery("insert into cart values('" + Username + "', '" + Stocknum + "', '" + Quantity + "')");
		rs4.close();
	}
	
	//straight to check-out
	CheckMeowt(conn, in);
	rs.close();
	rs2.close();
	
	
}
//-------------------------------------------------------------------------------------------------------------------//
//      CHANGE CUSTOMER TO MANAGER 
//-------------------------------------------------------------------------------------------------------------------//
public static void managerize(Connection conn, BufferedReader in) throws SQLException{
	String User = "";
	
	System.out.print("\nEnter Username you wish to give manager privileges: ");
	User = getMyLine(in);
	
	Statement stmt = conn.createStatement();
	
	ResultSet rs1 = stmt.executeQuery("select * from Customer C where C.CID = '" + User + "'");
	if(!rs1.isBeforeFirst()){
        System.out.println("No such CID, let's show some effort, shall we.");
        FindOrder(conn, in);
        return;
    }

	ResultSet rs = stmt.executeQuery("update Customer C set C.manager = 1 where C.CID = '" + User + "'");
	System.out.println(User + "'s privileges changed!");
	rs.close();

}

//-------------------------------------------------------------------------------------------------------------------//
//      PRINT STATEMENT FK
//-------------------------------------------------------------------------------------------------------------------//
public static void printStmt(Connection conn, BufferedReader in) throws SQLException{
    String item = "";
    String choose = "";
    int month = 0;
    int year = 0;
    double itemtotal = 0;
    int itemqnt = 0;
    double categorytotal = 0;
    double highcustomer = 0;
    String Customer = "";
    String ordno = "";
    String category = "";
    while(true){
        System.out.print("Input a month from 1 to 12: ");
        choose = getMyLine(in);
        if(choose.equals("")){
            month = -1;
        }else{
            month = Integer.parseInt(choose);
        }
        if(month < 1 || month > 12){
                System.out.println("Incorrect month.\n");
        }else{
            break;
        }
    }
    while(true){
        System.out.print("Input a year from 1950 and beyond: ");
        choose = getMyLine(in);
        if(choose.equals("")){
            year = -1;
        }else{
            year = Integer.parseInt(choose);
        }
        if(year < 1950){
                System.out.println("Incorrect year.\n");
        }else{
            break;
        }
    }
    System.out.println("----MONTHLY STATEMENT----\n");
    System.out.println("Item Sales: ");
    Statement stmt = conn.createStatement();
    Statement stmt2 = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select stocknum from products");
    while(rs.next()){
        item = rs.getString(1);
        itemtotal = 0;
        itemqnt = 0;
        //System.out.println("select (quantity*price), quantity from OrderItems where Stocknum = '" + item + "' AND OrderID IN(select P.OrderID from Orders P where P.month = " + month + ", AND P.year = " + year +" )"); 
        ResultSet rs2 = stmt2.executeQuery("select (quantity*price), quantity from OrderItems where Stocknum = '" + item + "' AND OrderID IN(select P.OrderID from Orders P where P.month = " + month + " AND P.year = " + year +" )"); 
        while(rs2.next()){
            itemtotal = itemtotal + rs2.getDouble(1);
            itemqnt = itemqnt + rs2.getInt(2);
        }
        System.out.println("\tItem " + item + " - \tPrice: $" + itemtotal + "\n\t\t\tQnt: " + itemqnt);
    }
    System.out.println("Category Sales: ");
    rs = stmt.executeQuery("select distinct category from products");
    while(rs.next()){
        category = rs.getString(1);
        itemtotal = 0;
        itemqnt = 0;
        //System.out.println("select (quantity*price), quantity from OrderItems where Stocknum = '" + item + "' AND OrderID IN(select P.OrderID from Orders P where P.month = " + month + ", AND P.year = " + year +" )"); 
        ResultSet rs2 = stmt2.executeQuery("select (quantity*price), quantity from OrderItems where OrderID IN(select P.OrderID from Orders P where P.month = " + month + " AND P.year = " + year +" ) AND stocknum IN (select T.stocknum from products T where category = '" + category + "')"); 
        while(rs2.next()){
            itemtotal = itemtotal + rs2.getDouble(1);
            itemqnt = itemqnt + rs2.getInt(2);
        }
        System.out.println("\tCategory " + category + " - \tPrice: $" + itemtotal + "\n\t\t\t\tQnt: " + itemqnt);
    }
    System.out.println("Highest Customer: ");
    rs = stmt.executeQuery("select distinct O.OrderID from Orders O where O.month = " + month + " AND O.year = " + year + " AND O.OrderID IN(select distinct P.OrderID from OrderItems P)");
	Statement stmt3 = conn.createStatement();
	ResultSet rs3 = stmt3.executeQuery("create table custhigh ( Price REAL, Quantity INTEGER, CID VARCHAR(20), PRIMARY KEY(CID))");
    while(rs.next()){
        itemtotal = 0;
        itemqnt = 0;
        ordno = rs.getString(1);
        //System.out.println("select (quantity*price), quantity, OrderID from OrderItems where Stocknum = '" + item + "' AND OrderID IN(select P.OrderID from Orders P where P.month = " + month + ", AND P.year = " + year +" )"); 
        //ResultSet rs2 = stmt2.executeQuery("select (quantity*price), quantity from OrderItems where OrderID IN(select P.OrderID from Orders P, Orders O where P.CID = '" + Customer + "' AND P.month = " + month + " AND P.year = " + year +")"); 
        ResultSet rs2 = stmt2.executeQuery("select (O.quantity*O.price) as col1, O.quantity as col2, C.CID as col3 from OrderItems O, Orders P, Customer C where O.orderid = '" + ordno + "' AND O.orderid = P.orderid AND C.CID = P.CID");
        
        
        while(rs2.next()){
            highcustomer = rs2.getDouble(1);
            itemqnt = rs2.getInt(2);
            Customer = rs2.getString(3);
            Statement s = conn.createStatement();
			ResultSet rs4 = s.executeQuery("select CID from custhigh where CID = '" + Customer + "'");
			if(rs4.isBeforeFirst()){
				rs4 = s.executeQuery("select * from custhigh where CID = '" + Customer + "'");
				rs4.next();
				int q = rs4.getInt(2) + itemqnt;
				double p = rs4.getDouble(1) + highcustomer;
				rs4 = s.executeQuery("update custhigh set Price = " + p + " where CID = '" + Customer + "'");
				rs4 = s.executeQuery("update custhigh set Quantity = " + q + " where CID = '" + Customer + "'");
			}else{
				rs4 = s.executeQuery("insert into custhigh values(" + highcustomer + ", " + itemqnt + ", '" + Customer + "')");
			}
			rs4.close();
        }
		rs2.close();
    }
	rs = stmt.executeQuery("select A.Price, A.Quantity, A.CID from custhigh A where A.Price >= ALL(select MAX(B.Price) from custhigh B)");
	while(rs.next()){
		System.out.println("\t" + rs.getString(3) + "\t\tPrice: " + rs.getDouble(1) + "\n\t\t\tQnt: " + rs.getInt(2));
	}
	rs = stmt.executeQuery("drop table custhigh");
	rs.close();
	rs3.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      DELETE ALL UNNECESSARY TRANSACTIONS YO
//-------------------------------------------------------------------------------------------------------------------//
public static void deleteUnnecessary(Connection conn, BufferedReader in) throws SQLException{
	String User = "";
	String ooID = "";

	System.out.print("\nSelect Username to remove unneccessary transactions from list: \n");
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select cid from customer where manager = 0");
    if(!rs.isBeforeFirst()){
        System.out.println("You have no customers...kind of sad.\n\n");
        return;
    }
    while(rs.next()){
        System.out.println("\t" + rs.getString(1));
    }
    System.out.print("\n>> ");
	User = getMyLine(in);
	
	Statement stmt2 = conn.createStatement();
	rs = stmt.executeQuery("select * from Customer C where C.CID = '" + User + "'");
	if(!rs.isBeforeFirst()){
        System.out.println("Of all the Usernames in the database, you went ahead and chose a non-existant one.");
        //deleteUnnecessary(conn, in);
        return;
    }
	
	//rs = stmt.executeQuery("select * from (select * from Orders where CID = '" + Username + "' Order by OrderID DESC) where rownum > 3"); 
	rs = stmt.executeQuery("select * from Orders where CID = '" + User + "' MINUS (select * from (select * from Orders where CID = '" + User + "' Order by OrderID DESC) where rownum <= 3)");

	
	while(rs.next()){
		ResultSet rs1;
		ooID = rs.getString(1);
		rs1 = stmt2.executeQuery("delete from OrderItems where OrderID = '" + ooID + "'");
		rs1 = stmt2.executeQuery("delete from Orders where OrderID = '" + ooID + "'");
		rs1.close();
	}
	
	System.out.println("\nUnnecessary entries for " + User + " removed."); 
	rs.close();
}

//-------------------------------------------------------------------------------------------------------------------//
//      MANAGER ORDER REQUEST
//-------------------------------------------------------------------------------------------------------------------//
public static void request(Connection conn, BufferedReader in) throws SQLException{
	String item = "";
	int quantity = 0;
	String choice = "";
	 java.util.Date date = new java.util.Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH)+1;
    int year = cal.get(Calendar.YEAR);
	Statement stmt = conn.createStatement();
	
	while(true){
        System.out.print("\n\n(1) Add items\n(2) Delete items\n(3) Exit\n>> ");
        choice = getMyLine(in);
        if(choice.equals("1")){
		System.out.print("\nEnter the stock number of the item you wish to request: ");
		item = getMyLine(in);
		ResultSet rs0 = stmt.executeQuery("select * from products where stocknum = '" + item + "'");
		if(rs0.isBeforeFirst()){
			break;
		}else{
			System.out.println("Stock Number is not a valid number.\n\n");
		}
		rs0.close();
    }else if(choice.equals("2")){
        ResultSet rs6 = stmt.executeQuery("select Ca.Stocknum, Ca.Quantity from Cart Ca where CA.CID = '" + Username + "'");
		System.out.println("\n-------------------\nRequest:\n");
		while(rs6.next()){
			System.out.print("Stock Number: " + rs6.getString(1));
			System.out.println("\t\tQuantity: " + rs6.getString(2));
		}
		System.out.println("-------------------");
        rs6.close();
        del(conn,in);
    }else if(choice.equals("3")){
        return;
    }else{
        continue;
    }
	}
	System.out.print("Enter the quantity of the item are requesting: ");
	quantity = Integer.parseInt(getMyLine(in));
	ResultSet rs = stmt.executeQuery("select quantity from cart where cid = '" + Username + "' AND stocknum = '" + item + "'");

		if(rs.isBeforeFirst()){
			rs.next();
			System.out.println("Adding " + quantity + " more of item " + item + " to your current request.");
			quantity = quantity + rs.getInt(1);
			rs = stmt.executeQuery("update cart set quantity = " + quantity + " where stocknum = '" + item + "' AND cid = '" + Username + "'");
		}else{
			System.out.println("Requested " + quantity + " of item " + item + ".");
			rs = stmt.executeQuery("insert into cart values('" + Username + "', '" + item + "', " + quantity + ")");
		}
		rs = stmt.executeQuery("select Ca.Stocknum, Ca.Quantity from Cart Ca where CA.CID = '" + Username + "'");
		System.out.println("\n-------------------\nRequest:\n");
		while(rs.next()){
			System.out.print("Stock Number: " + rs.getString(1));
			System.out.println("\t\tQuantity: " + rs.getString(2));
		}
		System.out.println("-------------------");
		while(true){
			System.out.print("\n(1) Add more items\n(2) Process Request\n(3) Process Later\n>> ");
			choice = getMyLine(in);
			if(choice.equals("1")){
				request(conn,in);
			}else if(choice.equals("2")){
				int order = 0;
				rs = stmt.executeQuery("select count(orderid) from orders");
                rs.next();
				order = rs.getInt(1);
				order++;
				//System.out.println("OrderID: " + order);
				rs = stmt.executeQuery("select stocknum, quantity from cart where cid = '" + Username + "'");
				Statement stmt2 = conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery("insert into Orders values('" + order + "', '" + Username + "', 0, " + month + ", " + year + ")");
				while(rs.next()){
					//System.out.println("Here");
					rs2 = stmt2.executeQuery("insert into EMartOrder values('m" + order + "', '" + rs.getString(1) + "', " + rs.getInt(2) + ", 0)");
					//System.out.println("Here2");
					rs2.close();
				}
				rs = stmt.executeQuery("delete from cart where cid = '" + Username + "'");
				rs.close();
				return;
			}else if(choice.equals("3")){
			rs.close();
				return;
			}else{
				continue;
			}
		
		}
    
		
	

}

}




