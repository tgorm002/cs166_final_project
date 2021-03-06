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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

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
		while(rs.next()){ //was if here -------------------------------------------------------------------------------
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
		try{
			String query = "insert into Doctor (doctor_ID, name, specialty, did) values (";
			
			System.out.print("\tEnter Doctor ID: ");
			String id = in.readLine();
			query += id; //gets the doctor ID
			
			System.out.print("\tEnter Doctor name: ");
			String doc_name = in.readLine();
			query += ", '";
			query += doc_name; //gets the doctor name
			query += "', "; //query added as: , 'doc_name',

			System.out.print("\tEnter Doctor specialty: ");
			String doc_spec = in.readLine();
			query += "'";
			query += doc_spec; //gets the doctor specailty
			query += "', "; //query added as: 'doc_spec', 

			System.out.print("\tEnter Doctor Department ID: ");
			String doc_did = in.readLine();
			query += doc_did; //gets the doctor department id
			query += ")"; //need closing parenthases here
   
			//int rowCount = esql.executeQuery(query); 
			esql.executeUpdate(query);
			System.out.println ("ADDED VALUES");
			//System.out.println ("total row(s): " + rowCount);
		 }
		 catch(Exception e){
			System.err.println (e.getMessage());
		 }
	}

	public static void AddPatient(DBproject esql) {//2
		try{
			String query = "insert into Patient (patient_ID, name, gtype, age, address, number_of_appts) values (";

			System.out.print("\tEnter Patient ID: ");
			String id = in.readLine();
			query += id; 
			
			System.out.print("\tEnter Patient name: ");
			String pat_name = in.readLine();
			query += ", '";
			query += pat_name; 
			query += "', "; //query added as: , 'pat_name',

			System.out.print("\tEnter Patient gender: "); //looks like it either takes 'M' or 'F' soooo 2 genders??????????????
			String pat_gender = in.readLine();
			query += "'";
			query += pat_gender; 
			query += "', "; //query added as: 'pat_gender',

			System.out.print("\tEnter Patient age: ");
			String age = in.readLine();
			query += age; 

			System.out.print("\tEnter Patient address: ");
			String pat_address = in.readLine();
			query += ", '";
			query += pat_address; 
			query += "', "; //query added as: , 'pat_address',

			System.out.print("\tEnter number of appointments for the Patient: ");
			String num_appts = in.readLine();
			query += num_appts; 
			query += ")"; //need closing parenthases here


			//int rowCount = esql.executeQuery(query);
			esql.executeUpdate(query);
			System.out.println ("ADDED VALUES");
			//System.out.println ("total row(s): " + rowCount);
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		 }
	}

	public static void AddAppointment(DBproject esql) {//3
		try{
			String query  = "insert into Appointment (appnt_ID, adate, time_slot, status) values (";
			
			System.out.print("\tEnter Appointment ID: ");
			String id = in.readLine();
			query += id; 

			System.out.print("\tEnter Appointment month: ");
			String appt_m = in.readLine();
			query += ", '";
			query += appt_m; 
			query += "/"; //query added as: , 'appt_m/

			System.out.print("\tEnter Appointment day: ");
			String appt_d = in.readLine();
			query += appt_d; 
			query += "/"; //query added as: appt_d/

			System.out.print("\tEnter Appointment year: ");
			String appt_y = in.readLine();
			query += appt_y; 
			query += "', "; //query added as: appt_y', 

			System.out.print("\tEnter Appointment starting time in this format 'hour:minutes' (ie. 8:00 or 14:30): ");
			String appt_startTime = in.readLine();
			query += "'";
			query += appt_startTime; 
			query += "-"; //query added as: 'appt_startTIme-

			System.out.print("\tEnter Appointment ending time in this format 'hour:minutes' (ie. 8:00 or 14:30): ");
			String appt_endTime = in.readLine();
			query += appt_endTime; 
			query += "', "; //query added as: appt_endTime', 

			System.out.print("\tEnter Appointment status: ");
			String appt_status = in.readLine();
			query += "'";
			query += appt_status; 
			query += "')"; //query added as: 'appt_status') with ending parenthases

			//int rowCount = esql.executeQuery(query);
			esql.executeUpdate(query);
			System.out.println ("ADDED VALUES");
			//System.out.println ("total row(s): " + rowCount);

		}
		catch(Exception e){
			System.err.println (e.getMessage());
		 }

	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		try{
			String query  = "if exists(select * from Doctor D, Appointment A, has_appointment H where D.doctor_ID = ";

			System.out.print("\tWe will take in the doctor's id: ");
			String doc_id = in.readLine();
			query += doc_id;

			query += " and D.doctor_ID = H.doctor_id and H.appt_id = ";

			System.out.print("\tNow we will take in the appointment id: ");
			String appointment_id = in.readLine();
			query += appointment_id;

			query += " and A.status = 'AV') begin insert into Patient (patient_ID, name, gtype, age, address, number_of_appts) values (";

			System.out.println("\tWe will now take in your patient info :)");
			
			System.out.print("\tEnter Patient ID: ");
			String id = in.readLine();
			query += id; 
			
			System.out.print("\tEnter Patient name: ");
			String pat_name = in.readLine();
			query += ", '";
			query += pat_name; 
			query += "', "; //query added as: , 'pat_name',

			System.out.print("\tEnter Patient gender: "); //looks like it either takes 'M' or 'F' soooo 2 genders??????????????
			String pat_gender = in.readLine();
			query += "'";
			query += pat_gender; 
			query += "', "; //query added as: 'pat_gender',

			System.out.print("\tEnter Patient age: ");
			String age = in.readLine();
			query += age; 

			System.out.print("\tEnter Patient address: ");
			String pat_address = in.readLine();
			query += ", '";
			query += pat_address; 
			query += "', "; //query added as: , 'pat_address',

			System.out.print("\tEnter number of appointments: ");
			String num_appts = in.readLine();
			query += num_appts; 
			query += ")"; //need closing parenthases here

			//int rowCount = esql.executeQuery(query);
			esql.executeUpdate(query);
			System.out.println ("ADDED VALUES");
			//System.out.println ("total row(s): " + rowCount);

		}
		catch(Exception e){
			System.err.println (e.getMessage());
		 }
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		//https://stackoverflow.com/questions/14208958/select-data-from-date-range-between-two-dates
		//so after seeing this stack overflow website i am now thinking that dates are not strings which means some of my insert into statments would be wrong but should be an easy fix
		//nvm they are strings HEHEH
		//thesres this thing called to date but i dont wana use that 
		try {
			String query  = "select A.adate, A.appnt_ID from Appointment A, Doctor D, has_appointment H where H.doctor_id = ";


			System.out.print("\tEnter Doctor ID: ");
			String doc_ID = in.readLine();
			query += doc_ID; 

			query += " and H.appt_id = A.appnt_ID and (A.status = 'AC' or A.status = 'AV') and A.adate > ";

			System.out.print("\tEnter a left bound date in this format: month/day/year (3/10/2021 or 11/5/2020): ");
			String dateStr = in.readLine();
			query += "'";
			query += dateStr;
			query += "'";

			query += " and A.adate < ";

			System.out.print("\tEnter a right bound date in this format: month/day/year (3/10/2021 or 11/5/2020): ");
			String dateStr2 = in.readLine();
			query += "'";
			query += dateStr2;
			query += "' group by A.appnt_ID";

			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println ("total row(s): " + rowCount);
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		 }
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		try{ //okay this is how it connects: Department -> Doctor id thoruhg request maint -> appt id through has appointment -> appointment outputs ids for all availble that match the date 
			String query = " select A.appnt_ID from Appointment A, Department D, request_maintenance R, has_appointment H where R.dept_name = ";
			
			System.out.print("\tEnter Department name: ");
			String dname = in.readLine();
			query += "'";
			query += dname; 
			query += "'";

			query += "and R.did = H.doctor_id and H.appt_id = A.appnt_ID and A.status = 'AV' and A.adate = ";

			System.out.print("\tEnter a date in this format: month/day/year (3/10/2021 or 11/5/2020): ");
			String dateStr = in.readLine();
			query += "'";
			query += dateStr;
			query += "' group by A.appnt_ID";
			

			//int rowCount = esql.executeQuery(query);
			int rowCount = esql.executeQueryAndPrintResult(query);

			System.out.println ("total row(s): " + rowCount);
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		}
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		//asuming this one has no user input
		//just lists the doctors in descedning order based on how many appointments they have with all types of appointments
		try{
			//this link for help:https://learnsql.com/cookbook/how-to-order-by-count-in-sql/#:~:text=The%20first%20step%20is%20to,IDs%20with%20COUNT(id)%20.
			//String query  = "select A.status, count(*) from Doctor D, Appointment A, has_appointment H where D.doctor_ID = H.doctor_id and H.appt_id = A.appnt_ID group by A.status order by count(*) desc";
			String query  = "select D.name, A.status, count(*) from Doctor D, Appointment A, has_appointment H where D.doctor_ID = H.doctor_id and H.appt_id = A.appnt_ID group by D.name, A.status order by count(*) desc";
			//may want count(A.status) here

			//int rowCount = esql.executeQuery(query);
			int rowCount = esql.executeQueryAndPrintResult(query);

			System.out.println ("total row(s): " + rowCount);	
		}		
		catch(Exception e){
			System.err.println (e.getMessage());
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		try{
			String query = "select D.name, count(A.status) from Appointment A, Doctor D, has_appointment H where A.appnt_ID = H.appt_id and H.doctor_id = D.doctor_ID and A.status = " ;

			System.out.print("\tEnter Appointment status: ");
			String statass = in.readLine();
			query += "'";
			query += statass; 
			query += "'";
			query += " group by D.name";

			//int rowCount = esql.executeQuery(query);
			//int rowCount = esql.executeQueryAndReturnResult(query);
			int rowCount = esql.executeQueryAndPrintResult(query);

			System.out.println ("total row(s): " + rowCount);
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		}
	}
} 