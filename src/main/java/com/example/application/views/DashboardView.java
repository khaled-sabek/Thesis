package com.example.application.views;

import com.example.application.data.edu.GClassroom;
import com.example.application.security.SecurityService;
import com.example.application.services.GoogleClassroomService;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.Student;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
@PermitAll
public class DashboardView extends VerticalLayout {


    @Autowired
    private SecurityService securityService;


    @Autowired
    private GoogleClassroomService googleClassroomService;
    private UI ui;

    private Button updateDatabase;


    public DashboardView(GoogleClassroomService googleClassroomService) {
        this.googleClassroomService = googleClassroomService;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        googleClassroomService.setupDetails();

        updateDatabase = readAndPopulateDatabase();
        updateDatabase.setAutofocus(true);
        updateDatabase.setTabIndex(1);
        updateDatabase.setDisableOnClick(true);

        H2 welcome = new H2("Welcome " + googleClassroomService.getUsersName());

        H3 updateDatabaseText = new H3("Click the button below to update your data from Google Classroom");

        updateDatabase.setTooltipText("Click here if you made edits via Google Classroom since you last visited");


        add(
                welcome
        );

        if (googleClassroomService.getUsersEmail().equals("lms@tawfikials.com")) {
            add(updateDatabaseText,
                    updateDatabase);
        }

        add(
                getNumberOfStudents(),
                getClassroomsChart()
        );

    }

    static class tempThread extends Thread {
        private String usersEmail;
        private String usersName;

        private UI ui;

        private Button updateDatabase;

        private GoogleClassroomService googleClassroomService;

        private Notification mainNotification;


        public tempThread(String usersEmail, String usersName, UI ui, Button updateDatabase, GoogleClassroomService googleClassroomService) {
            super();
            this.ui = ui;
            this.updateDatabase = updateDatabase;
            this.googleClassroomService = googleClassroomService;
            this.usersEmail = usersEmail;
            this.usersName = usersName;
        }

        @Override
        public void run() {

            // anything happening inside this thread will not be updated to the ui automatically, and needs to be pushed to the ui.
            try {
                this.ui.access(() -> {

                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Updating Database from Google Classroom", "middle", 6);
                    this.mainNotification=GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("    Updating ...", "bottom", 0);

                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Getting Courses", "bottom", 6);

                });


                List<Course> courses = googleClassroomService.getCourses();
                if (courses.isEmpty()) {
                    this.ui.access(() -> {
                        GoogleClassroomService.errorNotification("You don't have any classrooms!", updateDatabase,null);
                    });
                }
                this.ui.access(() -> {
                    GoogleClassroomService.completedDatabasePopulationSectionNotification("Got all " + courses.size() + " courses!", false, "bottom", 6);

                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Getting Students", "bottom", 6);
                });
                Set<Student> students = new HashSet<>();
                Dictionary<Course, List<Student>> studentCourseDict = new Hashtable<Course, List<Student>>();
                for (Course course : courses) {
                    this.ui.access(() -> {
                        GoogleClassroomService.updatingDatabaseFromAPIStatusNotification(
                                "Course: " + (courses.indexOf(course) + 1)
                                        + " out of " + courses.size() + " courses!", "bottom", 6);
                        GoogleClassroomService.updatingDatabaseFromAPIStatusNotification(
                                "Getting Students from course: "
                                        + course.getName(), "bottom", 6);
                    });
                    Set<Student> tempStudents = new HashSet<>();
                    tempStudents = googleClassroomService.getStudentsPerCourse(course);
                    final int numStudents = tempStudents.size();
                    students.addAll(tempStudents);
                    students = removeDuplicatesByEmail(students);
                    studentCourseDict.put(course, new ArrayList<Student>(tempStudents));
                    this.ui.access(() -> {
                        GoogleClassroomService.updatingDatabaseFromAPIStatusNotification(
                                "Got all " + numStudents + " students from course: " + course.getName(), "bottom", 6);
                    });
                }


                final int numStudents = students.size();
                this.ui.access(() -> {
                    GoogleClassroomService.completedDatabasePopulationSectionNotification("Got all " + numStudents + " students!", false, "bottom", 6);
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Deleting old Data", "bottom", 6);
                });
                //delete old database
                googleClassroomService.deleteOldDatabase();
                this.ui.access(() -> {
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Inserting new Data from Google Classroom", "bottom", 6);
                });
                //populate
                this.ui.access(() -> {
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Populating Classrooms", "bottom", 6);
                });
                googleClassroomService.populateClassrooms(courses, studentCourseDict,usersEmail);
                this.ui.access(() -> {
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Populating Students", "bottom", 6);
                });
                googleClassroomService.populateStudents(students, studentCourseDict,usersEmail);

                googleClassroomService.populateStudies(studentCourseDict, courses,usersEmail);

                this.ui.access(() -> {
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification("Making it faster! (DB Indices)", "bottom", 6);
                });
                googleClassroomService.createIndices();
                this.ui.access(() -> {
                    GoogleClassroomService.completedDatabasePopulationSectionNotification("All Data Updated! Refresh complete!", true, "bottom", 6);
                    updateDatabase.setEnabled(true);
                    mainNotification.close();
                });
            } catch (Exception e) {
                e.printStackTrace();
                this.ui.access(() -> {
                    mainNotification.close();
                    updateDatabase.setEnabled(true);
                    GoogleClassroomService.errorNotification("Error, please try refreshing again",null,ui);
                });
            }


        }
    }

