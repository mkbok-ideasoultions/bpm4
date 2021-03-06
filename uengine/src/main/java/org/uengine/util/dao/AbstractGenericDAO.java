package org.uengine.util.dao;

import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Clob; //freshka 2015.10.21
import java.sql.Blob; //freshka 2015.10.21
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ReleaseResourceListener;
import org.uengine.kernel.UEngineException;
import org.uengine.persistence.dao.DAOFactory;
import org.uengine.processmanager.ProcessManagerBean;
import org.uengine.processmanager.ProcessTransactionContext;
import org.uengine.processmanager.TransactionContext;
import org.uengine.util.ForLoop;

import com.sun.rowset.CachedRowSetImpl;



/**
 * Generic DAO
 *
 * @author <a href="mailto:ghbpark@hanwha.co.kr">Sungsoo Park</a>, Jinyoung Jang
 * @version $Id: AbstractGenericDAO.java,v 1.34 2011/07/22 07:33:20 curonide Exp $
 */
public abstract class AbstractGenericDAO implements InvocationHandler, IDAO {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;	
//	protected boolean isAutoCommit = true;
	
	protected boolean isConnective = false;
		public boolean isConnective() {
			return isConnective;
		}
		public void setConnective(boolean isConnective) {
			this.isConnective = isConnective;
		}
		
	private boolean isDirty = false;
	
	private boolean isUpdateOnlyWhenDirty = true;
		protected boolean updateOnlyWhenDirty() {
			return isUpdateOnlyWhenDirty;
		}
		public void setUpdateOnlyWhenDirty(boolean isUpdateOnlyWhenDirty) {
			this.isUpdateOnlyWhenDirty = isUpdateOnlyWhenDirty;
		}
		
    protected CachedRowSetImpl rowSet = null;
    
    private ConnectionFactory connectionFactory;
		public ConnectionFactory getConnectionFactory() {
			return connectionFactory;
		}
		public void setConnectionFactory(ConnectionFactory factory) {
			if(factory==null) throw new RuntimeException(new UEngineException("ConnectionFactory should not be null."));
			
			connectionFactory = factory;
		}
    
    //private HashMap propertyMap;
	protected HashMap cache;
    protected HashMap outFieldMap;
    protected HashMap modifiedFieldMap;
    
    protected boolean isBatchUpdate = false;
    
    String sqlStmt;
		public String getSqlStmt() {
			return sqlStmt;
		}
		public void setSqlStmt(String sqlStmt) {
			this.sqlStmt = sqlStmt;
		}

	String tableName;
		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		
	String keyField;
		public String getKeyField() {
			return keyField;
		}
		public void setKeyField(String keyField) {
			this.keyField = keyField;
		}

    
	Class daoClass;

	HashMap propertyTypes;
		public HashMap getPropertyTypes() {
			return propertyTypes;
		}


//	HashMap isOptional;
	
	boolean autoSQLGeneration;
		public boolean isAutoSQLGeneration() {
			return autoSQLGeneration;
		}
		public void setAutoSQLGeneration(boolean autoSQLGeneration) {
			this.autoSQLGeneration = autoSQLGeneration;
		}	

	protected AbstractGenericDAO(Class daoClass, boolean isConnective) throws Exception{
		initialize(daoClass, isConnective);
	}	


    /**
     * JNDI ?????????????? Retrive?????? '?????? ??????????????? ???????? ?????????? ????????
     *
     * @param jndiName
     * @param statement
     * @throws DAOException
     */
    
	protected AbstractGenericDAO(final String jndiName, boolean isConnective, String sqlStmt, Class daoClass) throws Exception {
		this(
			new ConnectionFactory(){
				public Connection getConnection() throws Exception{
					InitialContext ctx = null;
					ctx = new InitialContext();
					DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiName);
					return ds.getConnection();
				}
			}, 
			isConnective,
			sqlStmt, daoClass
		);		
	}
	
//	protected AbstractGenericDAO(TransactionContext tc, String sqlStmt, Class daoClass)	throws Exception {
//		this(
//			new TransactionContextConnectionFactoryAdapter(tc),
//			false,
//			sqlStmt, daoClass
//		);
//		this.tc = tc;
//	}		

