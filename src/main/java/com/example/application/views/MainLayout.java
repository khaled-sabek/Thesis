package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.services.GoogleClassroomService;
import com.example.application.views.list.ClassroomList;
import com.example.application.views.list.StudentList;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    private final GoogleClassroomService googleClassroomService;

    public MainLayout(SecurityService securityService, GoogleClassroomService googleClassroomService) {
        this.googleClassroomService = googleClassroomService;
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("TLS GC Pro");
        logo.addClassNames("text-l", "m-m");

        Button logOut = new Button("Log out", event -> securityService.logout());
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logOut);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();

        addToNavbar(header);
    }
    private void createDrawer() {
        VerticalLayout verticalLayout=new VerticalLayout();
        RouterLink dashboard=new RouterLink("Dashboard",DashboardView.class);
        dashboard.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink classrooms=new RouterLink("Classrooms", ClassroomList.class);
        classrooms.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink students=new RouterLink("Students", StudentList.class);
        students.setHighlightCondition(HighlightConditions.sameLocation());

        verticalLayout.add(
                dashboard,
                classrooms,
                students
        );

        if(!googleClassroomService.findAllClassroomsWithAttendancePermission().isEmpty()) {
            RouterLink attendance = new RouterLink("Attendance", AttendanceView.class);
            attendance.setHighlightCondition(HighlightConditions.sameLocation());
            verticalLayout.add(attendance);
        }
        addToDrawer(verticalLayout);

    }
}