    public Button readAndPopulateDatabase() {
        addAttachListener(event -> {
            this.ui = event.getUI();
        });
        String userEmail = googleClassroomService.getUsersEmail();
        String userName = googleClassroomService.getUsersName();


        Button testButton = new Button("Refresh Database", click -> {
            tempThread t = new tempThread(userEmail, userName, this.ui, updateDatabase, googleClassroomService);
            t.start();
        });
        return testButton;
    }

    private static Set<Student> removeDuplicatesByEmail(Set<Student> students) {
        Set<Student> studentsNoDuplicates = new HashSet<>();
        Set<String> emails = new HashSet<>();
        for (Student student : students) {
            if (!emails.contains(student.getProfile().getEmailAddress())) {
                studentsNoDuplicates.add(student);
                emails.add(student.getProfile().getEmailAddress());
            }
        }
        return studentsNoDuplicates;
    }

    private void apiErrorLoginAgain(String error) {
        addAttachListener(event -> {
            this.ui = event.getUI();
        });
        Button testButton = new Button("Refresh Database", click -> {
            Thread backgroundThread = new Thread(() -> {
                this.ui.access(() -> {
                    GoogleClassroomService.updatingDatabaseFromAPIStatusNotification(error, "bottom", 6);
                });
                securityService.logout();
            });
        });
        testButton.click();

    }

    private Component getNumberOfStudents() {
        int numOfStudents = googleClassroomService.findAllStudents(null).size();
        Span stats = new Span(numOfStudents + (numOfStudents == 1 ? " Student" : " Students"));
        stats.addClassNames("text-xl", "mt-m");
        return stats;
    }

