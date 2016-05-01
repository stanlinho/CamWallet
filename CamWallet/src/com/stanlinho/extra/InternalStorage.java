package com.stanlinho.extra;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

public final class InternalStorage{
	 // implement ASyncTask?
	   private InternalStorage() {}
	 
	   public static void writeObject(Context context, String key, Object object) throws IOException {
		// TODO test for file exists and return false

		  FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
	      ObjectOutputStream oos = new ObjectOutputStream(fos);
	      oos.writeObject(object);
	      oos.close();
	      fos.close();
	      
	   }
	 
	   public static Object readObject(Context context, String key) throws IOException,
	         ClassNotFoundException {
	      FileInputStream fis = context.openFileInput(key);
	      ObjectInputStream ois = new ObjectInputStream(fis);
	      Object object = ois.readObject();
	      return object;
	   }
	   	 
	   public static boolean checkObject(Context context, String key) throws IOException {
		// TODO test for file exists and return false
		  boolean result = false;
		  String [] files = context.fileList();
		  for (int i = 0; i < files.length; i ++){
			  if (files[i].equals(key)){
				  result = true;
			  }
		  }
		    return result;
	      
	   }	   	 
	   public static boolean checkCreated(Context context) throws IOException {
		// TODO test for file exists and return false
		  boolean result = false;
		  String [] files = context.fileList();
//		  if(files.equals(null)){
//			  result = false;
//		  } 
		  for (int i = 0; i < files.length; i ++){
		  int nullCount = 0;
			  if (files[i].isEmpty()){
				  nullCount ++;
			  }
			  if(files.length > nullCount){
				  result = true;
			  }
		  }
		    return result;
	      
	   }
	   
	   public static void appendObject(Context context, String key, Object object) throws IOException {
		// TODO test for file exists and return false
		  FileOutputStream fos = context.openFileOutput(key, Context.MODE_APPEND);
	      ObjectOutputStream oos = new ObjectOutputStream(fos);
	      oos.writeObject(object);
	      oos.close();
	      fos.close();
	   }
		 
	   public static void replaceObject(Context context, String key, Object object) throws IOException {
		// TODO test for file exists and return false
		  context.deleteFile(key);
		  FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
	      ObjectOutputStream oos = new ObjectOutputStream(fos);
	      oos.writeObject(object);
	      oos.close();
	      fos.close();
	      
	   }
	}