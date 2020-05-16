package homework.advertisment.main;

import homework.advertisment.model.Category;
import homework.advertisment.model.Gender;
import homework.advertisment.model.Item;
import homework.advertisment.model.User;
import homework.advertisment.storage.DataStorage;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import project.Employee;
import project.EmployeeDAO;

import java.io.*;
import java.util.*;

import static java.lang.Long.valueOf;


public class AdversimentMain implements Commands {

    private static Scanner scanner = new Scanner(System.in);
    private static DataStorage dataStorage = new DataStorage();
    private static User currentUser = null;

    public static void main(String[] args) {
        //init data
        dataStorage.initData();

        boolean isRun = true;
        while (isRun) {
            Commands.printMainCommands();
            int command;
            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                command = -1;
            }
            switch (command) {
                case EXIT:
                    isRun = false;
                    break;
                case LOGIN:
                    loginUser();
                    break;
                case REGISTER:
                    registerUser();
                    break;
                case IMPORT_USERS:
                    importFromXlsxUser();
                    break;
                case IMPORT_ITEM:
                    importFromXlsxItem();
                    break;

                default:
                    System.out.println("Wrong command!");
            }
        }
    }

    private static void importFromXlsxUser() {
        System.out.println("Please select xlsx path");
        String xlsxPath = scanner.nextLine();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(xlsxPath);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                String name = row.getCell(0).getStringCellValue();
                String surname = row.getCell(1).getStringCellValue();
                Double age = row.getCell(2).getNumericCellValue();
                Gender gender = Gender.valueOf(row.getCell(3).getStringCellValue());
                String phoneNumberStr = row.getCell(4).getCellType() == CellType.NUMERIC ?
                        String.valueOf(row.getCell(4).getNumericCellValue()) : row.getCell(4).getStringCellValue();
                String passwordStr = row.getCell(5).getCellType() == CellType.NUMERIC ?
                        String.valueOf(Double.valueOf(row.getCell(5).getNumericCellValue()).intValue()) : row.getCell(5).getStringCellValue();
                User user = new User();
                user.setName(name);
                user.setSurname(surname);
                user.setAge((int) age.doubleValue());
                user.setGender(gender);
                user.setPhoneNumber(phoneNumberStr);
                user.setPassword(passwordStr);
                System.out.println(user);
                dataStorage.add(user);
                System.out.println("Import was success!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while importing users");
        }

    }

    private static void importFromXlsxItem() {
        System.out.println("Please select xlsx path");
        String xlsxPath = scanner.nextLine();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(xlsxPath);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                Long id= (long) row.getCell(0).getNumericCellValue();
                String title = row.getCell(1).getStringCellValue();
                String text = row.getCell(2).getStringCellValue();
                Double price=row.getCell(3).getNumericCellValue();
                Category category=Category.valueOf(row.getCell(4).getStringCellValue());
                Item item=new Item();
                item.setId(id);
                item.setTitle(title);
                item.setText(text);
                item.setPrice(price);
                item.setCategory(category);
                System.out.println(item);
                dataStorage.add(item);
                System.out.println("Import was success!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while importing users");
        }
    }

    private static void exportFromXlsxItem() throws IOException {
        Item item = new Item();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        Row row = sheet.createRow(0);
        Cell text = row.createCell(0);
        text.setCellValue("Text");
        Cell title = row.createCell(1);
        title.setCellValue("Title");
        Cell price = row.createCell(2);
        price.setCellValue("Price");
        Cell category = row.createCell(3);
        category.setCellValue("Category");
        for (int i = 1; i < rowNum; i++) {
            Row sheetRow = sheet.createRow(i);
            text.setCellValue(item.getText());
            title.setCellValue(item.getTitle());
            price.setCellValue(item.getPrice());
            category.setCellValue(String.valueOf(item.getCategory()));
            workbook.write(new FileOutputStream("C:\\Users\\Admin\\IdeaProjects\\untitled\\FullStack20\\src\\main\\resources\\exportItem.xls"));
            workbook.close();
        }
    }


        private static void registerUser () {
            System.out.println("Please input user data " +
                    "name,surname,gender(MALE,FEMALE),age,phoneNumber,password");
            try {
                String userDataStr = scanner.nextLine();
                String[] userDataArr = userDataStr.split(",");
                User userFromStorage = dataStorage.getUser(userDataArr[4]);
                if (userFromStorage == null) {
                    User user = new User();
                    user.setName(userDataArr[0]);
                    user.setSurname(userDataArr[1]);
                    user.setGender(Gender.valueOf(userDataArr[2].toUpperCase()));
                    user.setAge(Integer.parseInt(userDataArr[3]));
                    user.setPhoneNumber(userDataArr[4]);
                    user.setPassword(userDataArr[5]);
                    dataStorage.add(user);
                    System.out.println("User was successfully added");
                } else {
                    System.out.println("User already exists!");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Wrong Data!");
            }
        }

        private static void loginUser () {
            System.out.println("Please input phoneNumber,password");
            try {
                String loginStr = scanner.nextLine();
                String[] loginArr = loginStr.split(",");
                User user = dataStorage.getUser(loginArr[0]);
                if (user != null && user.getPassword().equals(loginArr[1])) {
                    currentUser = user;
                    loginSuccess();
                } else {
                    System.out.println("Wrong phoneNumber or password");
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Wrong Data!");
            }
        }

        private static void loginSuccess () {
            System.out.println("Welcome " + currentUser.getName() + "!");
            boolean isRun = true;
            while (isRun) {
                Commands.printUserCommands();
                int command;
                try {
                    command = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    command = -1;
                }
                switch (command) {
                    case LOGOUT:
                        isRun = false;
                        break;
                    case ADD_NEW_AD:
                        addNewItem();
                        break;
                    case PRINT_MY_ADS:
                        dataStorage.printItemsByUser(currentUser);
                        break;
                    case PRINT_ALL_ADS:
                        dataStorage.printItems();
                        break;
                    case PRINT_ADS_BY_CATEGORY:
                        printByCategory();
                        break;
                    case PRINT_ALL_ADS_SORT_BY_TITLE:
                        dataStorage.printItemsOrderByTitle();
                        break;
                    case PRINT_ALL_ADS_SORT_BY_DATE:
                        dataStorage.printItemsOrderByDate();
                        break;
                    case DELETE_MY_ALL_ADS:
                        dataStorage.deleteItemsByUser(currentUser);
                        break;
                    case DELETE_AD_BY_ID:
                        deleteById();
                        break;
                    case EXPORT_ITEM:
                        try {
                            exportFromXlsxItem();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("Wrong command!");
                }
            }
        }

        private static void deleteById () {
            System.out.println("please choose id from list");
            dataStorage.printItemsByUser(currentUser);
            long id = Long.parseLong(scanner.nextLine());
            Item itemById = dataStorage.getItemById(id);
            if (itemById != null && itemById.getUser().equals(currentUser)) {
                dataStorage.deleteItemsById(id);
            } else {
                System.out.println("Wrong id!");
            }
        }

        private static void printByCategory () {
            System.out.println("Please choose category name from list: " + Arrays.toString(Category.values()));
            try {
                String categoryStr = scanner.nextLine();
                Category category = Category.valueOf(categoryStr);
                dataStorage.printItemsByCategory(category);
            } catch (Exception e) {
                System.out.println("Wrong Category!");
            }
        }

        private static void addNewItem () {
            System.out.println("Please input item data title,text,price,category");
            System.out.println("Please choose category name from list: " + Arrays.toString(Category.values()));
            try {
                String itemDataStr = scanner.nextLine();
                String[] itemDataArr = itemDataStr.split(",");
                Item item = new Item(itemDataArr[0], itemDataArr[1], Double.parseDouble(itemDataArr[2])
                        , currentUser, Category.valueOf(itemDataArr[3]), new Date());
                dataStorage.add(item);
                System.out.println("Item was successfully added");
            } catch (Exception e) {
                System.out.println("Wrong Data!");
            }

        }

    }