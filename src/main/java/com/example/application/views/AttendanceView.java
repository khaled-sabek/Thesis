package com.example.application.views;

import com.example.application.data.edu.GAttendanceRecord;
import com.example.application.data.edu.GClassroom;
import com.example.application.data.edu.GStudent;
import com.example.application.services.GoogleClassroomService;
import com.google.api.services.classroom.Classroom;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@PageTitle("Attendance")
@Route(value = "attendance", layout = MainLayout.class)
@PermitAll
public class AttendanceView extends VerticalLayout implements Receiver {

    private File uploadedFile;
    private File previousFile;
    private Spreadsheet spreadsheet;
    ComboBox<GClassroom> comboBoxClass;

    private String courseSelectedID;

    private GClassroom courseSelected;

    private Classroom courseSelectedClassroom;

    private String courseSelectedEmail;
    private List<GStudent> students;

    private Button submitButton;
    private final GoogleClassroomService googleClassroomService;

    private Cell instructions;

    private UI ui;

    public AttendanceView(GoogleClassroomService googleClassroomService) throws IOException, URISyntaxException {
        this.googleClassroomService = googleClassroomService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        spreadsheet = new Spreadsheet();
        spreadsheet.setHeight("400px");
        //add(spreadsheet);

        add(createViewHeader(), spreadsheet);
        ///ij j j
        ///i
        ///i

        //leaving first row for instructions
        //second will have dates
        //third will be start of attendance
        //first column will be emails
        //second column will be names
        //third column will be attendance
        //instructions:
        instructions = getCreateCell(0, 0);
        instructions.setCellValue("Please fill the attendance cells with 1 for present and 0 for absent, leaving empty will mean no attendance");
        spreadsheet.refreshCells(instructions);

        Cell headers = getCreateCell(2, 0);
        headers.setCellValue("Email");
        spreadsheet.refreshCells(headers);

        Cell headers1 = getCreateCell(2, 1);
        headers1.setCellValue("Surname");
        spreadsheet.refreshCells(headers1);

        Cell headers2 = getCreateCell(2, 2);
        headers2.setCellValue("Name");
        spreadsheet.refreshCells(headers2);

        Cell headers3 = getCreateCell(1, 3);
        headers3.setCellValue("Attendance Date");
        spreadsheet.refreshCells(headers3);

        //this.startOfAttendanceCells = getCreateCell(2, 2);
        //this.startOfStudentEmails = getCreateCell(2, 0);
        //this.startOfStudentNames = getCreateCell(2, 1);
        //this.attendanceDateCell = getCreateCell(1, 2);
    }


    private VerticalLayout createViewHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.addClassName(LumoUtility.Padding.Bottom.XSMALL);

        HorizontalLayout viewHeading = new HorizontalLayout();
        viewHeading.setWidthFull();
        viewHeading.setAlignItems(Alignment.BASELINE);
        viewHeading.addClassName(LumoUtility.Padding.Left.SMALL);


