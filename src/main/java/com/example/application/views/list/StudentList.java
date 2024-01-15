package com.example.application.views.list;

import com.example.application.data.edu.GStudent;
import com.example.application.services.GoogleClassroomService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Component
@Scope("prototype")
@Route(value = "studentsList", layout = MainLayout.class)
@PageTitle("Students")
@PermitAll
public class StudentList extends VerticalLayout {

    private GoogleClassroomService googleservice;
    Grid<GStudent> grid = new Grid<>(GStudent.class);
    TextField filterText = new TextField();
    TextField filterYear = new TextField();

    TextField filterHome = new TextField();

    private List<GStudent> students;


    public StudentList(GoogleClassroomService googleservice) {
        this.googleservice = googleservice;
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

        filterYear.setPlaceholder("Filter by year...");
        filterYear.setClearButtonVisible(true);
        filterYear.setValueChangeMode(ValueChangeMode.LAZY);
        filterYear.addValueChangeListener(e -> updateList());

        filterHome.setPlaceholder("Filter by Home...");
        filterHome.setClearButtonVisible(true);
        filterHome.setValueChangeMode(ValueChangeMode.LAZY);
        filterHome.addValueChangeListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, filterYear, filterHome);
        toolbar.addClassName("toolbar");
        return toolbar;
    }


    private void updateList() {
        students=googleservice.findAllStudents(filterText.getValue().toLowerCase());
        grid.setItems(filterByHomeAndYear(students));
    }

    private List<GStudent> filterByHomeAndYear(List<GStudent> students) {
        String filterTextHome = filterHome.getValue().toLowerCase();
        String filterTextYear = filterYear.getValue().toLowerCase();

        Stream<GStudent> studentStream = students.stream();

        if (!filterTextHome.isEmpty()) {
            studentStream = studentStream.filter(student -> student.getHomeroom().toLowerCase().contains(filterTextHome));
        }

        if (!filterTextYear.isEmpty()) {
            studentStream = studentStream.filter(student -> student.getYearGroup().toLowerCase().contains(filterTextYear));
        }

        return studentStream.collect(Collectors.toList());
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid);
        content.setFlexGrow(2, grid);
        content.addClassName("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {
        grid.addClassName("contact-grid");
        grid.setSizeFull();
        //to set the columns of the grid
        grid.setColumns("firstName", "lastName", "yearGroup", "homeroom", "numOfClassrooms", "email");
        //set grid column names
        //id
        grid.getColumnByKey("firstName").setHeader("First Name");
        grid.getColumnByKey("lastName").setHeader("Last Name");
        grid.getColumnByKey("yearGroup").setHeader("Year Group");
        grid.getColumnByKey("homeroom").setHeader("Homeroom");
        grid.getColumnByKey("numOfClassrooms").setHeader("Number of Classes");
        grid.getColumnByKey("email").setHeader("Email");
        grid.addColumn(student -> {
            return googleservice.getPresentToday(student.getEmail()) == 1 ? "Present" :
                    (googleservice.getPresentToday(student.getEmail()) == -1 ? "Not Set" : "Absent");
        }).setHeader("Present Today").setSortable(true);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

    }
}