//	protected AbstractGenericDAO(final Connection conn, boolean isConnective, String sqlStmt, Class daoClass)	throws Exception {
//		this(
//			new ConnectionFactory(){
//				public Connection getConnection() throws Exception{
//					return conn;
//				}
//			},
//			isConnective,
//			sqlStmt, daoClass
//		);
//	}		

	protected AbstractGenericDAO(ConnectionFactory con, boolean isConnective, String sqlStmt, Class daoClass) throws Exception {
		initialize(con, isConnective, sqlStmt, daoClass);
	}
	
	protected void initialize(ConnectionFactory cf, boolean isConnective, String sqlStmt, Class daoClass)
		throws Exception {
		
		setConnectionFactory(cf);

/*		propertyMap = new HashMap();{
			String[] splits = statement.split("?");
			for(int i=0; i<splits.length; i++){
				String token = splits[i];
    		
				String splits2[] = token.split(" ");        		
				String propertyName = splits2[0];
    		
				propertyMap.put(propertyName.toUpperCase(), new Integer(i));
			}
		}*/
//		this.outFieldMap = new HashMap();    
//		this.cache = new HashMap();
		
//		this.selectSqlStmt = selectSqlStmt;
//		this.insertSqlStmt = insertSqlStmt;
//		this.updateSqlStmt = updateSqlStmt;
		
//		this.actualSelectSqlStmt = getActualSqlStmt(selectSqlStmt); 
//		this.actualInsertSqlStmt = getActualSqlStmt(insertSqlStmt);
//		this.actualUpdateSqlStmt = getActualSqlStmt(updateSqlStmt);
		
//		this.daoClass = daoClass;
//		Method[] methods = daoClass.getMethods();
//		propertyTypes = new HashMap(methods.length);
//		for(int i=0; i<methods.length; i++){
//			String propName = methods[i].getName();
//			
//			if(propName.startsWith("get")){
//				propName = propName.substring(3).toUpperCase();
//				propertyTypes.put(propName, methods[i].getReturnType());
//			}
//		}
		setStatement(sqlStmt);
		initialize(daoClass, isConnective);
	}

	
	protected void initialize(Class daoClass, boolean isConnective) throws Exception {
//		this.isAutoCommit = isAutoCommit;
		this.isConnective = isConnective;
		
//		if ( isAutoCommit ) isConnective = false;
		
		this.outFieldMap = new HashMap();    
		this.cache = new HashMap();
		
		this.daoClass = daoClass;
		Method[] methods = daoClass.getMethods();
		propertyTypes = new HashMap(methods.length);
		for(int i=0; i<methods.length; i++){
			String propName = methods[i].getName();
			
			
			if(propName.startsWith("get") && methods[i].getParameterTypes().length == 0){
				
				try{
					propName = propName.substring(3);
					daoClass.getMethod("set" + propName, new Class[]{methods[i].getReturnType()});

					propName = propName.toUpperCase();
					propertyTypes.put(propName, methods[i].getReturnType());
				}catch(Exception e){
					//ignore if there's no setter having same property naming convention.
				}
				
			}
		}
	}
	
	
	
	public void select() throws Exception {
		
		if(sqlStmt == null && isAutoSQLGeneration()){
			createSelectSql();
		}
		
//		rowSet = null;
		//TODO: need to be cached
		releaseResource();
		
		PreparedStatement pstmt = null;
		ConnectionFactory cf = getConnectionFactory();
		Connection con = cf.getConnection();
		try{
			if(con == null) throw new UEngineException("Connection is null");
			
			pstmt = con.prepareStatement(getActualSqlStmt(getStatement()));
		
			lateBindProperties(getStatement(), pstmt);
			rowSet = new CachedRowSetImpl();
			/*
			ResultSet rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount(); 
			int j = 0;
			while(rs.next()){
				System.out.println();
				for(int i =1; i<columnCount; i++){
					System.out.println( rsmd.getColumnName(i));
					System.out.println("rs.object: " + rs.getObject(i));	
				}
				System.out.println();
			}
			*/
			rowSet.populate(pstmt.executeQuery());
			rowSet.beforeFirst();
			
			isDirty = false;
			
		}catch(Exception e){
			
			throw e;
		}finally{
			if (rowSet!= null && cf instanceof TransactionContext) {
				TransactionContext tc = (TransactionContext) cf;
				tc.addReleaseResourceListeners(new ReleaseResourceListener() {
					
					public void beforeReleaseResource(TransactionContext tx) throws Exception {
						releaseResource();
					}
				});
			}
			
			try{pstmt.close();}catch(Exception e){}
			if ( !isConnective ) {
				checkOkToCloseConnection();
				try{con.close();}catch(Exception e){}
			}
			
		}		
	}
	
	private void checkOkToCloseConnection() throws Exception{
		if(getConnectionFactory() instanceof ProcessTransactionContext){
			ProcessManagerBean pm = ((ProcessTransactionContext)getConnectionFactory()).getProcessManager();
			if(!pm.isManagedTransaction()) throw new UEngineException("This thread tries to close any of connection in uEngine-managed transaction.");
		}
	}
	
	public void createInsertSql() throws Exception{
		final StringBuffer sql_KeyNames = new StringBuffer();
		final StringBuffer sql_ValuePlaceHolders = new StringBuffer();
		
		if(rowSet==null){	// fetch ???????????? sql??????: cache???????????? ?????????? ???????????? ???????????? ????????? ??????????????????.
			ForLoop loopForCacheKeys = new ForLoop(){
				String sep = "";

				public void logic(Object target) {
					String propertyName = (String)target;
					
					sql_KeyNames.append(sep + propertyName);
					sql_ValuePlaceHolders.append(sep + "?" + propertyName);
					
					sep =", ";
				}
				
			};
			
			loopForCacheKeys.run(cache.keySet());
			sqlStmt = "insert into " + getTableName() + "("+ sql_KeyNames +") values (" + sql_ValuePlaceHolders + ")";

		}else{				// fetch ???????????? ????????????????? dao?????? insert?????? ?????????????????????????????????? ???????????? ??????????????????
			String sep = "";
			ResultSetMetaData rsMetaData = rowSet.getMetaData();
			for(int i=1; i<=rsMetaData.getColumnCount(); i++){
				String propertyName = rsMetaData.getColumnName(i);
				
				sql_KeyNames.append(sep + propertyName);
				sql_ValuePlaceHolders.append(sep + "?" + propertyName);
				
				sep =", ";
			}

			sqlStmt = "insert into " + getTableName() + "("+ sql_KeyNames +") values (" + sql_ValuePlaceHolders + ")";

		   	adjustMetaDataIfFetched();
		}
		
	}
	
	public int insert() throws Exception {
		if(sqlStmt == null && isAutoSQLGeneration()){
			createInsertSql();
		}
		
		return update();
	}

	private static Hashtable typeMappingHT = new Hashtable();
	static{
		typeMappingHT.put(Integer.valueOf(Types.VARCHAR), 	String.class);
		typeMappingHT.put(Integer.valueOf(Types.INTEGER), 	Number.class);
		typeMappingHT.put(Integer.valueOf(2), 				Number.class);
		typeMappingHT.put(Integer.valueOf(Types.DATE),		Date.class);
		typeMappingHT.put(Integer.valueOf(Types.BOOLEAN),	Boolean.class);
		typeMappingHT.put(Integer.valueOf(Types.CLOB),	Clob.class); //freshka 2015.10.21
		typeMappingHT.put(Integer.valueOf(Types.BLOB),	Blob.class); //freshka 2015.10.21
		typeMappingHT.put(Integer.valueOf(Types.BINARY),	byte[].class); //freshka 2015.10.21
	}
	protected void adjustMetaDataIfFetched() throws Exception{
		if(rowSet==null) return;
		
		ResultSetMetaData rsMetaData = rowSet.getMetaData();
		for(int i=1; i<=rsMetaData.getColumnCount(); i++){
			String propertyName = rsMetaData.getColumnName(i);
			int type = rsMetaData.getColumnType(i);

			Class propertyCls = (Class)typeMappingHT.get(Integer.valueOf(type));
			if(propertyCls!=null)
				propertyTypes.put(propertyName, propertyCls);
		}
	}
	
	public int update(String sqlStmt) throws Exception {	
		setStatement(sqlStmt);
		return update();
	}
	
	
	private int batchCount = 0;
		public int getBatchCount() {
			return batchCount;
		}

	private PreparedStatement pstmtForBatch = null;
	private Connection connForBatch = null;
	
	public void addBatch() throws Exception {
		isBatchUpdate = true;
		if ( batchCount == 0 ) {
//			if ( getConnectionFactory() == null ) 
//				setConnectionFactory(new ConnectionFactory());
			connForBatch = getConnectionFactory().getConnection();
			pstmtForBatch = connForBatch.prepareStatement(getActualSqlStmt(getStatement()));
		} 
		lateBindProperties(getStatement(), pstmtForBatch);
		pstmtForBatch.addBatch();
		batchCount++;
	}
	
	public int[] updateBatch() throws Exception {
		try{
			if ( batchCount > 0 ) {
				return pstmtForBatch.executeBatch();
			} else {
				int[] btc = new int[0];
				return btc;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new UEngineException("Error when to try sql [" + getStatement() + "] ", e);
			
		}finally{
			try{pstmtForBatch.close();}catch(Exception e){}
			if ( !isConnective ) {
				checkOkToCloseConnection();
				try{connForBatch.close();}catch(Exception e){}
			}
		}			
	}
	
	
	public void createUpdateSql() throws Exception{
		final StringBuffer sql_SetPairs = new StringBuffer();

		if(getTableName()==null || getKeyField()==null)
			throw new UEngineException("Although Update query is set to be build automatically, the table name or key field is not set.");
		
		if(rowSet==null){
			
			ForLoop loopForCacheKeys = new ForLoop(){
				String sep = "";

				public void logic(Object target) {
					String propertyName = (String)target;
					
					if(modifiedFieldMap!=null && !modifiedFieldMap.containsKey(propertyName)) return;

					sql_SetPairs.append(sep + propertyName + "=?" + propertyName);
					
					sep =", ";
				}
				
			};
			
			loopForCacheKeys.run(cache.keySet());
			sqlStmt = "update " + getTableName() + " set "+ sql_SetPairs +" where " + getKeyField() + "=?" + getKeyField();

		}else{				// fetch ???????????? ????????????????? dao?????? insert?????? ?????????????????????????????????? ???????????? ??????????????????
			String sep = "";
			ResultSetMetaData rsMetaData = rowSet.getMetaData();
			for(int i=1; i<=rsMetaData.getColumnCount(); i++){
				String propertyName = rsMetaData.getColumnName(i);				
				
				if(propertyName.equalsIgnoreCase(getKeyField()) || modifiedFieldMap!=null && !modifiedFieldMap.containsKey(propertyName)) continue;
				
				sql_SetPairs.append(sep + propertyName + "=?" + propertyName);				
				sep =", ";
			}

			sqlStmt = "update " + getTableName() + " set "+ sql_SetPairs +" where " + getKeyField() + "=?" + getKeyField();
			
			if(sql_SetPairs.length() == 0)
				sqlStmt = null;
			
		   	adjustMetaDataIfFetched();
		}
	}
	
	public void createSelectSql() throws Exception{
		sqlStmt = "select * from " + getTableName() + " where " + getKeyField() + "=?" + getKeyField();
	}
	
	public int update() throws Exception {
		if(sqlStmt == null && isAutoSQLGeneration()){
			createUpdateSql();
		}
		
		if(sqlStmt == null || updateOnlyWhenDirty() && !isDirty) return 0;
			
//		if ( tc != null ) tc.setHasDML(true);
//		if ( getConnectionFactory() == null ) 
//			setConnectionFactory(new ConnectionFactory());
		
		//rowSet = null;

		//TODO: need to be cached
		Connection con = getConnectionFactory().getConnection();
		PreparedStatement pstmt = null;
		
		try{
//			System.out.println("getStatement() : " + getStatement());
			
			pstmt = con.prepareStatement(getActualSqlStmt(getStatement()));
			
			lateBindProperties(getStatement(), pstmt);
			int rowAffected = pstmt.executeUpdate();
			
			return rowAffected;
		}catch(Exception e){
			throw new UEngineException("Error when to try sql [" + getStatement() + "] ", e);
		}finally{
			try{pstmt.close();}catch(Exception e){}
			if ( !isConnective ) {
				checkOkToCloseConnection();
				try{con.close();}catch(Exception e){}
			}
		}		
	}	
	
	
	public int call() throws Exception {
		CallableStatement cstmt = null;		
		Connection con = getConnectionFactory().getConnection();
		try{
//			rowSet = null;
			releaseResource();
		
			cstmt = con.prepareCall(getActualSqlStmt(getStatement()));
		
			lateBindProperties(getStatement(), cstmt);
			int rowAffected = cstmt.executeUpdate();
		
			for(Iterator iter = outFieldMap.keySet().iterator(); iter.hasNext();){
				String fieldName = (String)iter.next();
				int order = ((Integer)outFieldMap.get(fieldName)).intValue();
				Object value = cstmt.getObject(order);
//				Array array = cstmt.getArray("");
				cache.put(fieldName.toUpperCase(), value);
			}
		
			return rowAffected;			
		}catch(Exception e){
			throw e;
		}finally{
			try{cstmt.close();}catch(Exception e){}
			if ( !isConnective ) {
				checkOkToCloseConnection();
				try{con.close();}catch(Exception e){}
			}
		}		
	}
	
//	public boolean absolute(int row) throws Exception {
//		if(rowSet==null)
//			throw new Exception("This DAO is not selected yet.");
//
//		return rowSet.absolute(row);
//	}

//	public int getRow() throws Exception {
//		if(rowSet==null)
//			throw new Exception("This DAO is not selected yet.");
//
//		return rowSet.getRow();
//	}

	public void beforeFirst() throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		rowSet.beforeFirst();
	}

	public boolean previous() throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");
		return rowSet.previous();
	}    

    public boolean next() throws Exception {
    	if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");
		return rowSet.next();
    }

	public void afterLast() throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");
    	
    	rowSet.afterLast();
	}
    
	public boolean last() throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");
    	
		return rowSet.last();
	}
        
    
    public boolean first() throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.first();
    }
    
    public boolean absolute(int pos) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.absolute(pos);
    }    

    public int size() {
		if(rowSet==null)
			throw new RuntimeException("This DAO is not selected yet.");

        return rowSet.size();
    }
    
	public String getString(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getString(key);
	}
	public Integer getInt(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");
		return Integer.valueOf(rowSet.getInt(key));
	}
	public Long getLong(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return Long.valueOf(rowSet.getLong(key));		
	}
	public Boolean getBoolean(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return Boolean.valueOf(rowSet.getBoolean(key));			
	}
	
	public Date getDate(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getDate(key);				 
	}

	public Timestamp getTimestamp(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getTimestamp(key);				 
	}
	
	 //freshka 2015.10.21
	public Clob geClob(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getClob(key);
	}	
	 //freshka 2015.10.21
	public Blob geBlob(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getBlob(key);
	}	
	 //freshka 2015.10.21
	public byte[] geBytes(String key) throws Exception {
		if(rowSet==null)
			throw new Exception("This DAO is not selected yet.");

		return rowSet.getBytes(key);
	}

	protected void lateBindProperties(String statement, PreparedStatement pStmt) throws Exception{
		getActualSqlStmt(statement, pStmt);    	
	}
	
	protected String getActualSqlStmt(String statement) throws Exception{
		return getActualSqlStmt(statement, null);
	}

    protected String getActualSqlStmt(String statement, PreparedStatement pstmt) throws Exception{
    	
    	if(statement==null) return null;
    	
     	StringBuffer realSql=new StringBuffer();
     	StringBuffer realSqlForValue=new StringBuffer(); //freshka 2016.03.08
    	
		String[] splits = statement.split("\\?");
						
		//if(splits.length>0) 
		//	realSql.append(splits[0]);
		if(splits.length>0) {
			realSql.append(splits[0]);
			if(GlobalContext.logLevelIsDebug){
				realSqlForValue.append(splits[0]); //freshka 2016.03.08
			}			
		}
			
		outFieldMap.clear();

		if(splits.length>1){
			for(int i=1; i<splits.length; i++){
				String token = splits[i];
		
				String splits2[] = token.split("[ ,);]");        		
				String propertyNamePart = splits2[0];
				String propertyName = propertyNamePart.trim();
				boolean isOut = false;
				
				if(propertyNamePart.startsWith("*")){
					isOut = true;
					propertyName = propertyNamePart.substring(1);
					
					outFieldMap.put(propertyName.toUpperCase(), Integer.valueOf(i));
				}
						
				token = token.replaceFirst((isOut ? "\\*" : "") + propertyName, "\\?");			
				realSql.append(token);
				
///System.out.println("\n\n ===================== real sql =========================\n  " + realSql);
				
				if (pstmt != null) {
					propertyName = propertyName.toUpperCase();
					
					Object value;
					if (rowSet != null)
						value = rowSet.getObject(propertyName);
					else
						value = cache.get(propertyName);
					
					Class type = (Class) propertyTypes.get(propertyName);
					
//					if ( tc != null && tc.getThreadId() != null && tc.isHasDML() ) {
//						apprLogger.warn("sql update value : " + value);
//					}					
					
					boolean failToFindInMethods = false;
					
					if (type == null)
						failToFindInMethods = true;
					if (type == null && value != null)
						type = value.getClass();
					
					if (isOut) {
						CallableStatement cstmt = (CallableStatement) pstmt;
						if (type == String.class) {
							cstmt.registerOutParameter(i, Types.VARCHAR);
						} else if (Number.class.isAssignableFrom(type)) {
							cstmt.registerOutParameter(i, Types.BIGINT);
						} else if (type == java.util.Date.class) {
							cstmt.registerOutParameter(i, Types.DATE);
						}
					} else {
						try{												
							if (type == String.class) {
								pstmt.setString(i, (String) value);
							} else if (type != null && Number.class.isAssignableFrom(type)) {
								if (value != null) {
									Number numberValue;
									if (value instanceof Boolean) {
										numberValue = Long.valueOf(((Boolean)value).booleanValue() ? 1 : 0);
									} else {
										if (value instanceof String)
										try {
											value = Long.valueOf((String)value);
										} catch (Exception e) {
										}
										
										numberValue = (Number)value;
									}
									
							
//									pstmt.setLong(i, numberValue.longValue());
									pstmt.setString(i, numberValue.toString());
								} else
									pstmt.setNull(i, Types.BIGINT);
								
							} else if (type != null && java.util.Date.class.isAssignableFrom(type)) {
								if (value != null) {
									java.util.Date dateValue = (java.util.Date)value;
						
									long timeInMS = dateValue.getTime(); 
									value = new Timestamp(timeInMS);							 
								}
							
								pstmt.setTimestamp(i, (Timestamp)value);													
							} else if ( type != null && (Boolean.class.isAssignableFrom(type) || boolean.class == type)) {
								Boolean booleanValue = Boolean.valueOf(false);
								if ( value != null) {
									if(value instanceof Boolean)
										booleanValue = (Boolean)value;
									else if(value instanceof Number)
										booleanValue = Boolean.valueOf(((Number)value).intValue() == 1);
								}
								// 0 : false, 1 : true  [default : 0]
								pstmt.setInt(i, (booleanValue.booleanValue())?1:0);
							} else if (type != null && type == Clob.class) {
								pstmt.setClob(i, (Clob)value); //freshka 2015.10.22
							} else if (type != null && type == Blob.class ) {
								pstmt.setBlob(i, (Blob)value);
							} else {
								
/*								if(value instanceof java.util.Date && DAOFactory.getInstance().getDBMSProductName().equals("`")){
									pstmt.setTimestamp(i, new Timestamp(((java.util.Date)value).getTime()));
								}
*/								
								pstmt.setObject(i, value);
							}
							
						}catch(java.sql.SQLException sqlException){
							String additionalInfo = "";
							if(failToFindInMethods && value == null)
								additionalInfo = "Make sure that the property [" + propertyName + "] is declared as a setter/getter.";
																
							throw new Exception("GenericDAO ["+ daoClass.getName() +"] failed to bind value [" + value + "] to field [" + propertyName + "]. " + additionalInfo, sqlException);
						}
					}
					//freshka 2016.03.08
					if(GlobalContext.logLevelIsDebug){
						token = token.replaceFirst((isOut ? "\\*" : "") + "\\?", "'"+java.util.regex.Matcher.quoteReplacement(new StringBuffer().append(value).toString())+"'");
						realSqlForValue.append(token);
					}
				}
			}
		}
		//freshka 2016.03.08
		if(GlobalContext.logLevelIsDebug){
			
		}
		
		return realSql.toString();
    }
    
	public Object invoke(Object proxy, Method m, Object[] args)	throws Throwable{
		String methodName = m.getName();
		
		if(m.getName().equals("getImplementationObject")){
			return getImplementationObject();
		}
		
		//if getter
		if(m.getName().startsWith("get")){
			String propertyName = methodName.substring(3).toUpperCase();
			propertyName = replaceReservedKeyword(propertyName);

			if(propertyName.length()==0 && args.length==1){//from untyped DAO's getter
				return get((String)args[0]);
			}else{//from typed DAO's getter
				Object returnValue;
				
				if(rowSet!=null) {
//					try {
//					if(rowSet.isClosed()) { 
//						throw new UEngineException("This DAO has been already closed. If you use TransactionContext, Use DAO during TransactionContext is alive.");
//					}
//					}catch (Exception ex) {
//						ex.printStackTrace();
//					}
					
					if ( Boolean.class.isAssignableFrom(m.getReturnType()) ) {
						returnValue = Boolean.valueOf( (rowSet.getInt(propertyName)==1)?true:false );
					} else {
						if (m.getName().equals("getInt") ||  
								m.getName().equals("getLong") || 
								m.getName().equals("getBoolean") || 
								m.getName().equals("getDate") || 
								m.getName().equals("getString")) {
							try{
								return m.invoke(this, args);		
							}catch(Exception e){
								throw e.getCause();
							}
						} 
						returnValue = rowSet.getObject(propertyName);
					}
				} else {
					if (args!=null && args.length==1 && args[0] instanceof String && 
							(m.getName().equals("getInt") ||  
							m.getName().equals("getLong") || 
							m.getName().equals("getBoolean") || 
							m.getName().equals("getDate") || 
							m.getName().equals("getString"))
						)
					{
						propertyName = ((String) args[0]).toUpperCase();
					}
					
					returnValue = cache.get(propertyName);
				}
				
				// try to convert an integer value to proper mapping values for types
				if(returnValue instanceof Number){
					Number returnValueInNumber = (Number)returnValue;
					if(m.getReturnType() == Long.class || m.getReturnType() == long.class ){
						return Long.valueOf(returnValueInNumber.longValue());
					}
					if(m.getReturnType() == Integer.class || m.getReturnType() == int.class ){
						return Integer.valueOf(returnValueInNumber.intValue());
					}
					if(m.getReturnType() == Boolean.class || m.getReturnType() == boolean.class ){
						return Boolean.valueOf(returnValueInNumber.intValue() == 1);
					}
				}
				
				// try to null values into proper default primitive types' values
				if(returnValue == null){
					if(m.getReturnType() == boolean.class){
						return Boolean.valueOf(false);
					}					
					if(m.getReturnType() == int.class){
						return Integer.valueOf(0);
					}					
					if(m.getReturnType() == long.class){
						return Long.valueOf(0);
					}					
				}
				
				//	try to parse the string value into integer type.
				if(returnValue instanceof String){ 
					if(m.getReturnType() == int.class || Integer.class.isAssignableFrom(m.getReturnType())){
						try{
							return new Integer((String)returnValue);
						}catch(Exception e){
						}
					}	
					
					if(m.getReturnType() == long.class || Long.class.isAssignableFrom(m.getReturnType())){
						try{
							return new Long((String)returnValue);
						}catch(Exception e){
						}
					}
				}
				
				//primitive type mappings
				if(returnValue instanceof Boolean && m.getReturnType() == boolean.class){
					return returnValue;
				}
				
				if(returnValue instanceof Number && (m.getReturnType() == int.class || m.getReturnType() == long.class)){
					return returnValue;
				}
				//end
					
				if(returnValue!=null && !m.getReturnType().isAssignableFrom(returnValue.getClass())){
					if (m.getReturnType() == Blob.class && returnValue.getClass() == byte[].class) { //mariadb, mysql
						returnValue = new javax.sql.rowset.serial.SerialBlob((byte[])returnValue);
					}
					else if (m.getReturnType() == Clob.class && returnValue.getClass() == String.class) { //mariadb, mysql
						returnValue = new javax.sql.rowset.serial.SerialClob(((String)returnValue).toCharArray());
					}
					else {
						//throw new Exception("DAO's field type of '"+propertyName+"' is mismatch with the actual table's field.");
						throw new Exception("DAO's field type of '"+propertyName+"' is mismatch with the actual table's field." + " actual("+m.getReturnType()+") vs DAO("+returnValue.getClass()+")");						
					}
				}
				
				return returnValue;
			}
		}else
		//if setter 
		if(m.getName().startsWith("set")){
			isDirty = true;
			
			String propertyName = methodName.substring(3);
			
			propertyName = replaceReservedKeyword(propertyName);
			
			if(modifiedFieldMap==null) modifiedFieldMap = new HashMap();
			modifiedFieldMap.put(propertyName.toUpperCase(), propertyName);
			
			if(rowSet!=null){
				cache.clear();
				ResultSetMetaData rsMetaData = rowSet.getMetaData();
				for(int i=1; i<=rsMetaData.getColumnCount(); i++){
					String propName = rsMetaData.getColumnName(i);
					cache.put(propName.toUpperCase(), rowSet.getObject(i));
				}
				
//				rowSet = null;
				releaseResource();
			}
			
			if(propertyName.length()==0 && args.length==2){
				propertyName = args[0].toString().toUpperCase();
				cache.put(propertyName, args[1]);
			}else{//TODO: type check for typed DAO 				
				cache.put(propertyName.toUpperCase(), args[0]);
			}
			
//			else 
//				pstmt.setObject(order, args[0]);
			
			if(modifiedFieldMap==null) modifiedFieldMap = new HashMap();
			modifiedFieldMap.put(propertyName.toUpperCase(), propertyName);
			

			return null;

		}else{
			try{
				return m.invoke(this, args);
				
			}catch(Exception e){
				throw e.getCause();
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public static String replaceReservedKeyword(String propertyName) {
		try {
			if(DAOFactory.getInstance().getDBMSProductName().equals("Cubrid")) {
				if (
						"alias".equalsIgnoreCase(propertyName) ||
						"value".equalsIgnoreCase(propertyName) ||
						"day".equalsIgnoreCase(propertyName)
				) {
					propertyName = propertyName + "_";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return propertyName;
	}
	
	public static IDAO createDAOImpl(String jndiName, String sqlStmt, Class daoClass) throws Exception{		
		return (IDAO)Proxy.newProxyInstance(
			daoClass.getClassLoader(),
			new Class[]{daoClass},
			new GenericDAO(jndiName, sqlStmt, daoClass)
		);		
	}
	
	public AbstractGenericDAO getImplementationObject(){
		return this;
	}

//	/**
//	 * @deprecated
//	 */
//	public static IDAO createDAOImpl(Connection conn, String sqlStmt, Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			daoClass.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(conn, sqlStmt, daoClass)
//		);		
//	}
	
//	public static IDAO createDAOImpl(String jndiName, String selectSqlStmt, String insertSqlStmt, String updateSqlStmt, Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			daoClass.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(jndiName, selectSqlStmt, insertSqlStmt, updateSqlStmt, daoClass)
//		);		
//	}
//
//	public static IDAO createDAOImpl(ConnectionFactory conn, String selectSqlStmt, String insertSqlStmt, String updateSqlStmt, Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			daoClass.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(conn, selectSqlStmt, insertSqlStmt, updateSqlStmt, daoClass)
//		);		
//	}

//	public static IDAO createDAOImpl(ConnectionFactory conn, String sqlStmt, Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			daoClass.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(conn, sqlStmt, daoClass)
//		);		
//	}
//
//	public static IDAO createDAOImpl(ConnectionFactory conn, String sqlStmt) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			IDAO.class.getClassLoader(),
//			new Class[]{IDAO.class},
//			new GenericDAO(conn, sqlStmt, IDAO.class)
//		);
//	}
//
//	public static IDAO createDAOImpl(String sqlStmt, Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			IDAO.class.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(new ConnectionFactory(), sqlStmt, daoClass)
//		);
//	}
//
//	public static IDAO createDAOImpl() throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			IDAO.class.getClassLoader(),
//			new Class[]{IDAO.class},
//			new GenericDAO(IDAO.class)
//		);
//	}
//
//	public static IDAO createDAOImpl(Class daoClass) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			IDAO.class.getClassLoader(),
//			new Class[]{daoClass},
//			new GenericDAO(daoClass)
//		);
//	}
//
//	public static IDAO createDAOImpl(String sqlStmt) throws Exception{		
//		return (IDAO)Proxy.newProxyInstance(
//			IDAO.class.getClassLoader(),
//			new Class[]{IDAO.class},
//			new GenericDAO(new ConnectionFactory(), sqlStmt, IDAO.class)
//		);
//	}

	public Object get(String propertyName) throws Exception {
		propertyName = propertyName.toUpperCase();
		
		if(rowSet!=null)
			return rowSet.getObject(propertyName);
		else
			return cache.get(propertyName);
	}

	public Object set(String propertyName, Object value) throws Exception {
		isDirty = true;
		
		if(modifiedFieldMap==null) modifiedFieldMap = new HashMap();
		modifiedFieldMap.put(propertyName.toUpperCase(), propertyName);
		
		if(rowSet!=null){
			cache.clear();
			ResultSetMetaData rsMetaData = rowSet.getMetaData();
			for(int i=1; i<=rsMetaData.getColumnCount(); i++){
				String propName = rsMetaData.getColumnName(i);
				cache.put(propName.toUpperCase(), rowSet.getObject(i));
			}
			
			//rowSet = null;
			releaseResource();
		}
		
		return cache.put(propertyName.toUpperCase(), value);
	}
	

	public String getStatement() {
		return sqlStmt.toUpperCase(); //
	}
	
	public void setStatement(String sql) {
//		System.out.println("set SqlStmt : " + sql);
		this.sqlStmt = sql;
	}

	public CachedRowSetImpl getRowSet(){
		return rowSet;
	}
	


	public String toString(){
		StringBuffer sb = new StringBuffer();
	    Iterator i = propertyTypes.keySet().iterator();
	    while (i.hasNext()) {
	    	String propertyName = (String) i.next();
	    	
	    	Object value = null;
	    	try{
	    		value = get(propertyName);
	    	}catch(Exception e){
	    	}
	    	
	    	if (sb.length() > 0) sb.append(", ");
	    	sb.append(propertyName).append("=").append(value);
		};
		
		return sb.toString();
	}
	
	public void releaseResource() throws Exception {
		if (rowSet != null) {
			rowSet.close();
			rowSet = null;
		}
	}
}

/*
 * $Log: AbstractGenericDAO.java,v $
 * Revision 1.34  2011/07/22 07:33:20  curonide
 * codi engine sync
 *
 * Revision 1.33  2010/04/08 01:27:21  allbegray
 * *** empty log message ***
 *
 * Revision 1.32  2010/04/06 08:58:35  allbegray
 * *** empty log message ***
 *
 * Revision 1.29  2010/01/25 13:30:13  pongsor
 * patch for problem on reused DAO, which has its values in cache, doesn't support the unnamed and method name-typed call
 *
 * Revision 1.28  2009/06/16 09:02:25  erim79
 * *** empty log message ***
 *
 * Revision 1.27  2009/04/15 09:17:04  kmooje
 * *** empty log message ***
 *
 * Revision 1.26  2009/01/05 09:08:01  curonide
 * *** empty log message ***
 *
 * Revision 1.25  2008/10/02 15:59:39  pongsor
 * *** empty log message ***
 *
 * Revision 1.24  2008/03/24 02:14:43  pongsor
 * WS-AT Support
 *
 * Revision 1.23  2008/03/19 13:18:11  pongsor
 * *** empty log message ***
 *
 * Revision 1.22  2007/12/05 02:31:36  curonide
 * *** empty log message ***
 *
 * Revision 1.5  2007/12/04 07:34:44  bpm
 * *** empty log message ***
 *
 * Revision 1.3  2007/12/04 05:25:44  bpm
 * *** empty log message ***
 *
 * Revision 1.21  2007/09/14 10:23:11  pongsor
 * *** empty log message ***
 *
 * Revision 1.20  2007/01/16 22:31:35  pongsor
 * Popup Subprocess Drilling down
 *
 * Revision 1.18  2007/01/12 06:39:09  pongsor
 * 1. Connection Leakage Detector
 * 2. Transaction Demarcation Support for External connection
 * 4. DRoolsActivity has been enhanced
 *
 * Revision 1.17  2006/12/18 08:07:36  pongsor
 * *** empty log message ***
 *
 * Revision 1.16  2006/12/06 06:00:10  pongsor
 * *** empty log message ***
 *
 * Revision 1.15  2006/12/05 02:21:23  pongsor
 * *** empty log message ***
 *
 * Revision 1.14  2006/12/04 13:46:19  pongsor
 * *** empty log message ***
 *
 * Revision 1.13  2006/11/30 11:40:36  pongsor
 * *** empty log message ***
 *
 * Revision 1.12  2006/11/28 04:23:36  iseenote
 * *** empty log message ***
 *
 * Revision 1.11  2006/11/23 05:06:17  pongsor
 * *** empty log message ***
 *
 * Revision 1.10  2006/11/21 07:05:08  pongsor
 * *** empty log message ***
 *
 * Revision 1.9  2006/10/16 04:49:29  pongsor
 * 2.0.2_02
 *
 * Revision 1.8  2006/09/14 01:16:48  pongsor
 * uE 2.0_03
 *
 * Revision 1.7  2006/09/02 06:28:40  pongsor
 * *** empty log message ***
 *
 * Revision 1.6  2006/08/10 05:21:53  pongsor
 * ue20 final
 *
 * Revision 1.5  2006/07/17 09:03:15  pongsor
 * - Multi-lingual support
 * - Compensation Handler Event is added to ScopeActivity
 *
 * Revision 1.4  2006/07/17 03:20:22  pongsor
 * *** empty log message ***
 *
 * Revision 1.2  2006/07/07 01:53:22  pongsor
 * 1. Database Synchronized Process Variable
 * 2. Customizable Instance List Feature is added
 * 3. 'Key' field name has been renamed as 'KeyName' in the ProcessVariable table since the 'key' is a reserved word in MySQL.
 *
 * Revision 1.1  2006/06/17 07:51:26  pongsor
 * uEngine 2.0
 *
 * Revision 1.25  2006/04/06 02:35:42  phz
 * 1. back ???????? ?????????
 *
 * 2. insert ?????? rowSet; ?????????????? ???????????? ?????????????? ??????d
 *
 * Revision 1.24  2006/03/31 08:31:53  phz
 * ???????? ?????? ?????????????????=.
 *
 * Revision 1.23  2006/03/31 00:46:17  uengine
 * [Definition Synchronizer] Addition of a tool for synchronizing definition data between the development system and the production system. Note that a few associated changes for implementing the tool are applied to GenericDAO framework.
 *
 * Revision 1.22  2006/03/18 08:24:37  cidea429
 * log4j ????????? ??????d
 *
 * Revision 1.21  2006/03/12 09:15:24  ghbpark
 * *** empty log message ***
 *
 * Revision 1.20  2006/03/11 05:57:22  ghbpark
 * *** empty log message ***
 *
 * Revision 1.19  2006/02/28 03:25:18  ghbpark
 * *** empty log message ***
 *
 * Revision 1.18  2006/01/27 08:21:54  ghbpark
 * *** empty log message ***
 *
 * Revision 1.17  2006/01/24 10:19:36  uengine
 * [????????????????] ????????????
 * [?????? ???????????? ????????]
 * [??????????????????????????] ???????????? ????????
 *
 * Revision 1.16  2006/01/06 18:48:00  ghbpark
 * *** empty log message ***
 *
 * Revision 1.15  2005/12/07 06:57:02  uengine
 * *** empty log message ***
 *
 * Revision 1.14  2005/12/06 05:19:21  uengine
 * *** empty log message ***
 *
 * Revision 1.13  2005/11/16 23:55:10  southshine
 * *** empty log message ***
 *
 * Revision 1.12  2005/11/04 11:05:26  phz
 * phz
 *
 * Revision 1.11  2005/10/21 07:15:21  ghbpark
 * *** empty log message ***
 *
 * Revision 1.10  2005/10/21 06:11:40  ghbpark
 * *** empty log message ***
 *
 * Revision 1.9  2005/10/21 05:01:43  ghbpark
 * *** empty log message ***
 *
 * Revision 1.8  2005/10/19 14:25:01  ghbpark
 * *** empty log message ***
 *
 * Revision 1.7  2005/10/05 10:58:42  alcolins
 * *** empty log message ***
 *
 * Revision 1.6  2005/10/05 08:29:43  ghbpark
 * .
 *
 * Revision 1.5  2005/10/05 08:00:16  ghbpark
 * .
 *
 * Revision 1.4  2005/09/28 02:42:42  khw
 * *** empty log message ***
 *
 * Revision 1.3  2005/09/27 13:09:48  uengine
 * ?????????????????? ?????????????? ????????
 *
 * Revision 1.2  2005/09/07 07:04:17  ghbpark
 * .
 *
 * Revision 1.1  2005/09/06 07:08:15  ghbpark
 * EagleBPM 2.0 start
 *
 * Revision 1.5  2005/05/09 01:37:01  ghbpark
 * *** empty log message ***
 *
 * Revision 1.4  2005/04/11 10:48:50  ghbpark
 * *** empty log message ***
 *
 * Revision 1.3  2005/04/11 10:45:17  uengine
 * *** empty log message ***
 *
 * Revision 1.12.2.1  2005/03/28 02:14:10  uengine
 * *** empty log message ***
 *
 * Revision 1.12  2005/03/08 10:57:00  uengine
 * *** empty log message ***
 *
 * Revision 1.11  2005/02/28 05:04:15  uengine
 * *** empty log message ***
 *
 * Revision 1.10  2005/02/24 05:32:33  uengine
 * *** empty log message ***
 *
 * Revision 1.9  2005/02/23 01:27:09  uengine
 * *** empty log message ***
 *
 * Revision 1.8  2005/02/17 05:22:54  uengine
 * *** empty log message ***
 *
 * Revision 1.7  2005/02/16 07:43:43  ghbpark
 * *** empty log message ***
 *
 * Revision 1.6  2005/02/11 08:00:13  uengine
 * *** empty log message ***
 *
 * Revision 1.5  2005/02/02 06:12:04  uengine
 * *** empty log message ***
 *
 * Revision 1.4  2005/01/20 02:51:15  uengine
 * ???w??????????? ?????????????????????? shot; hashtable(serializable)???????? ??????; ?????? ?????????????? ???????????????????? ??????: ???????????? ???w??????????? ????????? ???????????????; v?????????????? ???????????????????? viewer???????????? ??????????????????????? ???????????? ?????? ??????=..
 * 
 * - 1?? ????????? ?????????????? shotcopy?????? ?????????????? ????????????????? ??????=
 * - viewer?????? shot; ???????????? ???????????????????? ???????? ??????????????????????? ??????=
 * - ProcessManagerRemote.getProcessInstance(String instanceId)?????? ???????????? remote?????? ??????; ?????? ??????=
 *
 * Revision 1.3  2004/12/31 00:57:55  uengine
 * *** empty log message ***
 *
 * Revision 1.2  2004/12/28 03:01:41  uengine
 * *** empty log message ***
 *
 * Revision 1.4  2004/12/01 11:39:27  pongsor
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/12 04:41:21  pongsor
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/05 09:54:31  pongsor
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/03 23:38:55  pongsor
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/01 08:38:25  ghbpark
 * first() ?????????? ???????????? ????????
 *
 * Revision 1.6  2003/11/27 06:54:24  ghbpark
 * previous() ?????????? ????????
 *
 * Revision 1.5  2003/11/06 11:08:40  ghbpark
 * ?????????????? '???????? ??=8?????? ?????????????????????? ?????????? ????????
 *
 * Revision 1.4  2003/10/10 10:55:52  winman
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/01 09:08:36  ghbpark
 * size() ?????????? ????????
 * -> RowSet?????? ?????????????? ?????? ????????????
 *
 * Revision 1.2  2003/09/30 15:30:37  ghbpark
 * *** empty log message ***
 *
 * Revision 1.1  2003/09/29 11:51:01  ghbpark
 * ???????????? DAO ?????? ???????????? ??????f ????????
 *
*/
