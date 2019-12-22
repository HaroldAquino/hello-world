/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author TOM
 */

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OffersTom {
    public static String offersAPI = "http://5df9c696e9f79e0014b6b31e.mockapi.io/offers/near_by?lat=1.313492&lon=103.860359&rad=20";
    public static String checkInDate = "2019-12-21";
    public static ArrayList<JSONObject> finalList = new ArrayList<>();
    
    public OffersTom(String offersAPI, String checkInDate){
        this.offersAPI = offersAPI;
        this.checkInDate = checkInDate;
    }
    
    public ArrayList<JSONObject> getFinalList(){
        return finalList;
    }
    
    public static boolean isUrlValid(String url){
        try{
            URL obj = new URL(url);
            obj.toURI();
            return true;
        } catch (MalformedURLException e){
            return false;
        } catch (URISyntaxException e){
            return false;
        }
    }
    
    
    public static void main(String args[]) throws IOException{
        if(isUrlValid(offersAPI)){
            URL url = new URL(offersAPI);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());
                
                JSONParser parser = new JSONParser();
                try{
                    JSONObject o = (JSONObject) parser.parse(response.toString());
                    JSONArray array = (JSONArray) o.get("offers");
                    ArrayList<JSONObject> list = new ArrayList<>();
                    ArrayList<JSONObject> remove = new ArrayList<>();
                    
                    
                    for(int i=0; i<array.size(); i++){
                        list.add((JSONObject) array.get(i));
                    }
                    
//                    System.out.println("with 4");
//                    for (JSONObject obj:list){
//                        System.out.println(obj);
//                    }
                    
                    // removing entries of category Hotel(3)
                    list.removeIf(obj -> Integer.parseInt(obj.get("category").toString()) == 3);
                    
//                    for (JSONObject obj:list){
//                        int category = Integer.parseInt(obj.get("category").toString());
//                        if(category == 3){
//                            list.remove(obj);
//                        }
//                    }
//                    System.out.println("remove 4");
//                    for (JSONObject obj:list){
//                        System.out.println(obj);
//                    }
                    
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
                    
                    Date date = format.parse(checkInDate);
                    
                    cal.setTime(date);
                    cal.add(Calendar.DATE, 5);
                    
                    date = cal.getTime();
                    
//                    System.out.println("checkin"+date);
                    
                    // Remove if valid_to is less than checkInDate + 5days
                    for (JSONObject obj:list){
                        Date validTo = format.parse(obj.get("valid_to").toString());
//                        System.out.println(validTo);
                        if(!validTo.after(date)){
//                            System.out.println("Yes I Am");
                            remove.add(obj);
                        }
                    }
                    list.removeAll(remove);
                    
//                    for (JSONObject obj:list){
//                        System.out.println(obj);
//                    }
                    
                    // Remove multiple merchants
                    for (JSONObject obj:list){
                        JSONArray merchants = (JSONArray) obj.get("merchants");
                        
//                        System.out.println(((JSONObject)obj.get("merchants")).get("distance"));
                        if (merchants.size() > 1){
                            double minDist = Double.parseDouble(((JSONObject) merchants.get(0)).get("distance").toString());
                            for(int i=0; i<merchants.size(); i++){
//                                System.out.println(((JSONObject) merchants.get(i)).get("distance"));
                                double distance = Double.parseDouble(((JSONObject) merchants.get(i)).get("distance").toString());
                                if (distance < minDist){
                                    minDist = distance;
                                }
                            }
                            for(int i=0; i<merchants.size(); i++){
                                double distance = Double.parseDouble(((JSONObject) merchants.get(i)).get("distance").toString());
                                if(distance != minDist){
                                    merchants.remove(i);
//                                    System.out.println("removed");
                                }
                            }
                        }
                    }
                    
//                    for (JSONObject obj:list){
//                        System.out.println(obj);
//                    }
                    // Sort by distance
                    Collections.sort(list, new Comparator<JSONObject>(){
                        @Override
                        public int compare(JSONObject o1, JSONObject o2) {
                            JSONArray merchants1 = (JSONArray) o1.get("merchants");
                            JSONArray merchants2 = (JSONArray) o2.get("merchants");
                            int compare = 0;
                            try
                            {
                                double keyA = Double.parseDouble(((JSONObject) merchants1.get(0)).get("distance").toString());
                                double keyB = Double.parseDouble(((JSONObject) merchants2.get(0)).get("distance").toString());
                                compare = Double.compare(keyA, keyB);
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            return compare;
                        }
                    });
                    
//                    System.out.println("Sort by distance");
                    
//                    for (JSONObject obj:list){
//                        System.out.println(obj);
//                    }
                    
                    // Picking 2 offers and placing them in finalList
                    int counter = 0;
                    for (JSONObject obj:list){
                        if( counter == 0){
                            finalList.add(obj);
                            counter++;
                        } else if (finalList.get(0).get("category") != obj.get("category")) {
                            finalList.add(obj);
                            counter++;
                            break;
                        } 
                    }
                    if (counter < 2){
                        finalList.add(list.get(0));
                    }
                    
                    System.out.println("Final List");
                    for (JSONObject obj:finalList){
                        System.out.println(obj);
                    }
                    
                } catch (Exception e){
                    e.printStackTrace();
                }
                
            } else {
                System.out.println("GET request not worked");
            }
        } else {
            System.out.println("Enter valid URL");
        }
        
    }
}



//class MyJSONComparator2 implements Comparator<JSONObject> {
//
//    @Override
//    public int compare(JSONObject o1, JSONObject o2) {
//        int v1 = Integer.parseInt(((JSONObject) o1.get("merchants")).get("distance").toString());
//        int v3 = Integer.parseInt(((JSONObject) o2.get("merchants")).get("distance").toString());
//        return v1.compareTo(v3);
//    }
//
//}
