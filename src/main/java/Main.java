import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {

    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileName = "data.csv";
        List<Employee> employeesList = parseCSV(columnMapping, fileName);
        String json = listToJson(employeesList);
        writeString(json, "data.json");
        List<Employee> employeesListFromXML = parseXML("data.xml");
        String jsonXML = listToJson(employeesListFromXML);
        writeString(jsonXML, "data2.json");
        String jsonFromFile = readString("data2.json");
        System.out.println(jsonToList(jsonFromFile));

    }

    private static List<Employee> jsonToList(String jsonString) {
        List<Employee> employees = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(jsonString);
            JSONArray jsonArray = (JSONArray) obj;
            Gson gson = new GsonBuilder().create();

            for (Object object : jsonArray) {
                employees.add(gson.fromJson(gson.toJson(object), Employee.class));
            }

            return employees;
        } catch (ParseException e) {
            System.out.println("Ошибка конвертации json в объект - " + e.getMessage());
            return new ArrayList<>();
        }

    }

    private static String readString(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл - " + e.getMessage());
            return "";
        }
    }

    private static List<Employee> parseXML(String path) {
        try {
            List<Employee> employees = new ArrayList<>();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(path);
            NodeList nodeList = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;

                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());

                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    employees.add(employee);
                }
            }

            return employees;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("Ошибка парсинга xml файла - " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void writeString(String text, String path) {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(text);
        } catch (IOException e) {
            System.out.println("Ошибка записи json в файл - " + e.getMessage());
        }
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columns, String file) {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columns);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            return csv.parse();
        } catch (IOException e) {
            System.out.println("Ошибка парсинга csv файла - " + e.getMessage());
            return new ArrayList<>();
        }
    }

}