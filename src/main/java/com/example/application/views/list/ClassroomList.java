package com.example.application.views.list;

import com.example.application.data.edu.GClassroom;
import com.example.application.services.GoogleClassroomService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

import java.util.List;

@org.springframework.stereotype.Component
@Scope("prototype")
@Route(value = "classroomsList", layout = MainLayout.class)
@PageTitle("Classrooms")
@PermitAll
public class ClassroomList extends VerticalLayout {
    private final GoogleClassroomService googleClassroomService;

    Grid<GClassroom> grid = new Grid<>(GClassroom.class);
    TextField filterText = new TextField();

    private final String usersEmail;

    private Dialog attendanceDialog;

    public ClassroomList(GoogleClassroomService googleClassroomService) {
        this.googleClassroomService = googleClassroomService;
        this.usersEmail = googleClassroomService.getUsersEmail();
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        add(getToolbar(), getContent());
        updateList();
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(filterText);
        toolbar.addClassName("toolbar");
        return toolbar;
    }


    private void updateList() {
        grid.setItems(
                googleClassroomService.findAllClassrooms(filterText.getValue()));
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid);
        content.addClassName("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {
        grid.addClassName("contact-grid");
        grid.setSizeFull();
        grid.setColumns("classroomName", "classroomDepartment", "numberOfStudents");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.getColumnByKey("classroomName").setHeader("Classroom Name");
        grid.getColumnByKey("classroomDepartment").setHeader("Department");
        grid.getColumnByKey("numberOfStudents").setHeader("Number of Students");


        grid.addColumn((classroom -> {
            return (classroom.getAttendanceResponsibility() == null ? "Not Set" :
                    classroom.getAttendanceResponsibility().equalsIgnoreCase(usersEmail) ? "Granted" : classroom.getAttendanceResponsibility());
        })).setHeader("Attendance Responsibility");

        //new button to set class attendance responsibility
        //only if admin
        if (usersEmail.equalsIgnoreCase("lms@tawfikials.com")) {
            grid.addComponentColumn(classroom -> {
                return new Button("Enter Email", clickEvent -> {
                    attendanceDialog = attendanceEmailDialog(classroom.getgClassroomEmail());
                    attendanceDialog.open();
                });
            }).setHeader("Set Attendance Responsibility");

        }

    }

    public Dialog attendanceEmailDialog(String classroomEmail) {
        Dialog dialog = new Dialog();
        dialog.getElement().setAttribute("aria-label", "Set Email");

        VerticalLayout dialogLayout = createDialogLayout(dialog, classroomEmail);
        dialog.add(dialogLayout);

        return dialog;
    }

    private VerticalLayout createDialogLayout(Dialog dialog, String classroomEmail) {
        H2 headline = new H2("Set Attendance Responsibility");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        TextField email = new TextField("Email");
        VerticalLayout fieldLayout = new VerticalLayout(email);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button saveButton = new Button("Save", e -> {
            if (classVisibleToAssigne(email.getValue(), classroomEmail)) {
                saveAttendanceEmail(email.getValue(), classroomEmail);

            } else {
                GoogleClassroomService.errorNotification("The user with this email, " +
                        "won't be able to see the classroom, so can't possibly post attendance for it!", null,null);
            }
            updateList();
            dialog.close();
        });
        saveButton.addClickShortcut(Key.ENTER);
        cancelButton.addClickShortcut(Key.ESCAPE);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonLayout
                .setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(headline, fieldLayout,
                buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return dialogLayout;
    }

    private boolean classVisibleToAssigne(String emailToCheck, String classroomEmail) {
        List<GClassroom> visibleClasses = googleClassroomService.findAllClassroomsForEmail(null, emailToCheck);
        for (GClassroom visibleClass : visibleClasses) {
            if (visibleClass.getgClassroomEmail().equalsIgnoreCase(classroomEmail)) {
                return true;
            }
        }
        return false;
    }

    private void saveAttendanceEmail(String value, String classroomEmail) {
        googleClassroomService.setAttendanceResponsibility(value, classroomEmail);
        grid.getDataProvider().refreshAll();
    }

}







