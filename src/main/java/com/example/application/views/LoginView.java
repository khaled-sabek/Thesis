package com.example.application.views;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


@Route("login")
@PageTitle("Login GCPRO")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterListener, ComponentEventListener<AbstractLogin.LoginEvent> {

    private static final String OAUTH_URL = "/oauth2/authorization/google";

    private final LoginForm login = new LoginForm();


    public LoginView(@Autowired Environment env) {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");

        login.addLoginListener(this);
        H2 title = new H2("GC Pro");

        add(
                title
        );

        String clientkey = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        if (clientkey == null || clientkey.length() < 32) {
            Paragraph text = new Paragraph("Could not find OAuth client key in application.properties. "
                    + "Please double-check the key and refer to the README.md file for instructions.");
            text.getStyle().set("padding-top", "100px");
            add(text);
        } else {
            String src = "public/web_dark_rd_SI@4x.png";
            Image img = new Image(src, "Sign in with Google");
            img.setWidth(200, Unit.PIXELS);
            img.setHeight(48, Unit.PIXELS);


            Button imgButton = new Button();
            imgButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            imgButton.setIcon(img);

            imgButton.setTooltipText("Click to sign in with Google!");
            imgButton.setWidth("50 em");
            imgButton.setHeight("50 em");
            imgButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            imgButton.addClickListener(e -> {
                UI.getCurrent().getPage().setLocation(OAUTH_URL);
            });
            add(imgButton);
        }

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }

    @Override
    public void onComponentEvent(AbstractLogin.LoginEvent loginEvent) {

    }
}