    private Component getClassroomsChart() {
        Chart chart = new Chart(ChartType.COLUMN);

        Configuration conf = chart.getConfiguration();
        conf.setTitle("Number of Students per Classroom");
        conf.setSubTitle("differentiated by homeroom");

        List<GClassroom> allClassrooms = googleClassroomService.findAllClassrooms(null);

        String[] uniqueYeargroups = getUniqueYeargroups(allClassrooms);
        for (int i = 0; i < uniqueYeargroups.length; i++) {
            uniqueYeargroups[i] = uniqueYeargroups[i].replace("Year", "Year ");
            uniqueYeargroups[i] = uniqueYeargroups[i].replace("KG", "KG ");
        }

        String colours[] = getUniqueColours(allClassrooms);
        //check if year 9 or 8, if yes then add no colour
        if (Arrays.asList(uniqueYeargroups).contains("Year 8") || Arrays.asList(uniqueYeargroups).contains("Year 9")) {
            ArrayList<String> coloursList = new ArrayList<String>(Arrays.asList(colours));
            coloursList.add("No Colour");
            colours = coloursList.toArray(new String[0]);
        }

        int[][] countPerColourPerYear = getCountPerColourPerYear(allClassrooms, colours, uniqueYeargroups);

        for (int color = 0; color < colours.length; color++) {
            ListSeries series = new ListSeries();
            series.setName(colours[color]);

            // The rows of the table
            for (int location = 0; location < countPerColourPerYear.length; location++) {
                series.addData(countPerColourPerYear[location][color]);
                PlotOptionsSeries options = new PlotOptionsSeries();
                switch (colours[color]) {
                    case "Red":
                        options.setColor(SolidColor.RED);
                        break;
                    case "Blue":
                        options.setColor(SolidColor.BLUE);
                        break;
                    case "Green":
                        options.setColor(SolidColor.GREEN);
                        break;
                    case "Yellow":
                        options.setColor(SolidColor.YELLOW);
                        break;
                    case "Orange":
                        options.setColor(SolidColor.ORANGE);
                        break;
                    case "Violet":
                        options.setColor(SolidColor.PURPLE);
                        break;
                    case "No Colour":
                        options.setColor(SolidColor.BLACK);
                        break;
                    default:
                        options.setColor(SolidColor.CORNFLOWERBLUE);
                        break;
                }
                SeriesTooltip seriesTooltip = new SeriesTooltip();
                seriesTooltip.setPointFormatter("function() { return this.y + ' Students' }");

                options.setTooltip(seriesTooltip);

                series.setPlotOptions(options);
            }
            conf.addSeries(series);
        }

        // Tooltip element needs to be configured on chart
        chart.getConfiguration().setTooltip(new Tooltip());

        XAxis xaxis = new XAxis();
        xaxis.setTitle("Classrooms");

        xaxis.setCategories(uniqueYeargroups);
        conf.addxAxis(xaxis);

        YAxis yaxis = new YAxis();
        yaxis.setTitle("Student Count");
        conf.addyAxis(yaxis);

        chart.setConfiguration(conf);
        return chart;
    }

    private String[] getUniqueYeargroups(List<GClassroom> allClassrooms) {
        Set<String> uniqueYeargroups = new HashSet<>();
        for (GClassroom gClassroom : allClassrooms) {
            String classroomName = gClassroom.getClassroomName();
            String yearGroup = classroomName.split("-")[0];
            uniqueYeargroups.add(yearGroup);
        }
        String[] uniqueYeargroupsArray = uniqueYeargroups.toArray(new String[0]);
        Arrays.sort(uniqueYeargroupsArray);
        return uniqueYeargroupsArray;
    }

    private int[][] getCountPerColourPerYear(List<GClassroom> allClassrooms, String[] colours, String[] uniqueYeargroupsArray) {
        //find how many year groups there are

        int[][] countPerColourPerYear = new int[uniqueYeargroupsArray.length][colours.length];
        for (GClassroom gClassroom : allClassrooms) {
            String classroomName = gClassroom.getClassroomName();
            String yearGroup = classroomName.split("-")[0].replace("Year", "Year ").replace("KG", "KG ");
            if (classroomName.split("-").length > 1) {
                String colour = classroomName.split("-")[1];
                int yearGroupIndex = Arrays.asList(uniqueYeargroupsArray).indexOf(yearGroup);
                int colourIndex = Arrays.asList(colours).indexOf(colour);
                countPerColourPerYear[yearGroupIndex][colourIndex] = gClassroom.getNumberOfStudents();
            } else {
                int yearGroupIndex = Arrays.asList(uniqueYeargroupsArray).indexOf(yearGroup);
                countPerColourPerYear[yearGroupIndex][colours.length - 1] = gClassroom.getNumberOfStudents();
            }
        }
        return countPerColourPerYear;
    }

    private String[] getUniqueColours(List<GClassroom> allClassrooms) {
        Set<String> colours = new HashSet<>();
        for (GClassroom gClassroom : allClassrooms) {
            String classroomName = gClassroom.getClassroomName();
            if (classroomName.split("-").length > 1) {
                String colour = classroomName.split("-")[1];
                colours.add(colour);
            }
        }
        return colours.toArray(new String[0]);
    }
}