        comboBoxClass = new ComboBox<>("Classrooms");
        //sort on name
        List<GClassroom> classrooms = googleClassroomService.findAllClassroomsWithAttendancePermission();
        classrooms.sort(Comparator.comparing(GClassroom::getClassroomName));
        comboBoxClass.setItems(classrooms);
        comboBoxClass.setItemLabelGenerator(GClassroom::getClassroomName);
        comboBoxClass.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                String gid = comboBoxClass.getValue().getGID();
                courseSelectedID = gid;
                courseSelectedEmail = comboBoxClass.getValue().getgClassroomEmail();
                courseSelected = comboBoxClass.getValue();
                this.students = googleClassroomService.findStudentsInCourse(courseSelected.getgClassroomEmail());
                submitButton.setEnabled(true);
                clearStudents();
                setAttendanceDetailsToSpreadsheet();
                setZeroAttendanceEntry();

            }
        });


        //updateInvoiceNumberAndSource();

        submitButton = new Button("Submit Attendance", e -> {
            submitButton.setEnabled(false);
            //spreadsheet
            submitAttendance();
            //disableWhileSubmittingData
        });
        submitButton.addDoubleClickListener(e -> {
            //do nothing
        });
        //disable until class is chosen
        submitButton.setEnabled(false);

        HorizontalLayout fields = new HorizontalLayout();
        fields.add(comboBoxClass, submitButton);
        fields.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        viewHeading.add(fields);
        header.add(viewHeading, createMenuBar());

        return header;
    }

    private void clearStudents() {
        for (int i = 0; i < students.size(); i++) {
            Cell studentFirstName = getCreateCell(3 + i, 2);
            studentFirstName.setCellValue("");
            spreadsheet.refreshCells(studentFirstName);

            Cell studentLastName = getCreateCell(3 + i, 1);
            studentLastName.setCellValue("");
            spreadsheet.refreshCells(studentLastName);

            Cell studentEmail = getCreateCell(3 + i, 0);
            studentEmail.setCellValue("");
            spreadsheet.refreshCells(studentEmail);
        }
    }

    public void submitAttendance() {
        //check if all data are as is:
        if (!checkStudentDetails()) {
            return;
        }
        if (!checkAttendanceDate()) {
            return;
        }
        //get attendance data
        List<GAttendanceRecord> attendanceRecords = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < students.size(); i++) {
            Cell cell = getCreateCell(3 + i, 3);
            String cellValue = getStringOfCell(cell);
            if (cellValue.equals("1") || cellValue.equals("0") || cellValue.isEmpty()) {
                GAttendanceRecord gAttendanceRecord = new GAttendanceRecord();
                gAttendanceRecord.setClassroomEmail(courseSelected.getgClassroomEmail());
                gAttendanceRecord.setStudentEmail(students.get(i).getEmail());
                gAttendanceRecord.setAttendanceDate(today.toString());
                gAttendanceRecord.setPresent(cellValue.equals("1"));
                attendanceRecords.add(gAttendanceRecord);
            }
        }
        //save attendance data
        Notification.show("Attendance Ready to be saved");
        googleClassroomService.saveAttendanceRecords(attendanceRecords, courseSelectedEmail, today.toString());
        GoogleClassroomService.completedDatabasePopulationSectionNotification(
                "Attendance Saved", false, "bottom", 6);
        clearTable();
        submitButton.setEnabled(true);
    }

    public void setAttendanceDetailsToSpreadsheet() {

        setSheetWithDate();
        setSheetWithStudentNamesEmailsIDs();
    }

    private void setSheetWithDate() {
        Cell attendanceDateCell2 = getCreateCell(2, 3);
        LocalDate today = LocalDate.now();
        attendanceDateCell2.setCellValue(today.toString());
        spreadsheet.refreshCells(attendanceDateCell2);
    }

    private void setSheetWithStudentNamesEmailsIDs() {
        for (int i = 0; i < students.size(); i++) {
            Cell studentFirstName = getCreateCell(3 + i, 2);
            studentFirstName.setCellValue(students.get(i).getFirstName());
            spreadsheet.refreshCells(studentFirstName);

            Cell studentLastName = getCreateCell(3 + i, 1);
            studentLastName.setCellValue(students.get(i).getLastName());
            spreadsheet.refreshCells(studentLastName);

            Cell studentEmail = getCreateCell(3 + i, 0);
            studentEmail.setCellValue(students.get(i).getEmail());
            spreadsheet.refreshCells(studentEmail);
        }
    }


    public void resetAttendance() {
        Notification.show("Everything was reset to avoid wrong results");
        setAttendanceDetailsToSpreadsheet();
        //delete all attendance set
        setZeroAttendanceEntry();
    }

    public void clearTable() {
        //for each column
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < students.size(); i++) {
                Cell cell = getCreateCell(3 + i, j);
                cell.setCellValue("");
                spreadsheet.refreshCells(cell);
            }
        }
    }

    public void setZeroAttendanceEntry() {
        for (int i = 0; i < students.size(); i++) {
            Cell cell = getCreateCell(3 + i, 3);
            cell.setCellValue(0);
            spreadsheet.refreshCells(cell);
        }
    }

    public int getNumberValue(Cell cell) {
        int result = 0;
        try {
            result = (int) cell.getNumericCellValue();

            return result;
        } catch (Exception e) {
            Notification.show("Please enter a valid number in cell: " + cell.getAddress());
            cell.setCellValue("");
            spreadsheet.refreshCells(cell);
            return -1;
        }
    }

    public String getStringOfCell(Cell cell) {
        String result = "";
        try {
            result = cell.getStringCellValue();
            return result;
        } catch (Exception e) {
            return Integer.toString((int) cell.getNumericCellValue());
        }
    }

    private Cell getCreateCell(int i, int j) {
        Cell cell = spreadsheet.getCell(i, j);
        if (cell == null) {
            cell = spreadsheet.createCell(i, j, "");
        }
        return cell;
    }


    private boolean checkStudentDetails() {
        for (int i = 0; i < students.size(); i++) {
            Cell studentFirstName = getCreateCell(3 + i, 2);
            String cellValueName = getStringValue(new CellReference(studentFirstName));
            String studentNameValue = students.get(i).getFirstName();
            if (!studentNameValue.equals(cellValueName)) {
                Notification.show("Please don't change the student names, fill only the attendance cells");
                submitButton.setEnabled(true);
                return false;
            }

            Cell studentLastName = getCreateCell(3 + i, 1);
            String cellValueLastName = getStringValue(new CellReference(studentLastName));
            String studentLastNameValue = students.get(i).getLastName();
            if (!studentLastNameValue.equals(cellValueLastName)) {
                Notification.show("Please don't change the student names, fill only the attendance cells");
                submitButton.setEnabled(true);
                return false;
            }

            Cell studentEmail = getCreateCell(3 + i, 0);
            String cellValueEmail = getStringValue(new CellReference(studentEmail));
            String studentEmailValue = students.get(i).getEmail();
            if (!studentEmailValue.equals(cellValueEmail)) {
                Notification.show("Please don't change the student emails, fill only the attendance cells");
                submitButton.setEnabled(true);
                return false;
            }
        }
        return true;
    }

    private boolean checkAttendanceDate() {
        LocalDate today = LocalDate.now();
        Cell attendanceDateCell = getCreateCell(2, 3);
        String cellValueDate = getStringValue(new CellReference(attendanceDateCell));
        if (!cellValueDate.equals(today.toString())) {
            Notification.show("Please don't change the date, fill only the attendance cells");
            submitButton.setEnabled(true);
            return false;
        }
        return true;
    }

    public void setComment(String message, Cell cell) {
        Drawing<?> drawing = spreadsheet.getActiveSheet()
                .createDrawingPatriarch();
        CreationHelper factory = spreadsheet.getActiveSheet().getWorkbook()
                .getCreationHelper();

        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(message));

        cell.setCellComment(comment);
    }


    private String getStringValue(CellReference cellReference) {
        return spreadsheet.getDataFormatter().formatCellValue(spreadsheet.getCell(cellReference));
    }

    private Cell getOrCreateCell(CellReference cellRef) {
        Cell cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
        if (cell == null) {
            cell = spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(), "");
        }
        return cell;
    }


    //features

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    private void addComment(CellReference cellReference) {
        Cell cell = getOrCreateCell(cellReference);
        createCellComment(spreadsheet, spreadsheet.getActiveSheet(), cell, cellReference);
        spreadsheet.refreshCells(cell);
        spreadsheet.editCellComment(cellReference);
    }

    private void createCellComment(Spreadsheet spreadsheet, Sheet sheet, Cell cell, CellReference cellRef) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("");
        comment.setString(str);

        // Fetch author from provider or fall back to default
        String author = null;
        if (spreadsheet.getCommentAuthorProvider() != null) {
            author = spreadsheet.getCommentAuthorProvider().getAuthorForComment(cellRef);
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Spreadsheet User";
        }
        comment.setAuthor(author);

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        try {
            File file = new File(fileName);
            file.deleteOnExit();
            uploadedFile = file;
            return new FileOutputStream(uploadedFile);
        } catch (FileNotFoundException e) {
            getLogger().warn("ERROR reading file " + fileName, e);
        }
        return null;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);

        AtomicReference<CellRangeAddress> selectedCells = new AtomicReference<>();
        AtomicReference<CellRangeAddress> selectedCellMergedRegion = new AtomicReference<>();
        AtomicReference<CellReference> selectedCellReference = new AtomicReference<>();

        spreadsheet.setGridlinesVisible(true);

        //spreadsheet.addSheetChangeListener(event -> toggleTitleVisibility());

        spreadsheet.addCellValueChangeListener(event -> {
            Cell cellA2 = spreadsheet.getCell("A2");
            Cell cellD4 = spreadsheet.getCell("D4");

            //toggleTitleVisibility();

            if (cellA2 == null || cellD4 == null) {
                return;
            }

            CellReference a2 = new CellReference(cellA2);
            CellReference d4 = new CellReference(cellD4);
            if (event.getChangedCells().contains(a2) || event.getChangedCells().contains(d4)) {
                //updateInvoiceNumberAndSource();
            }

        });
        spreadsheet.addSelectionChangeListener(e -> {
            selectedCells.set(e.getCellRangeAddresses().stream().findFirst().orElse(null));
            selectedCellMergedRegion.set(e.getSelectedCellMergedRegion());
            selectedCellReference.set(e.getSelectedCellReference());
        });

        Dialog uploadFileDialog = createUploadDialog();

        MenuItem fileMenu = menuBar.addItem("File");
        SubMenu fileSubMenu = fileMenu.getSubMenu();
        createIconItem(fileSubMenu, LumoIcon.UPLOAD, "Import", "Import", e -> uploadFileDialog.open());
        createIconItem(fileSubMenu, LumoIcon.DOWNLOAD, "Export", "Export", e -> downloadSpreadsheetFile());

        MenuItem viewMenu = menuBar.addItem("View");
        SubMenu viewSubMenu = viewMenu.getSubMenu();
        createCheckableItem(viewSubMenu, "Grid lines", true,
                e -> spreadsheet.setGridlinesVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Column and row headings", true,
                e -> spreadsheet.setRowColHeadingsVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Top bar", true,
                e -> spreadsheet.setFunctionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Bottom bar", true,
                e -> spreadsheet.setSheetSelectionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Report mode", false,
                e -> spreadsheet.setReportStyle(e.getSource().isChecked()));

        MenuItem formatMenu = menuBar.addItem("Format");
        SubMenu formatSubMenu = formatMenu.getSubMenu();

        createIconItem(formatSubMenu, VaadinIcon.BOLD, "Bold", "Bold",
                e -> changeSelectedCellsFont(font -> font.setBold(!font.getBold())));
        createIconItem(formatSubMenu, VaadinIcon.ITALIC, "Italic", "Italic",
                e -> changeSelectedCellsFont(font -> font.setItalic(!font.getItalic())));

        MenuItem colorMenu = formatSubMenu.addItem("Color");
        SubMenu colorSubMenu = colorMenu.getSubMenu();

        MenuItem textColorMenu = colorSubMenu.addItem("Text");
        textColorMenu.getSubMenu().addItem("Black",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLACK, null))));
        textColorMenu.getSubMenu().addItem("Blue",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLUE, null))));
        textColorMenu.getSubMenu().addItem("Red",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.RED, null))));
        textColorMenu.getSubMenu().addItem("Green",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.GREEN, null))));
        textColorMenu.getSubMenu().addItem("Orange",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.ORANGE, null))));

        MenuItem backgroundColorMenu = colorSubMenu.addItem("Background");
        backgroundColorMenu.getSubMenu().addItem("Light gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.LIGHT_GRAY, null))));
        backgroundColorMenu.getSubMenu().addItem("White", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.WHITE, null))));
        backgroundColorMenu.getSubMenu().addItem("Cyan", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.CYAN, null))));
        backgroundColorMenu.getSubMenu().addItem("Pink", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.PINK, null))));
        backgroundColorMenu.getSubMenu().addItem("Yellow", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.YELLOW, null))));
        backgroundColorMenu.getSubMenu().addItem("Dark gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.DARK_GRAY, null))));

        MenuItem mergeMenu = menuBar.addItem("Merge");
        SubMenu mergeSubMenu = mergeMenu.getSubMenu();

        mergeSubMenu.addItem("Merge selected", e -> mergeSelectedCells(selectedCells.get()));
        mergeSubMenu.addItem("Unmerge selected", e -> unmergeSelectedRegion(selectedCellMergedRegion.get()));

        MenuItem miscMenu = menuBar.addItem("Miscellaneous");
        SubMenu miscSubMenu = miscMenu.getSubMenu();
        miscSubMenu.addItem("Add comment", e -> addComment(selectedCellReference.get()));

        MenuItem freezePanesMenu = miscSubMenu.addItem("Freeze panes");
        SubMenu freezePanesSubMenu = freezePanesMenu.getSubMenu();
        freezePanesSubMenu.addItem("Freeze columns to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getLastFrozenRow(), spreadsheet.getSelectedCellReference().getCol()));
        freezePanesSubMenu.addItem("Freeze rows to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getSelectedCellReference().getRow(), spreadsheet.getLastFrozenColumn()));
        freezePanesSubMenu.addItem("Unfreeze all", e -> spreadsheet.removeFreezePane());

        MenuItem tableMenu = miscSubMenu.addItem("Table");
        SubMenu tableSubMenu = tableMenu.getSubMenu();
        tableSubMenu.addItem("Create table", e -> createTable(selectedCells.get()));

        return menuBar;
    }

    private Dialog createUploadDialog() {
        Upload uploadSpreadsheet = new Upload(this);

        Dialog uploadFileDialog = new Dialog();
        uploadFileDialog.setHeaderTitle("Upload a spreadsheet file");
        uploadFileDialog.addOpenedChangeListener(e -> {
            uploadSpreadsheet.clearFileList();
        });
        uploadFileDialog.add(uploadSpreadsheet);

        Button openSpreadsheetButton = new Button("Open spreadsheet", ev -> {
            if (uploadedFile != null) {
                try {
                    if (previousFile == null
                            || !previousFile.getAbsolutePath().equals(uploadedFile.getAbsolutePath())) {
                        spreadsheet.read(uploadedFile);
                        //toggleTitleVisibility();
                        previousFile = uploadedFile;
                        uploadFileDialog.close();
                    } else {
                        Notification.show("Please, select a different file.");
                    }
                } catch (Exception e) {
                    getLogger().warn("ERROR reading file " + uploadedFile, e);
                }
            } else {
                Notification.show("Please, select a file to upload first.");
            }
        });
        openSpreadsheetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> uploadFileDialog.close());

        uploadFileDialog.getFooter().add(cancelButton, openSpreadsheetButton);
        return uploadFileDialog;
    }

    private void downloadSpreadsheetFile() {
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            spreadsheet.write(outputStream);
            final StreamResource resource = new StreamResource("file.xlsx",
                    () -> new ByteArrayInputStream(outputStream.toByteArray()));
            final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry()
                    .registerResource(resource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (Exception e) {
            getLogger().warn("Error while processing the file to download", e);
        }
    }

    private void changeSelectedCellsFont(Consumer<XSSFFont> fontConsumer) {
        changeSelectedCellsStyle(cellStyle -> {
            XSSFFont cellFont = (XSSFFont) cloneFont(cellStyle);
            fontConsumer.accept(cellFont);
            cellStyle.setFont(cellFont);
        });
    }

    private void changeSelectedCellsStyle(Consumer<XSSFCellStyle> cellStyleConsumer) {
        final ArrayList<Cell> cellsToRefresh = new ArrayList<>();
        spreadsheet.getSelectedCellReferences().forEach(cellReference -> {
            Cell cell = getOrCreateCell(cellReference);
            CellStyle cellStyle = cell.getCellStyle();
            XSSFCellStyle newCellStyle = (XSSFCellStyle) spreadsheet.getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(cellStyle);

            cellStyleConsumer.accept(newCellStyle);

            cell.setCellStyle(newCellStyle);

            cellsToRefresh.add(cell);
        });
        spreadsheet.refreshCells(cellsToRefresh);
    }

    private Font cloneFont(CellStyle cellstyle) {
        Font newFont = spreadsheet.getWorkbook().createFont();
        Font originalFont = spreadsheet.getWorkbook().getFontAt(cellstyle.getFontIndex());
        if (originalFont != null) {
            newFont.setBold(originalFont.getBold());
            newFont.setItalic(originalFont.getItalic());
            newFont.setFontHeight(originalFont.getFontHeight());
            newFont.setUnderline(originalFont.getUnderline());
            newFont.setStrikeout(originalFont.getStrikeout());
            // This cast an only be done when using .xlsx files
            XSSFFont originalXFont = (XSSFFont) originalFont;
            XSSFFont newXFont = (XSSFFont) newFont;
            newXFont.setColor(originalXFont.getXSSFColor());
        }
        return newFont;
    }

    private MenuItem createCheckableItem(HasMenuItems menu, String item, boolean checked,
                                         ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem menuItem = menu.addItem(item, clickListener);
        menuItem.setCheckable(true);
        menuItem.setChecked(checked);

        return menuItem;
    }

    private MenuItem createIconItem(HasMenuItems menu, LumoIcon iconName, String label, String ariaLabel,
                                    ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon("lumo", iconName.toString().toLowerCase());

        MenuItem item = menu.addItem(icon, clickListener);
        item.setAriaLabel(ariaLabel);

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName, String label, String ariaLabel,
                                    ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon(iconName);

        icon.getStyle().set("width", "var(--lumo-icon-size-s)");
        icon.getStyle().set("height", "var(--lumo-icon-size-s)");
        icon.getStyle().set("marginRight", "var(--lumo-space-s)");

        MenuItem item = menu.addItem(icon, clickListener);
        item.setAriaLabel(ariaLabel);

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private void mergeSelectedCells(CellRangeAddress selectedCells) {
        if (selectedCells == null) {
            Notification.show("Please select a region of cells to be merged.");
            return;
        }
        spreadsheet.addMergedRegion(selectedCells);
    }

    private void unmergeSelectedRegion(CellRangeAddress selectedCellMergedRegion) {
        if (selectedCellMergedRegion == null) {
            Notification.show("Please select a merged region of cells to be unmerged.");
            return;
        }
        for (int i = 0; i < spreadsheet.getActiveSheet().getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = spreadsheet.getActiveSheet().getMergedRegion(i);
            if (selectedCellMergedRegion.getFirstRow() == mergedRegion.getFirstRow()
                    && selectedCellMergedRegion.getFirstColumn() == mergedRegion.getFirstColumn()) {
                spreadsheet.removeMergedRegion(i);
            }
        }
    }

    private void createTable(CellRangeAddress cellAddresses) {
        if (cellAddresses == null) {
            Notification.show("Please select a region of cells to create the table.");
            return;
        }
        SpreadsheetTable table = new SpreadsheetFilterTable(spreadsheet, cellAddresses);
        spreadsheet.registerTable(table);
    }
}
