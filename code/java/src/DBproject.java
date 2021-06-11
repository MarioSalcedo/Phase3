/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException; 

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		Scanner in = new Scanner(System.in);
		int docid = 0;
		System.out.println("Please input the doctors name (first and last): ");
		String dname = in.nextLine();
		System.out.println("Please input the doctors speciality: ");
		String spec = in.nextLine();
		String dupcheck = "SELECT name FROM Doctor WHERE name = '"+dname+"' AND specialty = '"+spec+"' ";
		try { 
		if (esql.executeQuery(dupcheck)>0){
			System.out.println("This doctor already exists!");
			return;
		} 
		else{
			String count = "SELECT doctor_ID FROM Doctor";
			try {
				docid = esql.executeQuery(count);
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Please input the doctors department ID: ");
			int did = Integer.parseInt(in.nextLine());
			System.out.println(did);
			System.out.println(docid);
			String putIn = "INSERT INTO Doctor (doctor_ID, name,specialty, did) VALUES ('"+docid+"', '"+dname+"', '"+spec+"', '"+did+"')";
			try{
				esql.executeUpdate(putIn);
			}
			catch (SQLException e) {
				e.printStackTrace();
			} 
		}
		}  catch(SQLException e) {
                                e.printStackTrace();
                }

		return;
	}

	public static void AddPatient(DBproject esql) {//2
		Scanner in = new Scanner(System.in);
		char g; int pid = 0;
		System.out.println("Please input the patient name (first and last): ");
		String pname = in.nextLine();
		System.out.println("Please input the gender (M/F): ");
		String gender = in.nextLine();

		while(!(Objects.equals(gender,"M")) && !(Objects.equals(gender,"F")) ) {
			System.out.println("Invalid Input. Please input the gender (M/F): ");
			gender = in.nextLine();
		}
		g = gender.charAt(0);
		System.out.println("Please input age: ");
		int page = Integer.parseInt(in.nextLine());

		System.out.println("Please input address: ");
		String padd = in.nextLine();

		String dupcheck = "SELECT name FROM Patient WHERE name = '"+pname+"' AND gtype = '"+g+"' AND age = '"+page+"' AND address = '"+padd+"' ";


		try {

		if (esql.executeQuery(dupcheck)>0){
			System.out.println("This Patient already exists!");
			return;}
		else{
			String count = "SELECT patient_ID FROM Patient";
			try {
				pid = esql.executeQuery(count);
			}
			catch(SQLException e) {
                        	e.printStackTrace();
			}
			String putIn = "INSERT INTO Patient VALUES ('"+pid+" ', '"+pname+"', '"+g+"', '"+page+"', '"+padd+"', 0)";
			
			try {
			esql.executeUpdate(putIn);
			} catch (SQLException e) {
                        e.printStackTrace();
                	}

		}
		} catch(SQLException e) {
			e.printStackTrace();	
		}
		return;

	}

	public static void AddAppointment(DBproject esql) {//3
		Scanner in = new Scanner (System.in);
		boolean loop = true; Date date2 = null;
		while (loop) {
		System.out.println("Please input the date of your desired appointment (MM/DD/YYYY):");
		String date = in.nextLine();
            	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
    			date2 = dateFormat.parse(date);	
			loop = false;
		} catch (ParseException e) {
    			e.printStackTrace();
		}
		System.out.println(date2); 
		}
	
		System.out.println("Please assign a time slot of your appointment: ");
		System.out.println("Start Time ");
		String value1 = in.nextLine(); String[] arraySplit = value1.split(":");
		int num1 = Integer.parseInt(arraySplit[0]); int num2 = Integer.parseInt(arraySplit[1]);
		boolean isBeg = true;
		if ((num1 >= 8 && num1 <=16) && num2 == 0) { isBeg = false; }
		while (isBeg) {
			System.out.println("Invalid Input. Please input a start time no earlier than 8:00 and no later than 16:00. Note: appointments only start at an hour sharp.");
			value1 = in.nextLine(); arraySplit = value1.split(":");
			num1 = Integer.parseInt(arraySplit[0]); num2 = Integer.parseInt(arraySplit[1]);
			if ((num1 >= 8 && num1 <=16) && num2 == 0) { isBeg = false; }
		}
		System.out.println("End Time (Can't be earlier or equal to your start time and no later than 17:00)");
		String value2 = in.nextLine(); String[] arraySplit2 = value2.split(":");
		int num3 = Integer.parseInt(arraySplit2[0]); 
		while (num3 <= num1 || num3 > 17) {
			System.out.println("Invalid Input. Please input an end time no earlier than your           start time and no later than 17:00");
			value2 = in.nextLine(); arraySplit2 = value2.split(":");
			num3 = Integer.parseInt(arraySplit2[0]);
		}
		if (num3 == 17) { arraySplit2[1] = "00"; }
		String begin = String.valueOf(num1); String end = String.valueOf(num2);
		String timeslot = value1+"-"+arraySplit2[0]+":"+arraySplit2[1];
		System.out.println(timeslot);

		String dupcheck = "SELECT adate FROM Appointment WHERE time_slot = '"+timeslot+"' AND adate = '"+date2+"'";
		try {
		if (esql.executeQuery(dupcheck) > 0) {
			System.out.println("This time slot is already taken!");
			return;
		} else {
			String count = "SELECT appnt_ID FROM Appointment";
			int appntid = esql.executeQuery(count);
			String putIn = "INSERT INTO Appointment VALUES ('"+appntid+"','"+date2+"','"+timeslot+"','AV')";
			try { esql.executeUpdate(putIn); } catch (SQLException e) { e.printStackTrace(); }
		}
		} catch (SQLException e) {
                        e.printStackTrace();
                }
	return;
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		Scanner in = new Scanner (System.in);
		int docid = 0; boolean loop = true;
		Date date = null; Date date2 = null;	
		while(loop) {
			System.out.println("Please input your doctor ID"); 
			try {
				docid = Integer.parseInt(in.nextLine());
				loop = false;
			}
			catch(Exception e) {
				System.out.println("Invalid doctor ID!");
				System.out.println(e);
			}
		}
		/*System.out.println(docid);*/

		loop = true;
		while(loop) {
			System.out.println("Insert start date");
			String line = in.nextLine();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			try {
    				date = dateFormat.parse(line);
    				loop = false;
			}  catch (ParseException e) {
				System.out.println("Invalid date!");
    				e.printStackTrace();
			}
		}
		/*System.out.println(date);*/
		loop = true;
		while(loop) {
			System.out.println("Insert end date");
			String line2 = in.nextLine();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
    			date2 = dateFormat.parse(line2);
			if (date.before(date2) || date.equals(date2)) {
				loop = false; break;
			}
			System.out.println("Invalid date, please choose a date after " + date);
		} catch (ParseException e) {
			System.out.println("Invalid date!");
    			e.printStackTrace();	
		}
		}		
		/*System.out.println(date2);*/
		try {
			String psqlQuery = "SELECT A.adate, A.appnt_ID, A.status  FROM Appointment A, Doctor D, has_appointment H WHERE H.doctor_id = '"+docid+"' AND A.adate >= '"+date+"' AND A.adate <= '"+date2+"' AND H.appt_id = A.appnt_ID AND H.doctor_id = D.doctor_ID AND (A.status = 'AC' OR A.status = 'AV') ORDER BY A.appnt_ID";	
		esql.executeQueryAndPrintResult(psqlQuery);
		} catch(Exception e) {
			System.out.println("This doctor ID does not exist");
			System.out.println(e);
		}
	}



	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		int i = 0;
		int k = 0;
		int a = -1;
		int b = -1;
		int c = -1;
		int d = -1;
		int uAV = 0;
		int uAC = 0;
		int uWL = 0;
		int uPA = 0;
		String doc = "SELECT* FROM Doctor";
		try{
		while(i < esql.executeQuery(doc)){
			String getdoc = "SELECT name FROM Doctor WHERE doctor_ID = '"+i+"'";
			try{
			esql.executeQueryAndPrintResult(getdoc);
			}catch(SQLException e) {e.printStackTrace();}
			String getAV = "SELECT * FROM Appointment A, has_appointment H WHERE A.status = 'AV' AND H.doctor_id='"+i+"' AND H.appt_id=A.appnt_ID";
			String getAC = "SELECT * FROM Appointment A, has_appointment H WHERE A.status = 'AC' AND H.doctor_id='"+i+"' AND H.appt_id=A.appnt_ID";
			String getWL = "SELECT * FROM Appointment A, has_appointment H WHERE A.status = 'WL' AND H.doctor_id='"+i+"' AND H.appt_id=A.appnt_ID";
			String getPA = "SELECT * FROM Appointment A, has_appointment H WHERE A.status = 'PA' AND H.doctor_id='"+i+"' AND H.appt_id=A.appnt_ID";
			try{
			a = esql.executeQuery(getAV);
			} catch(SQLException e) {e.printStackTrace();}
			try{
			b = esql.executeQuery(getAC);
			} catch(SQLException e) {e.printStackTrace();}
			try{
			c = esql.executeQuery(getWL);
			}catch(SQLException e) {e.printStackTrace();}
			try{
			d = esql.executeQuery(getPA);
			}catch(SQLException e) {e.printStackTrace();}
			ArrayList<Integer> orderdown = new ArrayList<Integer>();
			orderdown.add(a);
			orderdown.add(b);
			orderdown.add(c);
			orderdown.add(d);
			Collections.sort(orderdown, Collections.reverseOrder());
			while( k < 4){
				if(orderdown.get(k) == 0){}
				else if(orderdown.get(k) == a && uAV == 0){
					System.out.print(orderdown.get(k));
					System.out.print(" AV ");
					uAV = 1;
				}
				else if(orderdown.get(k) == b && uAC == 0){
					System.out.print(orderdown.get(k));
					System.out.print(" AC ");
					uAC = 1;
				}
				else if(orderdown.get(k) == c && uWL == 0){
					System.out.print(orderdown.get(k));
					System.out.print(" WL ");
					uWL = 1;
				}
				else if(orderdown.get(k) == d && uPA == 0){
					System.out.print(orderdown.get(k));
					System.out.print(" PA ");
					uPA  = 1;
				}
				else{System.out.print("what?howd you get here?");}
				++k;
			}
			System.out.println();
			k=0;
			++i;
			uAV = 0;
			uAC = 0;
			uWL = 0;
			uPA = 0;
		}
		} catch(SQLException e) {e.printStackTrace();}
			System.out.println();
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		Scanner in = new Scanner(System.in);
		System.out.println("Please give a status (AV, AC, WL, PA): ");
		String stat = in.nextLine();
		int i = 0;
		String doc = "SELECT* FROM Doctor";
		try{
		while(i < esql.executeQuery(doc)){
			String getdoc = "SELECT name FROM Doctor WHERE doctor_ID = '"+i+"'";
			try{
			esql.executeQueryAndPrintResult(getdoc);
			}catch(SQLException e) {e.printStackTrace();}
			String getcount = "SELECT * FROM Appointment A, has_appointment H WHERE A.status = '"+stat+"' AND H.doctor_id='"+i+"' AND H.appt_id = A.appnt_ID";
			try{ 
			System.out.println(" has " + esql.executeQuery(getcount) + "'"+stat+"'");
			}catch(SQLException e) {e.printStackTrace();}
			++i;
		}
		}catch(SQLException e) {e.printStackTrace();}
	}
}
