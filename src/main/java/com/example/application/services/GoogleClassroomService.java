package com.example.application.services;

import com.example.application.data.GUserRepository;
import com.example.application.data.edu.*;
import com.example.application.security.SecurityService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.Student;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@Service
@Lazy
public class GoogleClassroomService {
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private Classroom Gservice;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GClassroomRepository gClassroomRepository;

    @Autowired
    private GStudentRepository gStudentRepository;

    @Autowired
    private GStudiesRepository gStudiesRepository;

    @Autowired
    private GAttendanceRepository gAttendanceRepository;

    @Autowired
    private GUserRepository gUserRepository;

    public GoogleClassroomService(OAuth2AuthorizedClientService authorizedClientService, SecurityService securityService) {
        this.authorizedClientService = authorizedClientService;
        this.securityService = securityService;
        setupDetails();
    }

    public static void successNotification(String message) {
        Notification notification = Notification
                .show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        notification.open();
    }

    public static void errorNotification(String message, Button refresh,UI ui) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(0);
        var layout = new HorizontalLayout();
        Button closeButton = new CloseButton();
        Button reportButton = new ReportButton();
        Button retryButton = new Button("Retry");
        try{
            retryButton.addClickListener(e -> ui.getCurrent().getPage().reload());
        }
        catch(Exception e){
            retryButton.addClickListener(f -> UI.getCurrent().getPage().reload());
        }
        if (refresh != null) {
            layout = new HorizontalLayout(new Text(message), retryButton, reportButton, closeButton);
        } else {
            layout = new HorizontalLayout(new Text(message), reportButton, closeButton);
        }
        notification.add(layout);
        notification.open();
    }


    public void setupDetails() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = new GoogleCredential().setAccessToken(getSessionBearerToken());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            Gservice =
                    new Classroom.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                            .setApplicationName("GCPRO")
                            .build();

        } catch (Exception e) {
            e.printStackTrace();
            apiErrorLoginAgain("Sorry, we couldn't connect to your Google account,please try logging in again!");
        }
    }

    public String getUsersEmail() {

        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleCredential credential = new GoogleCredential().setAccessToken(getSessionBearerToken());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if(oAuth2User==null){
            System.out.println("oAuth2User==null");
        }
        return oAuth2User.getAttribute("email");

    }

    public String getUsersName() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleCredential credential = new GoogleCredential().setAccessToken(getSessionBearerToken());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User=(OAuth2User) authentication.getPrincipal();
        return oAuth2User.getAttribute("given_name");
    }

    private void apiErrorLoginAgain(String error) {
        sendErrorNotification(error);
        securityService.logout();
    }

    private void sendErrorNotification(String error) {
        //notification with error
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Div text = new Div(new Text("Failed to generate report"));

        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(event -> {
            notification.close();
        });

        HorizontalLayout layout = new HorizontalLayout(text, closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }


    private String getSessionBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName());

        if (client == null || client.getAccessToken() == null) {
            throw new RuntimeException("Failed to retrieve OAuth2 token");
        }

        return client.getAccessToken().getTokenValue();
    }

    public void createIndices() {
        String sql1 = "CREATE INDEX idx_gclassroom_email ON GClassroom(gClassroomEmail) USING HASH";
        String sql2 = "CREATE INDEX idx_gstudies_student_email ON GStudies(gStudentEmail) USING HASH";
        String sql3 = "CREATE INDEX idx_gstudies_classroom_email ON GStudies(gClassroomEmail) USING HASH";
        String sql4 = "CREATE INDEX idx_gstudies_student_email ON GStudies(gStudentEmail) USING HASH";

        try {
            jdbcTemplate.execute(sql1);
        } catch (Exception e) {
        }
        try {
            jdbcTemplate.execute(sql2);
        } catch (Exception e) {
        }
        try {
            jdbcTemplate.execute(sql3);
        } catch (Exception e) {
        }
        try {
            jdbcTemplate.execute(sql4);
        } catch (Exception e) {
        }
    }

    public List<Course> getCourses() {
        try {
            List<Course> courses = Gservice.courses().list().setPageSize(60).execute().getCourses();
            String pageToken = Gservice.courses().list().setPageSize(60).execute().getNextPageToken();
            while (pageToken != null) {

                courses.addAll(Gservice.courses().list().setPageSize(60).setPageToken(
                                pageToken)
                        .execute().getCourses());
                pageToken = Gservice.courses().list().setPageSize(60).setPageToken(pageToken).execute().getNextPageToken();
            }
            List<Course> coursesToRemove = new ArrayList<>();
            for (Course course : courses) {
                if (course.getCourseState().equals("ARCHIVED")) {
                    coursesToRemove.add(course);
                }
            }
            courses.removeAll(coursesToRemove);
            coursesToRemove = new ArrayList<>();
            for (Course course : courses) {
                if (UserStudentInCourse(course)) {
                    coursesToRemove.add(course);
                }
            }
            courses.removeAll(coursesToRemove);
            return courses;
        } catch (Exception e) {
            e.printStackTrace();
            apiErrorLoginAgain("Sorry, we couldn't get your courses, please try logging in again!");
        }
        return new ArrayList<Course>();
    }

    public Set<Student> getStudentsPerCourse(Course course) throws IOException {

        List<Student> courseStudents = Gservice.courses().students().list(course.getId()).setPageSize(60).execute().getStudents();
        if (courseStudents == null) {
            return new HashSet<Student>();
        }
        String pageToken = Gservice.courses().students().list(course.getId()).setPageSize(60).execute().getNextPageToken();
        while (pageToken != null) {
            //get next page
            List<Student> nextPage = Gservice.courses().students().list(course.getId())
                    .setPageSize(60)
                    .setPageToken(pageToken)
                    .execute().getStudents();
            courseStudents.addAll(nextPage);
            pageToken = Gservice.courses().students().list(course.getId())
                    .setPageSize(60)
                    .setPageToken(pageToken)
                    .execute().getNextPageToken();
        }

        return new HashSet<Student>(courseStudents);
    }

    public void deleteOldDatabase() {
        gClassroomRepository.deleteAll();
        gStudiesRepository.deleteAll();
        gStudentRepository.deleteAll();
    }

    public void populateClassrooms(List<Course> courses, Dictionary studentCourseDict,String usersEmail) {
        List<GClassroom> gClassrooms = new ArrayList<>();
        for (Course course : courses) {
            GClassroom courseEntity = new GClassroom();
            courseEntity.setOwnerUserEmail(usersEmail);
            courseEntity.setClassroomName(course.getName());
            courseEntity.setClassroomDescription(course.getDescription());
            courseEntity.setgClassroomEmail(course.getCourseGroupEmail());
            String yearGroup = course.getName().split("-")[0]
                    .substring(0, course.getName().split("-")[0].length() - 1);
            courseEntity.setClassroomDepartment(yearGroup);
            courseEntity.setGID(course.getId());
            List<Student> retrivedStudents = (List<Student>) studentCourseDict.get(course);
            if (retrivedStudents == null) {
                courseEntity.setNumberOfStudents(0);
            } else {
                courseEntity.setNumberOfStudents(retrivedStudents.size());
            }
            gClassrooms.add(courseEntity);
        }
        gClassroomRepository.saveAll(gClassrooms);

    }

    public void populateStudents(Set<Student> students, Dictionary studentCourseDict,String usersEmail) {
        List<GStudent> gStudents = new ArrayList<>();

        for (Student student : students) {
            GStudent studentEntity = new GStudent();
            studentEntity.setFirstName(student.getProfile().getName().getGivenName());
            studentEntity.setEmail(student.getProfile().getEmailAddress());
            studentEntity.setLastName(student.getProfile().getName().getFamilyName());
            studentEntity.setPicture(student.getProfile().getPhotoUrl());
            studentEntity.setOwnerUserEmail(usersEmail);
            studentEntity.setGID(student.getUserId());
            setHomeRoomYearGroup(studentEntity, studentCourseDict);
            studentEntity.setNumOfClassrooms(calculateNumOfClasses(studentCourseDict, student.getProfile().getEmailAddress()));

            gStudents.add(studentEntity);
        }
        gStudentRepository.saveAll(gStudents);

    }

    private void setHomeRoomYearGroup(GStudent studentEntity, Dictionary studentCourseDict) {
        String emailAddress = studentEntity.getEmail();
        Enumeration<Course> keys = studentCourseDict.keys();
        while (keys.hasMoreElements()) {
            Course course = keys.nextElement();
            List<Student> students = (List<Student>) studentCourseDict.get(course);
            for (Student student : students) {
                if (student.getProfile().getEmailAddress().equals(emailAddress)) {
                    List<String> courseSplit = Arrays.asList(course.getName().split("-"));
                    if (courseSplit.size() == 2) {
                        String yearGroup = courseSplit.get(0);
                        int len = yearGroup.length();
                        yearGroup = yearGroup.substring(0, len - 1) + " " + yearGroup.substring(len - 1);
                        studentEntity.setYearGroup(yearGroup);
                        studentEntity.setHomeroom(courseSplit.get(1));
                    } else {

                        //check if teacher, and so already has data set, because multiple classes
                        if (studentEntity.getYearGroup() != null &&
                                (studentEntity.getYearGroup().toLowerCase().contains("year")
                                        || studentEntity.getYearGroup().toLowerCase().contains("kg"))) {
                            studentEntity.setYearGroup("T");
                            studentEntity.setHomeroom("T");
                        } else {
                            if (courseSplit.size() == 1) {
                                if (courseSplit.get(0).contains("Year")) {
                                    String yearGroup = courseSplit.get(0);
                                    int len = yearGroup.length();
                                    yearGroup = yearGroup.substring(0, len - 1) + " " + yearGroup.substring(len - 1);
                                    studentEntity.setYearGroup(yearGroup);
                                    studentEntity.setHomeroom("Unknown");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void populateStudies(Dictionary studentCourseDict, List<Course> courses,String usersEmail) {
        List<GStudies> gStudies = new ArrayList<>();
        for (Course course : courses) {
            List<Student> retrivedStudents = (List<Student>) studentCourseDict.get(course);

            if (retrivedStudents == null) {
                continue;
            }
            for (Student student : retrivedStudents) {
                GStudies gStudy = new GStudies();
                gStudy.setOwnerUserEmail(usersEmail);
                gStudy.setgClassroomEmail(course.getCourseGroupEmail());
                gStudy.setgStudentEmail(student.getProfile().getEmailAddress());

                gStudies.add(gStudy);
            }
        }
        gStudiesRepository.saveAll(gStudies);
    }


    private int calculateNumOfClasses(Dictionary studentCourseDict, String emailAddress) {
        int result = 0;
        Enumeration<Course> keys = studentCourseDict.keys();
        while (keys.hasMoreElements()) {
            Course course = keys.nextElement();
            List<Student> students = (List<Student>) studentCourseDict.get(course);
            for (Student student : students) {
                if (student.getProfile().getEmailAddress().equals(emailAddress)) {
                    result++;
                }
            }
        }
        return result;
    }


    public boolean UserStudentInCourse(Course course) {
        try {
            List<Student> students = Gservice.courses().students().list(course.getId()).execute().getStudents();
            if (students == null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            apiErrorLoginAgain("Sorry, we couldn't get your students, please try logging in again!");

        }
        return false;
    }

    public static void completedDatabasePopulationSectionNotification(String message, boolean done, String location, int duration) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(duration * 1000);
        Button closeButton = new CloseButton();
        HorizontalLayout layout = new HorizontalLayout(new Text(message), closeButton);

        notification.add(layout);
        notification.open();
        switch (location) {
            case "top":
                notification.setPosition(Notification.Position.TOP_CENTER);
                break;
            case "middle":
                notification.setPosition(Notification.Position.MIDDLE);
                break;
            case "bottom":
                notification.setPosition(Notification.Position.BOTTOM_START);
                break;
        }
        if (done) {
            notifyDatabaseEntitiesDone();
        }
    }

    public static void notifyDatabaseEntitiesDone() {
        createUploadSuccess("Classrooms");
        createUploadSuccess("Students");
    }

    public static void createUploadSuccess(String entity) {
        Notification notification = new Notification();

        notification.setDuration(0);
        Icon icon = VaadinIcon.CHECK_CIRCLE.create();
        icon.setColor("var(--lumo-success-color)");

        Div uploadSuccessful = new Div(new Text("Data Update successful"));
        uploadSuccessful.getStyle()
                .set("font-weight", "600")
                .setColor("var(--lumo-success-text-color)");

        Span fileName = new Span("Your " + entity);
        fileName.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600");

        Div info = new Div(uploadSuccessful,
                new Div(fileName, new Text(" are now available in "),
                        new Anchor(entity.toLowerCase() + "List", entity + " page")));

        info.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .setColor("var(--lumo-secondary-text-color)");

        var layout = new HorizontalLayout(icon, info,
                createCloseBtn(notification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setPosition(Notification.Position.BOTTOM_START);

        notification.open();

    }


    public static Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.CLOSE_SMALL.create(),
                clickEvent -> notification.close());
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    public static Notification updatingDatabaseFromAPIStatusNotification(String message, String location, int duration) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        notification.setDuration(duration * 1000);
        Button closeButton = new CloseButton();

        if(duration==0){
            //loading indicator notification, add loading icon
            String src = "/icons/loadingIcon1.gif";
            Image img = new Image(src, "Loading...");
            img.setWidth(40, Unit.PIXELS);
            img.setHeight(40, Unit.PIXELS);
            notification.add(img);
        }


        HorizontalLayout layout = new HorizontalLayout(new Text(message), closeButton);

        notification.add(layout);
        notification.open();
        switch (location) {
            case "top":
                notification.setPosition(Notification.Position.TOP_CENTER);
                break;
            case "middle":
                notification.setPosition(Notification.Position.MIDDLE);
                break;
            case "bottom":
                notification.setPosition(Notification.Position.BOTTOM_START);
                break;
        }
        return notification;
    }

    public List<GClassroom> findAllClassroomsWithAttendancePermission() {
        //if admin, then get all classes
        if (getUsersEmail().equalsIgnoreCase("lms@tawfikials.com")) {
            return gClassroomRepository.findAll();
        } else {
            List<GClassroom> allowedClassrooms = findAllClassrooms(null);
            List<GClassroom> classroomsWithAttendancePermission = new ArrayList<>();
            for (GClassroom classroom : allowedClassrooms) {
                if (classroom.getAttendanceResponsibility() != null && classroom.getAttendanceResponsibility().equalsIgnoreCase(getUsersEmail())) {
                    classroomsWithAttendancePermission.add(classroom);
                }
            }
            return classroomsWithAttendancePermission;
            //return gClassroomRepository.findClassroomWithAttendancePermission(getUsersEmail());
        }
    }

    public boolean attendanceOnDate(String date, String classroomEmail, String studentEmail) {
        return gAttendanceRepository.attendanceOnDateForStudent(date, classroomEmail, studentEmail);
    }

    public List<GStudent> findStudentsInCourse(String classEmail) {
        return gStudentRepository.findBygClassroom(classEmail);
    }

    public void saveAttendanceRecords(List<GAttendanceRecord> attendanceRecords, String classroomEmail, String date) {
        //delete old attendance data
        List<GAttendanceRecord> oldRecords = new ArrayList<>(gAttendanceRepository.attendanceOnDateForClassroom(date, classroomEmail));
        gAttendanceRepository.deleteAll(oldRecords);
        //save new attendance data
        gAttendanceRepository.saveAll(attendanceRecords);
    }

    public int getPresentToday(String studentEmail) {
        int present = 0;
        LocalDate today = LocalDate.now();

        List<GAttendanceRecord> record = gAttendanceRepository.getPresentToday(today.toString(), studentEmail);

        if (record != null && !record.isEmpty()) {
            return record.get(0).isPresent() ? 1 : 0;
        } else {
            return -1;
        }
    }

    public void setAttendanceResponsibility(String value, String classroomEmail) {
        //get classroom
        GClassroom classroom = gClassroomRepository.findBygClassroomEmail(classroomEmail);
        //set attendance responsibility
        classroom.setAttendanceResponsibility(value);
        //save classroom
        gClassroomRepository.save(classroom);
    }

    public static class RetryButton extends Button {
        public RetryButton(Button refresh) {
            super("Retry");
            addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            getElement().getStyle().set("margin-left",
                    "var(--lumo-space-xl)");
            addClickListener(e -> refresh.click());
        }
    }

    public static class ReportButton extends Button {
        public ReportButton() {
            super("Report");
            addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            getElement().getStyle().set("margin-left",
                    "var(--lumo-space-xl)");
            //on click redirect to google form
            addClickListener(e -> {
                UI.getCurrent().getPage().open("https://forms.gle/pejyf7CcTKgp2SY29", "_blank");
            });
        }
    }

    public static class CloseButton extends Button {
        public CloseButton() {
            super(new Icon("lumo", "cross"));
            addThemeVariants(LUMO_TERTIARY_INLINE);
            setAriaLabel("Close");
            addClickListener(e -> findAncestor(Notification.class).close());
        }
    }

    public List<GStudent> findAllStudents(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            if (getUsersEmail().toLowerCase().contains("year")) {
                return gStudentRepository.getYeargroup("year");
            } else if (getUsersEmail().toLowerCase().contains("kg")) {
                return gStudentRepository.getYeargroup("kg");
            } else if (getUsersEmail().toLowerCase().contains("elementary")) {
                return gStudentRepository.getSchoolSection("1", "2", "3");
            } else if (getUsersEmail().toLowerCase().contains("middle")) {
                return gStudentRepository.getSchoolSection("4", "5", "6");
            } else if (getUsersEmail().toLowerCase().contains("highschool")) {
                return gStudentRepository.getSchoolSection("7", "8", "9");
            } else if (getUsersEmail().equalsIgnoreCase("lms@tawfikials.com")) {
                return gStudentRepository.findAll();
            } else {
                return new ArrayList<GStudent>();
            }
        } else {
            if (getUsersEmail().toLowerCase().contains("year")) {
                return gStudentRepository.getYeargroupFiltered(stringFilter, "year");
            } else if (getUsersEmail().toLowerCase().contains("kg")) {
                return gStudentRepository.getYeargroupFiltered(stringFilter, "kg");
            } else if (getUsersEmail().toLowerCase().contains("elementary")) {
                return gStudentRepository.getSchoolSectionFiltered(stringFilter, "1", "2", "3");
            } else if (getUsersEmail().toLowerCase().contains("middle")) {
                return gStudentRepository.getSchoolSectionFiltered(stringFilter, "4", "5", "6");
            } else if (getUsersEmail().toLowerCase().contains("highschool")) {
                return gStudentRepository.getSchoolSectionFiltered(stringFilter, "7", "8", "9");
            } else if (getUsersEmail().equalsIgnoreCase("lms@tawfikials.com")) {
                return gStudentRepository.search(stringFilter, getUsersEmail());
            } else {
                return new ArrayList<GStudent>();
            }

        }
    }

    public List<GClassroom> findAllClassrooms(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            if (getUsersEmail().toLowerCase().contains("year")) {
                return gClassroomRepository.getYear("year");
            } else if (getUsersEmail().toLowerCase().contains("kg")) {
                return gClassroomRepository.getKG("kg");
            } else if (getUsersEmail().toLowerCase().contains("elementary")) {
                return gClassroomRepository.getSchoolSection("1", "2", "3");
            } else if (getUsersEmail().toLowerCase().contains("middle")) {
                return gClassroomRepository.getSchoolSection("4", "5", "6");
            } else if (getUsersEmail().toLowerCase().contains("highschool")) {
                return gClassroomRepository.getSchoolSection("7", "8", "9");
            } else if (getUsersEmail().equalsIgnoreCase("lms@tawfikials.com")) {
                return gClassroomRepository.findAll();
            } else {
                return new ArrayList<GClassroom>();
            }
        } else {
            if (getUsersEmail().toLowerCase().contains("year")) {
                return gClassroomRepository.getYearFiltered(stringFilter, "year");
            } else if (getUsersEmail().toLowerCase().contains("kg")) {
                return gClassroomRepository.getKGFiltered(stringFilter, "kg");
            } else if (getUsersEmail().toLowerCase().contains("elementary")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "1", "2", "3");
            } else if (getUsersEmail().toLowerCase().contains("middle")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "4", "5", "6");
            } else if (getUsersEmail().toLowerCase().contains("highschool")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "7", "8", "9");
            } else if (getUsersEmail().equalsIgnoreCase("lms@tawfikials.com")) {
                return gClassroomRepository.search(stringFilter);
            } else {
                return new ArrayList<GClassroom>();
            }
        }
    }

    public List<GClassroom> findAllClassroomsForEmail(String stringFilter, String emailToSearch) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            if (emailToSearch.toLowerCase().contains("year")) {
                return gClassroomRepository.getYear("year");
            } else if (emailToSearch.toLowerCase().contains("kg")) {
                return gClassroomRepository.getKG("kg");
            } else if (emailToSearch.toLowerCase().contains("elementary")) {
                return gClassroomRepository.getSchoolSection("1", "2", "3");
            } else if (emailToSearch.toLowerCase().contains("middle")) {
                return gClassroomRepository.getSchoolSection("4", "5", "6");
            } else if (emailToSearch.toLowerCase().contains("highschool")) {
                return gClassroomRepository.getSchoolSection("7", "8", "9");
            } else if (emailToSearch.equalsIgnoreCase("lms@tawfikials.com")) {
                return gClassroomRepository.findAll(getUsersEmail());
            } else {
                return new ArrayList<GClassroom>();
            }
        } else {
            if (emailToSearch.toLowerCase().contains("year")) {
                return gClassroomRepository.getYearFiltered(stringFilter, "year");
            } else if (emailToSearch.toLowerCase().contains("kg")) {
                return gClassroomRepository.getKGFiltered(stringFilter, "kg");
            } else if (emailToSearch.toLowerCase().contains("elementary")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "1", "2", "3");
            } else if (emailToSearch.toLowerCase().contains("middle")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "4", "5", "6");
            } else if (emailToSearch.toLowerCase().contains("highschool")) {
                return gClassroomRepository.getSchoolSectionFiltered(stringFilter, "7", "8", "9");
            } else if (emailToSearch.equalsIgnoreCase("lms@tawfikials.com")) {
                return gClassroomRepository.search(stringFilter);
            } else {
                return new ArrayList<GClassroom>();
            }
        }
    }

}
